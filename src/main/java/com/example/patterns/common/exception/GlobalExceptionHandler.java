package com.example.patterns.common.exception;

import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>以 {@link RestControllerAdvice} 全局拦截各 Demo_Controller 抛出的异常，并统一转换为
 * {@link AjaxResult} 形式的可观察错误响应，使 Controller 内无需编写任何 try-catch
 * （落实编码规范 C6，满足需求 6.6、11.3 的“非静默失败”要求）。</p>
 *
 * <p><b>处理优先级（具体子类优先于父类）：</b></p>
 * <ol>
 *     <li>{@link IllegalStateTransitionException}：订单状态机非法流转，转为业务错误响应；</li>
 *     <li>{@link PermissionDeniedException}：敏感操作权限校验失败，转为 403 风格错误响应；</li>
 *     <li>{@link ServiceException}：通用业务异常，按其携带的业务错误码或默认系统码转换；</li>
 *     <li>{@link MethodArgumentNotValidException}：{@code @Validated} 参数校验失败，聚合字段级错误并转为 400 风格响应；</li>
 *     <li>{@link Exception}：兜底处理一切未预期异常，记录日志并返回固定提示，不泄露堆栈细节。</li>
 * </ol>
 *
 * <p><b>关于优先级的实现说明：</b>{@code IllegalStateTransitionException} 与
 * {@code PermissionDeniedException} 均继承自 {@code ServiceException}，Spring MVC 的异常处理器
 * 解析机制会按异常类型的“最近继承距离”择优匹配，因此为子类单独注册的处理器天然优先于父类
 * {@code ServiceException} 的处理器，无需额外的顺序声明。即便某子类异常未来未单独注册处理器，
 * 也会被父类分支兜底，避免逃逸为未预期异常。</p>
 *
 * <p><b>关于 HTTP 状态码：</b>本工程统一采用“HTTP 状态恒为 200、业务状态由响应体
 * {@code AjaxResult.code} 表达”的约定（与 {@link AjaxResult} 设计一致），故各处理方法不附加
 * {@code @ResponseStatus}，403/400/500 等语义通过 {@code AjaxResult} 的状态码字段体现。</p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 日志记录器：兜底异常记录完整堆栈，可预期的业务异常仅记录告警信息。 */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 多个字段级校验错误之间的拼接分隔符。 */
    private static final String FIELD_ERROR_DELIMITER = "; ";

    /** 单条字段级校验错误中“字段名”与“错误描述”之间的连接符。 */
    private static final String FIELD_MESSAGE_SEPARATOR = ": ";

    /** 参数校验失败但无法解析到任何具体字段错误时使用的兜底提示消息。 */
    private static final String DEFAULT_VALIDATION_MESSAGE = "参数校验失败";

    /** 兜底异常向调用方返回的固定提示消息（刻意不含异常细节，避免泄露堆栈）。 */
    private static final String UNEXPECTED_ERROR_MESSAGE = "系统异常";

    /**
     * 处理订单状态机非法流转异常。
     *
     * <p>当针对订单当前状态触发了一个未被定义为合法流转的状态变更动作时抛出；此时订单状态保持不变，
     * 本方法将其转换为指示“流转非法”的业务错误响应（满足需求 4.6）。</p>
     *
     * @param e 非法状态流转异常，其消息通常包含当前状态与被拒绝的流转动作以便定位
     * @return 携带非法流转原因的统一错误响应
     */
    @ExceptionHandler(IllegalStateTransitionException.class)
    public AjaxResult handleIllegalStateTransition(IllegalStateTransitionException e) {
        log.warn("非法状态流转：{}", e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理敏感操作权限不足异常。
     *
     * <p>当删除等敏感操作的权限校验未通过时抛出（由权限校验切面触发）；此时目标数据保持不变，
     * 本方法将其转换为 403 风格的权限错误响应（满足需求 9.10、9.11）。</p>
     *
     * @param e 权限不足异常，其消息通常包含被拒绝的操作或所需权限标识
     * @return 携带 403（访问受限）状态码与权限不足原因的统一错误响应
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public AjaxResult handlePermissionDenied(PermissionDeniedException e) {
        log.warn("权限校验未通过：{}", e.getMessage());
        return AjaxResult.error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /**
     * 处理通用业务异常。
     *
     * <p>承载未知支付渠道、无匹配可注入实现、限流超阈值、建造者必选部件缺失等各类可预期的业务失败。
     * 若异常自带业务错误码则原样采用，否则回退到默认系统错误码 {@link HttpStatus#ERROR}。</p>
     *
     * @param e 业务异常，可能携带自定义业务错误码（{@link ServiceException#getCode()} 可能为 {@code null}）
     * @return 统一业务错误响应：存在业务错误码时采用该码，否则采用默认系统错误码
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e) {
        log.warn("业务处理异常：{}", e.getMessage());
        Integer code = e.getCode();
        if (code != null) {
            return AjaxResult.error(code, e.getMessage());
        }
        return AjaxResult.error(e.getMessage());
    }

    /**
     * 处理参数校验异常。
     *
     * <p>由 Controller 上的 {@code @Validated} 配合 Request 对象的 JSR-303 校验注解触发，
     * 本方法聚合全部字段级校验错误并转换为 400 风格的错误响应（满足需求 9.11 删除标识缺失等校验场景）。</p>
     *
     * @param e 参数校验异常，其内部 {@code BindingResult} 承载各字段的校验错误
     * @return 携带 400（请求参数错误）状态码并聚合字段级错误信息的统一错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleValidation(MethodArgumentNotValidException e) {
        String message = buildValidationMessage(e);
        log.warn("参数校验失败：{}", message);
        return AjaxResult.error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 兜底处理一切未预期异常。
     *
     * <p>作为最后一道防线捕获未被前述处理器命中的异常，记录含完整堆栈的错误日志以便排查，
     * 但仅向调用方返回固定的“系统异常”提示，刻意不回传异常细节，避免泄露内部实现与堆栈信息。</p>
     *
     * @param e 未被前述处理器捕获的未预期异常
     * @return 携带固定提示消息且不泄露堆栈细节的统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("系统未预期异常", e);
        return AjaxResult.error(UNEXPECTED_ERROR_MESSAGE);
    }

    /**
     * 聚合参数校验异常中的全部字段级错误信息。
     *
     * <p>将每个字段错误拼接为“字段名: 错误描述”，并以分隔符连接为单条可读消息；
     * 当无法解析到任何具体字段错误时，回退为默认提示消息。</p>
     *
     * @param e 参数校验异常，其内部 {@code BindingResult} 承载各字段的校验错误
     * @return 形如「字段: 错误描述; 字段: 错误描述」的聚合错误消息；无字段错误时返回默认提示
     */
    private String buildValidationMessage(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        if (CollectionUtils.isEmpty(fieldErrors)) {
            return DEFAULT_VALIDATION_MESSAGE;
        }
        return fieldErrors.stream()
                .map(error -> error.getField() + FIELD_MESSAGE_SEPARATOR + error.getDefaultMessage())
                .collect(Collectors.joining(FIELD_ERROR_DELIMITER));
    }
}
