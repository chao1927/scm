package com.chaobo.scm.wms.infrastructure.persistence.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsInboundReadModelPort;
import com.chaobo.scm.wms.domain.inbound.InboundOrderStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * 基于 WMS 业务表构建入库工作台读模型。
 *
 * <p>该适配器只执行参数化查询，不修改命令模型。仓库和货主条件来自应用层已经校验过的
 * {@link DataScope}，详情查询与列表查询复用同一范围条件，避免通过业务编号绕过数据权限。
 */
@Repository
public class JdbcWmsInboundReadModel implements WmsInboundReadModelPort {

    private static final String INBOUND_FROM = """
        from wms_inbound i
        where i.deleted = 0
        """;

    private static final String RECEIPT_FROM = """
        from wms_receipt r
        join wms_inbound i on i.inbound_id = r.inbound_id and i.deleted = 0
        where 1 = 1
        """;

    private static final String INSPECTION_FROM = """
        from wms_inspection q
        join wms_receipt r on r.receipt_id = q.receipt_id
        join wms_inbound i on i.inbound_id = r.inbound_id and i.deleted = 0
        where 1 = 1
        """;

    private static final String PUTAWAY_FROM = """
        from wms_putaway_task p
        join wms_inspection q on q.inspection_id = p.inspection_id
        join wms_receipt r on r.receipt_id = q.receipt_id
        join wms_inbound i on i.inbound_id = r.inbound_id and i.deleted = 0
        where 1 = 1
        """;

    private static final String STOCK_FROM = """
        from wms_stock_ledger l
        join wms_putaway_task p on l.source_type = 'PUTAWAY_TASK' and l.source_no = p.task_no
        join wms_inspection q on q.inspection_id = p.inspection_id
        join wms_receipt r on r.receipt_id = q.receipt_id
        join wms_inbound i on i.inbound_id = r.inbound_id and i.deleted = 0
        where 1 = 1
        """;

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * 创建读模型适配器。
     *
     * @param jdbc Spring 参数化 JDBC 模板
     */
    public JdbcWmsInboundReadModel(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PageResult<InboundSummary> pageInbounds(PageCriteria criteria, DataScope scope) {
        var query = query(INBOUND_FROM, "i", criteria, scope,
                "i.inbound_order_no, i.source_order_no", "i.inbound_status");
        var select = """
            select i.inbound_id, i.inbound_order_no, i.source_type, i.source_order_no,
              i.warehouse_id, i.owner_id, i.inbound_status, i.expected_arrival_at, i.updated_at,
              coalesce((select sum(r.received_qty) from wms_receipt r where r.inbound_id=i.inbound_id), 0) received_qty,
              coalesce((select sum(q.qualified_qty) from wms_inspection q join wms_receipt r on r.receipt_id=q.receipt_id where r.inbound_id=i.inbound_id), 0) qualified_qty,
              coalesce((select sum(p.putaway_qty) from wms_putaway_task p join wms_inspection q on q.inspection_id=p.inspection_id join wms_receipt r on r.receipt_id=q.receipt_id where r.inbound_id=i.inbound_id), 0) putaway_qty
            """ + query.sql() + order(criteria, "i.updated_at", "i.created_at",
                "i.inbound_status", "received_qty") + limit();
        return page(criteria, query, select, this::mapInbound);
    }

    @Override
    public Optional<InboundDetail> inboundDetail(String inboundOrderNo, DataScope scope) {
        var criteria = new PageCriteria(inboundOrderNo, null, 1, 1,
                SortField.UPDATED_AT, SortDirection.DESC);
        var query = query(INBOUND_FROM, "i", criteria, scope,
                "i.inbound_order_no", "i.inbound_status");
        var inbound = jdbc.query("""
            select i.inbound_id, i.inbound_order_no, i.source_type, i.source_order_no,
              i.warehouse_id, i.owner_id, i.inbound_status, i.expected_arrival_at, i.updated_at,
              coalesce((select sum(r.received_qty) from wms_receipt r where r.inbound_id=i.inbound_id), 0) received_qty,
              coalesce((select sum(q.qualified_qty) from wms_inspection q join wms_receipt r on r.receipt_id=q.receipt_id where r.inbound_id=i.inbound_id), 0) qualified_qty,
              coalesce((select sum(p.putaway_qty) from wms_putaway_task p join wms_inspection q on q.inspection_id=p.inspection_id join wms_receipt r on r.receipt_id=q.receipt_id where r.inbound_id=i.inbound_id), 0) putaway_qty
            """ + query.sql() + " and i.inbound_order_no = :businessNo",
                query.params().addValue("businessNo", inboundOrderNo), this::mapInbound)
            .stream().findFirst();
        if (inbound.isEmpty()) {
            return Optional.empty();
        }
        var scoped = scopeParams(scope).addValue("inboundOrderNo", inboundOrderNo);
        var receipts = jdbc.query(receiptSelect() + RECEIPT_FROM
                + scopeSql("i") + " and i.inbound_order_no = :inboundOrderNo order by r.updated_at desc",
            scoped, this::mapReceipt);
        var inspections = jdbc.query(inspectionSelect() + INSPECTION_FROM
                + scopeSql("i") + " and i.inbound_order_no = :inboundOrderNo order by q.updated_at desc",
            scoped, this::mapInspection);
        var putaways = jdbc.query(putawaySelect() + PUTAWAY_FROM
                + scopeSql("i") + " and i.inbound_order_no = :inboundOrderNo order by p.updated_at desc",
            scoped, this::mapPutaway);
        return Optional.of(new InboundDetail(inbound.get(), receipts, inspections, putaways));
    }

    @Override
    public PageResult<ReceiptSummary> pageReceipts(PageCriteria criteria, DataScope scope) {
        var query = query(RECEIPT_FROM, "i", criteria, scope,
                "r.receipt_no, i.inbound_order_no, r.sku_code", "r.receipt_status");
        return page(criteria, query, receiptSelect() + query.sql()
            + order(criteria, "r.updated_at", "r.created_at", "r.receipt_status", "r.received_qty")
            + limit(), this::mapReceipt);
    }

    @Override
    public Optional<ReceiptSummary> receiptDetail(String receiptNo, DataScope scope) {
        return detail(receiptSelect() + RECEIPT_FROM, "i", "r.receipt_no",
                receiptNo, scope, this::mapReceipt);
    }

    @Override
    public PageResult<InspectionSummary> pageInspections(PageCriteria criteria, DataScope scope) {
        var query = query(INSPECTION_FROM, "i", criteria, scope,
                "q.inspection_no, r.receipt_no, r.sku_code", "q.inspection_status");
        return page(criteria, query, inspectionSelect() + query.sql()
            + order(criteria, "q.updated_at", "q.created_at", "q.inspection_status", "q.inspect_qty")
            + limit(), this::mapInspection);
    }

    @Override
    public Optional<InspectionSummary> inspectionDetail(String inspectionNo, DataScope scope) {
        return detail(inspectionSelect() + INSPECTION_FROM, "i", "q.inspection_no",
                inspectionNo, scope, this::mapInspection);
    }

    @Override
    public PageResult<PutawaySummary> pagePutawayTasks(PageCriteria criteria, DataScope scope) {
        var query = query(PUTAWAY_FROM, "i", criteria, scope,
                "p.task_no, q.inspection_no, r.sku_code", "p.task_status");
        return page(criteria, query, putawaySelect() + query.sql()
            + order(criteria, "p.updated_at", "p.created_at", "p.task_status", "p.putaway_qty")
            + limit(), this::mapPutaway);
    }

    @Override
    public Optional<PutawaySummary> putawayDetail(String taskNo, DataScope scope) {
        return detail(putawaySelect() + PUTAWAY_FROM, "i", "p.task_no",
                taskNo, scope, this::mapPutaway);
    }

    @Override
    public PageResult<StockSummary> pageStocks(PageCriteria criteria, DataScope scope) {
        var query = query(STOCK_FROM, "i", criteria, scope,
                "l.location_code, l.sku_code, l.batch_no", null);
        var grouped = query.sql() + """
             group by l.warehouse_id, i.owner_id, l.location_code, l.sku_code, l.batch_no
            """;
        var total = jdbc.queryForObject("select count(*) from (select 1 " + grouped
            + ") stock_count", query.params(), Long.class);
        var sql = stockSelect() + grouped
            + order(criteria, "last_occurred_at", "last_occurred_at", "last_occurred_at", "quantity")
            + limit();
        var records = jdbc.query(sql, pageParams(query.params(), criteria), this::mapStock);
        return new PageResult<>(criteria.pageNo(), criteria.pageSize(),
            total == null ? 0 : total, records);
    }

    @Override
    public Optional<StockSummary> stockDetail(String stockKey, DataScope scope) {
        var params = scopeParams(scope).addValue("stockKey", stockKey);
        return jdbc.query(stockSelect() + STOCK_FROM + scopeSql("i") + """
             group by l.warehouse_id, i.owner_id, l.location_code, l.sku_code, l.batch_no
             having stock_key = :stockKey
            """, params, this::mapStock).stream().findFirst();
    }

    /**
     * 执行普通列表的总数与记录查询。
     */
    private <T> PageResult<T> page(PageCriteria criteria, SqlQuery query,
                                   String selectSql, RowMapper<T> mapper) {
        var total = jdbc.queryForObject("select count(*) " + query.sql(),
            query.params(), Long.class);
        var records = jdbc.query(selectSql, pageParams(query.params(), criteria), mapper);
        return new PageResult<>(criteria.pageNo(), criteria.pageSize(),
            total == null ? 0 : total, records);
    }

    /**
     * 执行带数据范围的精确业务编号查询。
     */
    private <T> Optional<T> detail(String selectAndFrom, String inboundAlias,
                                   String column, String businessNo, DataScope scope,
                                   RowMapper<T> mapper) {
        var params = scopeParams(scope).addValue("businessNo", businessNo);
        return jdbc.query(selectAndFrom + scopeSql(inboundAlias)
                + " and " + column + " = :businessNo",
            params, mapper).stream().findFirst();
    }

    /**
     * 组合固定表结构、数据范围、关键字和状态条件。列名只由调用处常量给出，
     * 客户端值全部通过命名参数绑定。
     */
    private SqlQuery query(String from, String inboundAlias, PageCriteria criteria,
                           DataScope scope, String keywordColumns, String statusColumn) {
        var sql = new StringBuilder(from).append(scopeSql(inboundAlias));
        var params = scopeParams(scope);
        if (criteria.keyword() != null) {
            sql.append(" and concat_ws(' ', ").append(keywordColumns)
                .append(") like :keyword");
            params.addValue("keyword", "%" + criteria.keyword() + "%");
        }
        if (criteria.status() != null && statusColumn != null) {
            sql.append(" and ").append(statusColumn).append(" = :status");
            params.addValue("status", criteria.status());
        }
        return new SqlQuery(sql.toString(), params);
    }

    private static String scopeSql(String alias) {
        return " and (:allWarehouses = true or " + alias + ".warehouse_id in (:warehouseIds))"
            + " and (:allOwners = true or " + alias + ".owner_id in (:ownerIds))";
    }

    private static MapSqlParameterSource scopeParams(DataScope scope) {
        return new MapSqlParameterSource()
            .addValue("allWarehouses", scope.allWarehouses())
            .addValue("warehouseIds", scope.allWarehouses() ? List.of(-1L) : scope.warehouseIds())
            .addValue("allOwners", scope.allOwners())
            .addValue("ownerIds", scope.allOwners() ? List.of(-1L) : scope.ownerIds());
    }

    private static MapSqlParameterSource pageParams(MapSqlParameterSource source,
                                                    PageCriteria criteria) {
        return source.addValue("offset", criteria.offset())
            .addValue("pageSize", criteria.pageSize());
    }

    private static String order(PageCriteria criteria, String updated, String created,
                                String status, String quantity) {
        var field = switch (criteria.sortField()) {
            case UPDATED_AT -> updated;
            case CREATED_AT -> created;
            case STATUS -> status;
            case QUANTITY -> quantity;
        };
        var direction = criteria.sortDirection() == SortDirection.ASC ? " asc" : " desc";
        return " order by " + field + direction + ", " + updated + " desc";
    }

    private static String limit() {
        return " limit :pageSize offset :offset";
    }

    private static String receiptSelect() {
        return """
            select r.receipt_id, r.receipt_no, i.inbound_order_no, i.warehouse_id, i.owner_id,
              r.sku_code, r.expected_qty, r.received_qty, r.rejected_qty,
              (r.expected_qty-r.received_qty-r.rejected_qty) difference_qty,
              r.receipt_status, r.updated_at
            """;
    }

    private static String inspectionSelect() {
        return """
            select q.inspection_id, q.inspection_no, r.receipt_no, i.warehouse_id, i.owner_id,
              r.sku_code, q.inspect_qty, q.qualified_qty, q.unqualified_qty,
              q.inspection_status, q.updated_at
            """;
    }

    private static String putawaySelect() {
        return """
            select p.task_id, p.task_no, q.inspection_no, i.warehouse_id, i.owner_id,
              r.sku_code, p.required_qty, p.putaway_qty, p.task_status, p.updated_at
            """;
    }

    private static String stockSelect() {
        return """
            select sha2(concat_ws(char(31), l.warehouse_id, coalesce(i.owner_id, ''),
              l.location_code, l.sku_code, coalesce(l.batch_no, '')), 256) stock_key,
              l.warehouse_id, i.owner_id, l.location_code, l.sku_code, l.batch_no,
              sum(l.quantity) quantity, max(l.occurred_at) last_occurred_at
            """;
    }

    private InboundSummary mapInbound(ResultSet rs, int rowNum) throws SQLException {
        var status = InboundOrderStatus.of(rs.getInt("inbound_status"));
        return new InboundSummary(rs.getLong("inbound_id"), rs.getString("inbound_order_no"),
            rs.getString("source_type"), rs.getString("source_order_no"),
            rs.getLong("warehouse_id"), nullableLong(rs, "owner_id"), status.code(),
            status.label(), timestamp(rs, "expected_arrival_at"), rs.getBigDecimal("received_qty"),
            rs.getBigDecimal("qualified_qty"), rs.getBigDecimal("putaway_qty"),
            timestamp(rs, "updated_at"));
    }

    private ReceiptSummary mapReceipt(ResultSet rs, int rowNum) throws SQLException {
        var status = rs.getInt("receipt_status");
        return new ReceiptSummary(rs.getLong("receipt_id"), rs.getString("receipt_no"),
            rs.getString("inbound_order_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), rs.getString("sku_code"),
            rs.getBigDecimal("expected_qty"), rs.getBigDecimal("received_qty"),
            rs.getBigDecimal("rejected_qty"), rs.getBigDecimal("difference_qty"), status,
            switch (status) { case 1 -> "收货中"; case 2 -> "已收货"; default -> "异常"; },
            timestamp(rs, "updated_at"));
    }

    private InspectionSummary mapInspection(ResultSet rs, int rowNum) throws SQLException {
        var status = rs.getInt("inspection_status");
        return new InspectionSummary(rs.getLong("inspection_id"), rs.getString("inspection_no"),
            rs.getString("receipt_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), rs.getString("sku_code"),
            rs.getBigDecimal("inspect_qty"), rs.getBigDecimal("qualified_qty"),
            rs.getBigDecimal("unqualified_qty"), status,
            status == 1 ? "待检" : "已完成", timestamp(rs, "updated_at"));
    }

    private PutawaySummary mapPutaway(ResultSet rs, int rowNum) throws SQLException {
        var status = rs.getInt("task_status");
        return new PutawaySummary(rs.getLong("task_id"), rs.getString("task_no"),
            rs.getString("inspection_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), rs.getString("sku_code"),
            rs.getBigDecimal("required_qty"), rs.getBigDecimal("putaway_qty"), status,
            status == 1 ? "待上架" : "已完成", timestamp(rs, "updated_at"));
    }

    private StockSummary mapStock(ResultSet rs, int rowNum) throws SQLException {
        return new StockSummary(rs.getString("stock_key"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), rs.getString("location_code"),
            rs.getString("sku_code"), rs.getString("batch_no"), rs.getBigDecimal("quantity"),
            timestamp(rs, "last_occurred_at"), true);
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static OffsetDateTime timestamp(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }

    private record SqlQuery(String sql, MapSqlParameterSource params) {
    }
}
