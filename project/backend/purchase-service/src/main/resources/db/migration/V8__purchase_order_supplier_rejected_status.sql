alter table purchase_order
    modify column status tinyint not null comment '1草稿 2待审批 3已审批 4待供应商确认 5供应商已确认 6供应商差异 7部分入库 8已完成 9已取消 10已关闭 11已驳回 12供应商已拒绝';
