package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IamAdminMapper {
    @Select("select app_code appCode,app_name appName,home_url homeUrl,app_status status,version from iam_app where app_code=#{appCode}")
    AppRow findApp(@Param("appCode") String appCode);

    @Select("select app_code appCode,app_name appName,home_url homeUrl,app_status status,version from iam_app order by id desc")
    List<AppRow> listApps();

    @Insert("insert into iam_app(app_code,app_name,home_url,app_status,version,created_at,updated_at) values(#{appCode},#{appName},#{homeUrl},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertApp(AppRow row);

    @Update("update iam_app set app_name=#{appName},home_url=#{homeUrl},app_status=#{status},version=#{version},updated_at=now(3) where app_code=#{appCode}")
    void updateApp(AppRow row);

    @Select("select menu_code menuCode,app_code appCode,parent_code parentCode,menu_name menuName,route_path routePath,sort_no sortNo,menu_status status,version from iam_menu where menu_code=#{menuCode}")
    MenuRow findMenu(@Param("menuCode") String menuCode);

    @Select("select menu_code menuCode,app_code appCode,parent_code parentCode,menu_name menuName,route_path routePath,sort_no sortNo,menu_status status,version from iam_menu where app_code=#{appCode} order by sort_no,id")
    List<MenuRow> listMenus(@Param("appCode") String appCode);

    @Insert("insert into iam_menu(menu_code,app_code,parent_code,menu_name,route_path,sort_no,menu_status,version,created_at,updated_at) values(#{menuCode},#{appCode},#{parentCode},#{menuName},#{routePath},#{sortNo},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMenu(MenuRow row);

    @Update("update iam_menu set parent_code=#{parentCode},menu_name=#{menuName},route_path=#{routePath},sort_no=#{sortNo},menu_status=#{status},version=#{version},updated_at=now(3) where menu_code=#{menuCode}")
    void updateMenu(MenuRow row);

    @Select("select sso_code ssoCode,app_code appCode,redirect_url redirectUrl,secret_hash secretHash,sso_status status,version from iam_sso_client where sso_code=#{ssoCode}")
    SsoRow findSso(@Param("ssoCode") String ssoCode);

    @Select("select sso_code ssoCode,app_code appCode,redirect_url redirectUrl,secret_hash secretHash,sso_status status,version from iam_sso_client order by id desc")
    List<SsoRow> listSso();

    @Insert("insert into iam_sso_client(sso_code,app_code,redirect_url,secret_hash,sso_status,version,created_at,updated_at) values(#{ssoCode},#{appCode},#{redirectUrl},#{secretHash},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSso(SsoRow row);

    @Update("update iam_sso_client set redirect_url=#{redirectUrl},secret_hash=#{secretHash},sso_status=#{status},version=#{version},updated_at=now(3) where sso_code=#{ssoCode}")
    void updateSso(SsoRow row);

    @Insert("insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))")
    void insertOutbox(IamPermissionOpenApiMapper.OutboxEventRow row);

    @Insert("insert ignore into iam_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(3),now(3))")
    int claimEvent(EventInboxRow row);

    @Update("update iam_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now(3) where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,error_message errorMessage from iam_event_inbox order by updated_at desc")
    List<EventInboxRow> listInbox();

    record AppRow(Long id, String appCode, String appName, String homeUrl, int status, long version) {}
    record MenuRow(Long id, String menuCode, String appCode, String parentCode, String menuName, String routePath,
                   int sortNo, int status, long version) {}
    record SsoRow(Long id, String ssoCode, String appCode, String redirectUrl, String secretHash, int status,
                  long version) {}
    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status,
                         String errorMessage) {}
}
