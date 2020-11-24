package com.ail.location.service;

import com.ail.location.commom.core.BusCode;
import com.ail.location.model.dto.DeviceAlarmDto;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.vo.*;
import com.baomidou.mybatisplus.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeviceInfoService extends IService<DeviceInfoEntity> {


    /**
     * 新增设备信息
     * @param dto
     * @return
     */
    BusCode addDevices(DeviceInfoDto dto);



    /**
     * 修改设备信息
     * @param dto
     * @return
     */
    BusCode updateDevices(DeviceInfoDto dto);



    /**
     * 查询设备列表信息 ,DeviceInfoDto deviceInfoDto
     * @return
     */
    DeviceTotalVo queryDeviceInfoList(DeviceInfoDto dto);



    /**
     * 查询设备信息
     * @param deviceId
     * @return
     */
    DeviceInfoVo queryDeviceInfo(String deviceId);



    /**
     * 删除设备信息
     * @param id
     * @return
     */
    BusCode deleteDeviceInfo(String id);


    /**
     * 查询省区设备区域分布
     */
    DeviceDistributionVo queryProDistribution();


    /**
     * 查询省区设备区域分布
     */
    DeviceDistributionVo queryCyDistribution(String province);



    /**
     * 告警推送
     */
    BusCode deviceAlarm(DeviceAlarmDto dto);


    /**
     * 查询未被绑定的车辆
     */
    List<CarVo> queryCarList(String vehicleNo);


    /**
     * 根据车牌号查询车辆信息
     */
    CarVo queryCarInformation(String vehicleNo);



    /**
     * 导入
     */
    void importExcel(MultipartFile file);


    /**
     * 修改设备信息
     * @param dto
     * @return
     */
    BusCode oneTransactional(DeviceInfoDto dto);



    /**
     * 修改设备信息
     * @param dto
     * @return
     */
    BusCode twoTransactional(DeviceInfoDto dto);


}
