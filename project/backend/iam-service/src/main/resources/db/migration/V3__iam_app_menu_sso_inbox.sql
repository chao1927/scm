create table iam_app (
    id bigint primary key auto_increment,
    app_code varchar(64) not null,
    app_name varchar(128) not null,
    home_url varchar(512),
    app_status tinyint not null comment '1启用 2停用',
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_app_code(app_code)
) comment 'IAM应用';

create table iam_menu (
    id bigint primary key auto_increment,
    menu_code varchar(128) not null,
    app_code varchar(64) not null,
    parent_code varchar(128),
    menu_name varchar(128) not null,
    route_path varchar(512),
    sort_no int not null,
    menu_status tinyint not null comment '1启用 2停用',
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_menu_code(menu_code),
    key idx_iam_menu_app(app_code, sort_no)
) comment 'IAM菜单';

create table iam_sso_client (
    id bigint primary key auto_increment,
    sso_code varchar(64) not null,
    app_code varchar(64) not null,
    redirect_url varchar(512) not null,
    secret_hash varchar(256) not null,
    sso_status tinyint not null comment '1启用 2停用',
    version bigint not null,
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    unique key uk_iam_sso_code(sso_code),
    key idx_iam_sso_app(app_code)
) comment 'IAM SSO客户端';

create table iam_event_inbox (
    event_id varchar(128) primary key,
    event_type varchar(128) not null,
    business_no varchar(128) not null,
    payload text not null,
    event_status tinyint not null comment '1处理中 2成功 3失败 4忽略',
    error_message varchar(512),
    created_at datetime(3) not null,
    updated_at datetime(3) not null,
    key idx_iam_inbox_business(business_no)
) comment 'IAM入站事件';
