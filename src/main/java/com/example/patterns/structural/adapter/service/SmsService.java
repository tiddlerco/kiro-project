package com.example.patterns.structural.adapter.service;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.adapter.SmsSender;
import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信发送服务。
 *
 * <p>适配器模式中的「调用方/客户端（Client）」角色，仅依赖统一目标接口 {@link SmsSender}，
 * 不感知任何具体服务商（阿里云、腾讯云等）的 SDK 差异。本服务在启动期基于容器中所有
 * {@link SmsSender} 实现各自声明的 {@link SmsSender#vendor()} 建立「服务商标识 → 适配器」
 * 路由表，运行期按请求中携带的服务商标识选取对应适配器完成发送。</p>
 *
 * <p>新增服务商时只需新增一个实现 {@link SmsSender} 的适配器 Bean，本服务无需任何改动即可
 * 自动纳入路由（开闭原则）。</p>
 *
 * @since 1.0.0
 */
@Service
public class SmsService {

    /**
     * 容器中全部短信适配器实现，由 Spring 按类型注入。
     *
     * <p>用于在启动期构建「服务商标识 → 适配器」路由表，是路由表的唯一数据来源。</p>
     */
    @Resource
    private List<SmsSender> smsSenders;

    /**
     * 服务商标识到对应适配器的路由表。
     *
     * <p>键为服务商标识（{@link SmsSender#vendor()}），值为对应适配器实例，
     * 于启动期一次性构建完成，运行期只读，避免每次发送都遍历适配器列表。</p>
     */
    private final Map<String, SmsSender> vendorRouter = new HashMap<>();

    /**
     * 在 Bean 初始化完成后构建「服务商标识 → 适配器」路由表。
     *
     * <p>遍历容器注入的全部适配器，以各自声明的服务商标识为键登记到路由表；
     * 若出现重复的服务商标识，说明存在配置冲突，于启动期直接抛出异常实现快速失败，
     * 避免运行期路由到非预期的适配器。</p>
     */
    @PostConstruct
    public void initVendorRouter() {
        for (SmsSender smsSender : smsSenders) {
            String vendor = smsSender.vendor();
            SmsSender existed = vendorRouter.put(vendor, smsSender);
            if (existed != null) {
                throw new IllegalStateException("存在重复的短信服务商标识：" + vendor
                        + "，冲突适配器：" + existed.getClass().getName()
                        + " 与 " + smsSender.getClass().getName());
            }
        }
    }

    /**
     * 发送短信。
     *
     * <p>按请求中携带的服务商标识从路由表选取对应适配器并委派其完成发送；
     * 若服务商标识未知（路由表中无匹配适配器），抛出业务异常以实现非静默失败，
     * 由全局异常处理器统一转换为可观察的错误响应。</p>
     *
     * @param req 统一短信发送请求，承载目标服务商、接收手机号、短信内容与签名等要素
     * @return 实际处理本次发送的适配器返回的统一短信发送结果
     */
    public SmsSendResult send(SmsRequest req) {
        SmsSender smsSender = vendorRouter.get(req.getVendor());
        if (smsSender == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "未知的短信服务商：" + req.getVendor());
        }
        return smsSender.send(req);
    }
}
