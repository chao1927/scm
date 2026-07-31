alter table purchase_order_change
    add column base_order_version int null comment '创建变更单时的采购订单乐观锁版本' after order_no;

update purchase_order_change change_record
join purchase_order purchase_order_record on purchase_order_record.order_no = change_record.order_no
set change_record.base_order_version = purchase_order_record.version
where change_record.base_order_version is null;

alter table purchase_order_change
    modify column base_order_version int not null comment '创建变更单时的采购订单乐观锁版本';
