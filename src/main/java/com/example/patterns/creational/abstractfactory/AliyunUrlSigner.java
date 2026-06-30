package com.example.patterns.creational.abstractfactory;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 阿里云 OSS URL 签名器。
 *
 * <p>抽象工厂模式中的「具体产品（ConcreteProduct）」之一，实现阿里云对象存储 OSS 的
 * 签名 URL 生成逻辑，与 {@link AliyunStorageClient} 同属 {@value #VENDOR_ALIYUN} 产品族，
 * 由 {@link AliyunStorageFactory} 创建并返回。作为 Spring 组件交由容器管理，其默认 bean 名
 * {@code aliyunUrlSigner} 供具体工厂按名精确注入。</p>
 *
 * <p>本实现以「模拟」方式体现厂商差异：采用阿里云 OSS 特有的
 * {@code Expires/OSSAccessKeyId/Signature} 查询参数风格生成签名 URL，不真正调用阿里云 SDK。</p>
 *
 * @since 1.0.0
 */
@Component
public class AliyunUrlSigner implements UrlSigner {

    /**
     * 阿里云厂商标识。
     */
    private static final String VENDOR_ALIYUN = "aliyun";

    /**
     * 模拟的默认 OSS 存储桶名称。
     */
    private static final String DEFAULT_BUCKET = "oss-demo-bucket";

    /**
     * 模拟的 OSS 访问域名（地域 Endpoint）。
     */
    private static final String OSS_ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";

    /**
     * 模拟的 OSS 访问密钥 ID（AccessKeyId）。
     */
    private static final String MOCK_ACCESS_KEY_ID = "DEMO_OSS_AK";

    /**
     * 为指定对象生成阿里云 OSS 风格的签名访问 URL。
     *
     * <p>校验对象键与有效期合法后，按 OSS 规则计算绝对过期时刻（Unix 秒）与模拟签名，
     * 拼接为形如 {@code https://bucket.endpoint/key?Expires=..&OSSAccessKeyId=..&Signature=..}
     * 的临时访问地址；对象键为空或有效期非正时抛出 {@link ServiceException}（对应需求 6.6、11.3）。</p>
     *
     * @param key           对象键（在存储桶内的唯一路径标识）
     * @param expireSeconds 签名有效期（单位：秒，须为正数）
     * @return 阿里云 OSS 风格的带签名与过期参数的临时访问 URL
     */
    @Override
    public String sign(String key, long expireSeconds) {
        if (!StringUtils.hasText(key)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "对象键不能为空");
        }
        if (expireSeconds <= 0L) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "签名有效期必须为正数");
        }
        long expiresAt = System.currentTimeMillis() / 1000L + expireSeconds;
        String signature = buildMockSignature(key, expiresAt);
        return "https://" + DEFAULT_BUCKET + "." + OSS_ENDPOINT + "/" + key
                + "?Expires=" + expiresAt
                + "&OSSAccessKeyId=" + MOCK_ACCESS_KEY_ID
                + "&Signature=" + signature;
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

    /**
     * 计算模拟的 OSS 签名值。
     *
     * <p>以厂商、对象键与过期时刻的散列十六进制串模拟真实签名，保证相同入参得到稳定签名，
     * 仅用于演示，不具备真实的加密安全性。</p>
     *
     * @param key       对象键
     * @param expiresAt 绝对过期时刻（Unix 秒）
     * @return 模拟签名值（十六进制字符串）
     */
    private String buildMockSignature(String key, long expiresAt) {
        return Integer.toHexString(Objects.hash(VENDOR_ALIYUN, key, expiresAt));
    }
}
