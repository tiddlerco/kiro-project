package com.example.patterns.behavioral.command.mapper;

import com.example.patterns.behavioral.command.entity.ProductEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 商品 Mapper 接口（命令模式操作目标的持久化访问）。
 *
 * <p>面向 {@code biz_product} 表提供命令模式所需的增、删、改、查能力：其中「删除」采用
 * 逻辑删除（将状态置为 0）而非物理删除，并配套提供「恢复」（将状态置回 1），以支撑删除命令的撤销。
 * 所有 SQL 语句均外置于 {@code mapper/command/ProductMapper.xml}，本接口不使用任何
 * MyBatis-Plus Wrapper（QueryWrapper / LambdaQueryWrapper）。</p>
 *
 * @since 1.0.0
 */
public interface ProductMapper {

    /**
     * 新增商品。
     *
     * <p>插入成功后，由数据库自增主键回填至入参对象的 {@code id} 属性
     * （在 XML 中通过 {@code useGeneratedKeys} 与 {@code keyProperty} 实现）。</p>
     *
     * @param product 待新增的商品实体，至少需包含商品名称与价格
     * @return 受影响的行数（新增成功为 1）
     */
    int insert(ProductEntity product);

    /**
     * 根据主键更新商品信息。
     *
     * <p>按入参中的 {@code id} 定位记录，仅更新其中非空的字段（商品名称 / 价格 / 状态）。</p>
     *
     * @param product 携带主键 id 及待更新字段的商品实体
     * @return 受影响的行数（更新成功为 1）
     */
    int updateById(ProductEntity product);

    /**
     * 逻辑删除商品。
     *
     * <p>将指定商品的状态置为 0（已删除），不做物理删除，以便删除命令撤销时恢复数据。</p>
     *
     * @param id 目标商品的主键 id
     * @return 受影响的行数（删除成功为 1）
     */
    int logicDelete(@Param("id") Long id);

    /**
     * 恢复已被逻辑删除的商品。
     *
     * <p>将指定商品的状态置回 1（正常），用于删除命令的撤销恢复。</p>
     *
     * @param id 目标商品的主键 id
     * @return 受影响的行数（恢复成功为 1）
     */
    int restore(@Param("id") Long id);

    /**
     * 根据主键查询商品。
     *
     * @param id 目标商品的主键 id
     * @return 对应的商品实体；记录不存在时返回 {@code null}
     */
    ProductEntity selectById(Long id);
}
