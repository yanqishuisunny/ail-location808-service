package com.ail.location.service.impl;

import com.ail.location.commom.cache.CachePre;
import com.ail.location.commom.gpsUtils.StringUtil;
import com.ail.location.commom.mq.MqProducer;
import com.ail.location.commom.snowflake.SnowflakeIdWorker;
import com.ail.location.commom.utils.GeoCodeUtils;
import com.ail.location.commom.utils.RedisUtil;
import com.ail.location.dao.gps.CarDeviceMappingDao;
import com.ail.location.dao.gps.SupplierInfoDao;
import com.ail.location.model.dto.LocationDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.gis.GisResult;
import com.ail.location.model.mongo.Location;
import com.ail.location.model.vo.GisLocationVo;
import com.ail.location.model.vo.TrailVo;
import com.ail.location.service.DeviceInfoService;
import com.ail.location.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Title： LocationServiceImpl </p>
 * <p>Description： </p>
 * <p>Company：ail </p>
 *
 * @author sujunxuan
 * @version V1.0
 * @date 2020/1/13 15:53
 */
@Slf4j
@Service
public class LocationServiceImpl implements LocationService {

    /**
     * 赤道半径
     */
    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CarDeviceMappingDao carDeviceMappingDao;

    @Autowired
    private SupplierInfoDao supplierInfoDao;

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    /**
     * 获取车辆最新定位
     *
     * @param dto
     * @return 最新定位信息
     */
    @Override
    public Location xinYaLastLocation(LocationDto dto) {
        Query query = new Query();
        Location location = new Location();
        if (!StringUtils.isEmpty(dto.getVehicleNo())) {
            query.addCriteria(new Criteria().where("vehicle_no").is(dto.getVehicleNo()));
        }
        if (!StringUtils.isEmpty(dto.getImei())) {
            query.addCriteria(new Criteria().where("imei").is(dto.getImei()));
        }
        Criteria state = new Criteria().where("status").is(1);
        query.addCriteria(state);
        query.with(Sort.by(Sort.Order.desc("location_time"))); //排序逻辑
        query.limit(1);
        List<Location> list = mongoTemplate.find(query, Location.class);
        if (!CollectionUtils.isEmpty(list)) {
            location = list.get(0);
        }
        return location;
    }


    /**
     * 将三方数据写入mongo
     *
     * @param result 定位数据
     * @return 定位数据
     */
    @Override
    public void addMongoData(List<GisResult> result) {
        List<String> imeis = result.stream().map(GisResult::getImei).collect(Collectors.toList());
        Wrapper wrapper = new EntityWrapper<CarDeviceMappingEntity>();
        wrapper.in("imei", imeis);
        wrapper.eq("enable", 1);
        //查询设备绑定关系表数据
        List<CarDeviceMappingEntity> carDeviceMappingList = carDeviceMappingDao.selectList(wrapper);
        if (CollectionUtils.isEmpty(carDeviceMappingList)) {
            return;
        }
        //根据车辆分组
        Map<String, CarDeviceMappingEntity> carMappingMap = carDeviceMappingList.stream().collect(Collectors.toMap(
                CarDeviceMappingEntity::getVehicleNo, CarDeviceMappingEntity -> CarDeviceMappingEntity));
        //根据设备分组
        Map<String, CarDeviceMappingEntity> imeiMappingMap = carDeviceMappingList.stream().collect(Collectors.toMap(
                CarDeviceMappingEntity::getImei, CarDeviceMappingEntity -> CarDeviceMappingEntity));
        List<DeviceInfoEntity> deviceInfoList = deviceInfoService.selectList(wrapper);
        if (CollectionUtils.isEmpty(deviceInfoList)) {
            return;
        }
        Map<String, DeviceInfoEntity> deviceInfoMap = deviceInfoList.stream().collect(Collectors.toMap(
                DeviceInfoEntity::getImei, DeviceInfoEntity -> DeviceInfoEntity));
        //定位消息集合
        List<GisLocationVo> gisLocationVoList = new ArrayList<>();
        GisLocationVo gisLocationVo = null;
        CarDeviceMappingEntity carDeviceMappingEntity = null;
        DeviceInfoEntity deviceInfoEntity = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Location> locationList = new ArrayList<>();
        //获取redis缓存数据
        Map<String, Location> oldLocationMap = new HashMap<>();
        //上一个点定位数据
        Map<String, Object> vechilesMap = RedisUtil.getMapValue(CachePre.VECHILES_LOCATION_LAST);
        //获取正在绑定的上一个点的定位车辆
        for (String key : vechilesMap.keySet()) {
            CarDeviceMappingEntity carDeviceMapping = carMappingMap.get(key);
            if(null == carDeviceMapping){
                continue;
            }
            try{
                Location oldLocation = JSONObject.parseObject((String) vechilesMap.get(key), Location.class);
                if (null == oldLocation || StringUtils.isEmpty(oldLocation.getImei())) {
                    continue;
                }
                oldLocationMap.put(oldLocation.getImei(), oldLocation);
            }catch (Exception e){}
        }
        if (CollectionUtils.isEmpty(vechilesMap)) {
            vechilesMap = new HashMap<>();
        }
        //创建新的设备集合
        deviceInfoList = new ArrayList<>();
        for (GisResult gisResult : result) {
            if (StringUtils.isEmpty(gisResult.getImei())) {
                continue;
            }
            carDeviceMappingEntity = imeiMappingMap.get(gisResult.getImei());
            deviceInfoEntity = deviceInfoMap.get(gisResult.getImei());
            if (null != carDeviceMappingEntity) {
                //mongo数据
                Location location = new Location();
                location.setVehicleNo(carDeviceMappingEntity.getVehicleNo());
                location.setCreateTime(new Date());
                location.setImei(gisResult.getImei());
                location.setLat(Double.parseDouble(gisResult.getLat()));
                location.setLng(Double.parseDouble(gisResult.getLng()));
                location.setMileage(gisResult.getMileage());
                location.setPlatform(1);
                if (!StringUtils.isEmpty(gisResult.getSpeed())) {
                    location.setSpeed(Double.parseDouble(gisResult.getSpeed()));
                }
                location.setStatus(1);
                location.setLocationType("GPS");
                if (null != gisResult.getGpsTime()) {
                    try {
                        location.setLocationTime(format.parse(gisResult.getGpsTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                //计算距离
                if (!CollectionUtils.isEmpty(oldLocationMap) && null != oldLocationMap.get(gisResult.getImei())) {
                    Location oldLocation = oldLocationMap.get(gisResult.getImei());
                    if (!StringUtils.isEmpty(gisResult.getLng()) && !StringUtils.isEmpty(gisResult.getLat()) &&
                            !StringUtils.isEmpty(oldLocation.getLng()) && !StringUtils.isEmpty(oldLocation.getLat())) {
                        location.setDistance(getDistance(gisResult.getLng(), gisResult.getLat(), oldLocation.getLng().toString(), oldLocation.getLat().toString()));
                    }
                }
                if (null != location.getLng() && null != location.getLat()) {
                    Location gaoDeLocation = GeoCodeUtils.getAddress("location=" + location.getLng().toString() + "," + location.getLat().toString());
                    if (null != gaoDeLocation) {
                        if(!StringUtil.isEmpty(gaoDeLocation.getCountryCode()) && gaoDeLocation.getCountryCode().length() >= 2){
                            location.setProvinceCode(gaoDeLocation.getCountryCode().substring(0,2));
                        }
                        if(!StringUtil.isEmpty(gaoDeLocation.getCountryCode()) && gaoDeLocation.getCountryCode().length() >= 4){
                            location.setCityCode(gaoDeLocation.getCountryCode().substring(0,4));
                        }
                        location.setProvince(gaoDeLocation.getProvince());
                        location.setCountry(gaoDeLocation.getCountry());
                        location.setCountryCode(gaoDeLocation.getCountryCode());
                        location.setCity(gaoDeLocation.getCity());
                        location.setPlatform(1);
                    }
                }
                locationList.add(location);
                //rabbitmq数据
                gisLocationVo = new GisLocationVo();
                gisLocationVo.setLat(gisResult.getLat());
                gisLocationVo.setLng(gisResult.getLng());
                gisLocationVo.setLocationTime(gisResult.getGpsTime());
                gisLocationVo.setSpeed(gisResult.getSpeed());
                gisLocationVo.setVehicleNo(carDeviceMappingEntity.getVehicleNo());
                gisLocationVo.setId(SnowflakeIdWorker.generateId());
                gisLocationVoList.add(gisLocationVo);
                if (!StringUtil.isEmpty(carDeviceMappingEntity.getVehicleNo())) {
                    vechilesMap.put(carDeviceMappingEntity.getVehicleNo(), JSONObject.toJSONString(location));
                }
            }
            if (null != deviceInfoEntity) {
                if (null != gisResult.getStatus() && gisResult.getStatus().equals("0")) {
                    deviceInfoEntity.setStatus(2);
                } else {
                    deviceInfoEntity.setStatus(Integer.valueOf(gisResult.getStatus()));
                }
            }
        }
        if (!CollectionUtils.isEmpty(locationList)) {
            mongoTemplate.insertAll(locationList);
            log.info("写入mongo数据成功****************************");
            //发送定位消息
            log.info("发送三方定位数据消息****************************" +
                    JSONObject.toJSONString(gisLocationVoList.stream().map(GisLocationVo::getId).collect(Collectors.toList())));
            rabbitTemplate.convertAndSend(MqProducer.GpsQueue, JSONObject.toJSONString(gisLocationVoList));
        }
        //缓存车辆最后定位redis
        RedisUtil.hmset(CachePre.VECHILES_LOCATION_LAST, vechilesMap);
        //更新设备状态
        if (!CollectionUtils.isEmpty(deviceInfoList)) {
            deviceInfoService.updateBatchById(deviceInfoList);
        }
    }


    /**
     * Description : 通过经纬度获取距离(单位：米)
     * Group :
     *
     * @param originLon      出发点经度 113.94101783150425
     * @param originLat      出发点纬度 22.58474332760045
     * @param destinationLon 目的地经度
     * @param destinationLat 目的地纬度
     * @return double
     * @author Carlos
     * @date 2020/3/15 0015 9:14
     */
    public static String getDistance(String originLon, String originLat, String destinationLon, String destinationLat) {
        double radLat1 = rad(Double.parseDouble(originLat));
        double radLat2 = rad(Double.parseDouble(destinationLat));
        double a = radLat1 - radLat2;
        double b = rad(Double.parseDouble(originLon)) - rad(Double.parseDouble(destinationLon));
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        // 保留两位小数
        s = Math.round(s * 100d) / 100d;
        s = s * 1000;
        return String.valueOf(s);
    }



    /**
     * 查询车辆轨迹和总里程数
     *
     * @param dto
     * @return
     */
    @Override
    public TrailVo queryTrailAndMileage(LocationDto dto) {
        TrailVo trailVo = new TrailVo();
        Query query = new Query();
        if (!StringUtils.isEmpty(dto.getVehicleNo())) {
            query.addCriteria(new Criteria().where("vehicle_no").is(dto.getVehicleNo()));
        }
        if (!StringUtils.isEmpty(dto.getImei())) {
            query.addCriteria(new Criteria().where("imei").is(dto.getImei()));
        }
        if (null != dto.getStartTime() && null != dto.getEndTime()) {
            query.addCriteria(new Criteria().where("location_time").gte(dto.getStartTime()).lte(dto.getEndTime()));
        }
        Criteria state = new Criteria().where("status").is(1);
        query.addCriteria(state);
        query.with(Sort.by(Sort.Order.desc("location_time"))); //排序逻辑
        List<Location> list = mongoTemplate.find(query, Location.class);
        //计算总里程
        if (!CollectionUtils.isEmpty(list)) {
            trailVo.setMileage(String.valueOf(list.stream().filter(t -> !StringUtils.isEmpty(t.getDistance()) && !"0.0".equals(t.getDistance())).
                    mapToDouble(x -> Double.parseDouble(x.getDistance())).sum()));
        }
        //根据定位时间去重
        Long start = System.currentTimeMillis();
        list = list.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Location::getLocationTime))), ArrayList::new));
        log.info("end:" + (System.currentTimeMillis() - start));
        //排序
        list.sort((Location m1, Location m2) -> m2.getLocationTime().compareTo(m1.getLocationTime()));
        trailVo.setLocationList(list);
        return trailVo;
    }


    /**
     * 根据车牌号集合查询车辆定位集合
     *
     * @param carNos
     * @return
     */
    @Override
    public List<Location> queryLocationList(List<String> carNos) {
        List<Location> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(carNos)) {
            return list;
        }
        //定位数据
        Map<String, Object> vechilesMap = RedisUtil.getMapValue(CachePre.VECHILES_LOCATION_LAST);
        Location location = null;
        for (String carNo : carNos) {
            location = JSONObject.parseObject((String) vechilesMap.get(carNo), Location.class);
            if (null == location) {
                Query query = new Query();
                query.addCriteria(new Criteria().where("vehicle_no").is(carNo));
                query.limit(1);
                List<Location> result = mongoTemplate.find(query, Location.class);
                if (!CollectionUtils.isEmpty(result)) {
                    list.add(result.get(0));
                }
            } else {
                list.add(location);
            }
        }
        return list;
    }


    /**
     * 获取车辆最新定位
     *
     * @param dto
     * @return 最新定位信息
     */
    @Override
    public Location queryLatestLocation(LocationDto dto) {
        Location location = new Location();
        if (StringUtil.isEmpty(dto.getVehicleNo()) && StringUtil.isEmpty(dto.getImei())) {
            return location;
        }
        CarDeviceMappingEntity carDeviceMappingEntity = new CarDeviceMappingEntity();
        if (!StringUtil.isEmpty(dto.getVehicleNo())) {
            carDeviceMappingEntity.setVehicleNo(dto.getVehicleNo());
        }
        if (!StringUtil.isEmpty(dto.getImei())) {
            carDeviceMappingEntity.setImei(dto.getImei());
        }
        carDeviceMappingEntity.setEnable(true);
        carDeviceMappingEntity = carDeviceMappingDao.selectOne(carDeviceMappingEntity);
        if (null == carDeviceMappingEntity) {
            return location;
        }
        Query query = new Query();
        if (!StringUtils.isEmpty(carDeviceMappingEntity.getVehicleNo())) {
            query.addCriteria(new Criteria().where("vehicle_no").is(carDeviceMappingEntity.getVehicleNo()));
        }
        if (!StringUtils.isEmpty(carDeviceMappingEntity.getImei())) {
            query.addCriteria(new Criteria().where("imei").is(carDeviceMappingEntity.getImei()));
        }
        Criteria state = new Criteria().where("status").is(1);
        query.addCriteria(state);
        query.with(Sort.by(Sort.Order.desc("location_time"))); //排序逻辑
        query.limit(1);
        List<Location> list = mongoTemplate.find(query, Location.class);
        if (!CollectionUtils.isEmpty(list)) {
            location = list.get(0);
        } else {
            return location;
        }
        location.setCarrier(carDeviceMappingEntity.getCarrier());
        DeviceInfoEntity deviceInfoEntity = deviceInfoService.selectOne(new EntityWrapper<DeviceInfoEntity>().eq("imei", location.getImei()));
        if (null != deviceInfoEntity) {
            location.setDeviceId(deviceInfoEntity.getDeviceId().toString());
            location.setSupplierId(deviceInfoEntity.getSupplierId().toString());
            SupplierInfoEntity supplierInfoEntity = supplierInfoDao.selectById(deviceInfoEntity.getSupplierId());
            if (null != supplierInfoEntity) {
                location.setSupplierName(supplierInfoEntity.getSupplierName());
            }
        }
        return location;
    }


}
