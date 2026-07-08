package com.example.patterns.structural.adapter;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;
import com.example.patterns.structural.adapter.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 适配器核心行为单元测试（对应任务 10.3，验证需求 11.4）。
 */
class SmsAdapterServiceTest {

    private static final String VENDOR_ALIYUN = "aliyun";
    private static final String VENDOR_TENCENT = "tencent";
    private static final String ALIYUN_MESSAGE_ID_PREFIX = "ALI";
    private static final String TENCENT_MESSAGE_ID_PREFIX = "TX";

    private SmsService smsService;

    @BeforeEach
    void setUp() {
        AliyunSmsAdapter aliyunSmsAdapter = new AliyunSmsAdapter();
        ReflectionTestUtils.setField(aliyunSmsAdapter, "aliyunSmsClient", new AliyunSmsClient());

        TencentSmsAdapter tencentSmsAdapter = new TencentSmsAdapter();
        ReflectionTestUtils.setField(tencentSmsAdapter, "tencentSmsClient", new TencentSmsClient());

        smsService = new SmsService();
        ReflectionTestUtils.setField(smsService, "smsSenders",
                Arrays.asList(aliyunSmsAdapter, tencentSmsAdapter));
        smsService.initVendorRouter();
    }

    private SmsRequest newRequest(String vendor, String phone, String content) {
        SmsRequest request = new SmsRequest();
        request.setVendor(vendor);
        request.setPhone(phone);
        request.setContent(content);
        return request;
    }

    @Test
    @DisplayName("统一接口按 aliyun 正确委派至阿里云适配器并返回成功")
    void shouldDelegateToAliyunAdapterAndReturnSuccess() {
        SmsSendResult result = smsService.send(newRequest(VENDOR_ALIYUN, "13800000001", "阿里云短信内容"));
        assertThat(result.getVendor()).isEqualTo(VENDOR_ALIYUN);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isNotBlank();
        assertThat(result.getMessageId()).startsWith(ALIYUN_MESSAGE_ID_PREFIX);
    }

    @Test
    @DisplayName("统一接口按 tencent 正确委派至腾讯云适配器并返回成功")
    void shouldDelegateToTencentAdapterAndReturnSuccess() {
        SmsSendResult result = smsService.send(newRequest(VENDOR_TENCENT, "13900000002", "腾讯云短信内容"));
        assertThat(result.getVendor()).isEqualTo(VENDOR_TENCENT);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isNotBlank();
        assertThat(result.getMessageId()).startsWith(TENCENT_MESSAGE_ID_PREFIX);
    }

    @Test
    @DisplayName("接收手机号在统一结果中无损回传")
    void shouldPreservePhoneInResult() {
        String phone = "13712345678";
        SmsSendResult result = smsService.send(newRequest(VENDOR_ALIYUN, phone, "内容无损校验"));
        assertThat(result.getPhone()).isEqualTo(phone);
    }

    @Test
    @DisplayName("未知服务商调用发送应抛出 ServiceException")
    void shouldThrowServiceExceptionWhenVendorUnknown() {
        SmsRequest request = newRequest("unknown", "13600000003", "未知服务商内容");
        assertThatThrownBy(() -> smsService.send(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("unknown");
    }
}
