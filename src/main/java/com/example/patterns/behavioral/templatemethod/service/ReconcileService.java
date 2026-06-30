package com.example.patterns.behavioral.templatemethod.service;

import com.example.patterns.behavioral.templatemethod.AbstractReconcileTemplate;
import com.example.patterns.behavioral.templatemethod.domain.ReconcileReport;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账渠道路由服务。
 *
 * <p>承担模板方法模式演示中的「渠道路由」职责：聚合容器内所有
 * {@link AbstractReconcileTemplate} 具体实现，依据各模板自报的渠道标识
 * （{@link AbstractReconcileTemplate#channel()}）建立「渠道 → 模板」路由表，
 * 对外仅暴露按渠道标识触发对账的统一入口，使 Controller 无需感知具体渠道实现。</p>
 *
 * <p>路由表在 {@link #initRouteTable()} 中于 Bean 初始化阶段一次性构建：
 * 若出现重复的渠道标识，则在启动期直接抛出 {@link ServiceException} 快速失败，
 * 避免将「同一渠道存在多个模板、路由结果不确定」的隐患带入运行期。</p>
 *
 * @since 1.0.0
 */
@Service
public class ReconcileService {

    /**
     * 容器中全部对账模板实现。
     *
     * <p>由 Spring 自动收集所有 {@link AbstractReconcileTemplate} 类型的 Bean 注入，
     * 新增渠道模板只需声明为组件即可被自动纳入，无需改动本服务。</p>
     */
    @Resource
    private List<AbstractReconcileTemplate> reconcileTemplates;

    /**
     * 渠道路由表：渠道标识 → 对账模板。
     *
     * <p>于 Bean 初始化阶段构建一次后只读，运行期按渠道标识 O(1) 选取对应模板。</p>
     */
    private final Map<String, AbstractReconcileTemplate> routeTable = new LinkedHashMap<>();

    /**
     * 构建「渠道 → 模板」路由表。
     *
     * <p>在 Bean 依赖注入完成后由容器回调执行，遍历全部对账模板，以各模板自报的
     * 渠道标识为键登记到路由表。若发现重复渠道标识，立即抛出 {@link ServiceException}
     * 实现启动期快速失败，防止运行期路由歧义。</p>
     */
    @PostConstruct
    public void initRouteTable() {
        for (AbstractReconcileTemplate template : reconcileTemplates) {
            String channel = template.channel();
            AbstractReconcileTemplate existing = routeTable.put(channel, template);
            if (existing != null) {
                throw new ServiceException(String.format(
                        "对账渠道标识[%s]存在重复模板：%s 与 %s",
                        channel, existing.getClass().getName(), template.getClass().getName()));
            }
        }
    }

    /**
     * 按渠道标识执行对账。
     *
     * <p>依据传入的渠道标识从路由表选取对应的对账模板，并调用其
     * {@link AbstractReconcileTemplate#reconcile()} 触发完整对账流程。
     * 若渠道标识在路由表中不存在，则抛出 {@link ServiceException}。</p>
     *
     * @param channel 对账渠道标识，如 {@code "alipay"}、{@code "wechat"}
     * @return 对应渠道本次对账产出的差异报告
     */
    public ReconcileReport reconcile(String channel) {
        AbstractReconcileTemplate template = routeTable.get(channel);
        if (template == null) {
            throw new ServiceException(String.format("未知的对账渠道标识：%s", channel));
        }
        return template.reconcile();
    }
}
