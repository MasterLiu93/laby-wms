package com.laby.module.wms.enums;

import cn.hutool.core.util.ArrayUtil;
import com.laby.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * WMS 模块通用状态枚举
 * 
 * 说明：
 * - WMS 模块中的 status 字段统一定义：0-禁用，1-启用
 * - 这与 {@link com.laby.framework.common.enums.CommonStatusEnum} 的定义相反
 * - CommonStatusEnum: ENABLE(0, "开启"), DISABLE(1, "关闭")
 * - WmsStatusEnum: DISABLE(0, "禁用"), ENABLE(1, "启用")
 * 
 * 适用范围：
 * - 仓库（wms_warehouse）
 * - 库区（wms_warehouse_area）
 * - 商品（wms_goods）
 * - 商品分类（wms_goods_category）
 * - 供应商（wms_supplier）
 * - 客户（wms_customer）
 * - 承运商（wms_carrier）
 * 
 * 注意：
 * - 库位（wms_warehouse_location）使用专用的 location_status 字典
 * - 库存（wms_inventory）使用 {@link InventoryStatusEnum}
 * - 入库单/出库单等业务单据使用各自的状态枚举
 *
 * @author laby
 */
@Getter
@AllArgsConstructor
public enum WmsStatusEnum implements ArrayValuable<Integer> {

    /**
     * 禁用
     * 说明：资源/实体被禁用，不可使用
     */
    DISABLE(0, "禁用"),
    
    /**
     * 启用
     * 说明：资源/实体正常启用，可正常使用
     */
    ENABLE(1, "启用");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WmsStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    
    /**
     * 状态名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 判断是否为启用状态
     * 
     * @param status 状态值
     * @return true-启用，false-禁用或null
     */
    public static boolean isEnable(Integer status) {
        return ENABLE.status.equals(status);
    }

    /**
     * 判断是否为禁用状态
     * 
     * @param status 状态值
     * @return true-禁用，false-启用或null
     */
    public static boolean isDisable(Integer status) {
        return DISABLE.status.equals(status);
    }

    /**
     * 根据状态值获取枚举
     * 
     * @param status 状态值
     * @return 枚举对象，找不到返回 null
     */
    public static WmsStatusEnum valueOf(Integer status) {
        return ArrayUtil.firstMatch(o -> o.getStatus().equals(status), values());
    }

}

