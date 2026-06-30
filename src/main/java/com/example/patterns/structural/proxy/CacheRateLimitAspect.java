package com.example.patterns.structural.proxy;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报表查询「缓存 + 限流」代理切面（代理模式中的「代理 Proxy」角色）。
 *
 * <p>以 Spring AOP 环绕通知充当 {@link ReportQueryService} 的动态代理：在不修改真实主题
 * {@link ReportQueryServiceImpl} 任何代码的前提下，为其 {@code query} 方法透明地织入两项横切逻辑
 * （满足需求 3.1、10.2）：</p>
 * <ol>
 *     <li><b>限流</b>：以固定时间窗口计数器约束进入报表查询入口的请求速率，单位时间内超过阈值即抛出
 *         {@link ServiceException}（提示「请求过于频繁」），由全局异常处理器转换为可观察错误响应。</li>
 *     <li><b>缓存</b>：以 {@link ConcurrentHashMap} 缓存查询结果，相同查询条件命中缓存即直接复用既有结果，
 *         不再穿透到真实主题重复执行高成本计算。</li>
 * </ol>
 *
 * <p><b>织入顺序及其理由</b>：本切面先限流、后缓存。限流约束的是「请求到达速率」，用于保护查询入口整体
 * 不被高频调用打垮，故作用于所有进入 {@code query} 的请求（无论其随后是否命中缓存）；缓存优化的是「计算成本」，
 * 仅在通过限流后才查找复用。该顺序既符合「限流置于最外层」的网关式工程惯例，也使两项能力均可被独立观察——
 * 适度间隔的重复调用可观察缓存命中（结果生成时间不变），而短时间高频调用可观察限流拒绝。</p>
 *
 * <p>并发说明：缓存采用线程安全的 {@link ConcurrentHashMap}；固定窗口计数涉及「判断窗口是否过期—重置—自增—比较」
 * 的复合操作，为保证其原子性与计数正确性，{@link #enforceRateLimit()} 以方法级同步实现（演示场景吞吐要求不高，
 * 同步带来的开销可忽略，换取实现简单与语义正确）。本缓存为演示用途，未实现过期淘汰，
 * 生产环境可结合 Caffeine、Redis 等带 TTL 与容量上限的缓存增强。</p>
 *
 * @since 1.0.0
 */
@Aspect
@Component
public class CacheRateLimitAspect {

    /** 日志记录器，用于输出缓存命中/未命中等可观察的代理行为轨迹。 */
    private static final Logger log = LoggerFactory.getLogger(CacheRateLimitAspect.class);

    /**
     * 限流时间窗口长度（毫秒）。
     *
     * <p>固定窗口的时间跨度；同一窗口内累计的请求数受 {@link #MAX_REQUESTS_PER_WINDOW} 约束。</p>
     */
    private static final long RATE_LIMIT_WINDOW_MILLIS = 1000L;

    /**
     * 单个时间窗口内允许通过的最大请求数。
     *
     * <p>超过该阈值的请求将被限流拒绝；阈值取相对宽松的值，使适度的人工重复调用不致误触发限流，
     * 而脚本化的高频调用可稳定触发限流以供演示。</p>
     */
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    /**
     * 报表结果缓存。
     *
     * <p>键为查询条件的缓存键（见 {@link ReportQueryRequest#cacheKey()}），值为该条件首次计算出的报表结果。
     * 采用线程安全的 {@link ConcurrentHashMap} 以支撑并发读写。</p>
     */
    private final Map<String, ReportData> reportCache = new ConcurrentHashMap<>();

    /**
     * 当前限流时间窗口的起始时刻（毫秒）。
     *
     * <p>与 {@link #requestCountInWindow} 共同构成固定窗口限流状态，均由 {@link #enforceRateLimit()} 同步访问与更新。</p>
     */
    private long windowStartMillis = System.currentTimeMillis();

    /**
     * 当前时间窗口内已累计的请求数。
     *
     * <p>每进入一次报表查询入口自增一次；窗口过期时随窗口一并重置归零。</p>
     */
    private int requestCountInWindow = 0;

    /**
     * 报表查询方法切点。
     *
     * <p>以 {@code execution} 表达式精确匹配真实主题 {@link ReportQueryServiceImpl} 的 {@code query} 方法，
     * 使本切面仅对报表查询织入缓存与限流，不波及其他方法。</p>
     */
    @Pointcut("execution(* com.example.patterns.structural.proxy.ReportQueryServiceImpl.query(..))")
    public void reportQueryPointcut() {
        // 切点声明方法，仅用于承载切点表达式，无方法体
    }

    /**
     * 环绕通知：充当报表查询的代理，依次织入限流与缓存横切逻辑。
     *
     * <p>处理流程：先执行限流判定（超阈值即抛出业务异常，目标方法不被调用）；随后以查询条件的缓存键查找缓存，
     * 命中则直接复用既有结果而不穿透到真实查询；未命中才调用目标方法执行真实计算，并将结果写入缓存后返回。</p>
     *
     * @param pjp 连接点，代表被代理的报表查询方法调用，可经其获取入参并驱动目标方法执行
     * @return 报表查询结果；缓存命中时为既有结果对象，否则为真实计算得到的新结果
     * @throws Throwable 目标方法执行过程中抛出的任意异常将向上传播
     */
    @Around("reportQueryPointcut()")
    public Object aroundQuery(ProceedingJoinPoint pjp) throws Throwable {
        enforceRateLimit();
        ReportQueryRequest req = extractRequest(pjp.getArgs());
        if (req == null) {
            return pjp.proceed();
        }
        String cacheKey = req.cacheKey();
        ReportData cached = reportCache.get(cacheKey);
        if (cached != null) {
            log.info("[代理-缓存命中] 复用既有报表结果，缓存键={}", cacheKey);
            return cached;
        }
        log.info("[代理-缓存未命中] 执行真实报表查询，缓存键={}", cacheKey);
        Object result = pjp.proceed();
        cacheResultIfPresent(cacheKey, result);
        return result;
    }

    /**
     * 执行固定时间窗口限流判定。
     *
     * <p>判定逻辑：若当前时刻已越过窗口长度，则开启新窗口并将计数归零；随后对本次请求计数自增，
     * 若窗口内累计请求数超过阈值，则抛出业务异常以拒绝本次请求。「判断—重置—自增—比较」作为复合操作
     * 须保持原子性，故本方法以同步方式执行。</p>
     *
     * @throws ServiceException 当窗口内请求数超过 {@link #MAX_REQUESTS_PER_WINDOW} 时抛出，提示「请求过于频繁」
     */
    private synchronized void enforceRateLimit() {
        long now = System.currentTimeMillis();
        if (now - windowStartMillis >= RATE_LIMIT_WINDOW_MILLIS) {
            windowStartMillis = now;
            requestCountInWindow = 0;
        }
        requestCountInWindow++;
        if (requestCountInWindow > MAX_REQUESTS_PER_WINDOW) {
            throw new ServiceException("请求过于频繁");
        }
    }

    /**
     * 从连接点参数中提取报表查询请求。
     *
     * <p>切点已限定目标方法签名，正常情形下首个参数即为 {@link ReportQueryRequest}；此处仍做空值与类型防御，
     * 当参数缺失或类型不匹配时返回 {@code null}，由调用方据此跳过缓存处理直接放行目标方法。</p>
     *
     * @param args 连接点的方法入参数组
     * @return 报表查询请求；当参数缺失或类型不匹配时返回 {@code null}
     */
    private ReportQueryRequest extractRequest(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object first = args[0];
        if (first instanceof ReportQueryRequest) {
            return (ReportQueryRequest) first;
        }
        return null;
    }

    /**
     * 当目标方法返回结果为报表数据时，将其写入缓存。
     *
     * <p>仅缓存类型为 {@link ReportData} 的有效结果，避免将异常占位或空结果污染缓存。</p>
     *
     * @param cacheKey 查询条件的缓存键
     * @param result   目标方法的返回结果
     */
    private void cacheResultIfPresent(String cacheKey, Object result) {
        if (result instanceof ReportData) {
            reportCache.put(cacheKey, (ReportData) result);
        }
    }
}
