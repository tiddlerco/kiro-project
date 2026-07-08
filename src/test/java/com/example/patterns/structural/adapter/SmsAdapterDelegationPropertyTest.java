package com.example.patterns.structural.adapter;

import com.example.patterns.structural.adapter.domain.SmsRequest;
import com.example.patterns.structural.adapter.domain.SmsSendResult;
import com.example.patterns.structural.adapter.service.SmsService;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.GenerationMode;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 适配器统一接口委派属性测试（对应任务 10.4，Property 7，验证需求 3.2）。
 */
class SmsAdapterDelegationPropertyTest {

    private SmsService newService() {
        AliyunSmsAdapter aliyunSmsAdapter = new AliyunSmsAdapter();
        ReflectionTestUtils.setField(aliyunSmsAdapter, "aliyunSmsClient", new AliyunSmsClient());

        TencentSmsAdapter tencentSmsAdapter = new TencentSmsAdapter();
        ReflectionTestUtils.setField(tencentSmsAdapter, "tencentSmsClient", new TencentSmsClient());

        SmsService smsService = new SmsService();
        ReflectionTestUtils.setField(smsService, "smsSenders",
                Arrays.asList(aliyunSmsAdapter, tencentSmsAdapter));
        smsService.initVendorRouter();
        return smsService;
    }

    @Provide
    Arbitrary<String> vendors() {
        return Arbitraries.of("aliyun", "tencent");
    }

    @Provide
    Arbitrary<String> phones() {
        return Arbitraries.strings().withCharRange('0', '9').ofLength(11);
    }

    @Provide
    Arbitrary<String> contents() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    // Feature: design-patterns-showcase, Property 7: 适配器统一接口委派
    @Property(tries = 100, generation = GenerationMode.RANDOMIZED)
    @DisplayName("统一接口应按服务商正确委派并内容无损")
    void shouldDelegateByVendorAndPreserveContent(@ForAll("vendors") String vendor,
                                                  @ForAll("phones") String phone,
                                                  @ForAll("contents") String content) {
        SmsService smsService = newService();

        SmsRequest request = new SmsRequest();
        request.setVendor(vendor);
        request.setPhone(phone);
        request.setContent(content);

        SmsSendResult result = smsService.send(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getVendor()).isEqualTo(vendor);
        assertThat(result.getPhone()).isEqualTo(phone);
        assertThat(result.getMessageId()).isNotBlank();
    }
}
