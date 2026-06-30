package com.example.patterns.creational.abstractfactory;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.abstractfactory.domain.UploadResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AWS S3 存储客户端。
 *
 * <p>抽象工厂模式中的「具体产品（ConcreteProduct）」之一，实现 AWS 对象存储 S3 的上传逻辑，
 * 与 {@link AwsUrlSigner} 同属 {@value #VENDOR_AWS} 产品族，由
 * {@link AwsStorageFactory} 创建并返回。作为 Spring 组件交由容器管理，其默认 bean 名
 * {@code awsStorageClient} 供具体工厂按名精确注入。</p>
 *
 * <p>本实现以「模拟」方式体现厂商差异（按 S3 的 {@code s3://bucket/key} 协议生成存储路径），
 * 不真正调用 AWS SDK。</p>
 *
 * @since 1.0.0
 */
@Component
public class AwsStorageClient implements StorageClient {

    /**
     * AWS 厂商标识。
     */
    private static final String VENDOR_AWS = "aws";

    /**
     * 模拟的默认 S3 存储桶名称。
     */
    private static final String DEFAULT_BUCKET = "s3-demo-bucket";

    /**
     * AWS S3 存储路径协议前缀。
     */
    private static final String S3_SCHEME = "s3://";

    /**
     * 上传对象到 AWS S3。
     *
     * <p>校验对象键与内容非空后，按 S3 协议生成形如 {@code s3://bucket/key} 的存储路径，
     * 并返回填充完成的成功上传结果；对象键为空或内容为空时抛出 {@link ServiceException}，
     * 由全局异常处理器统一转换为可观察的错误响应（对应需求 6.6、11.3）。</p>
     *
     * @param key   对象键（在存储桶内的唯一路径标识）
     * @param bytes 待上传的对象字节内容
     * @return 表示 AWS S3 上传成功的上传结果
     */
    @Override
    public UploadResult upload(String key, byte[] bytes) {
        if (!StringUtils.hasText(key)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "对象键不能为空");
        }
        if (bytes == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "上传内容不能为空");
        }
        String storagePath = S3_SCHEME + DEFAULT_BUCKET + "/" + key;
        return UploadResult.success(VENDOR_AWS, DEFAULT_BUCKET, key, bytes.length, storagePath, "AWS S3 上传成功");
    }

    /**
     * 返回 AWS 厂商标识。
     *
     * @return 厂商标识 {@value #VENDOR_AWS}
     */
    @Override
    public String vendor() {
        return VENDOR_AWS;
    }
}
