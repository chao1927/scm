alter table wms_inbound
    add column source_system varchar(32) null after source_type,
    add column inbound_type varchar(32) null after source_system,
    add column source_line_no varchar(64) null after source_order_no,
    add column allowed_qty decimal(18,6) null after owner_id;

update wms_inbound
set inbound_type = case
        when source_type in ('INVENTORY_TRANSFER', 'STOCK_TRANSFER', 'TRANSFER') then 'TRANSFER'
        when source_type in ('AFTERSALE_RETURN', 'AFTER_SALE_RETURN', 'SALES_RETURN', 'RETURN') then 'SALES_RETURN'
        else 'PURCHASE'
    end,
    source_system = case
        when source_type in ('INVENTORY_TRANSFER', 'STOCK_TRANSFER', 'TRANSFER') then 'INVENTORY'
        when source_type in ('AFTERSALE_RETURN', 'AFTER_SALE_RETURN', 'SALES_RETURN', 'RETURN') then 'OMS'
        when source_type = 'SUPPLIER' then 'SUPPLIER'
        else 'PURCHASE'
    end,
    source_line_no = '0',
    allowed_qty = 1
where source_system is null;

alter table wms_inbound
    modify source_system varchar(32) not null,
    modify inbound_type varchar(32) not null,
    modify source_line_no varchar(64) not null,
    modify allowed_qty decimal(18,6) not null,
    drop index uk_wms_inbound_source,
    add unique key uk_wms_inbound_source(source_system, source_order_no, source_line_no, inbound_type);
