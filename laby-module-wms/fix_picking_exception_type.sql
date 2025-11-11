-- ============================================================
-- 修复拣货异常类型字典数据
-- 问题：字典值使用了字符串（EMPTY、SHORT、DAMAGED、EXPIRED、WRONG）
-- 修复：改为整数字符串（'1'、'2'、'3'、'4'、'5'）
-- 执行时间：立即执行
-- ============================================================

-- 1. 删除旧的错误字典数据
DELETE FROM `system_dict_data` WHERE `dict_type` = 'wms_picking_exception_type';

-- 2. 插入正确的字典数据（value改为整数字符串）
INSERT INTO `system_dict_data`
    (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES 
    (3109, 1, '库位空', '1', 'wms_picking_exception_type', 0, 'warning', '', '目标库位无库存', '1', NOW(), '1', NOW(), b'0'),
    (3110, 2, '库存不足', '2', 'wms_picking_exception_type', 0, 'warning', '', '库位库存数量小于计划拣货数量', '1', NOW(), '1', NOW(), b'0'),
    (3111, 3, '商品损坏', '3', 'wms_picking_exception_type', 0, 'danger', '', '库位商品已损坏，无法拣货', '1', NOW(), '1', NOW(), b'0'),
    (3112, 4, '商品过期', '4', 'wms_picking_exception_type', 0, 'danger', '', '库位商品已过期，无法拣货', '1', NOW(), '1', NOW(), b'0'),
    (3113, 5, '拣错商品', '5', 'wms_picking_exception_type', 0, 'danger', '', '拣货员拣选了错误的商品', '1', NOW(), '1', NOW(), b'0');

-- 3. 验证修复结果
SELECT 
    `sort`, 
    `label`, 
    `value`, 
    `color_type`,
    `remark`
FROM `system_dict_data` 
WHERE `dict_type` = 'wms_picking_exception_type' 
ORDER BY `sort`;

-- 预期结果：
-- +------+--------------+-------+------------+----------------------------------------+
-- | sort | label        | value | color_type | remark                                 |
-- +------+--------------+-------+------------+----------------------------------------+
-- |    1 | 库位空       | 1     | warning    | 目标库位无库存                          |
-- |    2 | 库存不足     | 2     | warning    | 库位库存数量小于计划拣货数量            |
-- |    3 | 商品损坏     | 3     | danger     | 库位商品已损坏，无法拣货                |
-- |    4 | 商品过期     | 4     | danger     | 库位商品已过期，无法拣货                |
-- |    5 | 拣错商品     | 5     | danger     | 拣货员拣选了错误的商品                  |
-- +------+--------------+-------+------------+----------------------------------------+

