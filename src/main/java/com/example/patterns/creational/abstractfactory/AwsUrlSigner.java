package com.example.patterns.creational.abstractfactory;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * AWS S3 URL 签名器。
 *
 * <p>抽象工厂模式中的「具体产品（ConcreteProduct）」之一，实现 AWS 对象存储 S3 的
 * 预签名 URL 生成逻辑，与 {@link AwsStorageClient} 同属 {@value #VENDOR_AWS} 产品族，
 * 由 {@link AwsStorageFactory} 创建并返回。作为 Spring 组件交由容器管理，其默认 bean 名
 * {@code awsUrlSigner} 供具体工厂按名精确注入。</p>
 *
 * <p>本实现以「模拟」方式体现厂商差异：采用 AWS S3 预签名特有的 {@code X-Amz-*} 系列查询参数
 * 风格生成签名 URL，与阿里云 OSS 的参数风格形成对照，不真正调用 AWS SDK。</p>
 *
 * @since 1.0.0
 */
@Component
public class AwsUrlSigner implements UrlSigner {

    /**
     * AWS 厂商标识。
     */
    private static final String VENDOR_AWS = "aws";

    /**
     * 模拟的默认 S3 存储桶名称。
     */
    private static final String DEFAULT_BUCKET = "s3-demo-bucket";

    /**
     * 模拟的 S3 访问域名（Endpoint）。
     */
    private static final String S3_ENDPOINT = "s3.amazonaws.com";

    /**
     * AWS 预签名所用签名算法标识。
     */
    private static final String SIGN_ALGORITHM = "AWS4-HMAC-SHA256";

    /**
     * 为指定对象生成 AWS S3 风格的预签名访问 URL。
     *
     * <p>校验对象键与有效期合法后，按 S3 预签名规则计算模拟签名，拼接为形如
     * {@code https://bucket.endpoint/key?X-Amz-Algorithm=..&X-Amz-Expires=..&X-Amz-Signature=..}
     * 的临时访问地址；对象键为空或有效期非正时抛出 {@link ServiceException}（对应需求 6.6、11.3）。</p>
     *
     * @param key           对象键（在存储桶内的唯一路径标识）
     * @param expireSeconds 签名有效期（单位：秒，须为正数）
     * @return AWS S3 风格的带签名与过期参数的临时访问 URL
     */
    @Override
    public String sign(String key, long expireSeconds) {
        if (!StringUtils.hasText(key)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "对象键不能为空");
        }
        if (expireSeconds <= 0L) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "签名有效期必须为正数");
        }
        String signature = buildMockSignature(key, expireSeconds);
        return "https://" + DEFAULT_BUCKET + "." + S3_ENDPOINT + "/" + key
                + "?X-Amz-Algorithm=" + SIGN_ALGORITHM
                + "&X-Amz-Expires=" + expireSeconds
                + "&X-Amz-Signature=" + signature;
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

    /**
     * 计算模拟的 S3 预签名值。
     *
     * <p>以厂商、对象键与有效期的散列十六进制串模拟真实签名，保证相同入参得到稳定签名，
     * 仅用于演示，不具备真实的加密安全性。</p>
     *
     * @param key           对象键
     * @param expireSeconds 签名有效期（秒）
     * @return 模拟签名值（十六进制字符串）
     */
    private String buildMockSignature(String key, long expireSeconds) {
        return Integer.toHexString(Objects.hash(VENDOR_AWS, key, expireSeconds));
    }
}
