package com.example.patterns.creational.abstractfactory.controller;

import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.creational.abstractfactory.domain.CloudUploadRequest;
import com.example.patterns.creational.abstractfactory.service.CloudStorageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 抽象工厂模式演示控制器。
 *
 * <p>对外暴露多云对象存储的 HTTP 演示入口，演示抽象工厂模式「按厂商选取具体工厂后，所得整族产品
 * （存储客户端 + URL 签名器）始终相互配套、归属一致」的运行效果。控制器仅负责路由分发：接收并
 * 校验请求、委派 {@link CloudStorageService} 完成上传与签名并统一返回，不承载任何上传、签名或
 * 厂商选取的业务逻辑。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/abstractfactory")
public class CloudStorageController extends BaseController {

    /**
     * 云存储应用服务（抽象工厂模式的客户端角色）。
     */
    @Resource
    private CloudStorageService cloudStorageService;

    /**
     * 上传对象并生成带时效的签名访问 URL。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，将文本内容按 UTF-8 转为字节后委派服务层
     * 按厂商选取具体工厂完成上传与签名，并以统一响应结构返回结果。</p>
     *
     * @param request 云存储上传请求，含厂商标识、对象键、文本内容与签名有效期
     * @return 携带上传结果与签名访问 URL 的统一成功响应
     */
    @PostMapping("/upload")
    public AjaxResult upload(@Validated @RequestBody CloudUploadRequest request) {
        return success(cloudStorageService.upload(request.getVendor(), request.getKey(),
                request.contentBytes(), request.getExpireSeconds()));
    }
}
