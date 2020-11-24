package com.ail.location.service.impl;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.exception.BusinessException;
import com.ail.location.dao.gps.CarDeviceMappingDao;
import com.ail.location.dao.gps.DeviceInfoDao;
import com.ail.location.dao.gps.SupplierInfoDao;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.dto.SupplierInfoDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.vo.SupplierVo;
import com.ail.location.service.SupplierInfoService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierInfoServiceImpl extends ServiceImpl<SupplierInfoDao, SupplierInfoEntity> implements SupplierInfoService {


    @Autowired
    private SupplierInfoDao supplierInfoDao;

    @Autowired
    private DeviceInfoDao deviceInfoDao;

    @Autowired
    private CarDeviceMappingDao carDeviceMappingDao;


    /**
     * 新增供应商信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public BusCode addSupplier(SupplierInfoDto dto) {
        Wrapper wrapper = new EntityWrapper<SupplierInfoEntity>();
        wrapper.eq("supplier_name", dto.getSupplierName());
        wrapper.eq("enable", 1);
        List<SupplierInfoEntity> list = supplierInfoDao.selectList(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            return BusCode.SUPPLIER_EXIST;
        }
        if (StringUtils.isEmpty(dto.getSupplierName())) {
            return BusCode.SUPPLIERNAME_NULL;
        }
        SupplierInfoEntity supplierInfoEntity = new SupplierInfoEntity();
        supplierInfoEntity.setSupplierId(0);
        supplierInfoEntity.setSupplierName(dto.getSupplierName());
        supplierInfoEntity.setSupplierCode(UUID.randomUUID().toString());
        supplierInfoEntity.setDescription(dto.getDescription());
        supplierInfoEntity.setSupplierAddress(dto.getSupplierAddress());
        supplierInfoEntity.setBusinessContacts(dto.getBusinessContacts());
        supplierInfoEntity.setBusinessContactPhone(dto.getBusinessContactPhone());
        supplierInfoEntity.setUserName(dto.getUserName());
        supplierInfoEntity.setUserPwd(dto.getUserPwd());
        if(!StringUtils.isEmpty(dto.getRemark())){
            supplierInfoEntity.setRemark(dto.getRemark());
        }
        supplierInfoEntity.setCreateBy("admin");//创建人
        supplierInfoEntity.setCreateTime(new Date());
        supplierInfoEntity.setModifyTime(new Date());
        supplierInfoEntity.setModifyBy("admin");//修改人
        supplierInfoDao.insert(supplierInfoEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 修改供应商信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public BusCode updateSupplier(SupplierInfoDto dto) {
        if (StringUtils.isEmpty(dto.getSupplierId())) {
            return BusCode.SUPPLIERID_NULL;
        }
        Wrapper wrapper = new EntityWrapper<SupplierInfoEntity>();
        wrapper.eq("supplier_name", dto.getSupplierName());
        wrapper.eq("enable", 1);
        wrapper.ne("supplier_id", dto.getSupplierId());
        List<SupplierInfoEntity> list = supplierInfoDao.selectList(wrapper);
        if(!CollectionUtils.isEmpty(list)){
            return BusCode.SUPPLIER_EXIST;
        }
        SupplierInfoEntity supplierInfoEntity = supplierInfoDao.selectById(dto.getSupplierId());
        if (null == supplierInfoEntity) {
            return BusCode.SUPPLIE_INFO_NULL;
        }
        if(!StringUtils.isEmpty(dto.getSupplierName())){
            supplierInfoEntity.setSupplierName(dto.getSupplierName());
        }
        if(!StringUtils.isEmpty(dto.getDescription())){
            supplierInfoEntity.setDescription(dto.getDescription());
        }
        if(!StringUtils.isEmpty(dto.getSupplierAddress())){
            supplierInfoEntity.setSupplierAddress(dto.getSupplierAddress());
        }
        if(!StringUtils.isEmpty(dto.getBusinessContacts())){
            supplierInfoEntity.setBusinessContacts(dto.getBusinessContacts());
        }
        if(!StringUtils.isEmpty(dto.getBusinessContactPhone())){
            supplierInfoEntity.setBusinessContactPhone(dto.getBusinessContactPhone());
        }
        if(!StringUtils.isEmpty(dto.getUserName())){
            supplierInfoEntity.setUserName(dto.getUserName());
        }
        if(!StringUtils.isEmpty(dto.getUserPwd())){
            supplierInfoEntity.setUserPwd(dto.getUserPwd());
        }
        if(!StringUtils.isEmpty(dto.getRemark())){
            supplierInfoEntity.setRemark(dto.getRemark());
        }
        supplierInfoEntity.setModifyTime(new Date());
        supplierInfoDao.updateById(supplierInfoEntity);
        return BusCode.SUCCESS;
    }



    /**
     * 查询供应商列表  供应商名称未null，则查询全部，分页，否者查询对应的供应商信息
     *
     * @param dto
     * @return
     */
    @Override
    public SupplierVo selectSupplierList(SupplierInfoDto dto) {
        SupplierVo supplierVo = new SupplierVo();
        List<SupplierInfoEntity> list = new ArrayList<>();
        Page<SupplierInfoEntity> page;
        if (!StringUtils.isEmpty(dto.getSupplierName())) {
            list = supplierInfoDao.selectList(new EntityWrapper<SupplierInfoEntity>().eq("enable", 1).eq("supplier_name", dto.getSupplierName()));
        } else {
            page = this.selectPage(new Page<>(dto.getPage(), dto.getLimit()),
                    new EntityWrapper<SupplierInfoEntity>().eq("enable", 1));
            list = page.getRecords();
        }
        Integer num;
        for (SupplierInfoEntity entity : list) {
            num = deviceInfoDao.selectCount(new EntityWrapper<DeviceInfoEntity>().eq("supplier_id", entity.getSupplierId()));
            entity.setEquipmentNumber(num);
        }
        int totalNum = 0;
        if (!StringUtils.isEmpty(dto.getSupplierName())) {
            totalNum = supplierInfoDao.selectCount(new EntityWrapper<SupplierInfoEntity>().eq("enable", 1).eq("supplier_name", dto.getSupplierName()));
        } else {
            totalNum = supplierInfoDao.selectCount(new EntityWrapper<SupplierInfoEntity>().eq("enable", "1"));
        }
        supplierVo.setList(list);
        supplierVo.setTotalNum(totalNum);
        return supplierVo;
    }


    /**
     * 查询供应商信息
     *
     * @param id
     * @return
     */
    @Override
    public SupplierInfoEntity querySupplier(String id) {
        SupplierInfoEntity supplierInfoEntity = new SupplierInfoEntity();
        supplierInfoEntity.setSupplierId(Integer.parseInt(id));
        supplierInfoEntity.setEnable(true);
        supplierInfoEntity = supplierInfoDao.selectById(supplierInfoEntity);
        return supplierInfoEntity;
    }


    /**
     * 删除供应商信息
     *
     * @param id
     * @return
     */
    @Override
    public BusCode deleteSupplier(String id) {
        if (StringUtils.isEmpty(id)) {
            return BusCode.CAR_DEVICE_MAPPING_ID_NULL;
        }
        SupplierInfoEntity supplierInfoEntity = supplierInfoDao.selectById(id);
        if (null == supplierInfoEntity) {
            return BusCode.CAR_DEVICE_MAPPING_ERROR;
        }
        supplierInfoEntity.setEnable(false);
        supplierInfoDao.updateById(supplierInfoEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 查询所有供应商名称
     */
    @Override
    public List<SupplierInfoEntity> selectAllSupplierName() {
        List<SupplierInfoEntity> list = supplierInfoDao.selectList(new EntityWrapper<SupplierInfoEntity>().eq("enable", "1"));
        return list;
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateTransactionalB(DeviceInfoDto dto, DeviceInfoEntity deviceInfoEntity) {
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        carDeviceMappingEntity.setDeviceId(deviceInfoEntity.getDeviceId());
        //修改设备和车辆关系
        carDeviceMappingEntity.setModifyTime(new Date());
        carDeviceMappingDao.updateById(carDeviceMappingEntity);
        if(null != carDeviceMappingEntity){
            throw new BusinessException("*************");
        }
    }



}
