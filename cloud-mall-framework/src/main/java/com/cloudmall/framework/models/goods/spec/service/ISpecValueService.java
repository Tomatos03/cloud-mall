package com.cloudmall.framework.models.goods.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudmall.framework.models.goods.spec.entity.SpecValue;

import java.util.List;

public interface ISpecValueService extends IService<SpecValue> {

    /**
     * 根据规格ID查询所有规格值
     *
     * @param specId 规格ID
     * @return 规格值列表
     */
    List<SpecValue> listBySpecId(Long specId);

    /**
     * 根据规格ID查询启用的规格值
     *
     * @param specId 规格ID
     * @return 规格值列表
     */
    List<SpecValue> listEnabledBySpecId(Long specId);

    /**
     * 根据ID获取规格值
     *
     * @param id 规格值ID
     * @return 规格值信息
     */
    SpecValue getValueById(Long id);

    /**
     * 根据规格ID和值查询规格值
     *
     * @param specId 规格ID
     * @param value  规格值
     * @return 规格值信息
     */
    SpecValue getBySpecIdAndValue(Long specId, String value);

    /**
     * 添加规格值
     *
     * @param specValue 规格值信息
     * @return 是否成功
     */
    boolean addValue(SpecValue specValue);

    /**
     * 修改规格值
     *
     * @param specValue 规格值信息
     * @return 是否成功
     */
    boolean updateValue(SpecValue specValue);

    /**
     * 删除规格值
     *
     * @param id 规格值ID
     * @return 是否成功
     */
    boolean removeValue(Long id);

    /**
     * 根据规格ID删除所有规格值
     *
     * @param specId 规格ID
     * @return 删除数量
     */
    int removeBySpecId(Long specId);

    /**
     * 启用规格值
     *
     * @param id 规格值ID
     * @return 是否成功
     */
    boolean enableValue(Long id);

    /**
     * 禁用规格值
     *
     * @param id 规格值ID
     * @return 是否成功
     */
    boolean disableValue(Long id);
}