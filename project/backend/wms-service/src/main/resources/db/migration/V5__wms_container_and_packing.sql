create table wms_container (
    container_id bigint not null primary key,
    container_no varchar(64) not null,
    outbound_id bigint not null,
    pick_task_id bigint not null,
    container_status tinyint not null comment '1已绑定 2已封箱',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_container_no(container_no),
    key idx_wms_container_outbound(outbound_id, container_status)
) comment 'WMS周转容器';

create table wms_packing (
    packing_id bigint not null primary key,
    packing_no varchar(64) not null,
    outbound_id bigint not null,
    container_no varchar(64) not null,
    packing_status tinyint not null comment '1待复核 2已复核',
    version int not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_wms_packing_no(packing_no),
    key idx_wms_packing_outbound(outbound_id, packing_status)
) comment 'WMS复核包装单';
