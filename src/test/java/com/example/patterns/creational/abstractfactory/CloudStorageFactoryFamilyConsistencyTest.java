package com.example.patterns.creational.abstractfactory;

import com.example.patterns.creational.abstractfactory.domain.UploadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 抽象工厂同族一致性示例测试（对应任务 5.3，验证需求 2.5、11.4）。
 *
 * <p>为每个云厂商具体工厂各举一例，断言由「同一工厂」产出的产品族
 * （{@link StorageClient} 与 {@link UrlSigner}）归属同一 {@code vendor}、相互配套，
 * 且与工厂自身的 {@link CloudStorageFactory#vendor()} 一致；同时验证上传结果
 * （{@link UploadResult#getVendor()}）与签名 URL 也均归属该厂商，从而直观展示
 * 「同一工厂产出的产品同族一致、不会跨厂商错配」这一抽象工厂核心价值。</p>
 *
 * <p>装配方式：手动 {@code new} 各具体产品并以 {@link ReflectionTestUtils} 注入具体工厂，
 * 再显式触发工厂的 {@code @PostConstruct} 校验方法，无需启动 Spring 容器，测试轻量而可靠。</p>
 *
 * @since 1.0.0
 */
class CloudStorageFactoryFamilyConsistencyTest {

    /**
     * 阿里云厂商标识。
     */
    private static final String VENDOR_ALIYUN = "aliyun";

    /**
     * AWS 厂商标识。
     */
    private static final String VENDOR_AWS = "aws";

    /**
     * 演示用对象键。
     */
    private static final String OBJECT_KEY = "demo/2024/report.txt";

    /**
     * 演示用签名有效期（秒）。
     */
    private static final long EXPIRE_SECONDS = 600L;

    /**
     * 构建一个完成产品族装配并通过归属校验的阿里云工厂。
     *
     * <p>以真实具体产品 {@link AliyunStorageClient}、{@link AliyunUrlSigner} 注入工厂字段，
     * 并触发其 {@code @PostConstruct} 归属校验方法，确保工厂处于可用且一致的状态。</p>
     *
     * @return 装配完成且校验通过的阿里云存储工厂
     */
    private AliyunStorageFactory newAliyunFactory() {
        AliyunStorageFactory factory = new AliyunStorageFactory();
        ReflectionTestUtils.setField(factory, "aliyunStorageClient", new AliyunStorageClient());
        ReflectionTestUtils.setField(factory, "aliyunUrlSigner", new AliyunUrlSigner());
        factory.verifyProductFamily();
        return factory;
    }

    /**
     * 构建一个完成产品族装配并通过归属校验的 AWS 工厂。
     *
     * <p>以真实具体产品 {@link AwsStorageClient}、{@link AwsUrlSigner} 注入工厂字段，
     * 并触发其 {@code @PostConstruct} 归属校验方法，确保工厂处于可用且一致的状态。</p>
     *
     * @return 装配完成且校验通过的 AWS 存储工厂
     */
    private AwsStorageFactory newAwsFactory() {
        AwsStorageFactory factory = new AwsStorageFactory();
        ReflectionTestUtils.setField(factory, "awsStorageClient", new AwsStorageClient());
        ReflectionTestUtils.setField(factory, "awsUrlSigner", new AwsUrlSigner());
        factory.verifyProductFamily();
        return factory;
    }

    /**
     * 验证阿里云工厂产出的整族产品均归属 {@code aliyun} 且相互配套。
     *
     * <p>断言工厂 {@code vendor()}、存储客户端 {@code vendor()}、URL 签名器 {@code vendor()}
     * 三者一致为 {@code aliyun}，上传结果 {@code vendor} 亦为 {@code aliyun}，签名 URL 归属
     * 阿里云 OSS 域名，展示同族一致、无跨厂商错配。</p>
     */
    @Test
    @DisplayName("阿里云工厂产出的产品族均归属 aliyun 且相互配套")
    void shouldProduceConsistentAliyunProductFamily() {
        AliyunStorageFactory factory = newAliyunFactory();

        StorageClient storageClient = factory.createStorageClient();
        UrlSigner urlSigner = factory.createUrlSigner();

        assertThat(factory.vendor()).isEqualTo(VENDOR_ALIYUN);
        assertThat(storageClient.vendor()).isEqualTo(VENDOR_ALIYUN);
        assertThat(urlSigner.vendor()).isEqualTo(VENDOR_ALIYUN);
        assertThat(storageClient.vendor()).isEqualTo(factory.vendor());
        assertThat(urlSigner.vendor()).isEqualTo(factory.vendor());

        UploadResult uploadResult = storageClient.upload(OBJECT_KEY,
                "阿里云上传内容".getBytes(StandardCharsets.UTF_8));
        assertThat(uploadResult.isSuccess()).isTrue();
        assertThat(uploadResult.getVendor()).isEqualTo(VENDOR_ALIYUN);
        assertThat(uploadResult.getStoragePath()).startsWith("oss://");

        String signedUrl = urlSigner.sign(OBJECT_KEY, EXPIRE_SECONDS);
        assertThat(signedUrl).contains("aliyuncs.com");
    }

    /**
     * 验证 AWS 工厂产出的整族产品均归属 {@code aws} 且相互配套。
     *
     * <p>断言工厂 {@code vendor()}、存储客户端 {@code vendor()}、URL 签名器 {@code vendor()}
     * 三者一致为 {@code aws}，上传结果 {@code vendor} 亦为 {@code aws}，签名 URL 归属
     * AWS S3 域名，展示同族一致、无跨厂商错配。</p>
     */
    @Test
    @DisplayName("AWS 工厂产出的产品族均归属 aws 且相互配套")
    void shouldProduceConsistentAwsProductFamily() {
        AwsStorageFactory factory = newAwsFactory();

        StorageClient storageClient = factory.createStorageClient();
        UrlSigner urlSigner = factory.createUrlSigner();

        assertThat(factory.vendor()).isEqualTo(VENDOR_AWS);
        assertThat(storageClient.vendor()).isEqualTo(VENDOR_AWS);
        assertThat(urlSigner.vendor()).isEqualTo(VENDOR_AWS);
        assertThat(storageClient.vendor()).isEqualTo(factory.vendor());
        assertThat(urlSigner.vendor()).isEqualTo(factory.vendor());

        UploadResult uploadResult = storageClient.upload(OBJECT_KEY,
                "AWS 上传内容".getBytes(StandardCharsets.UTF_8));
        assertThat(uploadResult.isSuccess()).isTrue();
        assertThat(uploadResult.getVendor()).isEqualTo(VENDOR_AWS);
        assertThat(uploadResult.getStoragePath()).startsWith("s3://");

        String signedUrl = urlSigner.sign(OBJECT_KEY, EXPIRE_SECONDS);
        assertThat(signedUrl).contains("amazonaws.com");
    }

    /**
     * 验证跨厂商错配会被工厂归属校验拒绝，佐证「产品族不会跨厂商混用」。
     *
     * <p>故意向阿里云工厂注入 AWS 的 URL 签名器，触发 {@code @PostConstruct} 校验时应抛出
     * {@link IllegalStateException}，证明同族一致性在装配阶段即被强制保障。</p>
     */
    @Test
    @DisplayName("阿里云工厂混入 AWS 签名器时归属校验抛出异常")
    void shouldRejectCrossVendorMisconfiguration() {
        AliyunStorageFactory factory = new AliyunStorageFactory();
        ReflectionTestUtils.setField(factory, "aliyunStorageClient", new AliyunStorageClient());
        ReflectionTestUtils.setField(factory, "aliyunUrlSigner", new AwsUrlSigner());

        assertThatThrownBy(factory::verifyProductFamily)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(VENDOR_AWS);
    }
}
