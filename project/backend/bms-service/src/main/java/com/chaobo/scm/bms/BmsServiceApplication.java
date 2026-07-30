package com.chaobo.scm.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BmsServiceApplication。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@SpringBootApplication
public class BmsServiceApplication {

    /**
     * 处理当前类型职责中的操作 {@code main}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param args 业务处理参数或成员，类型为 {@code String[]}
     */
    public static void main(String[] args) {
        SpringApplication.run(BmsServiceApplication.class, args);
    }
}
