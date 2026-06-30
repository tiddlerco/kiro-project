package com.example.patterns.creational.abstractfactory;

/**
 * 对象访问 URL 签名器。
 *
 * <p>抽象工厂模式中的「抽象产品 B（AbstractProductB）」角色，抽象出各云厂商
 * 「为对象生成带时效签名访问 URL」的统一能力。不同云厂商（阿里云 OSS、AWS S3 等）
 * 以独立实现类承担「具体产品（ConcreteProduct）」角色，由对应的
 * {@link CloudStorageFactory} 创建并返回，调用方仅依赖本接口而无需感知具体厂商实现
 * （依赖倒置，对应需求 8.4）。</p>
 *
 * <p>本接口与 {@link StorageClient} 共同构成同一云厂商的「对象存储产品族」，二者须由同一个
 * 具体工厂创建，从而保证相互关联一致（同属一个 {@link #vendor() vendor} 标识）。</p>
 *
 * @since 1.0.0
 */
public interface UrlSigner {

    /**
     * 为指定对象生成带时效的签名访问 URL。
     *
     * <p>不同云厂商的签名 URL 形态各异（如阿里云 OSS 采用 {@code Expires/OSSAccessKeyId/Signature}
     * 参数，AWS S3 采用 {@code X-Amz-*} 系列参数），实现类据此体现厂商差异。</p>
     *
     * @param key           对象键（在存储桶内的唯一路径标识）
     * @param expireSeconds 签名有效期（单位：秒，须为正数），到期后该 URL 不再可用
     * @return 带签名与过期参数的临时访问 URL
     */
    String sign(String key, long expireSeconds);

    /**
     * 返回当前签名器所属的云厂商标识。
     *
     * <p>该标识由实现类自身声明，用于体现并校验同一产品族内各产品归属一致，
     * 因而须在所有厂商之间保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 云厂商标识，如 {@code "aliyun"}、{@code "aws"}
     */
    String vendor();
}
