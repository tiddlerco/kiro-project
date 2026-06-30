package com.example.patterns.creational.abstractfactory;

/**
 * 云存储产品族抽象工厂。
 *
 * <p>抽象工厂模式中的「抽象工厂（AbstractFactory）」角色，声明创建一整族相互关联的对象存储
 * 产品（{@link StorageClient 存储客户端} 与 {@link UrlSigner URL 签名器}）的接口，而无需指定
 * 它们的具体类。每个云厂商（阿里云 OSS、AWS S3 等）以独立实现类承担「具体工厂
 * （ConcreteFactory）」角色，只生产本厂商产品族，从而保证由同一工厂产出的产品相互配套、
 * 归属一致（对应需求 2.5）。</p>
 *
 * <p>调用方仅依赖本抽象工厂与抽象产品接口，按 {@link #vendor() 厂商标识} 选取具体工厂后即可
 * 获得整族产品；新增云厂商只需新增一个具体工厂与其产品族实现，无需改动既有代码（开闭原则）。</p>
 *
 * @since 1.0.0
 */
public interface CloudStorageFactory {

    /**
     * 创建（获取）本厂商产品族中的存储客户端产品。
     *
     * @return 与本工厂同属一个云厂商的存储客户端
     */
    StorageClient createStorageClient();

    /**
     * 创建（获取）本厂商产品族中的 URL 签名器产品。
     *
     * @return 与本工厂同属一个云厂商的 URL 签名器
     */
    UrlSigner createUrlSigner();

    /**
     * 返回本工厂所属的云厂商标识。
     *
     * <p>该标识与本工厂所产出产品族的 {@code vendor} 一致，可供上层按厂商标识路由选取工厂，
     * 因而须在所有厂商之间保持唯一。</p>
     *
     * @return 云厂商标识，如 {@code "aliyun"}、{@code "aws"}
     */
    String vendor();
}
