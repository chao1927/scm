"""校验需求归档、九服务边界和多 Agent 任务契约。

该模块只读取仓库文件，不修改计划、任务或业务代码。命令行入口会把所有
违规一次性打印出来，并用非零退出码阻止继续领取存在冲突的任务。
"""

from __future__ import annotations

import argparse
from collections import Counter
from dataclasses import dataclass
from fnmatch import fnmatchcase
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ElementTree


EXPECTED_REQUIREMENT_COUNT = 64
EXPECTED_BUSINESS_SERVICES = (
    "supplier-service",
    "purchase-service",
    "wms-service",
    "inventory-service",
    "oms-service",
    "tms-service",
    "bms-service",
    "mdm-service",
    "iam-service",
)
COMMON_MODULE = "scm-common"
REQUIREMENT_ID_PATTERN = re.compile(r"\b[A-Z]+-REQ-\d+[A-Z]?\b")
NEXT_TASK_ID_PATTERN = re.compile(r"\b[A-Z]+-NEXT-[A-Z0-9]+\b")
NEXT_TASK_PATTERN = re.compile(r"^[A-Z]+-NEXT-(?:[A-Z0-9]+|\*)$")
COMPACT_NEXT_TASK_PATTERN = re.compile(
    r"^(?P<prefix>[A-Z]+-NEXT-[A-Z0-9]*?)(?P<first>[A-Z])/(?P<second>[A-Z])$"
)
PLAN_TASK_HEADING_PATTERN = re.compile(
    r"^###\s+(?P<task_id>[A-Z]+-NEXT-[A-Z0-9]+)\b",
    re.MULTILINE,
)
TODO_ROW_PATTERN = re.compile(
    r"^\|\s*(?P<status>\[[ x!\-]\])\s*"
    r"\|\s*(?P<task_id>[A-Z]+-(?:NEXT|REQ)-[A-Z0-9]+)\s*"
    r"\|\s*(?P<agent>[^|]+?)\s*\|",
    re.MULTILINE,
)
CODE_SPAN_PATTERN = re.compile(r"`([^`]+)`")
ACTIVE_STATUS = "[-]"


class ValidationError(RuntimeError):
    """表示计划或契约违反可并行执行约束。"""


@dataclass(frozen=True)
class TodoTask:
    """任务看板中的一条任务分配记录。"""

    status: str
    task_id: str
    agent: str


@dataclass(frozen=True)
class PlanTask:
    """并行计划中的任务编号及机器可比较文件范围。"""

    task_id: str
    scopes: tuple[str, ...]


def parse_matrix_requirement_ids(matrix_path: Path) -> list[str]:
    """读取状态矩阵 12.3 精确索引中的需求编号，保留重复项。"""

    text = matrix_path.read_text(encoding="utf-8")
    start_match = re.search(
        r"^###\s+12\.3\s+64\s+张需求精确覆盖索引\s*$",
        text,
        re.MULTILINE,
    )
    if start_match is None:
        raise ValidationError("状态矩阵缺少“12.3 64 张需求精确覆盖索引”章节")
    remaining = text[start_match.end() :]
    end_match = re.search(r"^#{1,3}\s+", remaining, re.MULTILINE)
    section = remaining[: end_match.start()] if end_match else remaining
    requirement_ids = REQUIREMENT_ID_PATTERN.findall(section)
    if not requirement_ids:
        raise ValidationError("状态矩阵精确覆盖索引未列出任何需求编号")
    return requirement_ids


def discover_requirement_ids(requirement_directory: Path) -> set[str]:
    """从正式需求文件名提取需求编号，并拒绝无法识别或重复的文件。"""

    markdown_files = sorted(requirement_directory.glob("*.md"))
    ids: list[str] = []
    invalid_files: list[str] = []
    for requirement_file in markdown_files:
        match = REQUIREMENT_ID_PATTERN.match(requirement_file.stem)
        if match is None:
            invalid_files.append(requirement_file.name)
            continue
        ids.append(match.group())
    if invalid_files:
        raise ValidationError(
            "正式需求文件名缺少规范需求编号: " + "、".join(invalid_files)
        )
    duplicates = _duplicates(ids)
    if duplicates:
        raise ValidationError("正式需求文件编号重复: " + "、".join(duplicates))
    return set(ids)


def validate_requirement_coverage(
    requirement_ids: set[str],
    matrix_ids: list[str],
    expected_count: int = EXPECTED_REQUIREMENT_COUNT,
) -> None:
    """校验正式需求数量及其在状态矩阵中的唯一归属。"""

    if len(requirement_ids) != expected_count:
        raise ValidationError(
            f"正式需求数量应为 {expected_count}，实际为 {len(requirement_ids)}"
        )
    duplicate_matrix_ids = _duplicates(matrix_ids)
    if duplicate_matrix_ids:
        raise ValidationError(
            "状态矩阵存在重复归属: " + "、".join(duplicate_matrix_ids)
        )
    matrix_id_set = set(matrix_ids)
    missing = sorted(requirement_ids - matrix_id_set)
    unexpected = sorted(matrix_id_set - requirement_ids)
    problems: list[str] = []
    if missing:
        problems.append("未归档=" + "、".join(missing))
    if unexpected:
        problems.append("无对应需求文件=" + "、".join(unexpected))
    if problems:
        raise ValidationError("正式需求与状态矩阵不一致: " + "；".join(problems))


def validate_backend_modules(backend_directory: Path) -> None:
    """校验 Maven Reactor 只包含九个业务服务，scm-common 不可部署。"""

    parent_pom = backend_directory / "pom.xml"
    root = _parse_xml(parent_pom)
    reactor_modules = {
        (element.text or "").strip()
        for element in root.iter()
        if _local_name(element.tag) == "module" and (element.text or "").strip()
    }
    expected_services = set(EXPECTED_BUSINESS_SERVICES)
    reactor_services = {module for module in reactor_modules if module.endswith("-service")}
    directory_services = {
        directory.name
        for directory in backend_directory.iterdir()
        if directory.is_dir()
        and directory.name.endswith("-service")
        and (directory / "pom.xml").is_file()
    }
    if reactor_services != expected_services or directory_services != expected_services:
        raise ValidationError(
            "业务服务边界必须严格为九个；"
            f"Reactor={_format_set(reactor_services)}；"
            f"目录={_format_set(directory_services)}"
        )
    if COMMON_MODULE not in reactor_modules:
        raise ValidationError("Maven Reactor 缺少公共模块 scm-common")
    common_pom = backend_directory / COMMON_MODULE / "pom.xml"
    if not common_pom.is_file():
        raise ValidationError("公共模块 scm-common 缺少 pom.xml")
    if _contains_spring_boot_plugin(common_pom):
        raise ValidationError("scm-common 是公共模块，不可部署或配置 Spring Boot 打包插件")
    missing_boot_plugins = [
        service
        for service in EXPECTED_BUSINESS_SERVICES
        if not _contains_spring_boot_plugin(backend_directory / service / "pom.xml")
    ]
    if missing_boot_plugins:
        raise ValidationError(
            "九个业务服务缺少 Spring Boot 打包插件: "
            + "、".join(missing_boot_plugins)
        )


def parse_todo_tasks(todo_path: Path) -> list[TodoTask]:
    """解析任务看板中的任务状态、编号和 Agent。"""

    text = todo_path.read_text(encoding="utf-8")
    tasks = [
        TodoTask(
            status=match.group("status"),
            task_id=match.group("task_id"),
            agent=match.group("agent").strip(),
        )
        for match in TODO_ROW_PATTERN.finditer(text)
    ]
    if not tasks:
        raise ValidationError("tasks/todo.md 未解析到任何任务")
    return tasks


def parse_plan_tasks(plan_path: Path) -> list[PlanTask]:
    """解析 06 计划中的 NEXT 任务及其文件范围。"""

    text = plan_path.read_text(encoding="utf-8")
    headings = list(PLAN_TASK_HEADING_PATTERN.finditer(text))
    tasks: list[PlanTask] = []
    for index, heading in enumerate(headings):
        section_end = (
            headings[index + 1].start() if index + 1 < len(headings) else len(text)
        )
        section = text[heading.end() : section_end]
        file_scope_match = re.search(
            r"^\|\s*文件范围\s*\|\s*(?P<scope>.*?)\s*\|\s*$",
            section,
            re.MULTILINE,
        )
        raw_scope = file_scope_match.group("scope") if file_scope_match else ""
        scopes = tuple(
            sorted(
                {
                    normalized
                    for literal in CODE_SPAN_PATTERN.findall(raw_scope)
                    if (normalized := _normalize_scope(literal)) is not None
                }
            )
        )
        tasks.append(PlanTask(task_id=heading.group("task_id"), scopes=scopes))
    if not tasks:
        raise ValidationError("06-多Agent并行执行计划未解析到 NEXT 任务")
    return tasks


def parse_execution_plan_patterns(execution_plan_path: Path) -> list[str]:
    """读取 tasks/plan.md“执行批次”中声明的精确编号或通配模式。"""

    text = execution_plan_path.read_text(encoding="utf-8")
    start_match = re.search(r"^##\s+3\.\s+执行批次\s*$", text, re.MULTILINE)
    if start_match is None:
        raise ValidationError("tasks/plan.md 缺少“3. 执行批次”章节")
    remaining = text[start_match.end() :]
    end_match = re.search(r"^#{1,2}\s+", remaining, re.MULTILINE)
    section = remaining[: end_match.start()] if end_match else remaining
    patterns = [
        pattern
        for literal in CODE_SPAN_PATTERN.findall(section)
        for pattern in _expand_execution_literal(literal)
    ]
    if not patterns:
        raise ValidationError("tasks/plan.md 执行批次未声明任何 NEXT 任务")
    return patterns


def validate_task_set(
    todo_tasks: list[TodoTask],
    plan_tasks: list[PlanTask],
) -> None:
    """校验 06 计划与任务看板拥有完全相同且唯一的 NEXT 任务集合。"""

    todo_ids = [
        task.task_id for task in todo_tasks if NEXT_TASK_ID_PATTERN.fullmatch(task.task_id)
    ]
    plan_ids = [task.task_id for task in plan_tasks]
    todo_duplicates = _duplicates(todo_ids)
    plan_duplicates = _duplicates(plan_ids)
    if todo_duplicates or plan_duplicates:
        details: list[str] = []
        if todo_duplicates:
            details.append("todo 重复=" + "、".join(todo_duplicates))
        if plan_duplicates:
            details.append("06 计划重复=" + "、".join(plan_duplicates))
        raise ValidationError("NEXT 任务编号不唯一: " + "；".join(details))
    todo_set = set(todo_ids)
    plan_set = set(plan_ids)
    if todo_set != plan_set:
        missing_in_todo = sorted(plan_set - todo_set)
        missing_in_plan = sorted(todo_set - plan_set)
        details = []
        if missing_in_todo:
            details.append("todo 缺少=" + "、".join(missing_in_todo))
        if missing_in_plan:
            details.append("06 计划缺少=" + "、".join(missing_in_plan))
        raise ValidationError("计划任务集合不一致: " + "；".join(details))


def validate_execution_plan_coverage(
    task_ids: set[str],
    execution_patterns: list[str],
) -> None:
    """校验 tasks/plan.md 的执行批次恰好覆盖当前 NEXT 任务集。"""

    uncovered = sorted(
        task_id
        for task_id in task_ids
        if not any(fnmatchcase(task_id, pattern) for pattern in execution_patterns)
    )
    patterns_without_task = sorted(
        {
            pattern
            for pattern in execution_patterns
            if not any(fnmatchcase(task_id, pattern) for task_id in task_ids)
        }
    )
    problems: list[str] = []
    if uncovered:
        problems.append("执行批次未覆盖=" + "、".join(uncovered))
    if patterns_without_task:
        problems.append("执行批次存在空模式=" + "、".join(patterns_without_task))
    if problems:
        raise ValidationError("tasks/plan 与 NEXT 任务集合不一致: " + "；".join(problems))


def validate_active_assignments(
    todo_tasks: list[TodoTask],
    plan_tasks: list[PlanTask],
) -> None:
    """拒绝进行中编号、Agent 或计划文件范围发生冲突。"""

    active_tasks = [task for task in todo_tasks if task.status == ACTIVE_STATUS]
    duplicate_ids = _duplicates([task.task_id for task in active_tasks])
    if duplicate_ids:
        raise ValidationError("存在重复进行中编号: " + "、".join(duplicate_ids))
    active_agents = [
        task.agent
        for task in active_tasks
        if task.agent not in {"未领取", "不可领取", ""}
    ]
    duplicate_agents = _duplicates(active_agents)
    if duplicate_agents:
        raise ValidationError("存在重复 Agent 领取: " + "、".join(duplicate_agents))

    plan_by_id = {task.task_id: task for task in plan_tasks}
    missing_plan_tasks = [
        task.task_id for task in active_tasks if task.task_id not in plan_by_id
    ]
    if missing_plan_tasks:
        raise ValidationError(
            "进行中任务缺少 06 计划定义: " + "、".join(missing_plan_tasks)
        )
    active_plan_tasks = [plan_by_id[task.task_id] for task in active_tasks]
    missing_scopes = [task.task_id for task in active_plan_tasks if not task.scopes]
    if missing_scopes:
        raise ValidationError(
            "进行中任务无法解析文件范围，请在 06 计划中使用反引号路径声明: "
            + "、".join(missing_scopes)
        )
    for index, left in enumerate(active_plan_tasks):
        for right in active_plan_tasks[index + 1 :]:
            conflicts = sorted(
                {
                    f"{left_scope} <> {right_scope}"
                    for left_scope in left.scopes
                    for right_scope in right.scopes
                    if _scopes_overlap(left_scope, right_scope)
                }
            )
            if conflicts:
                raise ValidationError(
                    f"进行中任务文件范围冲突: {left.task_id} 与 {right.task_id}；"
                    + "、".join(conflicts)
                )


def validate_repository(repository_root: Path) -> list[str]:
    """运行全部只读校验并返回通过项说明，任一规则失败时汇总抛出。"""

    checks = (
        (
            "64 张正式需求在状态矩阵唯一归属",
            lambda: validate_requirement_coverage(
                discover_requirement_ids(
                    repository_root / "docs/09-开发计划/需求单"
                ),
                parse_matrix_requirement_ids(
                    repository_root
                    / "docs/09-开发计划/05-九子系统模块状态矩阵.md"
                ),
            ),
        ),
        (
            "九个可部署业务服务及 scm-common 边界",
            lambda: validate_backend_modules(repository_root / "project/backend"),
        ),
        (
            "06 计划与 tasks/todo NEXT 任务集合",
            lambda: validate_task_set(
                parse_todo_tasks(repository_root / "tasks/todo.md"),
                parse_plan_tasks(
                    repository_root
                    / "docs/09-开发计划/06-多Agent并行执行计划.md"
                ),
            ),
        ),
        (
            "tasks/plan 执行批次覆盖 NEXT 任务集合",
            lambda: validate_execution_plan_coverage(
                {
                    task.task_id
                    for task in parse_todo_tasks(repository_root / "tasks/todo.md")
                    if NEXT_TASK_ID_PATTERN.fullmatch(task.task_id)
                },
                parse_execution_plan_patterns(repository_root / "tasks/plan.md"),
            ),
        ),
        (
            "进行中编号、Agent 与文件所有权",
            lambda: validate_active_assignments(
                parse_todo_tasks(repository_root / "tasks/todo.md"),
                parse_plan_tasks(
                    repository_root
                    / "docs/09-开发计划/06-多Agent并行执行计划.md"
                ),
            ),
        ),
    )
    passed: list[str] = []
    errors: list[str] = []
    for label, check in checks:
        try:
            check()
            passed.append(label)
        except (OSError, ElementTree.ParseError, ValidationError) as error:
            errors.append(f"{label}: {error}")
    if errors:
        raise ValidationError("\n".join(errors))
    return passed


def main(argv: list[str] | None = None) -> int:
    """执行命令行校验并返回适合 CI 使用的退出码。"""

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=Path.cwd(),
        help="仓库根目录，默认使用当前目录",
    )
    arguments = parser.parse_args(argv)
    repository_root = arguments.repo_root.resolve()
    try:
        passed = validate_repository(repository_root)
    except ValidationError as error:
        print("[FAIL] 计划与契约一致性校验失败", file=sys.stderr)
        for line in str(error).splitlines():
            print(f"  - {line}", file=sys.stderr)
        return 1
    print("[PASS] 计划与契约一致性校验通过")
    for label in passed:
        print(f"  - {label}")
    return 0


def _parse_xml(path: Path) -> ElementTree.Element:
    """读取 XML 根节点并保留调用方可识别的解析异常。"""

    return ElementTree.parse(path).getroot()


def _local_name(tag: str) -> str:
    """移除 XML namespace，返回标签本地名。"""

    return tag.rsplit("}", 1)[-1]


def _contains_spring_boot_plugin(pom_path: Path) -> bool:
    """判断模块是否声明 Spring Boot Maven 打包插件。"""

    root = _parse_xml(pom_path)
    return any(
        _local_name(element.tag) == "artifactId"
        and (element.text or "").strip() == "spring-boot-maven-plugin"
        for element in root.iter()
    )


def _normalize_scope(literal: str) -> str | None:
    """把计划中的简写文件范围转换为仓库相对所有权路径。"""

    raw_scope = literal.strip().replace("\\", "/")
    denotes_directory = raw_scope.endswith("/")
    scope = raw_scope.rstrip("/")
    if not scope:
        return None
    if scope in EXPECTED_BUSINESS_SERVICES or scope == COMMON_MODULE:
        return f"project/backend/{scope}/**"
    first_segment = scope.split("/", 1)[0]
    if first_segment in EXPECTED_BUSINESS_SERVICES or first_segment == COMMON_MODULE:
        normalized = "project/backend/" + scope
        return normalized + "/**" if denotes_directory else normalized
    if scope.startswith("resources/"):
        normalized = "project/frontend/src/config/" + scope
        return normalized + "/**" if denotes_directory else normalized
    if scope.startswith("src/config/"):
        normalized = "project/frontend/" + scope
        return normalized + "/**" if denotes_directory else normalized
    if scope.startswith("frontend/.../resources/"):
        suffix = scope.removeprefix("frontend/.../resources/")
        normalized = "project/frontend/src/config/resources/" + suffix
        return normalized + "/**" if denotes_directory else normalized
    if scope.startswith(("project/", "docs/", "tasks/")):
        return scope + "/**" if denotes_directory else scope
    return None


def _expand_execution_literal(literal: str) -> tuple[str, ...]:
    """展开 `WMS-NEXT-001B/C` 一类紧凑批次写法。"""

    if NEXT_TASK_PATTERN.fullmatch(literal):
        return (literal,)
    compact_match = COMPACT_NEXT_TASK_PATTERN.fullmatch(literal)
    if compact_match is None:
        return ()
    prefix = compact_match.group("prefix")
    return (
        prefix + compact_match.group("first"),
        prefix + compact_match.group("second"),
    )


def _scopes_overlap(left: str, right: str) -> bool:
    """判断两个规范化路径是否相同或存在目录包含关系。"""

    left_prefix = left.removesuffix("/**")
    right_prefix = right.removesuffix("/**")
    if left == right:
        return True
    if left.endswith("/**") and (
        right == left_prefix or right.startswith(left_prefix + "/")
    ):
        return True
    if right.endswith("/**") and (
        left == right_prefix or left.startswith(right_prefix + "/")
    ):
        return True
    return False


def _duplicates(values: list[str]) -> list[str]:
    """按字典序返回出现两次及以上的值。"""

    counts = Counter(values)
    return sorted(value for value, count in counts.items() if count > 1)


def _format_set(values: set[str]) -> str:
    """以稳定顺序格式化集合，便于错误输出和 CI 对比。"""

    return "[" + "、".join(sorted(values)) + "]"


if __name__ == "__main__":
    raise SystemExit(main())
