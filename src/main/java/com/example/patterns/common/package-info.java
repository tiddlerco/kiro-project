/**
 * 公共基础设施包（Common Infrastructure）。
 *
 * <p>本包提供跨模式复用的轻量基础设施，复刻 RuoYi 风格但不引入其完整脚手架，
 * 亦不引入 Spring Security，以保持示例工程的聚焦与可读性。</p>
 *
 * <p>规划中的基础设施能力：</p>
 * <ul>
 *     <li>统一响应结果 AjaxResult 与统一分页返回 TableDataInfo</li>
 *     <li>控制器基类 BaseController，统一封装成功/失败/分页返回</li>
 *     <li>统一异常体系与全局异常处理器，使控制器内无需 try-catch</li>
 *     <li>简化权限校验机制（自定义注解 + AOP 切面，替代 Spring Security）</li>
 *     <li>演示入口注册表与启动清单打印（监听 ApplicationReadyEvent）</li>
 *     <li>响应码常量与通用工具</li>
 * </ul>
 *
 * @since 1.0.0
 */
package com.example.patterns.common;
