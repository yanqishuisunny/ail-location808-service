package com.ail.location.service.impl;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.exception.BusinessException;
import com.ail.location.dao.gps.CarDeviceMappingDao;
import com.ail.location.model.dto.CarDeviceMappingDto;
import com.ail.location.model.dto.PageDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.vo.DeviceVo;
import com.ail.location.service.CarDeviceMappingService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Service
public class CarDeviceMappingServiceImpl extends ServiceImpl<CarDeviceMappingDao, CarDeviceMappingEntity> implements CarDeviceMappingService {


    @Resource
    private CarDeviceMappingDao carDeviceMappingDao;


    /**
     * 新增车辆设备关系信息
     * @param dto
     * @return
     */
    @Override
    public BusCode addMapping(CarDeviceMappingDto dto) {
        if(StringUtils.isEmpty(dto.getDeviceId())){
            throw new BusinessException(BusCode.DEVICEID_NULL);
        }
        if(StringUtils.isEmpty(dto.getImei())){
            throw new BusinessException(BusCode.DEVICE_IMEI_NULL);
        }
        if(StringUtils.isEmpty(dto.getVehicleId())){
            throw new BusinessException(BusCode.VEHICLEID_NULL);
        }
        if(StringUtils.isEmpty(dto.getVehicleNo())){
            throw new BusinessException(BusCode.VEHICLENO_NULL);
        }
        if(StringUtils.isEmpty(dto.getCarrier())){
            throw new BusinessException(BusCode.CARRIER_NULL);
        }
        Wrapper wrapper = new EntityWrapper<CarDeviceMappingEntity>();
        wrapper.eq("device_id",dto.getDeviceId());
        wrapper.eq("enable",1);
        List<CarDeviceMappingEntity> list = carDeviceMappingDao.selectList(wrapper);
        if(!CollectionUtils.isEmpty(list)){
            throw new BusinessException(BusCode.CAR_DEVICE_MAPPING_DEVICEID_EXIST);
        }
        Wrapper voWrapper = new EntityWrapper<CarDeviceMappingEntity>();
        voWrapper.eq("vehicle_no",dto.getVehicleNo());
        voWrapper.eq("enable",1);
        List<CarDeviceMappingEntity> voLists = carDeviceMappingDao.selectList(voWrapper);
        if(!CollectionUtils.isEmpty(voLists)){
            throw new BusinessException(BusCode.VEHICLENO_BINDING);
        }
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setId(BigInteger.ZERO);
        carDeviceMappingEntity.setDeviceId(dto.getDeviceId());
        carDeviceMappingEntity.setUnbindTime(new Date());
        carDeviceMappingEntity.setImei(dto.getImei());
        carDeviceMappingEntity.setVehicleId(dto.getVehicleId());
        carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
        carDeviceMappingEntity.setCreateBy("admin");
        carDeviceMappingEntity.setCreateTime(new Date());
        carDeviceMappingEntity.setModifyTime(new Date());
        carDeviceMappingEntity.setCarrier(dto.getCarrier());
        carDeviceMappingEntity.setModifyBy("admin");
        carDeviceMappingDao.insert(carDeviceMappingEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 修改车辆设备关系信息
     * @param dto
     * @return
     */
    @Override
    public BusCode updateMapping(CarDeviceMappingDto dto) {
        if(StringUtils.isEmpty(dto.getId())){
            throw new BusinessException(BusCode.CAR_DEVICE_MAPPING_ID_NULL);
        }
        CarDeviceMappingEntity carDeviceMappingEntity = carDeviceMappingDao.selectById(dto.getId());
        if(null == carDeviceMappingEntity){
            throw new BusinessException(BusCode.CAR_DEVICE_MAPPING_ERROR);
        }
        if(!StringUtils.isEmpty(dto.getVehicleNo())){
            carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
        }
        if(!StringUtils.isEmpty(dto.getVehicleId())){
            carDeviceMappingEntity.setVehicleId(dto.getVehicleId());
        }
        if(!StringUtils.isEmpty(dto.getImei())){
            carDeviceMappingEntity.setImei(dto.getImei());
        }
        if(!StringUtils.isEmpty(dto.getUnbindTime())){
            carDeviceMappingEntity.setUnbindTime(dto.getUnbindTime());
        }
        if(!StringUtils.isEmpty(dto.getDeviceId())){
            carDeviceMappingEntity.setDeviceId(dto.getDeviceId());
        }
        carDeviceMappingDao.updateById(carDeviceMappingEntity);
        return BusCode.SUCCESS;
    }



    /**
     * 查询车辆设备关系信息
     * @param id
     * @return
     */
    @Override
    public CarDeviceMappingEntity queryMapping(String id) {
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setId(new BigInteger(id));
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        return carDeviceMappingEntity;
    }



    /**
     * 查询车辆设备关系信息列表
     * @param dto
     * @return
     */
    @Override
    public List<CarDeviceMappingEntity> queryMappingList(PageDto dto) {
        Page<CarDeviceMappingEntity> page = this.selectPage(new Page<>(dto.getPage(), dto.getLimit()),
                new EntityWrapper<CarDeviceMappingEntity>().eq("enable", 1));
        List<CarDeviceMappingEntity> list = page.getRecords();
        return list;
    }



    /**
     * 删除车辆设备关系信息
     * @param id
     * @return
     */
    @Override
    public BusCode deleteMapping(String id) {
        if(StringUtils.isEmpty(id)){
            throw new BusinessException(BusCode.CAR_DEVICE_MAPPING_ID_NULL);
        }
        CarDeviceMappingEntity carDeviceMappingEntity = carDeviceMappingDao.selectById(id);
        if(null == carDeviceMappingEntity){
            throw new BusinessException(BusCode.CAR_DEVICE_MAPPING_ERROR);
        }
        carDeviceMappingEntity.setEnable(false);
        carDeviceMappingDao.updateById(carDeviceMappingEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 查询车辆设备信息
     * @param dto
     * @return
     */
    @Override
    public List<DeviceVo> queryDeviceInformation(CarDeviceMappingDto dto) {
        List<DeviceVo> list = carDeviceMappingDao.queryDeviceInformation(dto);
        return list;
    }


}
