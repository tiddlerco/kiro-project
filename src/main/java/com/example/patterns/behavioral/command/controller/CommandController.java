package com.example.patterns.behavioral.command.controller;

import com.example.patterns.behavioral.command.domain.CommandExecuteRequest;
import com.example.patterns.behavioral.command.service.ProductCommandService;
import com.example.patterns.common.core.controller.BaseController;
import com.example.patterns.common.core.domain.AjaxResult;
import com.example.patterns.common.security.RequiresPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 命令模式演示控制器。
 *
 * <p>对外暴露商品操作命令的 HTTP 演示入口，演示命令模式「将一次操作封装为命令对象，由调用者统一
 * 触发执行与撤销，并记录命令历史」的运行效果。控制器仅负责路由分发：接收并校验请求、委派
 * {@link ProductCommandService} 完成命令编排与执行，不承载命令构造与业务逻辑。</p>
 *
 * <p>其中删除接口刻意采用 {@code GET} 映射（本项目统一的传统 URL 路径风格约定），并通过
 * 「必填的删除确认标识 + 权限校验」两道防线降低被浏览器预取或链接爬取误触发的风险
 * （落实需求 9.7、9.10、9.11）。</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pattern/command")
public class CommandController extends BaseController {

    /**
     * 命令编排服务：根据请求构造对应命令并执行或撤销。
     */
    @Resource
    private ProductCommandService productCommandService;

    /**
     * 执行商品操作命令。
     *
     * <p>由 {@code @Validated} 触发请求对象的声明式校验，随后委派命令编排服务按命令类型
     * （新增 / 修改 / 删除）构造并执行对应命令，以统一成功响应返回。</p>
     *
     * @param request 命令执行请求，含命令类型、操作人及随类型按需填写的商品字段
     * @return 表示命令执行成功的统一响应结果
     */
    @PostMapping("/execute")
    public AjaxResult execute(@Validated @RequestBody CommandExecuteRequest request) {
        productCommandService.execute(request);
        return success();
    }

    /**
     * 撤销最近一次执行的命令。
     *
     * <p>委派命令编排服务弹出最近一次已执行命令执行逆向操作并恢复数据，以统一成功响应返回。</p>
     *
     * @return 表示撤销成功的统一响应结果
     */
    @PostMapping("/undo")
    public AjaxResult undo() {
        productCommandService.undoLast();
        return success();
    }

    /**
     * 删除商品（命令模式的删除演示入口）。
     *
     * <p>刻意采用 {@code GET} 映射（本项目传统 URL 路径风格约定），并以两道防线保障删除安全：
     * 其一，方法标注 {@link RequiresPermission}，由权限切面在方法执行前校验当前用户是否具备
     * {@code pattern:product:remove} 权限，不具备时抛出权限异常、阻止删除且数据不变；其二，
     * 必填的删除确认标识 {@code confirmDelete} 由框架保证非空，其「是否为真」的业务校验下沉到
     * 命令编排服务，二者均满足才真正执行删除命令（落实需求 9.7、9.10、9.11）。</p>
     *
     * @param productId     待删除的商品 id（必填的删除目标标识参数）
     * @param confirmDelete 删除确认标识（必填，仅当为 {@code true} 时才执行删除）
     * @param operator      操作人标识，记入命令历史，缺省时使用默认操作人
     * @return 表示删除成功的统一响应结果
     */
    @RequiresPermission("pattern:product:remove")
    @GetMapping("/deleteProduct")
    public AjaxResult deleteProduct(@RequestParam Long productId,
                                    @RequestParam(name = "confirmDelete") Boolean confirmDelete,
                                    @RequestParam(name = "operator", required = false, defaultValue = "admin") String operator) {
        productCommandService.executeDelete(productId, confirmDelete, operator);
        return success();
    }
}
