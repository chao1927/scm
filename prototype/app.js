/*
 * PROTOTYPE — 供应链九大子系统统一信息架构。
 * 已确认结构：工作台 → 业务列表 → 业务详情。
 */

(() => {
  "use strict";

  const data = window.PROTOTYPE_DATA;
  const app = document.querySelector("#app");
  const liveRegion = document.querySelector("#live-region");
  const params = new URLSearchParams(location.search);
  const allowedViews = ["workbench", "list", "detail"];
  const allowedTabs = ["basic", "lines", "fulfillment", "relations", "process", "logs", "events"];

  const state = {
    systemId: params.get("system") || data.systems[0].id,
    pageId: params.get("page") || "workbench",
    view: allowedViews.includes(params.get("view")) ? params.get("view") : "workbench",
    recordRef: params.get("record") || "",
    tab: allowedTabs.includes(params.get("tab")) ? params.get("tab") : "basic",
    query: "",
    status: "全部",
    mobileNav: false,
    activities: [],
  };

  const esc = (value) =>
    String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");

  const currentSystem = () => data.systems.find((item) => item.id === state.systemId) || data.systems[0];
  const currentPage = () => currentSystem().menu.find((item) => item.id === state.pageId) || currentSystem().menu[0];
  const currentRecord = () =>
    currentSystem().records.find((item) => item.ref === state.recordRef) || currentSystem().records[0];
  const firstBusinessPage = (system = currentSystem()) => system.menu.find((item) => item.id !== "workbench") || system.menu[0];

  function announce(message) {
    liveRegion.textContent = "";
    requestAnimationFrame(() => {
      liveRegion.textContent = message;
    });
  }

  function statusTone(status) {
    if (/失败|异常|冻结|拒|超时|差异|未命中/.test(status)) return "danger";
    if (/待|处理中|整改|审批|执行|发布/.test(status)) return "warning";
    if (/完成|成功|启用|确认|预约|运输中/.test(status)) return "success";
    return "neutral";
  }

  function riskBadge(risk) {
    const tone = risk === "高" ? "danger" : risk === "中" ? "warning" : "success";
    return `<span class="badge ${tone}"><span aria-hidden="true">●</span>${esc(risk)}</span>`;
  }

  function syncUrl(mode = "replace") {
    const next = new URL(location.href);
    next.search = "";
    next.searchParams.set("system", state.systemId);
    next.searchParams.set("page", state.pageId);
    next.searchParams.set("view", state.view);
    if (state.view === "detail") {
      next.searchParams.set("record", state.recordRef);
      next.searchParams.set("tab", state.tab);
    }
    history[mode === "push" ? "pushState" : "replaceState"]({}, "", next);
  }

  function setSystem(id) {
    const system = data.systems.find((item) => item.id === id);
    if (!system) return;
    state.systemId = id;
    state.pageId = "workbench";
    state.view = "workbench";
    state.recordRef = system.records[0].ref;
    state.status = "全部";
    state.query = "";
    state.mobileNav = false;
    render("push");
    announce(`已进入${system.name}工作台`);
  }

  function setPage(id) {
    const page = currentSystem().menu.find((item) => item.id === id);
    if (!page) return;
    state.pageId = id;
    state.view = id === "workbench" ? "workbench" : "list";
    state.status = "全部";
    state.query = "";
    state.mobileNav = false;
    render("push");
    requestAnimationFrame(() => document.querySelector("#main-content")?.focus());
  }

  function showList(pageId = state.pageId) {
    if (pageId === "workbench") pageId = firstBusinessPage().id;
    state.pageId = pageId;
    state.view = "list";
    state.tab = "basic";
    render("push");
  }

  function showDetail(ref) {
    state.recordRef = ref;
    state.view = "detail";
    state.tab = "basic";
    render("push");
    requestAnimationFrame(() => document.querySelector("#main-content")?.focus());
    announce(`已打开业务对象 ${ref} 的详情`);
  }

  function visibleRecords() {
    return currentSystem().records.filter((record) => {
      const query = state.query.trim().toLowerCase();
      const matchesQuery =
        !query ||
        [record.ref, record.subject, record.scope, record.owner, record.status].some((value) =>
          value.toLowerCase().includes(query),
        );
      const matchesStatus = state.status === "全部" || record.status === state.status;
      return matchesQuery && matchesStatus;
    });
  }

  function systemRail() {
    return `
      <nav class="system-rail" aria-label="子系统">
        <a class="brand-mark" href="?system=${data.systems[0].id}&page=workbench&view=workbench" aria-label="供应链系统首页">SC</a>
        <div class="system-list">
          ${data.systems
            .map(
              (system) => `
                <button class="system-button ${system.id === state.systemId ? "active" : ""}"
                  data-system="${system.id}" aria-current="${system.id === state.systemId ? "page" : "false"}">
                  <span class="system-icon" style="--system-color:${system.color}">${esc(system.icon)}</span>
                  <span>${esc(system.name)}</span>
                </button>`,
            )
            .join("")}
        </div>
        <button class="rail-help" data-action="show-help" aria-label="查看原型说明">?</button>
      </nav>`;
  }

  function sidebar(system) {
    return `
      <aside class="sidebar ${state.mobileNav ? "open" : ""}" aria-label="${esc(system.name)}功能导航">
        <div class="context-head">
          <span class="context-icon" style="--system-color:${system.color}">${esc(system.icon)}</span>
          <div><strong>${esc(system.domain)}</strong><small>${esc(system.domainType)}</small></div>
          <button class="icon-button sidebar-close" data-action="toggle-nav" aria-label="关闭功能菜单">×</button>
        </div>
        <nav class="page-menu">
          ${system.menu
            .map(
              (page) => `
                <button class="page-menu-item ${page.id === state.pageId ? "active" : ""}"
                  data-page="${page.id}" aria-current="${page.id === state.pageId ? "page" : "false"}">
                  <span aria-hidden="true">${page.id === "workbench" ? "⌂" : "•"}</span>
                  ${esc(page.label)}
                </button>`,
            )
            .join("")}
        </nav>
        <div class="scope-card">
          <strong>当前数据权限</strong>
          <span>${esc(system.scope)}</span>
        </div>
      </aside>`;
  }

  function topbar(system, page) {
    const viewName = state.view === "workbench" ? "工作台" : state.view === "list" ? "列表" : "详情";
    return `
      <header class="topbar">
        <button class="icon-button nav-toggle" data-action="toggle-nav" aria-expanded="${state.mobileNav}" aria-label="打开功能菜单">☰</button>
        <nav class="breadcrumb" aria-label="面包屑">
          <button data-page="workbench">${esc(system.name)}</button>
          <span aria-hidden="true">/</span>
          <button data-page="${page.id}">${esc(page.label)}</button>
          ${state.view === "detail" ? `<span aria-hidden="true">/</span><strong>${viewName}</strong>` : ""}
        </nav>
        <div class="topbar-actions">
          <label class="global-search">
            <span aria-hidden="true">⌕</span>
            <span class="sr-only">全局搜索</span>
            <input type="search" placeholder="搜索单号、SKU、供应商…" />
          </label>
          <button class="notification-button" data-action="show-events" aria-label="查看待办通知">7</button>
          <button class="user-button" data-action="show-help">
            <span class="avatar">林</span><span>${esc(system.role)}</span><span aria-hidden="true">⌄</span>
          </button>
        </div>
      </header>`;
  }

  function pageHeader(page, options = {}) {
    return `
      <header class="page-header">
        <div>
          <span class="eyebrow">${esc(currentSystem().domain)} · ${esc(currentSystem().domainType)}</span>
          <h1>${esc(options.title || page.label)}</h1>
          <p>${esc(options.description || page.purpose)}</p>
        </div>
        <div class="page-actions">
          ${(options.actions || page.actions)
            .slice(0, 4)
            .map(
              (action, index) =>
                `<button class="button ${index === 0 ? "primary" : ""}" data-command="${esc(action)}">${esc(action)}</button>`,
            )
            .join("")}
          <button class="button icon-only" data-action="show-events" aria-label="查看更多操作">•••</button>
        </div>
      </header>`;
  }

  function metricCards(system) {
    return `
      <section class="metric-grid" aria-label="关键业务指标">
        ${system.metrics
          .map(
            ([label, value, note], index) => `
              <button class="metric-card" data-action="metric-list">
                <span>${esc(label)}</span>
                <strong>${esc(value)}</strong>
                <small class="${index === 3 ? "risk-text" : ""}">${esc(note)}</small>
              </button>`,
          )
          .join("")}
      </section>`;
  }

  function workflow(system) {
    return `
      <section class="panel process-panel">
        <div class="panel-head"><div><span class="eyebrow">关键流程</span><h2>业务闭环进度</h2></div><button class="text-button" data-action="show-events">查看领域事件 →</button></div>
        <ol class="process-flow">
          ${system.workflow
            .map(
              (step, index) => `
                <li class="${index < 3 ? "done" : index === 3 ? "current" : ""}">
                  <span>${index < 3 ? "✓" : index + 1}</span><strong>${esc(step)}</strong>
                </li>`,
            )
            .join("")}
        </ol>
      </section>`;
  }

  function compactTaskList(system) {
    return `
      <section class="panel tasks-panel">
        <div class="panel-head"><div><span class="eyebrow">我的待办</span><h2>优先处理事项</h2></div><button class="text-button" data-action="all-tasks">查看全部 →</button></div>
        <div class="task-list">
          ${system.records
            .map(
              (record) => `
                <button class="task-row" data-record="${record.ref}">
                  <span class="task-subject"><strong>${esc(record.subject)}</strong><small>${esc(record.ref)} · ${esc(record.scope)}</small></span>
                  <span class="status ${statusTone(record.status)}">${esc(record.status)}</span>
                  <span>${esc(record.owner)}</span>
                  <time>${esc(record.updated)}</time>
                  ${riskBadge(record.risk)}
                </button>`,
            )
            .join("")}
        </div>
      </section>`;
  }

  function workbenchView(system, page) {
    return `
      <main id="main-content" class="main-content" tabindex="-1">
        ${pageHeader(page, { title: page.label, description: system.goal, actions: ["查看全部待办", "导出工作台"] })}
        ${metricCards(system)}
        <div class="workbench-grid">
          <div>${workflow(system)}${compactTaskList(system)}</div>
          <aside class="workbench-aside">
            <section class="panel">
              <div class="panel-head"><div><span class="eyebrow">业务提醒</span><h2>风险与异常</h2></div></div>
              <ul class="alert-list">
                ${system.records
                  .filter((record) => record.risk !== "正常")
                  .slice(0, 4)
                  .map(
                    (record) =>
                      `<li><button data-record="${record.ref}"><strong>${esc(record.subject)}</strong><small>${esc(record.status)} · ${esc(record.updated)}</small></button>${riskBadge(record.risk)}</li>`,
                  )
                  .join("")}
              </ul>
            </section>
            <section class="panel quick-entry">
              <div class="panel-head"><div><span class="eyebrow">常用功能</span><h2>快捷入口</h2></div></div>
              ${system.menu
                .filter((item) => item.id !== "workbench")
                .slice(0, 6)
                .map((item) => `<button data-page="${item.id}"><span>→</span>${esc(item.label)}</button>`)
                .join("")}
            </section>
          </aside>
        </div>
      </main>`;
  }

  function filterBar() {
    const statuses = ["全部", ...new Set(currentSystem().records.map((record) => record.status))];
    return `
      <section class="list-tools" aria-label="列表查询条件">
        <div class="status-tabs" role="group" aria-label="状态筛选">
          ${statuses
            .map((status) => {
              const count = status === "全部" ? currentSystem().records.length : currentSystem().records.filter((item) => item.status === status).length;
              return `<button class="${state.status === status ? "active" : ""}" data-status="${esc(status)}">${esc(status)} <span>${count}</span></button>`;
            })
            .join("")}
        </div>
        <div class="filter-row">
          <label class="filter-search"><span aria-hidden="true">⌕</span><span class="sr-only">筛选业务对象</span><input id="record-filter" type="search" value="${esc(state.query)}" placeholder="输入编号、对象、负责人或状态" /></label>
          <label>数据范围<select><option>全部授权范围</option><option>${esc(currentSystem().records[0].scope)}</option></select></label>
          <label>风险等级<select><option>全部</option><option>高</option><option>中</option><option>正常</option></select></label>
          <button class="button" data-action="reset-filter">重置</button>
        </div>
      </section>`;
  }

  function dataTable(records) {
    if (!records.length) {
      return `<div class="empty-state" role="status"><strong>没有符合条件的数据</strong><span>请调整关键词或状态筛选。</span><button class="button" data-action="reset-filter">清除筛选</button></div>`;
    }
    return `
      <div class="table-scroll">
        <table>
          <thead><tr><th><input type="checkbox" aria-label="选择本页全部数据" /></th><th>业务编号 / 对象</th><th>数据范围</th><th>状态</th><th>负责人</th><th>更新时间</th><th>风险</th><th>操作</th></tr></thead>
          <tbody>
            ${records
              .map(
                (record) => `
                  <tr>
                    <td><input type="checkbox" aria-label="选择 ${esc(record.ref)}" /></td>
                    <td><button class="record-link" data-record="${record.ref}">${esc(record.ref)}</button><small>${esc(record.subject)}</small></td>
                    <td>${esc(record.scope)}</td>
                    <td><span class="status ${statusTone(record.status)}">${esc(record.status)}</span></td>
                    <td>${esc(record.owner)}</td>
                    <td>${esc(record.updated)}</td>
                    <td>${riskBadge(record.risk)}</td>
                    <td><button class="text-button" data-record="${record.ref}">查看</button></td>
                  </tr>`,
              )
              .join("")}
          </tbody>
        </table>
      </div>
      <footer class="pagination">
        <span>共 ${records.length} 条，当前显示 1–${records.length} 条</span>
        <div><button class="button" disabled>上一页</button><button class="page-number active">1</button><button class="button" disabled>下一页</button></div>
      </footer>`;
  }

  function listView(system, page) {
    const records = visibleRecords();
    return `
      <main id="main-content" class="main-content" tabindex="-1">
        ${pageHeader(page)}
        <section class="panel list-panel">
          ${filterBar()}
          <div class="bulk-bar"><span>未选择数据</span><button class="text-button">批量导出</button></div>
          ${dataTable(records)}
        </section>
      </main>`;
  }

  const detailTabs = [
    ["basic", "基本信息"],
    ["lines", "明细信息"],
    ["fulfillment", "履约跟踪"],
    ["relations", "关联单据"],
    ["process", "流程记录"],
    ["logs", "操作日志"],
    ["events", "领域事件"],
  ];

  function facts(record) {
    return `
      <dl class="fact-list">
        <div><dt>业务编号</dt><dd>${esc(record.ref)}</dd></div>
        <div><dt>业务对象</dt><dd>${esc(record.subject)}</dd></div>
        <div><dt>数据范围</dt><dd>${esc(record.scope)}</dd></div>
        <div><dt>当前状态</dt><dd><span class="status ${statusTone(record.status)}">${esc(record.status)}</span></dd></div>
        <div><dt>当前负责人</dt><dd>${esc(record.owner)}</dd></div>
        <div><dt>风险等级</dt><dd>${riskBadge(record.risk)}</dd></div>
        <div><dt>更新时间</dt><dd>2026-07-28 ${esc(record.updated)}</dd></div>
        <div><dt>数据版本</dt><dd>v12 · 乐观锁</dd></div>
      </dl>`;
  }

  function detailTabContent(system, record) {
    if (state.tab === "basic") {
      return `<section class="detail-card"><h2>基本信息</h2>${facts(record)}</section>
        <section class="detail-card"><h2>业务说明</h2><p>${esc(currentPage().purpose)}</p><p>${esc(system.goal)}</p></section>`;
    }
    if (state.tab === "lines") {
      return `<section class="detail-card"><h2>明细信息</h2><div class="table-scroll"><table><thead><tr><th>行号</th><th>商品 / 费用项</th><th>计划数量</th><th>已处理</th><th>差异</th><th>行状态</th></tr></thead><tbody>
        <tr><td>10</td><td>${esc(record.subject)} · 主项</td><td>120</td><td>96</td><td>24</td><td><span class="status warning">处理中</span></td></tr>
        <tr><td>20</td><td>配套材料 / 附加费用</td><td>48</td><td>48</td><td>0</td><td><span class="status success">已完成</span></td></tr>
      </tbody></table></div></section>`;
    }
    if (state.tab === "fulfillment") {
      return `<section class="detail-card"><h2>履约进度</h2>${lifecycle(system, 3)}</section>
        <section class="detail-card"><h2>数量与时效</h2><dl class="fact-list compact"><div><dt>计划数量</dt><dd>168</dd></div><div><dt>完成数量</dt><dd>144</dd></div><div><dt>剩余数量</dt><dd>24</dd></div><div><dt>承诺时间</dt><dd>2026-07-29 18:00</dd></div></dl></section>`;
    }
    if (state.tab === "relations") {
      return `<section class="detail-card"><h2>关联单据</h2><ul class="relation-list">
        ${["上游需求单", "下游执行单", "库存/费用事实", "审批流程"]
          .map((name, index) => `<li><span>${esc(name)}</span><button class="record-link">${system.code}-${record.ref.slice(-6)}-${index + 1}</button><span class="status ${index < 2 ? "success" : "neutral"}">${index < 2 ? "已同步" : "只读"}</span></li>`)
          .join("")}
      </ul></section>`;
    }
    if (state.tab === "process") {
      return `<section class="detail-card"><h2>生命周期与审批流程</h2>${lifecycle(system, 3)}</section>`;
    }
    if (state.tab === "logs") {
      return `<section class="detail-card"><h2>操作日志</h2>${auditList([
        ...state.activities,
        `查看详情 · ${record.ref}`,
        `系统同步业务对象 · ${record.ref}`,
      ])}</section>`;
    }
    return `<section class="detail-card"><div class="section-title"><div><h2>领域事件</h2><p>用于技术支持和跨系统问题追踪，普通业务用户可按权限隐藏。</p></div><button class="button">导出事件</button></div>${auditList(system.events.map((event) => `${event} · ${record.ref}`), true)}</section>`;
  }

  function lifecycle(system, currentIndex) {
    return `<ol class="vertical-lifecycle">${system.workflow
      .map(
        (step, index) => `
          <li class="${index < currentIndex ? "done" : index === currentIndex ? "current" : ""}">
            <span>${index < currentIndex ? "✓" : index + 1}</span>
            <div><strong>${esc(step)}</strong><small>${index < currentIndex ? `2026-07-28 ${17 - index}:20 · 系统已完成` : index === currentIndex ? "当前节点 · 等待业务操作" : "尚未发生"}</small></div>
          </li>`,
      )
      .join("")}</ol>`;
  }

  function auditList(items, technical = false) {
    return `<ul class="audit-list">${items
      .map(
        (item, index) => `
          <li><time>${index ? `${16 - index}:0${index}` : "刚刚"}</time><div><strong>${esc(item)}</strong><small>${technical ? `eventId: EVT-${currentSystem().code}-20260728-${881 + index} · Outbox · 幂等消费成功` : `${index ? "系统" : currentSystem().role} · 原型内存记录`}</small></div></li>`,
      )
      .join("")}</ul>`;
  }

  function detailView(system, page) {
    const record = currentRecord();
    return `
      <main id="main-content" class="main-content detail-content" tabindex="-1">
        <button class="back-link" data-action="back-list">← 返回${esc(page.label)}列表</button>
        <header class="detail-header">
          <div><span class="eyebrow">${esc(system.domain)} · ${esc(page.label)}</span><h1>${esc(record.subject)}</h1><div class="detail-meta"><code>${esc(record.ref)}</code><span class="status large ${statusTone(record.status)}">${esc(record.status)}</span>${riskBadge(record.risk)}</div></div>
          <div class="page-actions">${page.actions.slice(0, 4).map((action, index) => `<button class="button ${index === 0 ? "primary" : ""}" data-command="${esc(action)}">${esc(action)}</button>`).join("")}</div>
        </header>
        <nav class="detail-tabs" aria-label="详情内容">
          ${detailTabs.map(([id, label]) => `<button class="${state.tab === id ? "active" : ""}" data-tab="${id}" aria-current="${state.tab === id ? "page" : "false"}">${label}</button>`).join("")}
        </nav>
        <div class="detail-body">${detailTabContent(system, record)}</div>
      </main>`;
  }

  function dialogs() {
    return `
      <dialog id="command-dialog" class="dialog">
        <form method="dialog">
          <div class="dialog-head"><div><span class="eyebrow">业务命令确认</span><h2 id="command-title">执行业务操作</h2></div><button class="icon-button" value="cancel" aria-label="关闭">×</button></div>
          <div class="command-summary"><div><span>目标对象</span><strong id="command-target"></strong></div><div><span>执行结果</span><strong id="command-result"></strong></div><div><span>后续记录</span><strong>状态变化、领域事件与审计日志</strong></div></div>
          <label class="dialog-field">操作原因<textarea id="command-reason" required>原型评审操作</textarea></label>
          <div class="dialog-actions"><button class="button" value="cancel">取消</button><button id="command-confirm" class="button primary" value="default">确认执行</button></div>
        </form>
      </dialog>
      <dialog id="info-dialog" class="dialog">
        <form method="dialog">
          <div class="dialog-head"><div><span class="eyebrow">统一原型说明</span><h2 id="info-title">工作台、列表与详情</h2></div><button class="icon-button" value="cancel" aria-label="关闭">×</button></div>
          <div id="info-content" class="info-content"></div>
          <div class="dialog-actions"><button class="button primary" value="default">知道了</button></div>
        </form>
      </dialog>`;
  }

  function render(historyMode = "replace") {
    const system = currentSystem();
    if (!system.menu.some((item) => item.id === state.pageId)) state.pageId = "workbench";
    if (state.pageId === "workbench") state.view = "workbench";
    if (!system.records.some((item) => item.ref === state.recordRef)) state.recordRef = system.records[0].ref;
    const page = currentPage();
    document.documentElement.style.setProperty("--accent", system.color);
    document.title = `${page.label} · ${system.name}`;
    const content =
      state.view === "detail"
        ? detailView(system, page)
        : state.view === "list"
          ? listView(system, page)
          : workbenchView(system, page);
    app.innerHTML = `${systemRail()}<div class="application-shell">${sidebar(system)}<div class="workspace">${topbar(system, page)}${content}</div></div>${dialogs()}`;
    syncUrl(historyMode);
  }

  function openCommand(command) {
    const dialog = document.querySelector("#command-dialog");
    const record = currentRecord();
    dialog.dataset.command = command;
    document.querySelector("#command-title").textContent = command;
    document.querySelector("#command-target").textContent = `${record.ref} / ${record.subject}`;
    document.querySelector("#command-result").textContent = `当前“${record.status}”将根据“${command}”推进`;
    dialog.showModal();
    document.querySelector("#command-reason").focus();
  }

  function confirmCommand() {
    const dialog = document.querySelector("#command-dialog");
    const command = dialog.dataset.command;
    const record = currentRecord();
    record.status = `已${command}`;
    state.activities.unshift(`${command} · ${record.ref}`);
    dialog.close();
    render();
    announce(`${record.ref} 已执行${command}，状态和操作日志已更新`);
  }

  function showInfo(type) {
    const system = currentSystem();
    const content = document.querySelector("#info-content");
    const title = document.querySelector("#info-title");
    if (type === "events") {
      title.textContent = `${system.name}领域事件`;
      content.innerHTML = `<p>跨系统协作通过已经发生的业务事实完成。本页面不直接修改其他限界上下文的数据。</p><ul>${system.events.map((event) => `<li><strong>${esc(event)}</strong><small>eventId 幂等 · Outbox 发布 · 失败可重试</small></li>`).join("")}</ul>`;
    } else {
      title.textContent = "工作台、列表与详情";
      content.innerHTML = `<p>九个子系统统一使用常见后台信息架构：</p><ol><li><strong>工作台</strong><small>发现指标、待办、风险和关键流程。</small></li><li><strong>业务列表</strong><small>查询、筛选、批量处理并进入具体对象。</small></li><li><strong>业务详情</strong><small>查看基本信息、明细、履约、关联单据、生命周期和审计记录。</small></li></ol><p>原型不连接后端，所有写操作只修改当前浏览器内存。</p>`;
    }
    document.querySelector("#info-dialog").showModal();
  }

  app.addEventListener("click", (event) => {
    const systemButton = event.target.closest("[data-system]");
    if (systemButton) return setSystem(systemButton.dataset.system);
    const pageButton = event.target.closest("[data-page]");
    if (pageButton) return setPage(pageButton.dataset.page);
    const recordButton = event.target.closest("[data-record]");
    if (recordButton) return showDetail(recordButton.dataset.record);
    const statusButton = event.target.closest("[data-status]");
    if (statusButton) {
      state.status = statusButton.dataset.status;
      render();
      return announce(`状态筛选：${state.status}`);
    }
    const tabButton = event.target.closest("[data-tab]");
    if (tabButton) {
      state.tab = tabButton.dataset.tab;
      render("push");
      return announce(`已切换到${tabButton.textContent.trim()}`);
    }
    const commandButton = event.target.closest("button[data-command]");
    if (commandButton) return openCommand(commandButton.dataset.command);
    const action = event.target.closest("[data-action]")?.dataset.action;
    if (action === "toggle-nav") {
      state.mobileNav = !state.mobileNav;
      return render();
    }
    if (action === "reset-filter") {
      state.query = "";
      state.status = "全部";
      return render();
    }
    if (action === "back-list") return showList();
    if (action === "metric-list" || action === "all-tasks") return showList(firstBusinessPage().id);
    if (action === "show-help") return showInfo("help");
    if (action === "show-events") return showInfo("events");
    if (event.target.id === "command-confirm") {
      event.preventDefault();
      return confirmCommand();
    }
  });

  app.addEventListener("input", (event) => {
    if (event.target.id !== "record-filter") return;
    state.query = event.target.value;
    const position = event.target.selectionStart;
    render();
    const next = document.querySelector("#record-filter");
    next?.focus();
    next?.setSelectionRange(position, position);
  });

  window.addEventListener("popstate", () => {
    const next = new URLSearchParams(location.search);
    state.systemId = next.get("system") || data.systems[0].id;
    state.pageId = next.get("page") || "workbench";
    state.view = allowedViews.includes(next.get("view")) ? next.get("view") : "workbench";
    state.recordRef = next.get("record") || "";
    state.tab = allowedTabs.includes(next.get("tab")) ? next.get("tab") : "basic";
    render();
  });

  render();
})();
