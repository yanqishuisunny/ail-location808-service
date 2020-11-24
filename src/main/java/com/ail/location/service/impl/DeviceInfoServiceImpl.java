package com.ail.location.service.impl;

import com.ail.location.commom.snowflake.SnowflakeIdWorker;
import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.exception.BusinessException;
import com.ail.location.commom.utils.GeoCodeUtils;
import com.ail.location.commom.utils.HttpUtils;
import com.ail.location.commom.utils.XinyaUrlUtils;
import com.ail.location.dao.gps.CarDeviceMappingDao;
import com.ail.location.dao.gps.DeviceInfoDao;
import com.ail.location.dao.gps.SupplierInfoDao;
import com.ail.location.model.dto.DeviceAlarmDto;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.vo.*;
import com.ail.location.service.CarDeviceMappingService;
import com.ail.location.service.DeviceInfoService;
import com.ail.location.service.SupplierInfoService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoDao, DeviceInfoEntity> implements DeviceInfoService {


    @Resource
    private DeviceInfoDao deviceInfoDao;

    @Resource
    @Lazy
    private DeviceInfoService deviceInfoService;

    @Autowired
    private CarDeviceMappingDao carDeviceMappingDao;

    @Autowired
    private SupplierInfoDao supplierInfoDao;

    @Autowired
    private CarDeviceMappingService carDeviceMappingService;

    private static final String XLS = "xls";

    private static final String XLSK = "xlsx";

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoServiceImpl.class);


    /**
     * 新增设备信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public BusCode addDevices(DeviceInfoDto dto) {
        if (StringUtils.isEmpty(dto.getImei())) {
            return BusCode.DEVICE_IMEI_NULL;
        }
        if (StringUtils.isEmpty(dto.getType())) {
            return BusCode.DEVICE_TYPE_NULL;
        }
        if (StringUtils.isEmpty(dto.getSupplierId())) {
            return BusCode.DEVICE_SUPPLIERID_NULL;
        }
        if (StringUtils.isEmpty(dto.getSupplierCode())) {
            return BusCode.DEVICE_SUPPLIERCODE_NULL;
        }
        if (StringUtils.isEmpty(dto.getSim())) {
            return BusCode.DEVICE_SIM_NULL;
        }
        Wrapper wrapper = new EntityWrapper<DeviceInfoEntity>();
        wrapper.eq("imei", dto.getImei());
        wrapper.eq("enable", 1);
        List<DeviceInfoEntity> list = deviceInfoDao.selectList(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            return BusCode.DEVICE_IMEI_HAVE;
        }
        DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
        BeanUtils.copyProperties(dto, deviceInfoEntity);
        deviceInfoEntity.setDeviceId(new BigInteger(SnowflakeIdWorker.generateId().toString()));
        deviceInfoEntity.setCreateTime(new Date());
        deviceInfoEntity.setModifyTime(new Date());
        deviceInfoEntity.setCreateBy("admin");
        deviceInfoEntity.setInstallTime(new Date());
        deviceInfoEntity.setStatus(1);
        if (!StringUtils.isEmpty(dto.getRemark())) {
            deviceInfoEntity.setRemark(dto.getRemark());
        }
        deviceInfoDao.insert(deviceInfoEntity);
        saveCarDeviceMapping(dto, deviceInfoEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 修改设备信息    点击编辑按钮后的，点击保存按钮执行的接口
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public BusCode updateDevices(DeviceInfoDto dto) {
        if (StringUtils.isEmpty(dto.getDeviceId())) {
            return BusCode.DEVICEID_NULL;
        }
        if (StringUtils.isEmpty(dto.getVehicleNo())) {
            return BusCode.VEHICLENO_NULL;
        }
        if (StringUtils.isEmpty(dto.getVehicleId())) {
            return BusCode.VEHICLEID_NULL;
        }
        if (StringUtils.isEmpty(dto.getCarrier())) {
            return BusCode.CARRIER_NULL;
        }
        DeviceInfoEntity deviceInfoEntity = this.selectById(dto.getDeviceId());
        if (null == deviceInfoEntity) {
            return BusCode.DEVICE_ERROR;
        }
        deviceInfoEntity.setExpiration(dto.getExpiration());
        deviceInfoEntity.setDeviceActivationTime(dto.getDeviceActivationTime());
        //安装信息
        deviceInfoEntity.setInstallPositionUrl(dto.getInstallPositionUrl());
        deviceInfoEntity.setInstallCarUrl(dto.getInstallCarUrl());
        deviceInfoEntity.setStatus(1);
        deviceInfoEntity.setModifyTime(new Date());
        deviceInfoDao.updateById(deviceInfoEntity);
        saveCarDeviceMapping(dto, deviceInfoEntity);
        return BusCode.SUCCESS;
    }


    /**
     * //绑定的车辆
     *
     * @param dto
     * @param deviceInfoEntity
     */
    @Transactional
    public void saveCarDeviceMapping(DeviceInfoDto dto, DeviceInfoEntity deviceInfoEntity) {
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        //查看此车牌号是否已经绑定其他设备
        if (null != carDeviceMappingEntity && carDeviceMappingEntity.getDeviceId().compareTo(deviceInfoEntity.getDeviceId()) != 0) {
            throw new BusinessException(BusCode.VEHICLENO_BINDING);
        }
        //查看该设备绑定的车牌
        carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setDeviceId(deviceInfoEntity.getDeviceId());
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        if (null == carDeviceMappingEntity) {
            //新增设备和车辆关系
            carDeviceMappingEntity = new CarDeviceMappingEntity();
            carDeviceMappingEntity.setId(BigInteger.ZERO);
            carDeviceMappingEntity.setDeviceId(deviceInfoEntity.getDeviceId());
            carDeviceMappingEntity.setImei(dto.getImei());
            carDeviceMappingEntity.setVehicleId(dto.getVehicleId());
            carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
            carDeviceMappingEntity.setCreateBy("admin");
            carDeviceMappingEntity.setCarrier(dto.getCarrier());
            carDeviceMappingEntity.setCreateTime(new Date());
            carDeviceMappingEntity.setModifyTime(new Date());
            carDeviceMappingEntity.setModifyBy("admin");
            carDeviceMappingDao.insert(carDeviceMappingEntity);
        } else {
            //修改设备和车辆关系
            carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
            carDeviceMappingEntity.setVehicleId(dto.getVehicleId());
            carDeviceMappingEntity.setModifyTime(new Date());
            carDeviceMappingEntity.setCarrier(dto.getCarrier());
            carDeviceMappingDao.updateById(carDeviceMappingEntity);
        }
    }


    /**
     * 查询设备列表信息 有模糊查询操作 根据设备 SIM 卡流量到期时间排序，最近到期的排在最前，以此类推 ,DeviceInfoDto deviceInfoDto
     *
     * @return
     */
    @Override
    public DeviceTotalVo queryDeviceInfoList(DeviceInfoDto dto) {
        DeviceTotalVo deviceTotalVo = new DeviceTotalVo();
        //查询分页列表startOffset
        int startOffset = (dto.getPage() - 1) * dto.getLimit();
        dto.setStartOffset(startOffset);
        dto.setPageSize(dto.getLimit());
        if (!StringUtils.isEmpty(dto.getProvince()) && "上海".equals(dto.getProvince())) {
            dto.setProvince("上海市");
        }
        if (null != dto.getSimExpirationTime() && !StringUtils.isEmpty(dto.getSimExpirationTime())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat query = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //结束时间
            Calendar endCalendar = new GregorianCalendar();
            endCalendar.setTime(dto.getSimExpirationTime());
            endCalendar.add(Calendar.DATE, 1);
            endCalendar.set(Calendar.HOUR_OF_DAY, 0);
            endCalendar.set(Calendar.MINUTE, 0);
            endCalendar.set(Calendar.SECOND, 0);
            endCalendar.set(Calendar.MILLISECOND, 0);
            dto.setEndSimExpirationTime(endCalendar.getTime());
        }
        List<DeviceVo> list = deviceInfoDao.queryDeviceList(dto);
        deviceTotalVo.setList(list);
        List<DeviceVo> totalList = deviceInfoDao.queryDeviceAllList(dto);
        if (!CollectionUtils.isEmpty(totalList)) {
            deviceTotalVo.setTotalNum(totalList.size());
        } else {
            deviceTotalVo.setTotalNum(0);
        }
        return deviceTotalVo;
    }

    /**
     * 查询设备信息  根据设备id查询出该设备，在进行修改  ,点击编辑按钮也可以使用这个接口
     *
     * @param deviceId
     * @return
     */
    @Override
    public DeviceInfoVo queryDeviceInfo(String deviceId) {
        DeviceInfoVo deviceInfoVo = new DeviceInfoVo();
        DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
        deviceInfoEntity.setDeviceId(new BigInteger(deviceId));
        deviceInfoEntity.setEnable(true);
        deviceInfoEntity = deviceInfoDao.selectOne(deviceInfoEntity);
        if (null != deviceInfoEntity) {
            BeanUtils.copyProperties(deviceInfoEntity, deviceInfoVo);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (null != deviceInfoEntity.getDeviceActivationTime()) {
                deviceInfoVo.setDeviceActivationTime(formatter.format(deviceInfoEntity.getDeviceActivationTime()));
            }
            if (null != deviceInfoEntity.getInstallTime()) {
                deviceInfoVo.setInstallTime(formatter.format(deviceInfoEntity.getInstallTime()));
            }
            if (null != deviceInfoEntity.getExpiration()) {
                deviceInfoVo.setExpiration(formatter.format(deviceInfoEntity.getExpiration()));
            }
            if (null != deviceInfoEntity.getSimActivationTime()) {
                deviceInfoVo.setSimActivationTime(formatter.format(deviceInfoEntity.getSimActivationTime()));
            }
            if (null != deviceInfoEntity.getSimExpirationTime()) {
                deviceInfoVo.setSimExpirationTime(formatter.format(deviceInfoEntity.getSimExpirationTime()));
            }
            deviceInfoVo.setDeviceId(deviceInfoEntity.getDeviceId().toString());
            if (!StringUtils.isEmpty(deviceInfoEntity.getSupplierId())) {
                //查询供应商名称
                SupplierInfoEntity supplierInfoEntity = new SupplierInfoEntity();
                supplierInfoEntity.setSupplierId(deviceInfoEntity.getSupplierId());
                supplierInfoEntity.setEnable(true);
                supplierInfoEntity = supplierInfoDao.selectOne(supplierInfoEntity);
                if (null != supplierInfoEntity) {
                    deviceInfoVo.setSupplierName(supplierInfoEntity.getSupplierName());
                    deviceInfoVo.setSupplierId(supplierInfoEntity.getSupplierId());
                    deviceInfoVo.setSupplierCode(supplierInfoEntity.getSupplierCode());
                }
            }
            //查询车辆关系信息
            CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
            carDeviceMappingEntity.setImei(deviceInfoEntity.getImei());
            carDeviceMappingEntity.setEnable(true);
            carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
            if (null != carDeviceMappingEntity) {
                //使用http接口调用车辆信息
                Map<String, String> paramMap = new HashMap<>();
                paramMap.put("vehicleNo", carDeviceMappingEntity.getVehicleNo());
                String resultUtil = HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.queryCar);
                if (!StringUtils.isEmpty(resultUtil)) {
                    CarVo carVo = JSONObject.parseObject(resultUtil, CarVo.class);
                    deviceInfoVo.setCarType(carVo.getCarType());
                    deviceInfoVo.setCarTypeName(carVo.getCarTypeName());
                    deviceInfoVo.setVehicleNo(carVo.getVehicleNo());
                    deviceInfoVo.setOrgID(carVo.getOrgId());
                    deviceInfoVo.setOrgName(carVo.getOrgName());
                    deviceInfoVo.setVehicleId(carVo.getVehicleId());
                    deviceInfoVo.setOrgContactPhone(carVo.getOrgContactPhone());
                    deviceInfoVo.setCarrier(carVo.getOrgName());
                    deviceInfoVo.setMobile(carVo.getMobile());
                    deviceInfoVo.setVinNo(carVo.getVinNo());
                    deviceInfoVo.setDriverName(carVo.getDriverName());
                }
            }
        }

        return deviceInfoVo;
    }


    /**
     * 删除设备信息
     *
     * @param id
     * @return
     */
    @Override
    public BusCode deleteDeviceInfo(String id) {
        if (StringUtils.isEmpty(id)) {
            return BusCode.DEVICEID_NULL;
        }
        DeviceInfoEntity deviceInfoEntity = deviceInfoDao.selectById(id);
        if (null == deviceInfoEntity) {
            return BusCode.DEVICE_ERROR;
        }
        deviceInfoEntity.setEnable(false);
        deviceInfoDao.updateById(deviceInfoEntity);
        return BusCode.SUCCESS;
    }


    /**
     * 查询省区设备区域分布
     */
    @Override
    public DeviceDistributionVo queryProDistribution() {
        DeviceDistributionVo deviceDistributionVo = new DeviceDistributionVo();
        List<ProDistributionVo> proList = deviceInfoDao.queryProDistribution();
        deviceDistributionVo.setProList(proList);
        List<StatusVo> stList = deviceInfoDao.queryStatusDib();
        Map<String, StatusVo> statusVoMap = stList.stream().collect(Collectors.toMap(StatusVo::getStatus, StatusVo -> StatusVo));
        //1:在线2:离线3:拆除
        StatusVo statusVo = null;
        if (null == statusVoMap.get("1")) {
            statusVo = new StatusVo();
            statusVo.setStatus("1");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        if (null == statusVoMap.get("2")) {
            statusVo = new StatusVo();
            statusVo.setStatus("2");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        if (null == statusVoMap.get("3")) {
            statusVo = new StatusVo();
            statusVo.setStatus("3");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        deviceDistributionVo.setStstusList(stList);
        return deviceDistributionVo;
    }


    /**
     * 查询省区设备区域分布
     */
    @Override
    public DeviceDistributionVo queryCyDistribution(String province) {
        if (!StringUtils.isEmpty(province) && "上海".equals(province)) {
            province = "上海市";
        }
        DeviceDistributionVo deviceDistributionVo = new DeviceDistributionVo();
        List<CyDistributionVo> cyList = deviceInfoDao.queryCyDistribution(province);
        deviceDistributionVo.setCyList(cyList);
        List<StatusVo> stList = deviceInfoDao.queryStatusDibByPro(province);
        deviceDistributionVo.setStstusList(stList);
        Map<String, StatusVo> statusVoMap = stList.stream().collect(Collectors.toMap(StatusVo::getStatus, StatusVo -> StatusVo));
        //1:在线2:离线3:拆除
        StatusVo statusVo = null;
        if (null == statusVoMap.get("1")) {
            statusVo = new StatusVo();
            statusVo.setStatus("1");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        if (null == statusVoMap.get("2")) {
            statusVo = new StatusVo();
            statusVo.setStatus("2");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        if (null == statusVoMap.get("3")) {
            statusVo = new StatusVo();
            statusVo.setStatus("3");
            statusVo.setStatusNumber(0);
            stList.add(statusVo);
        }
        return deviceDistributionVo;
    }


    /**
     * 告警推送
     */
    @Override
    @Transactional
    public BusCode deviceAlarm(DeviceAlarmDto dto) {
        if (null == dto || StringUtils.isEmpty(dto.getImei()) || StringUtils.isEmpty(dto.getAlarmType())) {
            return BusCode.FAILURE;
        }
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity.setImei(dto.getImei());
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        if (null == carDeviceMappingEntity) {
            throw new BusinessException("此设备绑定的车牌已经拆除");
        }
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("vehicleNo", carDeviceMappingEntity.getVehicleNo());
        paramMap.put("hardwareId", carDeviceMappingEntity.getDeviceId().toString());
        if ("135".equals(dto.getAlarmType()) || "6".equals(dto.getAlarmType()) || "1011".equals(dto.getAlarmType())) {
            //调用消息系统发送超速报警消息
            HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.speedingReminder);
        } else if ("19".equals(dto.getAlarmType())) {
            DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
            deviceInfoEntity.setImei(dto.getImei());
            deviceInfoEntity.setEnable(true);
            deviceInfoEntity = deviceInfoDao.selectOne(deviceInfoEntity);
            if (null != deviceInfoEntity) {
                deviceInfoEntity.setStatus(3);
                deviceInfoDao.updateById(deviceInfoEntity);
                //删除绑定车辆
                carDeviceMappingEntity.setEnable(false);
                carDeviceMappingDao.updateById(carDeviceMappingEntity);
                //调用消息系统发送拆除设备消息
                HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.removeReminder);
            }
        }
        return BusCode.SUCCESS;
    }


    /**
     * 查询未被绑定的车辆
     */
    @Override
    public List<CarVo> queryCarList(String vehicleNo) {
        List<CarVo> list = new ArrayList<>();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("vehicleNo", vehicleNo);
        //查询已经绑定的车牌
        Wrapper wrapper = new EntityWrapper<CarDeviceMappingEntity>();
        wrapper.eq("enable", 1);
        List<CarDeviceMappingEntity> carDeviceList = carDeviceMappingDao.selectList(wrapper);
        if (!CollectionUtils.isEmpty(carDeviceList)) {
            String vehicleNos = carDeviceList.stream().map(CarDeviceMappingEntity::getVehicleNo).collect(Collectors.joining(","));
            paramMap.put("vehicleNoStrs", vehicleNos);
        }
        String data = HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.queryListCar);
        if (!StringUtils.isEmpty(data)) {
            ArrayList<CarVo> carList = JSON.parseObject(data, new TypeReference<ArrayList<CarVo>>() {
            });
            if (!CollectionUtils.isEmpty(carList)) {
                list = carList;
            }
        }
        return list;
    }


    /**
     * 根据车牌号查询车辆信息
     */
    @Override
    public CarVo queryCarInformation(String vehicleNo) {
        CarVo carVo = null;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("vehicleNo", vehicleNo);
        String data = HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.queryCar);
        if (!StringUtils.isEmpty(data)) {
            carVo = JSON.parseObject(data, CarVo.class);
        }
        return carVo;
    }


    /**
     * 批量导入（批量添加）
     */
    @Override
    @Transactional
    public void importExcel(MultipartFile file) {
        List<DeviceInfoDto> list = new ArrayList<>();
        checkData(file, list);
        List<DeviceInfoDto> vehicleNoList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DeviceInfoDto::getVehicleNo))), ArrayList::new));
        if (vehicleNoList.size() < list.size()) {
            list = list.stream().filter(item -> !vehicleNoList.contains(item)).collect(Collectors.toList());
            throw new BusinessException("导入模板中车牌号:" + list.get(0).getVehicleNo() + "重复");
        }
        List<DeviceInfoDto> iemeList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DeviceInfoDto::getImei))), ArrayList::new));
        if (iemeList.size() < list.size()) {
            list = list.stream().filter(item -> !iemeList.contains(item)).collect(Collectors.toList());
            throw new BusinessException("导入模板中ieme:" + list.get(0).getImei() + "重复");
        }
        //查询业务库车牌号
        List<CarVo> carVoList = null;
        Map<String, String> paramMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(list)) {
            String vehicleNos = list.stream().map(DeviceInfoDto::getVehicleNo).collect(Collectors.joining(","));
            paramMap.put("vehicleNoStrs", vehicleNos);
        }
        String data = HttpUtils.sendPostJson(paramMap, XinyaUrlUtils.queryCarByVechils);
        if (!StringUtils.isEmpty(data)) {
            ArrayList<CarVo> carList = JSON.parseObject(data, new TypeReference<ArrayList<CarVo>>() {
            });
            if (!CollectionUtils.isEmpty(carList)) {
                carVoList = carList;
            }
        }
        if (CollectionUtils.isEmpty(carVoList)) {
            throw new BusinessException("车牌号全部不存在");
        }
        Map<String, CarVo> xinyaVehicleNoMap = carVoList.stream().filter(Objects::nonNull).collect(Collectors.toMap(CarVo::getVehicleNo, u -> u, (u1, u2) -> u2));
        //查询已经绑定的车牌
        List<CarDeviceMappingEntity> carDeviceMappingList = carDeviceMappingDao.selectList(new EntityWrapper<CarDeviceMappingEntity>().eq("enable", 1));
        //根据车牌号取集合
        Map<String, CarDeviceMappingEntity> mappingVehicleNoMap = carDeviceMappingList.stream().collect(Collectors.toMap(CarDeviceMappingEntity::getVehicleNo, CarDeviceMappingEntity -> CarDeviceMappingEntity));
        //根据设备取集合
        Map<String, CarDeviceMappingEntity> mappingIemeMap = carDeviceMappingList.stream().collect(Collectors.toMap(CarDeviceMappingEntity::getImei, CarDeviceMappingEntity -> CarDeviceMappingEntity));
        //查询所有设备
        List<DeviceInfoEntity> deviceList = deviceInfoDao.selectList(new EntityWrapper<DeviceInfoEntity>().eq("enable", 1));
        //根据设备分配
        Map<String, DeviceInfoEntity> deviceInfoIemeMap = deviceList.stream().collect(Collectors.toMap(DeviceInfoEntity::getImei, DeviceInfoEntity -> DeviceInfoEntity));
        //查询所有供应商
        List<SupplierInfoEntity> supplierList = supplierInfoDao.selectList(new EntityWrapper<SupplierInfoEntity>().eq("enable", 1));
        //根据车牌号分配
        Map<String, SupplierInfoEntity> supplierMap = supplierList.stream().collect(Collectors.toMap(SupplierInfoEntity::getSupplierName, SupplierInfoEntity -> SupplierInfoEntity));
        CarVo carVo = null;
        CarDeviceMappingEntity vehicleNoMapping = null;
        CarDeviceMappingEntity iemeMapping = null;
        DeviceInfoEntity deviceInfoEntity = null;
        SupplierInfoEntity supplierInfoEntity = null;
        List<CarDeviceMappingEntity> mappingResult = new ArrayList<>();
        List<DeviceInfoEntity> deviceResult = new ArrayList<>();
        for (DeviceInfoDto deviceInfoDto : list) {
            //1:校验所输入的车牌号是否全部存在于欣雅业务库中(list,xinyaVehicleNoMap)
            carVo = xinyaVehicleNoMap.get(deviceInfoDto.getVehicleNo());
            if (null == carVo) {
                throw new BusinessException("车牌号" + deviceInfoDto.getVehicleNo() + "不存在于系统中");
            }
            //2:校验所输入的车牌号是否已经绑定到车辆设备关系表(list,mappingVehicleNoMap)
            vehicleNoMapping = mappingVehicleNoMap.get(deviceInfoDto.getVehicleNo());
            if (null != vehicleNoMapping) {
                throw new BusinessException("车牌号" + deviceInfoDto.getVehicleNo() + "已经绑定在设备表,不能重复绑定");
            }
            //3:校验所输入的设备是否已经绑定到车辆设备关系表(list,mappingIemeMap)
            iemeMapping = mappingIemeMap.get(deviceInfoDto.getImei());
            if (null != iemeMapping) {
                throw new BusinessException("设备编码" + deviceInfoDto.getImei() + "已经存在于设备表,不能重复添加");
            }
            //4:校验所输入的设备ieme是否已经存在于设备表(list,deviceInfoIemeMap)
            deviceInfoEntity = deviceInfoIemeMap.get(deviceInfoDto.getImei());
            if (null != deviceInfoEntity) {
                throw new BusinessException("设备编码" + deviceInfoDto.getImei() + "已经存在于设备表,不能重复添加");
            }
            //5:校验所输入的供应商是否存在于供应商表(list,supplierMap)
            supplierInfoEntity = supplierMap.get(deviceInfoDto.getSupplierName());
            if (null == supplierInfoEntity) {
                throw new BusinessException("供应商" + deviceInfoDto.getSupplierName() + "不存在");
            }
            deviceInfoEntity = new DeviceInfoEntity();
            String deviceId = SnowflakeIdWorker.generateId().toString();
            BeanUtils.copyProperties(deviceInfoDto, deviceInfoEntity);
            deviceInfoEntity.setSupplierCode(supplierInfoEntity.getSupplierCode());
            deviceInfoEntity.setSupplierId(supplierInfoEntity.getSupplierId());
            deviceInfoEntity.setDeviceId(new BigInteger(deviceId));
            deviceInfoEntity.setCreateTime(new Date());
            deviceInfoEntity.setModifyTime(new Date());
            deviceInfoEntity.setCreateBy("admin");
            deviceInfoEntity.setModifyBy("admin");
            deviceInfoEntity.setStatus(1);
            deviceResult.add(deviceInfoEntity);
            vehicleNoMapping = new CarDeviceMappingEntity();
            vehicleNoMapping.setDeviceId(new BigInteger(deviceId));
            vehicleNoMapping.setId(BigInteger.ZERO);
            vehicleNoMapping.setImei(deviceInfoEntity.getImei());
            vehicleNoMapping.setVehicleId(carVo.getVehicleId());
            vehicleNoMapping.setVehicleNo(carVo.getVehicleNo());
            vehicleNoMapping.setCreateBy("admin");
            vehicleNoMapping.setCarrier(carVo.getOrgName());
            vehicleNoMapping.setCreateTime(new Date());
            vehicleNoMapping.setModifyTime(new Date());
            vehicleNoMapping.setModifyBy("admin");
            mappingResult.add(vehicleNoMapping);
        }
        if (!CollectionUtils.isEmpty(deviceResult)) {
            deviceInfoService.insertBatch(deviceResult);
        }
        if (!CollectionUtils.isEmpty(mappingResult)) {
            carDeviceMappingService.insertBatch(mappingResult);
        }
    }


    /**
     * 解析excel
     *
     * @param file
     * @param list
     * @return
     */
    private BusCode checkData(MultipartFile file, List<DeviceInfoDto> list) {
        Workbook workbook = null;
        String fileName = file.getOriginalFilename();
        if (fileName.endsWith(XLS)) {
            try {
                workbook = new HSSFWorkbook(file.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (fileName.endsWith(XLSK)) {
            try {
                workbook = new XSSFWorkbook(file.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new BusinessException("文件不是Excel文件");
        }
        Sheet sheet = workbook.getSheetAt(0);
        int rows = sheet.getLastRowNum();
        if (rows == 0) {
            throw new BusinessException("请填写行数");
        }
        for (int i = 1; i <= rows; i++) {
            //读取左上端单元格
            Row row = sheet.getRow(i);
            //行不为空
            if (row != null) {
                //读取cell
                DeviceInfoDto deviceInfoDto = new DeviceInfoDto();
                //设备ieme
                String ieme = getCellValue(row.getCell(0));
                if (i == rows && StringUtils.isEmpty(ieme)) {
                    continue;
                }
                //过滤excel最下方所有空白行
                if (StringUtils.isEmpty(getCellValue(row.getCell(1))) && StringUtils.isEmpty(getCellValue(row.getCell(2))) &&
                        StringUtils.isEmpty(getCellValue(row.getCell(3))) && StringUtils.isEmpty(getCellValue(row.getCell(4))) &&
                        StringUtils.isEmpty(getCellValue(row.getCell(5))) && StringUtils.isEmpty(getCellValue(row.getCell(6))) &&
                        StringUtils.isEmpty(getCellValue(row.getCell(7))) && StringUtils.isEmpty(getCellValue(row.getCell(8)))) {
                    break;
                }
                if (StringUtils.isEmpty(ieme)) {
                    throw new BusinessException("第" + i + "行设备IMEI编码不能为空");
                }
                deviceInfoDto.setImei(ieme);
                //设备型号
                String type = getCellValue(row.getCell(1));
                if (StringUtils.isEmpty(type)) {
                    throw new BusinessException("第" + i + "行设备型号不能为空");
                }
                deviceInfoDto.setType(type);
                //所属供应商
                String supplierName = getCellValue(row.getCell(2));
                if (StringUtils.isEmpty(supplierName)) {
                    throw new BusinessException("第" + i + "行所属供应商不能为空");
                }
                deviceInfoDto.setSupplierName(supplierName);
                logger.info(supplierName);
                //设备激活时间
                String deviceActivationTime = getCellValue(row.getCell(3));
                String fmt = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                Date date = null;
                try {
                    date = sdf.parse(deviceActivationTime);
                } catch (ParseException e) {
                }
                deviceInfoDto.setDeviceActivationTime(date);
                //设备激活时间
                String expiration = getCellValue(row.getCell(4));
                try {
                    date = sdf.parse(expiration);
                } catch (ParseException e) {
                }
                deviceInfoDto.setExpiration(date);
                //车牌号
                String vehicleNo = getCellValue(row.getCell(5));
                if (StringUtils.isEmpty(vehicleNo)) {
                    throw new BusinessException("第" + i + "行所属供应车牌号不能为空");
                }
                deviceInfoDto.setVehicleNo(vehicleNo);
                //sim卡号
                String sim = getCellValue(row.getCell(6));
                if (StringUtils.isEmpty(sim)) {
                    throw new BusinessException("第" + i + "行sim卡号不能为空");
                }
                deviceInfoDto.setSim(sim);
                //sim卡激活时间
                String simActivationTime = getCellValue(row.getCell(7));
                if (StringUtils.isEmpty(simActivationTime)) {
                    throw new BusinessException("第" + i + "行sim激活时间不能为空");
                }
                try {
                    date = sdf.parse(simActivationTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                deviceInfoDto.setSimActivationTime(date);
                //sim流量到期时间
                String simExpirationTime = getCellValue(row.getCell(8));
                if (StringUtils.isEmpty(simExpirationTime)) {
                    throw new BusinessException("第" + i + "行sim流量到期时间不能为空");
                }
                try {
                    date = sdf.parse(simExpirationTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                deviceInfoDto.setSimExpirationTime(date);
                list.add(deviceInfoDto);
            }
        }
        return BusCode.SUCCESS;
    }


    /**
     * 获取Cell内容
     *
     * @param cell
     * @return
     */
    private String getCellValue(Cell cell) {
        String value = "";
        if (cell != null) {
            //以下是判断数据的类型
            switch (cell.getCellType()) {
                case HSSFCell.CELL_TYPE_NUMERIC://数字
                    value = cell.getNumericCellValue() + "";
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        if (date != null) {
                            value = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        } else {
                            value = "";
                        }
                    } else {
                        value = new DecimalFormat("0").format(cell.getNumericCellValue());
                    }
                    break;
                case HSSFCell.CELL_TYPE_STRING: //字符串
                    value = cell.getStringCellValue();
                    break;
                case HSSFCell.CELL_TYPE_BOOLEAN: //boolean
                    value = cell.getBooleanCellValue() + "";
                    break;
                case HSSFCell.CELL_TYPE_FORMULA: //公式
                    value = cell.getCellFormula() + "";
                    break;
                case HSSFCell.CELL_TYPE_BLANK: //空值
                    value = "";
                    break;
                case HSSFCell.CELL_TYPE_ERROR: //故障
                    value = "非法字符";
                    break;
                default:
                    value = "未知类型";
                    break;
            }
        }
        return value.trim();
    }



    @Override
    public BusCode oneTransactional(DeviceInfoDto dto) {
        if (StringUtils.isEmpty(dto.getDeviceId())) {
            return BusCode.DEVICEID_NULL;
        }
        DeviceInfoEntity deviceInfoEntity = this.selectById(dto.getDeviceId());
        if (null == deviceInfoEntity) {
            return BusCode.DEVICE_ERROR;
        }
        deviceInfoEntity.setStatus(1);
        deviceInfoEntity.setModifyTime(new Date());
        deviceInfoDao.updateById(deviceInfoEntity);
        updateTransactionalB(dto, deviceInfoEntity);
        return BusCode.SUCCESS;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
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


    @Autowired
    private SupplierInfoService supplierInfoService;


    @Override
    public BusCode twoTransactional(DeviceInfoDto dto) {
        if (StringUtils.isEmpty(dto.getDeviceId())) {
            return BusCode.DEVICEID_NULL;
        }
        DeviceInfoEntity deviceInfoEntity = this.selectById(dto.getDeviceId());
        if (null == deviceInfoEntity) {
            return BusCode.DEVICE_ERROR;
        }
        deviceInfoEntity.setStatus(1);
        deviceInfoEntity.setModifyTime(new Date());
        deviceInfoDao.updateById(deviceInfoEntity);
        supplierInfoService.updateTransactionalB(dto, deviceInfoEntity);
        return BusCode.SUCCESS;
    }



}
