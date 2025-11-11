package com.laby.module.wms.service.supplier;

import cn.hutool.core.collection.CollUtil;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import com.laby.framework.common.pojo.PageResult;
import com.laby.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.laby.module.wms.controller.admin.supplier.vo.SupplierPageReqVO;
import com.laby.module.wms.controller.admin.supplier.vo.SupplierRespVO;
import com.laby.module.wms.controller.admin.supplier.vo.SupplierSaveReqVO;
import com.laby.module.wms.convert.supplier.SupplierConvert;
import com.laby.module.wms.dal.dataobject.supplier.SupplierDO;
import com.laby.module.wms.dal.mysql.supplier.SupplierMapper;
import com.laby.module.wms.enums.WmsStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.laby.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.laby.framework.common.util.collection.CollectionUtils.convertMap;
import static com.laby.module.wms.enums.ErrorCodeConstants.*;

/**
 * 供应商信息 Service 实现类
 *
 * 功能说明：
 * 1. 实现供应商的增删改查
 * 2. 供应商编码由后端自动生成（使用 CodeGeneratorService）
 * 3. 提供供应商批量查询Map接口
 * 4. 删除前校验是否有关联入库单
 *
 * @author laby
 */
@Slf4j
@Service
@Validated
public class SupplierServiceImpl implements SupplierService {

    @Resource
    private SupplierMapper supplierMapper;
    @Resource
    private CodeGeneratorService codeGeneratorService;

    @Override
    public Long createSupplier(SupplierSaveReqVO createReqVO) {
        // 1. 生成供应商编码（由后端自动生成）
        String supplierCode = codeGeneratorService.generateCode(CodePrefixEnum.SUPPLIER);
        
        // 2. 插入数据库
        SupplierDO supplier = SupplierConvert.INSTANCE.convert(createReqVO);
        supplier.setSupplierCode(supplierCode);
        // 如果前端未传status，默认为启用状态
        if (supplier.getStatus() == null) {
            supplier.setStatus(WmsStatusEnum.ENABLE.getStatus());
        }
        supplierMapper.insert(supplier);
        
        log.info("[createSupplier] 创建供应商成功，供应商编码：{}, ID：{}", supplier.getSupplierCode(), supplier.getId());
        return supplier.getId();
    }

    @Override
    public void updateSupplier(SupplierSaveReqVO updateReqVO) {
        // 1. 校验供应商是否存在
        validateSupplierExists(updateReqVO.getId());
        
        // 2. 更新数据库（供应商编码不可修改，由后端生成）
        SupplierDO updateObj = SupplierConvert.INSTANCE.convert(updateReqVO);
        supplierMapper.updateById(updateObj);
        
        log.info("[updateSupplier] 更新供应商成功，ID：{}", updateReqVO.getId());
    }

    @Override
    public void deleteSupplier(Long id) {
        // 1. 校验供应商是否存在
        validateSupplierExists(id);
        
        // TODO 2. 校验供应商是否有关联的入库单
        // 需要在入库模块完成后实现
        
        // 3. 删除供应商（软删除）
        supplierMapper.deleteById(id);
    }

    @Override
    public SupplierRespVO getSupplier(Long id) {
        SupplierDO supplier = supplierMapper.selectById(id);
        return SupplierConvert.INSTANCE.convert(supplier);
    }

    @Override
    public PageResult<SupplierRespVO> getSupplierPage(SupplierPageReqVO pageReqVO) {
        // 1. 查询分页数据
        PageResult<SupplierDO> pageResult = supplierMapper.selectPage(pageReqVO);
        
        // 2. 转换为 VO
        return SupplierConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<SupplierRespVO> getSupplierSimpleList() {
        // 查询所有启用状态的供应商
        List<SupplierDO> list = supplierMapper.selectList(
            new LambdaQueryWrapperX<SupplierDO>()
                .eq(SupplierDO::getStatus, WmsStatusEnum.ENABLE.getStatus())
                .orderByDesc(SupplierDO::getCreateTime)
        );
        
        log.info("[getSupplierSimpleList] 查询到 {} 个启用状态的供应商", list.size());
        if (!list.isEmpty()) {
            list.forEach(supplier -> log.debug("  - {}: {} (status={})", 
                supplier.getSupplierCode(), supplier.getSupplierName(), supplier.getStatus()));
        }
        
        return SupplierConvert.INSTANCE.convertList(list);
    }

    @Override
    public Map<Long, String> getSupplierMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SupplierDO> list = supplierMapper.selectList(SupplierDO::getId, ids);
        return convertMap(list, SupplierDO::getId, SupplierDO::getSupplierName);
    }

    /**
     * 校验供应商是否存在
     *
     * @param id 供应商ID
     */
    private void validateSupplierExists(Long id) {
        if (supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }
}

