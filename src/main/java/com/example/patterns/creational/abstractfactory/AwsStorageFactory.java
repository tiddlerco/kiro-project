package com.example.patterns.creational.abstractfactory;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * AWS 存储产品族工厂。
 *
 * <p>抽象工厂模式中的「具体工厂（ConcreteFactory）」之一，只生产 AWS
 * （{@value #VENDOR_AWS}）产品族：{@link AwsStorageClient} 与 {@link AwsUrlSigner}。
 * 由其创建的两个产品相互配套、归属同一云厂商（对应需求 2.5）。</p>
 *
 * <p>与 Spring 的结合方式：本工厂不在内部以 {@code new} 创建产品，而是以接口类型声明字段、
 * 借助 {@code @Resource} 默认按字段名匹配 bean 的语义，精确注入本族两件由容器管理的具体产品
 * （字段名 {@code awsStorageClient}/{@code awsUrlSigner} 恰为对应组件的默认 bean 名）。
 * 这样既以抽象接口声明依赖（依赖倒置），又将对象创建交给容器（对应需求 10.1），
 * 新增厂商只需新增一个具体工厂与其产品族即可（开闭原则）。</p>
 *
 * @since 1.0.0
 */
@Component
public class AwsStorageFactory implements CloudStorageFactory {

    /**
     * AWS 厂商标识。
     */
    private static final String VENDOR_AWS = "aws";

    /**
     * 本族存储客户端产品。
     *
     * <p>以抽象接口 {@link StorageClient} 声明，由 {@code @Resource} 按字段名注入容器中默认 bean 名
     * 为 {@code awsStorageClient} 的 {@link AwsStorageClient} 实例。</p>
     */
    @Resource
    private StorageClient awsStorageClient;

    /**
     * 本族 URL 签名器产品。
     *
     * <p>以抽象接口 {@link UrlSigner} 声明，由 {@code @Resource} 按字段名注入容器中默认 bean 名
     * 为 {@code awsUrlSigner} 的 {@link AwsUrlSigner} 实例。</p>
     */
    @Resource
    private UrlSigner awsUrlSigner;

    /**
     * 在依赖注入完成后校验本产品族归属一致。
     *
     * <p>断言注入的存储客户端与 URL 签名器声明同一 {@code vendor}，且与本工厂标识一致；
     * 若装配错误导致跨厂商混入，则快速失败抛出 {@link IllegalStateException}，
     * 将问题暴露在应用启动阶段而非运行期。</p>
     */
    @PostConstruct
    public void verifyProductFamily() {
        if (!Objects.equals(VENDOR_AWS, awsStorageClient.vendor())
                || !Objects.equals(VENDOR_AWS, awsUrlSigner.vendor())) {
            throw new IllegalStateException("AWS 存储产品族归属不一致：client="
                    + awsStorageClient.vendor() + ", signer=" + awsUrlSigner.vendor());
        }
    }

    /**
     * 创建（获取）AWS 存储客户端产品。
     *
     * @return AWS 存储客户端
     */
    @Override
    public StorageClient createStorageClient() {
        return awsStorageClient;
    }

    /**
     * 创建（获取）AWS URL 签名器产品。
     *
     * @return AWS URL 签名器
     */
    @Override
    public UrlSigner createUrlSigner() {
        return awsUrlSigner;
    }

    /**
     * 返回本工厂所属的 AWS 厂商标识。
     *
     * @return 厂商标识 {@value #VENDOR_AWS}
     */
    @Override
    public String vendor() {
        return VENDOR_AWS;
    }
}
