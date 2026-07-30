package com.chaobo.scm.wms.infrastructure.persistence.query;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.wms.application.query.WmsExceptionReadModelPort;
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
 * 退货、盘点和异常数据库读模型。
 */
@Repository
public class JdbcWmsExceptionReadModel implements WmsExceptionReadModelPort {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcWmsExceptionReadModel(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PageResult<ReturnSummary> pageReturns(WmsOutboundReadModelPort.Query q,
                                                 WmsOutboundReadModelPort.Scope s) {
        var sql = """
            select operation_id,after_sale_no,rma_no,warehouse_id,owner_id,sku_code,batch_no,
              expected_qty,received_qty,sellable_qty,defective_qty,frozen_qty,scrapped_qty,
              unmatched_qty,operation_status,version,updated_at
            from wms_return_operation where 1=1
            """ + scope() + keyword(q, "after_sale_no,rma_no,sku_code,batch_no")
            + status(q, "operation_status");
        return page(q, s, sql, this::mapReturn);
    }

    public Optional<ReturnSummary> returnDetail(String no, WmsOutboundReadModelPort.Scope s) {
        return pageReturns(exact(no), s).records().stream()
            .filter(v -> v.afterSaleNo().equals(no) || v.rmaNo().equals(no)).findFirst();
    }

    public PageResult<StocktakeSummary> pageStocktakes(WmsOutboundReadModelPort.Query q,
                                                       WmsOutboundReadModelPort.Scope s) {
        var sql = """
            select stocktake_id,stocktake_no,warehouse_id,owner_id,sku_code,difference_qty,
              stocktake_status,version,updated_at
            from wms_stocktake where 1=1
            """ + scope() + keyword(q, "stocktake_no,sku_code") + status(q, "stocktake_status");
        return page(q, s, sql, this::mapStocktake);
    }

    public Optional<StocktakeSummary> stocktakeDetail(String no, WmsOutboundReadModelPort.Scope s) {
        return pageStocktakes(exact(no), s).records().stream()
            .filter(v -> v.stocktakeNo().equals(no)).findFirst();
    }

    public PageResult<ExceptionSummary> pageExceptions(WmsOutboundReadModelPort.Query q,
                                                       WmsOutboundReadModelPort.Scope s) {
        var sql = """
            select exception_id,exception_no,warehouse_id,owner_id,reason,exception_status,
              version,updated_at
            from wms_warehouse_exception where 1=1
            """ + scope() + keyword(q, "exception_no,reason") + status(q, "exception_status");
        return page(q, s, sql, this::mapException);
    }

    public Optional<ExceptionSummary> exceptionDetail(String no, WmsOutboundReadModelPort.Scope s) {
        return pageExceptions(exact(no), s).records().stream()
            .filter(v -> v.exceptionNo().equals(no)).findFirst();
    }

    private <T> PageResult<T> page(WmsOutboundReadModelPort.Query q,
                                   WmsOutboundReadModelPort.Scope s, String sql, RowMapper<T> mapper) {
        var params = params(q, s);
        var total = jdbc.queryForObject("select count(*) from (" + sql + ") x", params, Long.class);
        params.addValue("pageSize", q.pageSize()).addValue("offset", q.offset());
        var records = jdbc.query(sql + " order by updated_at desc limit :pageSize offset :offset",
            params, mapper);
        return new PageResult<>(q.pageNo(), q.pageSize(), total == null ? 0 : total, records);
    }

    private static WmsOutboundReadModelPort.Query exact(String no) {
        return new WmsOutboundReadModelPort.Query(no, null, 1, 100,
            WmsOutboundReadModelPort.SortField.UPDATED_AT,
            WmsOutboundReadModelPort.SortDirection.DESC);
    }

    private static String scope() {
        return " and (:allWarehouses=true or warehouse_id in (:warehouseIds))"
            + " and (:allOwners=true or owner_id in (:ownerIds))";
    }
    private static String keyword(WmsOutboundReadModelPort.Query q, String columns) {
        return q.keyword() == null ? "" : " and concat_ws(' '," + columns + ") like :keyword";
    }
    private static String status(WmsOutboundReadModelPort.Query q, String column) {
        return q.status() == null ? "" : " and " + column + "=:status";
    }
    private static MapSqlParameterSource params(WmsOutboundReadModelPort.Query q,
                                                WmsOutboundReadModelPort.Scope s) {
        var p = new MapSqlParameterSource()
            .addValue("allWarehouses", s.allWarehouses())
            .addValue("warehouseIds", s.allWarehouses() ? List.of(-1L) : s.warehouseIds())
            .addValue("allOwners", s.allOwners())
            .addValue("ownerIds", s.allOwners() ? List.of(-1L) : s.ownerIds());
        if (q.keyword() != null) p.addValue("keyword", "%" + q.keyword() + "%");
        if (q.status() != null) p.addValue("status", q.status());
        return p;
    }

    private ReturnSummary mapReturn(ResultSet r, int row) throws SQLException {
        int status = r.getInt("operation_status");
        return new ReturnSummary(r.getLong("operation_id"), r.getString("after_sale_no"),
            r.getString("rma_no"), r.getLong("warehouse_id"), r.getLong("owner_id"),
            r.getString("sku_code"), r.getString("batch_no"), r.getBigDecimal("expected_qty"),
            r.getBigDecimal("received_qty"), r.getBigDecimal("sellable_qty"),
            r.getBigDecimal("defective_qty"), r.getBigDecimal("frozen_qty"),
            r.getBigDecimal("scrapped_qty"), r.getBigDecimal("unmatched_qty"), status,
            returnStatus(status), r.getInt("version"), time(r));
    }
    private StocktakeSummary mapStocktake(ResultSet r, int row) throws SQLException {
        int status = r.getInt("stocktake_status");
        return new StocktakeSummary(r.getLong("stocktake_id"), r.getString("stocktake_no"),
            r.getLong("warehouse_id"), nullableLong(r, "owner_id"), r.getString("sku_code"),
            r.getBigDecimal("difference_qty"), status, status == 1 ? "待确认" : "已确认",
            r.getInt("version"), time(r));
    }
    private ExceptionSummary mapException(ResultSet r, int row) throws SQLException {
        int status = r.getInt("exception_status");
        return new ExceptionSummary(r.getLong("exception_id"), r.getString("exception_no"),
            nullableLong(r, "warehouse_id"), nullableLong(r, "owner_id"), r.getString("reason"),
            status, status == 1 ? "处理中" : "已关闭", r.getInt("version"), time(r));
    }
    private static String returnStatus(int status) {
        return switch (status) {
            case 1 -> "收货中"; case 2 -> "待质检"; case 3 -> "已完成"; default -> "异常";
        };
    }
    private static Long nullableLong(ResultSet r, String c) throws SQLException {
        long v = r.getLong(c); return r.wasNull() ? null : v;
    }
    private static OffsetDateTime time(ResultSet r) throws SQLException {
        Timestamp v = r.getTimestamp("updated_at");
        return v == null ? null : v.toInstant().atOffset(ZoneOffset.UTC);
    }
}
