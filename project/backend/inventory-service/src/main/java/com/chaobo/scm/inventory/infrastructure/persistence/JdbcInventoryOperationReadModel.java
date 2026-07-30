package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryOperationReadModelPort;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 基于库存事实表的运营只读模型。
 *
 * <p>SQL 只使用服务端固定模板和白名单排序；货主、仓库、SKU、批次和状态均参数绑定。
 * 事件日志从标准信封提取范围，无法证明范围的历史消息只对双通配管理员可见。
 *
 * @author SCM Team
 */
@Repository
public class JdbcInventoryOperationReadModel implements InventoryOperationReadModelPort {

    private static final Set<String> SAFE_ORDER_BY = Set.of(
            "status asc", "status desc",
            "quantity asc", "quantity desc",
            "updated_at asc", "updated_at desc");
    private static final String EVENT_OWNER = """
            cast(coalesce(
                json_unquote(json_extract(envelope_json,'$.payload.ownerId')),
                json_unquote(json_extract(payload_json,'$.ownerId'))
            ) as unsigned)
            """;
    private static final String EVENT_WAREHOUSE = """
            cast(coalesce(
                json_unquote(json_extract(envelope_json,'$.payload.warehouseId')),
                json_unquote(json_extract(payload_json,'$.warehouseId'))
            ) as unsigned)
            """;
    private static final String EVENT_SKU = """
            coalesce(
                json_unquote(json_extract(envelope_json,'$.payload.sku')),
                json_unquote(json_extract(envelope_json,'$.payload.skuCode')),
                json_unquote(json_extract(payload_json,'$.sku')),
                json_unquote(json_extract(payload_json,'$.skuCode'))
            )
            """;
    private static final String EVENT_BATCH = """
            coalesce(
                json_unquote(json_extract(envelope_json,'$.payload.batchNo')),
                json_unquote(json_extract(payload_json,'$.batchNo'))
            )
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInventoryOperationReadModel(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page<ReservationView> reservations(
            QueryScope scope,
            OperationFilter query) {
        QuerySql sql = scoped(
                """
                from (
                    select r.reservation_no,s.owner_id,s.warehouse_id,s.sku_code,s.batch_no,
                           r.source_system,r.source_order_no,r.reserved_qty,r.released_qty,
                           r.reservation_status status,r.version,r.updated_at,
                           r.reserved_qty quantity
                      from inv_reservation r
                      join inv_stock_balance s on s.stock_id=r.stock_id
                ) x
                where 1=1
                """,
                "x",
                scope,
                query);
        return page(
                """
                select x.reservation_no,x.owner_id,x.warehouse_id,x.sku_code,x.batch_no,
                       x.source_system,x.source_order_no,x.reserved_qty,x.released_qty,
                       x.status,x.version,x.updated_at,x.quantity
                """ + sql.body(),
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> new ReservationView(
                        text(row, "reservation_no"),
                        number(row, "owner_id"),
                        number(row, "warehouse_id"),
                        text(row, "sku_code"),
                        text(row, "batch_no"),
                        text(row, "source_system"),
                        text(row, "source_order_no"),
                        decimal(row, "reserved_qty"),
                        decimal(row, "released_qty"),
                        integer(row, "status"),
                        integer(row, "version"),
                        time(row, "updated_at")));
    }

    @Override
    public Page<FreezeView> freezes(
            QueryScope scope,
            OperationFilter query) {
        QuerySql sql = scoped(
                "from inv_freeze x where 1=1 ",
                "x",
                scope,
                query);
        return page(
                """
                select x.freeze_no,x.owner_id,x.warehouse_id,x.sku_code,x.batch_no,
                       x.freeze_qty,x.unfrozen_qty,x.freeze_reason,x.freeze_status status,
                       x.approval_status,x.version,x.updated_at,x.freeze_qty quantity
                """ + sql.body(),
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> new FreezeView(
                        text(row, "freeze_no"),
                        number(row, "owner_id"),
                        number(row, "warehouse_id"),
                        text(row, "sku_code"),
                        text(row, "batch_no"),
                        decimal(row, "freeze_qty"),
                        decimal(row, "unfrozen_qty"),
                        text(row, "freeze_reason"),
                        integer(row, "status"),
                        integer(row, "approval_status"),
                        integer(row, "version"),
                        time(row, "updated_at")));
    }

    @Override
    public Page<AdjustmentView> adjustments(
            QueryScope scope,
            OperationFilter query) {
        QuerySql sql = scoped(
                "from inv_stock_adjustment x where 1=1 ",
                "x",
                scope,
                query);
        return page(
                """
                select x.adjustment_no,x.owner_id,x.warehouse_id,x.sku_code,x.batch_no,
                       x.adjust_qty,x.adjustment_type,x.adjustment_reason,
                       x.adjustment_status status,x.approval_status,x.version,x.updated_at,
                       abs(x.adjust_qty) quantity
                """ + sql.body(),
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> new AdjustmentView(
                        text(row, "adjustment_no"),
                        number(row, "owner_id"),
                        number(row, "warehouse_id"),
                        text(row, "sku_code"),
                        text(row, "batch_no"),
                        decimal(row, "adjust_qty"),
                        text(row, "adjustment_type"),
                        text(row, "adjustment_reason"),
                        integer(row, "status"),
                        integer(row, "approval_status"),
                        integer(row, "version"),
                        time(row, "updated_at")));
    }

    @Override
    public Page<EventLogView> eventLogs(
            QueryScope scope,
            OperationFilter query) {
        String events = """
                (
                    select 'INBOUND' direction,i.source_system,i.event_code,i.event_type,
                           i.event_version,
                           %s owner_id,%s warehouse_id,
                           i.aggregate_type,i.aggregate_id,i.status,i.retry_count,
                           i.last_error,i.updated_at,0 quantity,%s sku_code,%s batch_no
                      from inv_inbox_event i
                    union all
                    select 'OUTBOUND' direction,'INVENTORY' source_system,o.event_code,
                           o.event_type,o.event_version,
                           cast(json_unquote(json_extract(o.payload_json,'$.ownerId')) as unsigned)
                               owner_id,
                           cast(json_unquote(json_extract(o.payload_json,'$.warehouseId')) as unsigned)
                               warehouse_id,
                           o.aggregate_type,o.aggregate_id,o.status,o.retry_count,
                           o.last_error,o.updated_at,0 quantity,
                           json_unquote(json_extract(o.payload_json,'$.sku')) sku_code,
                           json_unquote(json_extract(o.payload_json,'$.batchNo')) batch_no
                      from inv_outbox_event o
                ) e
                """.formatted(
                        EVENT_OWNER,
                        EVENT_WAREHOUSE,
                        EVENT_SKU,
                        EVENT_BATCH);
        QuerySql sql = scoped("from " + events + " where 1=1 ", "e", scope, query);
        return page(
                """
                select e.direction,e.source_system,e.event_code,e.event_type,e.event_version,
                       e.owner_id,e.warehouse_id,e.aggregate_type,e.aggregate_id,e.status,
                       e.retry_count,e.last_error,e.updated_at,e.quantity
                """ + sql.body(),
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> new EventLogView(
                        text(row, "direction"),
                        text(row, "source_system"),
                        text(row, "event_code"),
                        text(row, "event_type"),
                        text(row, "event_version"),
                        nullableNumber(row, "owner_id"),
                        nullableNumber(row, "warehouse_id"),
                        text(row, "aggregate_type"),
                        text(row, "aggregate_id"),
                        integer(row, "status"),
                        integer(row, "retry_count"),
                        text(row, "last_error"),
                        time(row, "updated_at")));
    }

    @Override
    public Page<OperationLogView> operationLogs(
            QueryScope scope,
            OperationFilter query) {
        String base = """
                from (
                    select l.operation_log_id,l.operator_id,l.operation_type,l.operation_reason,
                           l.target_type,l.target_no,l.result,l.request_id,l.operation_at,
                           coalesce(f.owner_id,a.owner_id) owner_id,
                           coalesce(f.warehouse_id,a.warehouse_id) warehouse_id,
                           coalesce(f.sku_code,a.sku_code) sku_code,
                           coalesce(f.batch_no,a.batch_no) batch_no,
                           l.result status,l.operation_at updated_at,0 quantity
                      from inv_operation_audit_log l
                      left join inv_freeze f
                        on l.target_type='StockFreeze' and l.target_id=f.freeze_id
                      left join inv_stock_adjustment a
                        on l.target_type='InventoryAdjustment'
                       and l.target_id=a.stock_adjustment_id
                ) x
                where x.owner_id is not null and x.warehouse_id is not null
                """;
        QuerySql sql = scoped(base, "x", scope, query);
        return page(
                """
                select x.operation_log_id,x.owner_id,x.warehouse_id,x.operator_id,
                       x.operation_type,x.operation_reason,x.target_type,x.target_no,
                       x.result,x.request_id,x.operation_at,x.updated_at,x.quantity
                """ + sql.body(),
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> new OperationLogView(
                        number(row, "operation_log_id"),
                        number(row, "owner_id"),
                        number(row, "warehouse_id"),
                        number(row, "operator_id"),
                        text(row, "operation_type"),
                        text(row, "operation_reason"),
                        text(row, "target_type"),
                        text(row, "target_no"),
                        integer(row, "result"),
                        text(row, "request_id"),
                        time(row, "operation_at")));
    }

    @Override
    public Page<MetricView> metrics(
            MetricType metricType,
            QueryScope scope,
            MetricFilter query) {
        return switch (metricType) {
            case BOOK_PHYSICAL -> bookPhysical(scope, query);
            case STOCK_AGE -> stockAge(scope, query, false);
            case SLOW_MOVING -> stockAge(scope, query, true);
            case EXPIRY -> expiry(scope, query);
        };
    }

    private Page<MetricView> bookPhysical(QueryScope scope, MetricFilter query) {
        String base = """
                from inv_stock_reconcile r
                join inv_stock_balance s on s.stock_id=r.stock_id
                where 1=1
                """;
        QuerySql sql = scopedMetric(base, "s", scope, query);
        return metricPage(
                """
                select s.stock_id,s.owner_id,s.warehouse_id,s.sku_code,s.batch_no,
                       r.system_qty book_qty,r.wms_qty physical_qty,
                       r.difference_qty,r.updated_at fact_at
                """ + sql.body() + " order by r.updated_at desc",
                "select count(*) " + sql.body(),
                sql.parameters(),
                query,
                row -> metric(
                        row,
                        decimal(row, "book_qty"),
                        decimal(row, "physical_qty"),
                        decimal(row, "difference_qty"),
                        null,
                        null,
                        null,
                        null,
                        "库存对账单：系统数量与 WMS 实盘数量",
                        time(row, "fact_at")));
    }

    private Page<MetricView> stockAge(
            QueryScope scope,
            MetricFilter query,
            boolean slowMovingOnly) {
        String base = """
                from inv_stock_balance s
                left join (
                    select stock_id,
                           min(case when qty_delta>0 then created_at end) first_inbound_at,
                           max(created_at) last_movement_at
                      from inv_stock_ledger
                     group by stock_id
                ) movement on movement.stock_id=s.stock_id
                where s.on_hand_qty>0
                """;
        QuerySql sql = scopedMetric(base, "s", scope, query);
        MapSqlParameterSource parameters = sql.parameters();
        String body = sql.body();
        if (slowMovingOnly) {
            parameters.addValue("slowMovingDays", query.slowMovingDays());
            body += """
                     and datediff(
                         current_date,
                         date(coalesce(movement.last_movement_at,s.created_at))
                     ) >= :slowMovingDays
                    """;
        }
        String finalBody = body;
        return metricPage(
                """
                select s.stock_id,s.owner_id,s.warehouse_id,s.sku_code,s.batch_no,
                       s.on_hand_qty book_qty,
                       datediff(current_date,date(coalesce(movement.first_inbound_at,s.created_at)))
                           stock_age_days,
                       datediff(current_date,date(coalesce(movement.last_movement_at,s.created_at)))
                           inactive_days,
                       coalesce(movement.last_movement_at,s.created_at) fact_at
                """ + finalBody + " order by inactive_days desc,s.stock_id",
                "select count(*) " + finalBody,
                parameters,
                query,
                row -> metric(
                        row,
                        decimal(row, "book_qty"),
                        null,
                        null,
                        integer(row, "stock_age_days"),
                        integer(row, "inactive_days"),
                        null,
                        null,
                        slowMovingOnly
                                ? "在手量大于零，距最后库存流水达到阈值"
                                : "首次正向库存流水至查询日",
                        time(row, "fact_at")));
    }

    private Page<MetricView> expiry(QueryScope scope, MetricFilter query) {
        String base = """
                from inv_inventory_batch_fact f
                join inv_stock_balance s on s.stock_id=f.stock_id
                where s.on_hand_qty>0 and f.expiry_date is not null
                """;
        QuerySql sql = scopedMetric(base, "s", scope, query);
        sql.parameters().addValue("expiryWarningDays", query.expiryWarningDays());
        String body = sql.body()
                + " and f.expiry_date<=date_add(current_date,interval :expiryWarningDays day)";
        return metricPage(
                """
                select s.stock_id,s.owner_id,s.warehouse_id,s.sku_code,s.batch_no,
                       s.on_hand_qty book_qty,f.expiry_date,
                       datediff(f.expiry_date,current_date) days_to_expiry,
                       f.expiry_fact_at fact_at
                """ + body + " order by f.expiry_date,s.stock_id",
                "select count(*) " + body,
                sql.parameters(),
                query,
                row -> metric(
                        row,
                        decimal(row, "book_qty"),
                        null,
                        null,
                        null,
                        null,
                        date(row, "expiry_date"),
                        integer(row, "days_to_expiry"),
                        "WMS 上架完成事件提供的批次效期",
                        time(row, "fact_at")));
    }

    private QuerySql scoped(
            String base,
            String alias,
            QueryScope scope,
            OperationFilter query) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder(base);
        appendScope(sql, parameters, alias, scope);
        if (query.sku() != null) {
            sql.append(" and ").append(alias).append(".sku_code=:sku");
            parameters.addValue("sku", query.sku());
        }
        if (query.batchNo() != null) {
            sql.append(" and ifnull(").append(alias)
                    .append(".batch_no,'')=:batchNo");
            parameters.addValue("batchNo", query.batchNo());
        }
        if (query.status() != null) {
            sql.append(" and ").append(alias).append(".status=:status");
            parameters.addValue("status", query.status());
        }
        return new QuerySql(sql.toString(), parameters);
    }

    private QuerySql scopedMetric(
            String base,
            String alias,
            QueryScope scope,
            MetricFilter query) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder(base);
        appendScope(sql, parameters, alias, scope);
        if (query.sku() != null) {
            sql.append(" and ").append(alias).append(".sku_code=:sku");
            parameters.addValue("sku", query.sku());
        }
        if (query.batchNo() != null) {
            sql.append(" and ifnull(").append(alias)
                    .append(".batch_no,'')=:batchNo");
            parameters.addValue("batchNo", query.batchNo());
        }
        return new QuerySql(sql.toString(), parameters);
    }

    private static void appendScope(
            StringBuilder sql,
            MapSqlParameterSource parameters,
            String alias,
            QueryScope scope) {
        if (!scope.ownerIds().isEmpty()) {
            sql.append(" and ").append(alias).append(".owner_id in (:ownerIds)");
            parameters.addValue("ownerIds", scope.ownerIds());
        } else if (!scope.allOwners()) {
            sql.append(" and 1=0");
        }
        if (!scope.warehouseIds().isEmpty()) {
            sql.append(" and ").append(alias).append(".warehouse_id in (:warehouseIds)");
            parameters.addValue("warehouseIds", scope.warehouseIds());
        } else if (!scope.allWarehouses()) {
            sql.append(" and 1=0");
        }
    }

    private <T> Page<T> page(
            String select,
            String count,
            MapSqlParameterSource parameters,
            OperationFilter query,
            RowConverter<T> converter) {
        long total = count(count, parameters);
        parameters.addValue("limit", query.limit()).addValue("offset", query.offset());
        String orderBy = safeOrderBy(query.orderBy());
        List<T> records = jdbc.queryForList(
                        select + " order by " + orderBy + " limit :limit offset :offset",
                        parameters)
                .stream()
                .map(converter::convert)
                .toList();
        return new Page<>(total, records);
    }

    private Page<MetricView> metricPage(
            String select,
            String count,
            MapSqlParameterSource parameters,
            MetricFilter query,
            RowConverter<MetricView> converter) {
        long total = count(count, parameters);
        parameters.addValue("limit", query.limit()).addValue("offset", query.offset());
        List<MetricView> records = jdbc.queryForList(
                        select + " limit :limit offset :offset",
                        parameters)
                .stream()
                .map(converter::convert)
                .toList();
        return new Page<>(total, records);
    }

    private long count(String sql, MapSqlParameterSource parameters) {
        Long value = jdbc.queryForObject(sql, parameters, Long.class);
        return value == null ? 0L : value;
    }

    private static String safeOrderBy(String value) {
        if (!SAFE_ORDER_BY.contains(value)) {
            throw new IllegalArgumentException("不支持的运营读模型排序");
        }
        return value;
    }

    private static MetricView metric(
            Map<String, Object> row,
            BigDecimal bookQty,
            BigDecimal physicalQty,
            BigDecimal differenceQty,
            Integer stockAgeDays,
            Integer inactiveDays,
            LocalDate expiryDate,
            Integer daysToExpiry,
            String basis,
            LocalDateTime factAt) {
        return new MetricView(
                number(row, "stock_id"),
                number(row, "owner_id"),
                number(row, "warehouse_id"),
                text(row, "sku_code"),
                text(row, "batch_no"),
                bookQty,
                physicalQty,
                differenceQty,
                stockAgeDays,
                inactiveDays,
                expiryDate,
                daysToExpiry,
                basis,
                factAt);
    }

    private static String text(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : value.toString();
    }

    private static long number(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private static Long nullableNumber(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private static int integer(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static BigDecimal decimal(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? null : new BigDecimal(value.toString());
    }

    private static LocalDateTime time(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Timestamp timestamp
                ? timestamp.toLocalDateTime()
                : (LocalDateTime) value;
    }

    private static LocalDate date(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value instanceof Date sqlDate ? sqlDate.toLocalDate() : (LocalDate) value;
    }

    private record QuerySql(String body, MapSqlParameterSource parameters) {
    }

    @FunctionalInterface
    private interface RowConverter<T> {

        T convert(Map<String, Object> row);
    }
}
