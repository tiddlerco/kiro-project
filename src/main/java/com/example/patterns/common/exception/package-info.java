/**
 * 统一异常体系包（Exception）。
 *
 * <p>本包定义示例工程的业务异常类型，配合全局异常处理器
 * {@code GlobalExceptionHandler} 将异常统一转换为 {@code AjaxResult.error(...)}
 * 错误响应，使各 Demo_Controller 内无需编写 try-catch（落实编码规范 C6，
 * 满足需求 6.6、11.3）。</p>
 *
 * <p>异常类型一览：</p>
 * <ul>
 *     <li>{@link com.example.patterns.common.exception.ServiceException}：
 *         通用业务异常，作为体系基类，承载未知渠道、无匹配实现、限流超阈值、
 *         必选部件缺失等可预期失败。</li>
 *     <li>{@link com.example.patterns.common.exception.IllegalStateTransitionException}：
 *         非法状态流转异常，订单状态机遇到非法流转时抛出，保持原状态不变。</li>
 *     <li>{@link com.example.patterns.common.exception.PermissionDeniedException}：
 *         权限不足异常，删除等敏感操作权限校验失败时抛出，目标数据不变。</li>
 * </ul>
 *
 * @since 1.0.0
 */
package com.example.patterns.common.exception;
