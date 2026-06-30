package com.example.patterns.behavioral.command.service;

import com.example.patterns.behavioral.command.entity.ProductEntity;
import com.example.patterns.behavioral.command.mapper.ProductMapper;
import com.example.patterns.common.constant.HttpStatus;
import com.example.patterns.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 商品服务（命令模式中的「接收者 Receiver」角色）。
 *
 * <p>在命令模式中，接收者是真正执行业务操作的对象。本服务封装 {@link ProductMapper}，
 * 对外提供商品的新增、按 id 更新、逻辑删除、恢复与按 id 查询能力；各具体命令
 * （{@code AddProductCommand}/{@code UpdateProductCommand}/{@code DeleteProductCommand}）
 * 在其 {@code execute()}/{@code undo()} 中调用本服务完成正向操作与逆向回滚，
 * 命令对象自身不直接接触持久层。</p>
 *
 * <p>失败处理：所有写操作均校验受影响行数，若与预期不符（如目标记录不存在）即抛出
 * {@link ServiceException}，避免静默失败导致命令历史与真实数据不一致。</p>
 *
 * @since 1.0.0
 */
@Service
public class ProductService {

    /**
     * 商品持久化 Mapper。
     */
    @Resource
    private ProductMapper productMapper;

    /**
     * 新增商品。
     *
     * <p>调用 {@link ProductMapper#insert(ProductEntity)} 落库，新增成功后数据库自增主键
     * 会回填至入参对象的 {@code id} 属性，本方法据此返回新生成的主键 id，供命令记录 after 快照。</p>
     *
     * @param product 待新增的商品实体，要求非空且至少包含商品名称与价格
     * @return 新增成功后生成的商品主键 id
     */
    public Long addProduct(ProductEntity product) {
        Objects.requireNonNull(product, "待新增的商品不能为空");
        int affected = productMapper.insert(product);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "商品新增失败");
        }
        return product.getId();
    }

    /**
     * 根据主键更新商品信息。
     *
     * <p>调用 {@link ProductMapper#updateById(ProductEntity)}，按入参中的 {@code id} 定位记录，
     * 更新其中的商品名称、价格、状态等字段。撤销修改命令时亦复用本方法，用 before 快照覆盖回原值。</p>
     *
     * @param product 携带主键 id 及待更新字段的商品实体，要求非空且 {@code id} 不为空
     */
    public void updateProduct(ProductEntity product) {
        Objects.requireNonNull(product, "待更新的商品不能为空");
        Objects.requireNonNull(product.getId(), "待更新的商品 id 不能为空");
        int affected = productMapper.updateById(product);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "商品更新失败，商品可能不存在，id：" + product.getId());
        }
    }

    /**
     * 逻辑删除商品。
     *
     * <p>调用 {@link ProductMapper#logicDelete(Long)} 将商品状态置为 0（已删除），不做物理删除，
     * 以便删除命令撤销时通过 {@link #restoreProduct(Long)} 恢复数据。</p>
     *
     * @param id 目标商品的主键 id，要求非空
     */
    public void deleteProduct(Long id) {
        Objects.requireNonNull(id, "待删除的商品 id 不能为空");
        int affected = productMapper.logicDelete(id);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "商品删除失败，商品可能不存在，id：" + id);
        }
    }

    /**
     * 恢复已被逻辑删除的商品。
     *
     * <p>调用 {@link ProductMapper#restore(Long)} 将商品状态置回 1（正常），
     * 用于删除命令的撤销恢复。</p>
     *
     * @param id 目标商品的主键 id，要求非空
     */
    public void restoreProduct(Long id) {
        Objects.requireNonNull(id, "待恢复的商品 id 不能为空");
        int affected = productMapper.restore(id);
        if (affected != 1) {
            throw new ServiceException(HttpStatus.ERROR, "商品恢复失败，商品可能不存在，id：" + id);
        }
    }

    /**
     * 根据主键查询商品。
     *
     * <p>命令在执行前后调用本方法获取数据快照（before/after），用于命令历史的可追溯与撤销恢复。</p>
     *
     * @param id 目标商品的主键 id，要求非空
     * @return 对应的商品实体；记录不存在时返回 {@code null}
     */
    public ProductEntity getProduct(Long id) {
        Objects.requireNonNull(id, "待查询的商品 id 不能为空");
        return productMapper.selectById(id);
    }
}
