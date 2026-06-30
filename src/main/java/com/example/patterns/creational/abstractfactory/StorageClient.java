package com.example.patterns.creational.abstractfactory;

import com.example.patterns.creational.abstractfactory.domain.UploadResult;

/**
 * 对象存储客户端。
 *
 * <p>抽象工厂模式中的「抽象产品 A（AbstractProductA）」角色，抽象出各云厂商对象存储
 * 「上传对象」的统一能力。不同云厂商（阿里云 OSS、AWS S3 等）以独立实现类承担
 * 「具体产品（ConcreteProduct）」角色，由对应的
 * {@link CloudStorageFactory} 创建并返回，调用方仅依赖本接口而无需感知具体厂商实现
 * （依赖倒置，对应需求 8.4）。</p>
 *
 * <p>本接口与 {@link UrlSigner} 共同构成同一云厂商的「对象存储产品族」，二者须由同一个
 * 具体工厂创建，从而保证相互关联一致（同属一个 {@link #vendor() vendor} 标识）。</p>
 *
 * @since 1.0.0
 */
public interface StorageClient {

    /**
     * 上传对象到云端存储。
     *
     * @param key   对象键（在存储桶内的唯一路径标识）
     * @param bytes 待上传的对象字节内容
     * @return 上传结果，包含厂商、存储桶、对象键、字节数与最终存储路径等可观察信息
     */
    UploadResult upload(String key, byte[] bytes);

    /**
     * 返回当前存储客户端所属的云厂商标识。
     *
     * <p>该标识由实现类自身声明，用于体现并校验同一产品族内各产品归属一致，
     * 因而须在所有厂商之间保持唯一，不依赖 Spring 的 bean 名称。</p>
     *
     * @return 云厂商标识，如 {@code "aliyun"}、{@code "aws"}
     */
    String vendor();
}
