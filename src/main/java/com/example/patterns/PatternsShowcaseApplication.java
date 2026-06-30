package com.example.patterns;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 设计模式实战示例工程启动类。
 *
 * <p>作为整个 Spring Boot 应用的唯一入口，负责开启自动配置、组件扫描并启动应用上下文。
 * 借助 {@link SpringBootApplication} 以当前类所在包 {@code com.example.patterns} 为根进行组件扫描，
 * 使创建型 / 结构型 / 行为型三大类别子包下的各模式 Bean 均被纳入 Spring 容器管理。</p>
 *
 * <p>通过 {@link MapperScan} 扫描位于任意类别子包下、以 {@code mapper} 命名的包内的
 * MyBatis Mapper 接口（如状态模式的订单 Mapper、命令模式的商品与命令历史 Mapper、
 * 享元模式的风控规则 Mapper），将其注册为可注入的代理 Bean；其 SQL 语句统一外置于
 * mapper 目录下的 XML 映射文件（见 application.yml 中的 mapper-locations 配置），
 * 保持 SQL 与 Java 代码分离。</p>
 *
 * <p>应用启动时由 H2 内存库自动执行 {@code schema.sql} 建表、{@code data.sql} 写入演示数据，
 * 随后进入可接收 HTTP 请求的运行状态。</p>
 *
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.example.patterns.**.mapper")
public class PatternsShowcaseApplication {

    /**
     * 应用程序主入口，引导启动 Spring Boot 应用上下文。
     *
     * <p>执行流程：加载自动配置 → 创建并刷新应用上下文 → 初始化 H2 数据源并执行
     * schema.sql / data.sql 完成建表与演示数据写入 → 启动内嵌 Web 容器，
     * 使应用进入可接收请求的运行状态。</p>
     *
     * @param args 命令行启动参数，由 JVM 传入，可用于在启动时覆盖默认配置项（如 {@code --server.port=9090}）
     */
    public static void main(String[] args) {
        SpringApplication.run(PatternsShowcaseApplication.class, args);
    }
}
