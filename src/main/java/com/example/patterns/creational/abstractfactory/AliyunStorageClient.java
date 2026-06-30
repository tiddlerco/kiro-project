package com.example.patterns.creational.abstractfactory;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.abstractfactory.domain.UploadResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 阿里云 OSS 存储客户端。
 *
 * <p>抽象工厂模式中的「具体产品（ConcreteProduct）」之一，实现阿里云对象存储 OSS 的上传逻辑，
 * 与 {@link AliyunUrlSigner} 同属 {@value #VENDOR_ALIYUN} 产品族，由
 * {@link AliyunStorageFactory} 创建并返回。作为 Spring 组件交由容器管理，其默认 bean 名
 * {@code aliyunStorageClient} 供具体工厂按名精确注入。</p>
 *
 * <p>本实现以「模拟」方式体现厂商差异（按 OSS 的 {@code oss://bucket/key} 协议生成存储路径），
 * 不真正调用阿里云 SDK。</p>
 *
 * @since 1.0.0
 */
@Component
public class AliyunStorageClient implements StorageClient {

    /**
     * 阿里云厂商标识。
     */
    private static final String VENDOR_ALIYUN = "aliyun";

    /**
     * 模拟的默认 OSS 存储桶名称。
     */
    private static final String DEFAULT_BUCKET = "oss-demo-bucket";

    /**
     * 阿里云 OSS 存储路径协议前缀。
     */
    private static final String OSS_SCHEME = "oss://";

    /**
     * 上传对象到阿里云 OSS。
     *
     * <p>校验对象键与内容非空后，按 OSS 协议生成形如 {@code oss://bucket/key} 的存储路径，
     * 并返回填充完成的成功上传结果；对象键为空或内容为空时抛出 {@link ServiceException}，
     * 由全局异常处理器统一转换为可观察的错误响应（对应需求 6.6、11.3）。</p>
     *
     * @param key   对象键（在存储桶内的唯一路径标识）
     * @param bytes 待上传的对象字节内容
     * @return 表示阿里云 OSS 上传成功的上传结果
     */
    @Override
    public UploadResult upload(String key, byte[] bytes) {
        if (!StringUtils.hasText(key)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "对象键不能为空");
        }
        if (bytes == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "上传内容不能为空");
        }
        String storagePath = OSS_SCHEME + DEFAULT_BUCKET + "/" + key;
        return UploadResult.success(VENDOR_ALIYUN, DEFAULT_BUCKET, key, bytes.length, storagePath, "阿里云 OSS 上传成功");
    }

    /**
     * 返回阿里云厂商标识。
     *
     * @return 厂商标识 {@value #VENDOR_ALIYUN}
     */
    @Override
    public String vendor() {
        return VENDOR_ALIYUN;
    }
}
