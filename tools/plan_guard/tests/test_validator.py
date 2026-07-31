"""计划与契约只读校验器的回归测试。"""

from pathlib import Path
import tempfile
import unittest

from tools.plan_guard.validator import (
    EXPECTED_BUSINESS_SERVICES,
    ValidationError,
    parse_matrix_requirement_ids,
    parse_execution_plan_patterns,
    parse_plan_tasks,
    parse_todo_tasks,
    validate_active_assignments,
    validate_backend_modules,
    validate_execution_plan_coverage,
    validate_requirement_coverage,
    validate_task_set,
)


FIXTURES = Path(__file__).parent / "fixtures"


class RequirementCoverageTest(unittest.TestCase):
    """验证正式需求文件与开发总纲精确索引的一一对应关系。"""

    def test_accepts_unique_requirement_coverage(self) -> None:
        matrix_ids = parse_matrix_requirement_ids(FIXTURES / "matrix-valid.md")

        validate_requirement_coverage(
            {"ABC-REQ-001", "ABC-REQ-002"},
            matrix_ids,
            expected_count=2,
        )

    def test_rejects_duplicate_matrix_ownership(self) -> None:
        matrix_ids = parse_matrix_requirement_ids(FIXTURES / "matrix-duplicate.md")

        with self.assertRaisesRegex(ValidationError, "重复归属.*ABC-REQ-001"):
            validate_requirement_coverage(
                {"ABC-REQ-001", "ABC-REQ-002"},
                matrix_ids,
                expected_count=2,
            )


class BackendBoundaryTest(unittest.TestCase):
    """验证九个业务服务和 scm-common 的 Maven Reactor 边界。"""

    def test_accepts_exact_nine_services_and_common(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            backend = Path(temporary_directory)
            modules = ["scm-common", *EXPECTED_BUSINESS_SERVICES]
            self._write_parent_pom(backend, modules)
            for module in modules:
                module_directory = backend / module
                module_directory.mkdir()
                boot_plugin = (
                    "<build><plugins><plugin>"
                    "<artifactId>spring-boot-maven-plugin</artifactId>"
                    "</plugin></plugins></build>"
                    if module != "scm-common"
                    else ""
                )
                (module_directory / "pom.xml").write_text(
                    f"<project><artifactId>{module}</artifactId>{boot_plugin}</project>",
                    encoding="utf-8",
                )

            validate_backend_modules(backend)

    def test_rejects_tenth_business_service(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            backend = Path(temporary_directory)
            modules = ["scm-common", *EXPECTED_BUSINESS_SERVICES, "report-service"]
            self._write_parent_pom(backend, modules)
            for module in modules:
                module_directory = backend / module
                module_directory.mkdir()
                (module_directory / "pom.xml").write_text(
                    f"<project><artifactId>{module}</artifactId></project>",
                    encoding="utf-8",
                )

            with self.assertRaisesRegex(ValidationError, "业务服务边界"):
                validate_backend_modules(backend)

    def test_rejects_common_as_deployable_service(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            backend = Path(temporary_directory)
            modules = ["scm-common", *EXPECTED_BUSINESS_SERVICES]
            self._write_parent_pom(backend, modules)
            for module in modules:
                module_directory = backend / module
                module_directory.mkdir()
                plugin = (
                    "<build><plugins><plugin>"
                    "<artifactId>spring-boot-maven-plugin</artifactId>"
                    "</plugin></plugins></build>"
                )
                (module_directory / "pom.xml").write_text(
                    f"<project><artifactId>{module}</artifactId>{plugin}</project>",
                    encoding="utf-8",
                )

            with self.assertRaisesRegex(ValidationError, "scm-common.*不可部署"):
                validate_backend_modules(backend)

    @staticmethod
    def _write_parent_pom(backend: Path, modules: list[str]) -> None:
        module_xml = "".join(f"<module>{module}</module>" for module in modules)
        (backend / "pom.xml").write_text(
            f"<project><modules>{module_xml}</modules></project>",
            encoding="utf-8",
        )


class ParallelTaskTest(unittest.TestCase):
    """验证进行中任务的编号、Agent 和文件所有权冲突。"""

    def test_accepts_distinct_active_assignments(self) -> None:
        tasks = parse_todo_tasks(FIXTURES / "todo-valid.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-valid.md")

        validate_active_assignments(tasks, plan_tasks)

    def test_rejects_duplicate_active_task_id(self) -> None:
        tasks = parse_todo_tasks(FIXTURES / "todo-duplicate-id.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-valid.md")

        with self.assertRaisesRegex(ValidationError, "重复进行中编号"):
            validate_active_assignments(tasks, plan_tasks)

    def test_rejects_duplicate_active_agent(self) -> None:
        tasks = parse_todo_tasks(FIXTURES / "todo-duplicate-agent.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-valid.md")

        with self.assertRaisesRegex(ValidationError, "重复 Agent"):
            validate_active_assignments(tasks, plan_tasks)

    def test_rejects_overlapping_file_scopes(self) -> None:
        tasks = parse_todo_tasks(FIXTURES / "todo-valid.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-conflict.md")

        with self.assertRaisesRegex(ValidationError, "文件范围冲突"):
            validate_active_assignments(tasks, plan_tasks)

    def test_rejects_active_task_without_machine_readable_scope(self) -> None:
        tasks = parse_todo_tasks(FIXTURES / "todo-valid.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-missing-scope.md")

        with self.assertRaisesRegex(ValidationError, "无法解析文件范围.*PAR-NEXT-001"):
            validate_active_assignments(tasks, plan_tasks)

    def test_rejects_task_set_drift(self) -> None:
        todo_tasks = parse_todo_tasks(FIXTURES / "todo-valid.md")
        plan_tasks = parse_plan_tasks(FIXTURES / "plan-valid.md")
        todo_tasks.pop()

        with self.assertRaisesRegex(ValidationError, "任务集合不一致"):
            validate_task_set(todo_tasks, plan_tasks)

    def test_accepts_execution_plan_exact_and_wildcard_coverage(self) -> None:
        patterns = parse_execution_plan_patterns(
            FIXTURES / "execution-plan-valid.md"
        )

        validate_execution_plan_coverage(
            {
                "PAR-NEXT-001",
                "PAR-NEXT-002",
                "SUP-NEXT-001A",
                "WMS-NEXT-001B",
                "WMS-NEXT-001C",
            },
            patterns,
        )

    def test_rejects_execution_plan_missing_task(self) -> None:
        patterns = parse_execution_plan_patterns(
            FIXTURES / "execution-plan-missing.md"
        )

        with self.assertRaisesRegex(ValidationError, "执行批次未覆盖.*PAR-NEXT-002"):
            validate_execution_plan_coverage(
                {"PAR-NEXT-001", "PAR-NEXT-002"},
                patterns,
            )


if __name__ == "__main__":
    unittest.main()
