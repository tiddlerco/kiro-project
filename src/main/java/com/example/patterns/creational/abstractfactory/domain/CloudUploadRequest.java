package com.example.patterns.creational.abstractfactory.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;

/**
 * 云存储上传请求。
 *
 * <p>抽象工厂模式演示接口 {@code POST /pattern/abstractfactory/upload} 的入参对象，承载一次
 * 「上传对象并生成签名访问 URL」所需的全部输入：目标云厂商标识、对象键、待上传文本内容以及
 * 签名有效期。其上的校验注解供控制器以 {@code @Validated} 触发声明式参数校验，避免在控制器中
 * 手写 if 校验逻辑（与项目统一的请求对象校验规范一致）。</p>
 *
 * <p>本类为纯数据对象（DTO），不承载任何上传与签名业务逻辑；getter/setter 由 Lombok 的
 * {@link Data} 注解生成。为便于演示，待上传内容以文本形式传入，并由 {@link #contentBytes()}
 * 统一按 UTF-8 编码转换为字节数组后交由服务层处理，使控制器保持仅路由分发的职责。</p>
 *
 * @since 1.0.0
 */
@Data
public class CloudUploadRequest {

    /**
     * 目标云厂商标识。
     *
     * <p>服务层据此选取对应的具体工厂（如 {@code "aliyun"}、{@code "aws"}），必填。</p>
     */
    @NotBlank(message = "云厂商标识不能为空")
    private String vendor;

    /**
     * 对象键。
     *
     * <p>对象在存储桶内的唯一路径标识，必填。</p>
     */
    @NotBlank(message = "对象键不能为空")
    private String key;

    /**
     * 待上传的文本内容。
     *
     * <p>为便于演示以文本形式传入，由 {@link #contentBytes()} 按 UTF-8 编码转换为字节内容；
     * 为空时按空内容处理。</p>
     */
    private String content;

    /**
     * 签名访问 URL 的有效期（单位：秒）。
     *
     * <p>用于为上传后的对象生成带时效的签名访问 URL，业务上要求为正数。</p>
     */
    private long expireSeconds;

    /**
     * 将文本内容按 UTF-8 编码转换为字节数组。
     *
     * <p>统一在请求对象内完成文本到字节的转换，使控制器无需编写任何转换逻辑；
     * 当文本内容为空时返回空字节数组而非 {@code null}，便于服务层与存储客户端统一处理。</p>
     *
     * @return 文本内容对应的 UTF-8 字节数组，内容为空时返回空数组
     */
    public byte[] contentBytes() {
        if (content == null) {
            return new byte[0];
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
