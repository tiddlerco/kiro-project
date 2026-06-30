package com.example.patterns.creational.abstractfactory.service;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.creational.abstractfactory.CloudStorageFactory;
import com.example.patterns.creational.abstractfactory.StorageClient;
import com.example.patterns.creational.abstractfactory.UrlSigner;
import com.example.patterns.creational.abstractfactory.domain.CloudUploadResult;
import com.example.patterns.creational.abstractfactory.domain.UploadResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 云存储应用服务。
 *
 * <p>抽象工厂模式的「客户端（Client）」角色：仅依赖抽象工厂 {@link CloudStorageFactory} 与抽象
 * 产品接口（{@link StorageClient}、{@link UrlSigner}），按厂商标识选取具体工厂后，由同一工厂
 * 产出整族相互配套的产品协作完成「上传对象 + 生成签名 URL」，而无需感知任何具体厂商实现
 * （依赖倒置，满足需求 2.5、10.1）。</p>
 *
 * <p>与 Spring 的结合：以 {@code @Resource} 注入容器中全部 {@link CloudStorageFactory} 实现，
 * 在 {@code @PostConstruct} 阶段基于各工厂自声明的 {@link CloudStorageFactory#vendor() 厂商标识}
 * 建立「厂商 → 工厂」路由表；新增云厂商只需新增其产品族与具体工厂即可自动接入，无需改动本类
 * 与调用方（开闭原则）。若出现重复厂商标识，则在启动阶段快速失败，将装配问题尽早暴露。</p>
 *
 * @since 1.0.0
 */
@Service
public class CloudStorageService {

    /**
     * 容器中全部云存储具体工厂。
     *
     * <p>以抽象工厂接口类型注入，Spring 会收集所有 {@link CloudStorageFactory} 实现的 bean，
     * 用于在初始化阶段构建厂商路由表。</p>
     */
    @Resource
    private List<CloudStorageFactory> storageFactories;

    /**
     * 厂商标识到具体工厂的路由表。
     *
     * <p>在依赖注入完成后一次性构建，键为厂商标识、值为对应具体工厂，供运行期按厂商 O(1) 选取。</p>
     */
    private final Map<String, CloudStorageFactory> factoryRouting = new HashMap<>();

    /**
     * 在依赖注入完成后构建「厂商 → 工厂」路由表。
     *
     * <p>遍历注入的全部具体工厂，以各自 {@link CloudStorageFactory#vendor()} 为键登记到路由表；
     * 若发现重复厂商标识（同一厂商被多个工厂声明），则抛出 {@link IllegalStateException} 触发
     * 启动期快速失败，避免运行期出现不确定的工厂选取结果。</p>
     */
    @PostConstruct
    public void initFactoryRouting() {
        for (CloudStorageFactory factory : storageFactories) {
            String vendor = factory.vendor();
            CloudStorageFactory previous = factoryRouting.putIfAbsent(vendor, factory);
            if (previous != null) {
                throw new IllegalStateException("云存储厂商标识重复：" + vendor
                        + "，冲突工厂=" + previous.getClass().getName()
                        + " 与 " + factory.getClass().getName());
            }
        }
    }

    /**
     * 上传对象并生成带时效的签名访问 URL。
     *
     * <p>按厂商标识从路由表选取具体工厂，再由该工厂产出的同族产品协作完成上传与签名：
     * 先以存储客户端上传对象得到 {@link UploadResult}，再以同族 URL 签名器为该对象生成签名 URL，
     * 最终组装为 {@link CloudUploadResult} 返回。由于两件产品均来自同一具体工厂，故必然归属同一
     * 云厂商、相互配套（满足需求 2.5）。当厂商标识在路由表中不存在时，抛出
     * {@link ServiceException}，由全局异常处理器统一转换为可观察的错误响应（满足需求 6.6、11.3）。</p>
     *
     * @param vendor        目标云厂商标识（如 {@code "aliyun"}、{@code "aws"}）
     * @param key           对象键（存储桶内唯一路径标识）
     * @param content       待上传的对象字节内容
     * @param expireSeconds 签名访问 URL 的有效期（单位：秒，须为正数）
     * @return 包含上传结果与签名访问 URL 的云存储上传结果
     */
    public CloudUploadResult upload(String vendor, String key, byte[] content, long expireSeconds) {
        CloudStorageFactory factory = factoryRouting.get(vendor);
        if (factory == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "未知的云厂商标识：" + vendor);
        }
        StorageClient storageClient = factory.createStorageClient();
        UrlSigner urlSigner = factory.createUrlSigner();
        UploadResult uploadResult = storageClient.upload(key, content);
        String signedUrl = urlSigner.sign(key, expireSeconds);
        return CloudUploadResult.of(factory.vendor(), uploadResult, signedUrl);
    }
}
