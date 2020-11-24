package com.ail.location.service;

import com.ail.location.commom.core.BusCode;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.dto.PageDto;
import com.ail.location.model.dto.SupplierInfoDto;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.vo.SupplierVo;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

public interface SupplierInfoService extends IService<SupplierInfoEntity> {


    /**
     * 新增供应商信息
     *
     * @param dto
     * @return
     */
    BusCode addSupplier(SupplierInfoDto dto);


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return
     */
    BusCode updateSupplier(SupplierInfoDto dto);



    /**
     * 根据供应商名称查询供应商列表信息
     */
    SupplierVo selectSupplierList(SupplierInfoDto dto);


    /**
     * 查询供应商信息
     *
     * @param id
     * @return
     */
    SupplierInfoEntity querySupplier(String id);


    /**
     * 删除供应商信息
     *
     * @param id
     * @return
     */
    BusCode deleteSupplier(String id);


    /**
     * 查询所有供应商名称
     */
    List<SupplierInfoEntity> selectAllSupplierName();


    /**
     * 查询所有供应商名称
     */
    void updateTransactionalB(DeviceInfoDto dto, DeviceInfoEntity deviceInfoEntity);


}
