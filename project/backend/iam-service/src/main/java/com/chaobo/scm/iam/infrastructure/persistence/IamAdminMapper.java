package com.chaobo.scm.iam.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IamAdminMapper。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Mapper
public interface IamAdminMapper {

    /**
     * 查询并返回 {@code findApp}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code AppRow}
     */
    @Select("select app_code appCode,app_name appName,home_url homeUrl,app_status status,version from iam_app where app_code=#{appCode}")
    AppRow findApp(@Param("appCode") String appCode);

    /**
     * 查询并返回 {@code listApps}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<AppRow>}
     */
    @Select("select app_code appCode,app_name appName,home_url homeUrl,app_status status,version from iam_app order by id desc")
    List<AppRow> listApps();

    /**
     * 处理当前类型职责中的操作 {@code insertApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AppRow}
     */
    @Insert("insert into iam_app(app_code,app_name,home_url,app_status,version,created_at,updated_at) values(#{appCode},#{appName},#{homeUrl},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertApp(AppRow row);

    /**
     * 执行命令 {@code updateApp}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code AppRow}
     */
    @Update("update iam_app set app_name=#{appName},home_url=#{homeUrl},app_status=#{status},version=#{version},updated_at=now(3) where app_code=#{appCode}")
    void updateApp(AppRow row);

    /**
     * 查询并返回 {@code findMenu}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param menuCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code MenuRow}
     */
    @Select("select menu_code menuCode,app_code appCode,parent_code parentCode,menu_name menuName,route_path routePath,sort_no sortNo,menu_status status,version from iam_menu where menu_code=#{menuCode}")
    MenuRow findMenu(@Param("menuCode") String menuCode);

    /**
     * 查询并返回 {@code listMenus}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code List<MenuRow>}
     */
    @Select("select menu_code menuCode,app_code appCode,parent_code parentCode,menu_name menuName,route_path routePath,sort_no sortNo,menu_status status,version from iam_menu where app_code=#{appCode} order by sort_no,id")
    List<MenuRow> listMenus(@Param("appCode") String appCode);

    /**
     * 处理当前类型职责中的操作 {@code insertMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MenuRow}
     */
    @Insert("insert into iam_menu(menu_code,app_code,parent_code,menu_name,route_path,sort_no,menu_status,version,created_at,updated_at) values(#{menuCode},#{appCode},#{parentCode},#{menuName},#{routePath},#{sortNo},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMenu(MenuRow row);

    /**
     * 执行命令 {@code updateMenu}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code MenuRow}
     */
    @Update("update iam_menu set parent_code=#{parentCode},menu_name=#{menuName},route_path=#{routePath},sort_no=#{sortNo},menu_status=#{status},version=#{version},updated_at=now(3) where menu_code=#{menuCode}")
    void updateMenu(MenuRow row);

    /**
     * 查询并返回 {@code findSso}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code SsoRow}
     */
    @Select("select sso_code ssoCode,app_code appCode,redirect_url redirectUrl,secret_hash secretHash,sso_status status,version from iam_sso_client where sso_code=#{ssoCode}")
    SsoRow findSso(@Param("ssoCode") String ssoCode);

    /**
     * 查询并返回 {@code listSso}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<SsoRow>}
     */
    @Select("select sso_code ssoCode,app_code appCode,redirect_url redirectUrl,secret_hash secretHash,sso_status status,version from iam_sso_client order by id desc")
    List<SsoRow> listSso();

    /**
     * 处理当前类型职责中的操作 {@code insertSso}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SsoRow}
     */
    @Insert("insert into iam_sso_client(sso_code,app_code,redirect_url,secret_hash,sso_status,version,created_at,updated_at) values(#{ssoCode},#{appCode},#{redirectUrl},#{secretHash},#{status},#{version},now(3),now(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSso(SsoRow row);

    /**
     * 执行命令 {@code updateSso}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code SsoRow}
     */
    @Update("update iam_sso_client set redirect_url=#{redirectUrl},secret_hash=#{secretHash},sso_status=#{status},version=#{version},updated_at=now(3) where sso_code=#{ssoCode}")
    void updateSso(SsoRow row);

    /**
     * 处理当前类型职责中的操作 {@code insertOutbox}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code IamPermissionOpenApiMapper.OutboxEventRow}
     */
    @Insert("insert into iam_outbox_event(event_id,event_type,business_no,payload,event_status,occurred_at,created_at) values(#{eventId},#{eventType},#{businessNo},#{payload},1,#{occurredAt},now(3))")
    void insertOutbox(IamPermissionOpenApiMapper.OutboxEventRow row);

    /**
     * 处理当前类型职责中的操作 {@code claimEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    @Insert("insert ignore into iam_event_inbox(event_id,event_type,business_no,payload,event_status,error_message,created_at,updated_at) values(#{eventId},#{eventType},#{businessNo},#{payload},#{status},#{errorMessage},now(3),now(3))")
    int claimEvent(EventInboxRow row);

    /**
     * 执行命令 {@code updateEvent}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param row 业务处理参数或成员，类型为 {@code EventInboxRow}
     */
    @Update("update iam_event_inbox set event_status=#{status},error_message=#{errorMessage},updated_at=now(3) where event_id=#{eventId}")
    void updateEvent(EventInboxRow row);

    /**
     * 查询并返回 {@code listInbox}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code List<EventInboxRow>}
     */
    @Select("select event_id eventId,event_type eventType,business_no businessNo,payload,event_status status,error_message errorMessage from iam_event_inbox order by updated_at desc")
    List<EventInboxRow> listInbox();

    /**
     * AppRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record AppRow(Long id, String appCode, String appName, String homeUrl, int status, long version) {
    }

    /**
     * MenuRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record MenuRow(Long id, String menuCode, String appCode, String parentCode, String menuName, String routePath, int sortNo, int status, long version) {
    }

    /**
     * SsoRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record SsoRow(Long id, String ssoCode, String appCode, String redirectUrl, String secretHash, int status, long version) {
    }

    /**
     * EventInboxRow。
     *
     * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record EventInboxRow(String eventId, String eventType, String businessNo, String payload, int status, String errorMessage) {
    }
}
