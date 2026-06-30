package com.example.patterns.creational.abstractfactory.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对象上传结果。
 *
 * <p>承载某个云厂商存储客户端执行对象上传后的可观察结果，包括是否成功、所属云厂商、
 * 存储桶、对象键、上传字节数、最终存储路径、结果描述与完成时间。作为
 * {@link com.example.patterns.creational.abstractfactory.StorageClient#upload}
 * 的返回值，供调用方据此判断上传是否成功并展示结果（满足需求 11.2 可观察结果）。</p>
 *
 * <p>本类为纯数据对象（DTO），getter/setter 由 Lombok 的 {@link Data} 注解生成；
 * 同时提供 {@link #success} 静态工厂方法，便于各存储客户端以表达力更强的方式
 * 构造成功结果，避免逐字段手工装配。其中 {@code vendor} 字段标识产生本结果的
 * 云厂商，可用于校验「上传所用客户端」与「同族其它产品」归属一致。</p>
 *
 * @since 1.0.0
 */
@Data
public class UploadResult {

    /**
     * 是否上传成功。
     */
    private boolean success;

    /**
     * 云厂商标识。
     *
     * <p>取自实际执行本次上传的存储客户端所声明的厂商标识，如 {@code "aliyun"}、{@code "aws"}，
     * 用于体现并校验同一产品族内各产品归属一致。</p>
     */
    private String vendor;

    /**
     * 存储桶名称。
     *
     * <p>对象最终所归属的存储桶（Bucket），不同云厂商的桶命名与归属各不相同。</p>
     */
    private String bucket;

    /**
     * 对象键。
     *
     * <p>对象在存储桶内的唯一路径标识。</p>
     */
    private String key;

    /**
     * 上传字节数（单位：字节）。
     */
    private long size;

    /**
     * 对象最终存储路径。
     *
     * <p>由具体云厂商按其协议规则生成的可定位地址，如阿里云 {@code oss://bucket/key}、
     * AWS {@code s3://bucket/key}，体现不同厂商的存储路径差异。</p>
     */
    private String storagePath;

    /**
     * 结果描述信息。
     */
    private String message;

    /**
     * 上传完成时间。
     */
    private LocalDateTime uploadTime;

    /**
     * 构建一个表示「上传成功」的结果，并自动填充完成时间为当前时刻。
     *
     * @param vendor      实际执行上传的云厂商标识
     * @param bucket      对象所归属的存储桶名称
     * @param key         对象键（存储桶内唯一路径标识）
     * @param size        上传字节数
     * @param storagePath 对象最终存储路径
     * @param message     结果描述信息
     * @return 表示上传成功且各字段填充完成的上传结果
     */
    public static UploadResult success(String vendor, String bucket, String key, long size,
                                       String storagePath, String message) {
        UploadResult result = new UploadResult();
        result.setSuccess(true);
        result.setVendor(vendor);
        result.setBucket(bucket);
        result.setKey(key);
        result.setSize(size);
        result.setStoragePath(storagePath);
        result.setMessage(message);
        result.setUploadTime(LocalDateTime.now());
        return result;
    }
}
