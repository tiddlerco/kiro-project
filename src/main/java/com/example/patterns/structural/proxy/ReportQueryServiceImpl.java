package com.example.patterns.structural.proxy;

import com.example.patterns.common.exception.ServiceException;
import com.example.patterns.structural.proxy.domain.ReportData;
import com.example.patterns.structural.proxy.domain.ReportQueryRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 报表查询服务实现（代理模式中的「真实主题 RealSubject」角色）。
 *
 * <p>承载纯粹的报表查询逻辑：依据查询条件统计订单总数与销售总额并返回结果。
 * 为体现报表类查询的高成本特征、凸显缓存代理的价值，本实现刻意以
 * {@link #simulateSlowComputation()} 模拟一次较慢的数据计算。</p>
 *
 * <p>本实现完全不感知缓存与限流——这些横切关注点由切面
 * {@link CacheRateLimitAspect} 在外层透明织入，真实主题因此保持职责单一（仅负责查询），
 * 这正是代理模式「在不修改目标代码的前提下增强其行为」的核心价值（需求 3.1）。</p>
 *
 * <p>计算的确定性：相同查询条件恒得到相同的订单数与销售额（不依赖随机数或当前时间），
 * 以保证「同参幂等」，既符合报表语义，也为代理的缓存复用提供可验证的一致性基础。
 * 其中仅 {@link ReportData#getGeneratedAt()} 取自当前时刻，用作观察缓存命中的信号。</p>
 *
 * @since 1.0.0
 */
@Service
public class ReportQueryServiceImpl implements ReportQueryService {

    /**
     * 模拟报表计算的耗时（毫秒）。
     *
     * <p>用于刻画报表查询的高成本特征，使缓存命中（跳过该耗时）与未命中（承担该耗时）的差异可被观察。</p>
     */
    private static final long SLOW_COMPUTATION_MILLIS = 300L;

    /**
     * 订单总数基线值。
     *
     * <p>确定性派生订单数时的下界基数，确保统计结果落在一个具业务观感的区间内。</p>
     */
    private static final int ORDER_COUNT_BASE = 1000;

    /**
     * 订单总数派生的取模上界。
     *
     * <p>对查询条件的散列值取模的模数，将派生出的订单数约束在 {@code [基线, 基线+上界)} 区间内。</p>
     */
    private static final int ORDER_COUNT_BOUND = 9000;

    /**
     * 单笔订单均价（单位：元）。
     *
     * <p>由订单总数推算销售总额时所用的每单平均金额。</p>
     */
    private static final BigDecimal AVERAGE_ORDER_AMOUNT = new BigDecimal("88.88");

    /**
     * 执行报表查询。
     *
     * <p>编排一次完整的报表统计：先模拟高成本计算，再依据查询条件确定性地派生订单总数与销售总额，
     * 拼装统计维度描述，最后连同本次计算耗时构建为报表数据返回。</p>
     *
     * @param req 报表查询请求，承载报表类型、统计起止日期与可选地区维度
     * @return 报表数据，包含订单总数、销售总额、生成时间与本次计算耗时
     */
    @Override
    public ReportData query(ReportQueryRequest req) {
        long startMillis = System.currentTimeMillis();
        simulateSlowComputation();
        long orderCount = calculateOrderCount(req);
        BigDecimal totalSales = calculateTotalSales(orderCount);
        String dimension = buildDimension(req);
        long computeCostMillis = System.currentTimeMillis() - startMillis;
        return ReportData.of(req.getReportType(), dimension, orderCount, totalSales, computeCostMillis);
    }

    /**
     * 模拟一次较慢的报表数据计算。
     *
     * <p>以休眠固定时长的方式刻画报表查询的高成本，使缓存代理「命中即跳过该成本」的价值可被直观观察。
     * 若休眠期间线程被中断，则恢复中断标志并以业务异常形式向上抛出，避免静默吞掉中断信号。</p>
     */
    private void simulateSlowComputation() {
        try {
            Thread.sleep(SLOW_COMPUTATION_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("报表计算被中断");
        }
    }

    /**
     * 依据查询条件确定性地派生订单总数。
     *
     * <p>以查询条件缓存键的散列值对上界取模并取绝对值后叠加基线，保证相同条件恒得到相同订单数
     * （同参幂等），且结果落在 {@code [基线, 基线+上界)} 区间内。先取模再取绝对值，可规避
     * {@link Integer#MIN_VALUE} 取绝对值时的溢出问题。</p>
     *
     * @param req 报表查询请求
     * @return 与查询条件一一对应的订单总数
     */
    private long calculateOrderCount(ReportQueryRequest req) {
        int offset = Math.abs(req.cacheKey().hashCode() % ORDER_COUNT_BOUND);
        return ORDER_COUNT_BASE + offset;
    }

    /**
     * 依据订单总数推算销售总额。
     *
     * <p>以订单总数乘以单笔订单均价得到销售总额，并保留两位小数（四舍五入）以符合金额展示规范。</p>
     *
     * @param orderCount 订单总数
     * @return 销售总额（单位：元，保留两位小数）
     */
    private BigDecimal calculateTotalSales(long orderCount) {
        return AVERAGE_ORDER_AMOUNT.multiply(BigDecimal.valueOf(orderCount)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 拼装报表的统计维度描述。
     *
     * <p>以统计起止日期组成日期区间描述；当查询请求携带地区维度时，追加地区以体现更细分的统计口径。</p>
     *
     * @param req 报表查询请求
     * @return 可读的统计维度描述
     */
    private String buildDimension(ReportQueryRequest req) {
        String dateRange = req.getStartDate() + " ~ " + req.getEndDate();
        if (StringUtils.hasText(req.getRegion())) {
            return dateRange + " / " + req.getRegion();
        }
        return dateRange;
    }
}
