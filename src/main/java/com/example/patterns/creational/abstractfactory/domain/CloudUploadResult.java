package com.example.patterns.creational.abstractfactory.domain;

import lombok.Data;

/**
 * 云存储上传结果（含签名访问 URL）。
 *
 * <p>承载抽象工厂演示接口 {@code POST /pattern/abstractfactory/upload} 的整体处理结果，
 * 由「同一具体工厂产出的同族产品协作」得到：{@code uploadResult} 为存储客户端
 * （{@link com.example.patterns.creational.abstractfactory.StorageClient}）上传对象后返回的
 * 可观察结果，{@code signedUrl} 为 URL 签名器
 * （{@link com.example.patterns.creational.abstractfactory.UrlSigner}）为该对象生成的带时效
 * 签名访问地址。二者由同一云厂商产品族协作产生，从而向调用方直观体现「按厂商选取工厂后，
 * 所得产品始终相互配套、归属一致」（满足需求 2.5、11.2）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载业务逻辑；getter/setter 由 Lombok 的 {@link Data}
 * 注解生成。同时提供 {@link #of} 静态工厂方法，便于服务层以表达力更强的方式组装结果。</p>
 *
 * @since 1.0.0
 */
@Data
public class CloudUploadResult {

    /**
     * 云厂商标识。
     *
     * <p>实际承担本次上传与签名的云厂商标识（如 {@code "aliyun"}、{@code "aws"}），
     * 取自所选具体工厂的厂商标识。</p>
     */
    private String vendor;

    /**
     * 对象上传结果。
     *
     * <p>由所选厂商的存储客户端上传对象后返回，包含存储桶、对象键、字节数与最终存储路径等
     * 可观察信息。</p>
     */
    private UploadResult uploadResult;

    /**
     * 带时效的签名访问 URL。
     *
     * <p>由与上传所用客户端同族的 URL 签名器为该对象生成，体现不同厂商各异的签名 URL 形态。</p>
     */
    private String signedUrl;

    /**
     * 组装一个云存储上传结果。
     *
     * @param vendor       实际承担本次上传与签名的云厂商标识
     * @param uploadResult 对象上传结果
     * @param signedUrl    带时效的签名访问 URL
     * @return 各字段填充完成的云存储上传结果
     */
    public static CloudUploadResult of(String vendor, UploadResult uploadResult, String signedUrl) {
        CloudUploadResult result = new CloudUploadResult();
        result.setVendor(vendor);
        result.setUploadResult(uploadResult);
        result.setSignedUrl(signedUrl);
        return result;
    }
}
