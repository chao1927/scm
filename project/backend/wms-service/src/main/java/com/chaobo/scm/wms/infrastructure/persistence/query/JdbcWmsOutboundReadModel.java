package com.chaobo.scm.wms.infrastructure.persistence.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsOutboundReadModelPort;
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
 * 从出库命令模型构建只读作业链视图。
 *
 * <p>作业表均通过 {@code wms_outbound} 回溯来源单、仓库和货主，不复制另一套可变业务状态。
 */
@Repository
public class JdbcWmsOutboundReadModel implements WmsOutboundReadModelPort {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcWmsOutboundReadModel(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public PageResult<OutboundSummary> pageOutbounds(Query q, Scope s) {
        var select = """
            select o.outbound_id,o.outbound_no,o.source_type,o.source_no,o.warehouse_id,o.owner_id,
              o.outbound_status,o.updated_at,
              coalesce(sum(t.required_qty),0) required_qty,coalesce(sum(t.picked_qty),0) picked_qty,
              count(distinct c.container_id) container_count
            from wms_outbound o
             left join wms_pick_task t on t.outbound_id=o.outbound_id
             left join wms_container c on c.outbound_id=o.outbound_id
             where 1=1
            """ + scope("o") + keyword(q, "o.outbound_no,o.source_no,o.source_type")
            + status(q, "o.outbound_status") + """
             group by o.outbound_id,o.outbound_no,o.source_type,o.source_no,o.warehouse_id,o.owner_id,
               o.outbound_status,o.updated_at,o.created_at
            """;
        return groupedPage(q, s, select, order(q, "o.updated_at", "o.created_at",
            "o.outbound_status", "required_qty"), this::outbound);
    }

    @Override
    public Optional<OutboundSummary> outboundDetail(String no, Scope s) {
        return exact(pageOutbounds(new Query(no, null, 1, 100, SortField.UPDATED_AT,
            SortDirection.DESC), s).records(), value -> value.outboundNo().equals(no));
    }

    @Override
    public PageResult<WaveSummary> pageWaves(Query q, Scope s) {
        var select = """
            select w.wave_id,w.wave_no,w.warehouse_id,
              case when count(distinct o.owner_id)=1 then max(o.owner_id) else null end owner_id,
              w.wave_status,w.updated_at,count(distinct o.outbound_id) outbound_count,
              count(distinct t.task_id) task_count,coalesce(sum(t.required_qty),0) required_qty,
              coalesce(sum(t.picked_qty),0) picked_qty
            from wms_wave w
            join wms_pick_task t on t.wave_id=w.wave_id
            join wms_outbound o on o.outbound_id=t.outbound_id
            where 1=1
            """ + scope("o") + keyword(q, "w.wave_no,o.outbound_no,o.source_no")
            + status(q, "w.wave_status") + """
             group by w.wave_id,w.wave_no,w.warehouse_id,w.wave_status,w.updated_at,w.created_at
            """;
        return groupedPage(q, s, select, order(q, "w.updated_at", "w.created_at",
            "w.wave_status", "required_qty"), this::wave);
    }

    @Override
    public Optional<WaveSummary> waveDetail(String no, Scope s) {
        return exact(pageWaves(new Query(no, null, 1, 100, SortField.UPDATED_AT,
            SortDirection.DESC), s).records(), value -> value.waveNo().equals(no));
    }

    @Override
    public PageResult<PickSummary> pagePicks(Query q, Scope s) {
        var select = """
            select t.task_id,t.task_no,w.wave_no,o.outbound_no,o.source_no,o.warehouse_id,o.owner_id,
              t.sku_code,t.required_qty,t.picked_qty,t.task_status,t.updated_at,t.created_at
            from wms_pick_task t join wms_wave w on w.wave_id=t.wave_id
            join wms_outbound o on o.outbound_id=t.outbound_id where 1=1
            """ + scope("o") + keyword(q, "t.task_no,w.wave_no,o.outbound_no,o.source_no,t.sku_code")
            + status(q, "t.task_status");
        return plainPage(q, s, select, order(q, "t.updated_at", "t.created_at",
            "t.task_status", "t.picked_qty"), this::pick);
    }

    @Override
    public Optional<PickSummary> pickDetail(String no, Scope s) {
        return exact(pagePicks(new Query(no, null, 1, 100, SortField.UPDATED_AT,
            SortDirection.DESC), s).records(), value -> value.taskNo().equals(no));
    }

    @Override
    public PageResult<PackingSummary> pagePackings(Query q, Scope s) {
        var select = """
            select p.packing_id,p.packing_no,o.outbound_no,o.source_no,o.warehouse_id,o.owner_id,
              p.container_no,c.container_status,p.packing_status,p.updated_at,p.created_at
            from wms_packing p join wms_outbound o on o.outbound_id=p.outbound_id
            left join wms_container c on c.container_no=p.container_no where 1=1
            """ + scope("o") + keyword(q, "p.packing_no,p.container_no,o.outbound_no,o.source_no")
            + status(q, "p.packing_status");
        return plainPage(q, s, select, order(q, "p.updated_at", "p.created_at",
            "p.packing_status", "p.packing_id"), this::packing);
    }

    @Override
    public Optional<PackingSummary> packingDetail(String no, Scope s) {
        return exact(pagePackings(new Query(no, null, 1, 100, SortField.UPDATED_AT,
            SortDirection.DESC), s).records(), value -> value.packingNo().equals(no));
    }

    @Override
    public PageResult<ShipmentSummary> pageShipments(Query q, Scope s) {
        var select = """
            select h.handover_id,h.handover_no,o.outbound_no,o.source_no,o.warehouse_id,o.owner_id,
              h.handover_status,h.updated_at,h.created_at,count(distinct p.packing_id) packing_count
            from wms_shipment_handover h join wms_outbound o on o.outbound_id=h.outbound_id
            left join wms_packing p on p.outbound_id=o.outbound_id where 1=1
            """ + scope("o") + keyword(q, "h.handover_no,o.outbound_no,o.source_no")
            + status(q, "h.handover_status") + """
             group by h.handover_id,h.handover_no,o.outbound_no,o.source_no,o.warehouse_id,o.owner_id,
               h.handover_status,h.updated_at,h.created_at
            """;
        return groupedPage(q, s, select, order(q, "h.updated_at", "h.created_at",
            "h.handover_status", "packing_count"), this::shipment);
    }

    @Override
    public Optional<ShipmentSummary> shipmentDetail(String no, Scope s) {
        return exact(pageShipments(new Query(no, null, 1, 100, SortField.UPDATED_AT,
            SortDirection.DESC), s).records(), value -> value.handoverNo().equals(no));
    }

    private <T> PageResult<T> plainPage(Query q, Scope s, String select, String order,
                                        RowMapper<T> mapper) {
        var params = params(q, s);
        var total = jdbc.queryForObject("select count(*) from (" + select + ") x", params, Long.class);
        var records = jdbc.query(select + order + limit(), page(params, q), mapper);
        return result(q, total, records);
    }

    private <T> PageResult<T> groupedPage(Query q, Scope s, String select, String order,
                                          RowMapper<T> mapper) {
        return plainPage(q, s, select, order, mapper);
    }

    private static <T> PageResult<T> result(Query q, Long total, List<T> records) {
        return new PageResult<>(q.pageNo(), q.pageSize(), total == null ? 0 : total, records);
    }

    private static MapSqlParameterSource params(Query q, Scope s) {
        var params = new MapSqlParameterSource()
            .addValue("allWarehouses", s.allWarehouses())
            .addValue("warehouseIds", s.allWarehouses() ? List.of(-1L) : s.warehouseIds())
            .addValue("allOwners", s.allOwners())
            .addValue("ownerIds", s.allOwners() ? List.of(-1L) : s.ownerIds());
        if (q.keyword() != null) {
            params.addValue("keyword", "%" + q.keyword() + "%");
        }
        if (q.status() != null) {
            params.addValue("status", q.status());
        }
        return params;
    }

    private static MapSqlParameterSource page(MapSqlParameterSource params, Query q) {
        return params.addValue("offset", q.offset()).addValue("pageSize", q.pageSize());
    }

    private static String scope(String alias) {
        return " and (:allWarehouses=true or " + alias + ".warehouse_id in (:warehouseIds))"
            + " and (:allOwners=true or " + alias + ".owner_id in (:ownerIds))";
    }

    private static String keyword(Query q, String columns) {
        return q.keyword() == null ? "" : " and concat_ws(' '," + columns + ") like :keyword";
    }

    private static String status(Query q, String column) {
        return q.status() == null ? "" : " and " + column + "=:status";
    }

    private static String order(Query q, String updated, String created,
                                String status, String quantity) {
        var field = switch (q.sortField()) {
            case UPDATED_AT -> updated;
            case CREATED_AT -> created;
            case STATUS -> status;
            case QUANTITY -> quantity;
        };
        return " order by " + field + (q.direction() == SortDirection.ASC ? " asc" : " desc")
            + "," + updated + " desc";
    }

    private static String limit() {
        return " limit :pageSize offset :offset";
    }

    private static <T> Optional<T> exact(List<T> records,
                                         java.util.function.Predicate<T> predicate) {
        return records.stream().filter(predicate).findFirst();
    }

    private OutboundSummary outbound(ResultSet rs, int row) throws SQLException {
        int status = rs.getInt("outbound_status");
        return new OutboundSummary(rs.getLong("outbound_id"), rs.getString("outbound_no"),
            rs.getString("source_type"), rs.getString("source_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), status, outboundStatus(status),
            rs.getBigDecimal("required_qty"), rs.getBigDecimal("picked_qty"),
            rs.getInt("container_count"), time(rs, "updated_at"));
    }

    private WaveSummary wave(ResultSet rs, int row) throws SQLException {
        int status = rs.getInt("wave_status");
        return new WaveSummary(rs.getLong("wave_id"), rs.getString("wave_no"),
            rs.getLong("warehouse_id"), nullableLong(rs, "owner_id"), status,
            status == 1 ? "待释放" : "已释放", rs.getInt("outbound_count"),
            rs.getInt("task_count"), rs.getBigDecimal("required_qty"),
            rs.getBigDecimal("picked_qty"), time(rs, "updated_at"));
    }

    private PickSummary pick(ResultSet rs, int row) throws SQLException {
        int status = rs.getInt("task_status");
        return new PickSummary(rs.getLong("task_id"), rs.getString("task_no"),
            rs.getString("wave_no"), rs.getString("outbound_no"), rs.getString("source_no"),
            rs.getLong("warehouse_id"), nullableLong(rs, "owner_id"), rs.getString("sku_code"),
            rs.getBigDecimal("required_qty"), rs.getBigDecimal("picked_qty"), status,
            status == 1 ? "待拣货" : "已完成", time(rs, "updated_at"));
    }

    private PackingSummary packing(ResultSet rs, int row) throws SQLException {
        int status = rs.getInt("packing_status");
        var containerStatus = nullableInteger(rs, "container_status");
        return new PackingSummary(rs.getLong("packing_id"), rs.getString("packing_no"),
            rs.getString("outbound_no"), rs.getString("source_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), rs.getString("container_no"), containerStatus,
            containerStatus == null ? "未绑定" : containerStatus == 1 ? "已绑定" : "已封箱",
            status, status == 1 ? "待复核" : "已复核", time(rs, "updated_at"));
    }

    private ShipmentSummary shipment(ResultSet rs, int row) throws SQLException {
        int status = rs.getInt("handover_status");
        return new ShipmentSummary(rs.getLong("handover_id"), rs.getString("handover_no"),
            rs.getString("outbound_no"), rs.getString("source_no"), rs.getLong("warehouse_id"),
            nullableLong(rs, "owner_id"), status, status == 1 ? "待交接" : "已交接",
            rs.getInt("packing_count"), time(rs, "updated_at"));
    }

    private static String outboundStatus(int status) {
        return switch (status) {
            case 1 -> "待分配";
            case 2 -> "已分配";
            case 3 -> "拣货中";
            case 4 -> "已拣货";
            case 5 -> "已交接";
            default -> "已取消";
        };
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer nullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static OffsetDateTime time(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }
}
