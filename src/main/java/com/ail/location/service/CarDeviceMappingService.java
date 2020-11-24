package com.ail.location.service;

import com.ail.location.commom.core.BusCode;
import com.ail.location.model.dto.CarDeviceMappingDto;
import com.ail.location.model.dto.PageDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.vo.DeviceVo;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

public interface CarDeviceMappingService extends IService<CarDeviceMappingEntity> {


    /**
     * 新增车辆设备关系信息
     * @param dto
     * @return
     */
    BusCode addMapping(CarDeviceMappingDto dto);



    /**
     * 修改车辆设备关系信息
     * @param dto
     * @return
     */
    BusCode updateMapping(CarDeviceMappingDto dto);


    /**
     * 查询车辆设备关系信息
     * @param id
     * @return
     */
    CarDeviceMappingEntity queryMapping(String id);



    /**
     * 查询车辆设备关系信息列表
     * @param dto
     * @return
     */
    List<CarDeviceMappingEntity> queryMappingList(PageDto dto);



    /**
     * 删除车辆设备关系信息
     * @param id
     * @return
     */
    BusCode deleteMapping(String id);


    /**
     * 查询车辆设备信息
     * @param dto
     * @return
     */
    List<DeviceVo> queryDeviceInformation(CarDeviceMappingDto dto);

}
