package com.example.patterns.common.exception;

/**
 * 非法状态流转异常。
 *
 * <p>用于订单状态机（State 模式示例）在接收到一个未被定义为合法流转的状态变更请求时抛出。
 * 抛出后状态机将保持订单处于变更前的状态不变，并由全局异常处理器
 * {@code GlobalExceptionHandler} 转换为 {@code AjaxResult.error(...)} 错误响应，
 * 向调用方返回指示该流转非法的可观察结果（满足需求 4.6、11.3）。</p>
 *
 * <p>继承设计说明：本异常选择继承 {@link ServiceException} 而非直接继承
 * {@link RuntimeException}，原因在于“非法状态流转”在语义上是“业务异常”的一种特化
 * （is-a 关系成立，满足里氏替换原则）。如此设计可复用 {@link ServiceException}
 * 统一的错误码与消息模型，并使本异常在全局异常处理器未单独注册其处理器时，
 * 仍能落入业务异常分支被兜底处理，避免逃逸为未预期异常。</p>
 *
 * @since 1.0.0
 */
public class IllegalStateTransitionException extends ServiceException {

    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;

    /**
     * 使用错误消息构造非法状态流转异常。
     *
     * @param message 错误消息，建议包含当前状态与被拒绝的流转动作以便定位
     */
    public IllegalStateTransitionException(String message) {
        super(message);
    }
}
