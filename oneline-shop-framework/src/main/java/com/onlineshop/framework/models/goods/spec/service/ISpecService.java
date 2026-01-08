package com.onlineshop.framework.models.goods.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineshop.framework.models.goods.spec.entity.Spec;

import java.util.List;

public interface ISpecService extends IService<Spec> {

    /**
     * 获取所有启用的规格
     *
     * @return 规格列表
     */
    List<Spec> listAllEnabled();

    /**
     * 根据ID获取规格
     *
     * @param id 规格ID
     * @return 规格信息
     */
    Spec getSpecById(Long id);

    /**
     * 根据名称获取规格
     *
     * @param name 规格名称
     * @return 规格信息
     */
    Spec getSpecByName(String name);

    /**
     * 添加规格
     *
     * @param spec 规格信息
     * @return 是否成功
     */
    boolean addSpec(Spec spec);

    /**
     * 修改规格
     *
     * @param spec 规格信息
     * @return 是否成功
     */
    boolean updateSpec(Spec spec);

    /**
     * 删除规格
     *
     * @param id 规格ID
     * @return 是否成功
     */
    boolean removeSpec(Long id);

    /**
     * 启用规格
     *
     * @param id 规格ID
     * @return 是否成功
     */
    boolean enableSpec(Long id);

    /**
     * 禁用规格
     *
     * @param id 规格ID
     * @return 是否成功
     */
    boolean disableSpec(Long id);
}