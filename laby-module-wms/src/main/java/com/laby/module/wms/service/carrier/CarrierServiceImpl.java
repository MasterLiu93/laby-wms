package com.laby.module.wms.service.carrier;

import cn.hutool.core.collection.CollUtil;
import com.laby.framework.codegen.core.enums.CodePrefixEnum;
import com.laby.framework.codegen.core.service.CodeGeneratorService;
import com.laby.framework.common.pojo.PageResult;
import com.laby.module.wms.controller.admin.carrier.vo.CarrierPageReqVO;
import com.laby.module.wms.controller.admin.carrier.vo.CarrierRespVO;
import com.laby.module.wms.controller.admin.carrier.vo.CarrierSaveReqVO;
import com.laby.module.wms.convert.carrier.CarrierConvert;
import com.laby.module.wms.dal.dataobject.carrier.CarrierDO;
import com.laby.module.wms.dal.mysql.carrier.CarrierMapper;
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
 * 承运商信息 Service 实现类
 * 
 * 功能说明：
 * - 实现承运商的 CRUD 操作
 * - 承运商编码由后端自动生成（使用 CodeGeneratorService）
 * - 支持分页查询和简单列表查询
 * 
 * @author laby
 */
@Service
@Validated
@Slf4j
public class CarrierServiceImpl implements CarrierService {

    @Resource
    private CarrierMapper carrierMapper;
    @Resource
    private CodeGeneratorService codeGeneratorService;

    @Override
    public Long createCarrier(CarrierSaveReqVO createReqVO) {
        // 1. 生成承运商编码（由后端自动生成）
        String carrierCode = codeGeneratorService.generateCode(CodePrefixEnum.CARRIER);

        // 2. 插入数据库
        CarrierDO carrier = CarrierConvert.INSTANCE.convert(createReqVO);
        carrier.setCarrierCode(carrierCode);
        carrierMapper.insert(carrier);

        log.info("[createCarrier] 创建承运商成功，承运商编码：{}, ID：{}", carrier.getCarrierCode(), carrier.getId());
        return carrier.getId();
    }

    @Override
    public void updateCarrier(CarrierSaveReqVO updateReqVO) {
        // 1. 校验承运商是否存在
        validateCarrierExists(updateReqVO.getId());

        // 2. 更新数据库（承运商编码不可修改，由后端生成）
        CarrierDO updateObj = CarrierConvert.INSTANCE.convert(updateReqVO);
        carrierMapper.updateById(updateObj);

        log.info("[updateCarrier] 更新承运商成功，ID：{}", updateReqVO.getId());
    }

    @Override
    public void deleteCarrier(Long id) {
        // 1. 校验承运商是否存在
        validateCarrierExists(id);

        // TODO: 2. 校验是否有关联的出库单，如有则不允许删除
        // 可以添加类似于 hasOutboundByCarrierId 的校验

        // 3. 删除（逻辑删除）
        carrierMapper.deleteById(id);

        log.info("[承运商] 删除成功，承运商ID：{}", id);
    }

    @Override
    public CarrierRespVO getCarrier(Long id) {
        CarrierDO carrier = carrierMapper.selectById(id);
        return CarrierConvert.INSTANCE.convert(carrier);
    }

    @Override
    public PageResult<CarrierRespVO> getCarrierPage(CarrierPageReqVO pageReqVO) {
        // 1. 查询分页数据
        PageResult<CarrierDO> pageResult = carrierMapper.selectPage(pageReqVO);
        
        // 2. 转换为 VO
        return CarrierConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<CarrierRespVO> getCarrierSimpleList() {
        // 查询所有启用状态的承运商
        List<CarrierDO> list = carrierMapper.selectList(CarrierDO::getStatus, 1);
        return CarrierConvert.INSTANCE.convertList(list);
    }

    @Override
    public Map<Long, String> getCarrierMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<CarrierDO> list = carrierMapper.selectList(CarrierDO::getId, ids);
        return convertMap(list, CarrierDO::getId, CarrierDO::getCarrierName);
    }

    /**
     * 校验承运商是否存在
     * 
     * @param id 承运商ID
     */
    private void validateCarrierExists(Long id) {
        if (carrierMapper.selectById(id) == null) {
            throw exception(CARRIER_NOT_EXISTS);
        }
    }

}

