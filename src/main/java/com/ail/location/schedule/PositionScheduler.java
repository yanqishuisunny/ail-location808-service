package com.ail.location.schedule;

import com.ail.location.commom.cache.CachePre;
import com.ail.location.commom.utils.GeoCodeUtils;
import com.ail.location.commom.utils.RedisUtil;
import com.ail.location.model.dto.LocationDto;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.mongo.Location;
import com.ail.location.service.DeviceInfoService;
import com.ail.location.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.*;

@Component
public class PositionScheduler {

    @Autowired
    private LocationService locationService;

    @Autowired
    private DeviceInfoService deviceInfoService;

    private static final Logger log = LoggerFactory.getLogger(PositionScheduler.class);

    /**
     * 同步设备最新定位
     */
    @Scheduled(cron = "${scheduler.job2}")
    public void deviceLocation() {
        //获取所有的设备
        Wrapper wrapper = new EntityWrapper<DeviceInfoEntity>();
        wrapper.eq("enable", 1);
        List<DeviceInfoEntity>  deviceList = deviceInfoService.selectList(wrapper);
        if(CollectionUtils.isEmpty(deviceList)){
            return;
        }
        LocationDto locationDto = null;
        for(DeviceInfoEntity deviceInfoEntity:deviceList){
            locationDto = new LocationDto();
            locationDto.setImei(deviceInfoEntity.getImei());
            Location location = locationService.queryLatestLocation(locationDto);
            if(null == location || StringUtils.isEmpty(location.getLat()) || StringUtils.isEmpty(location.getLng())){
               continue;
            }
            location = GeoCodeUtils.getAddress("location="+location.getLng().toString()+","+location.getLat().toString());
            if(null == location){
                continue;
            }
            log.info("**********"+ location );
            deviceInfoEntity.setProvince(location.getProvince());
            deviceInfoEntity.setCity(location.getCity());
            deviceInfoEntity.setModifyTime(new Date());
        }
        deviceInfoService.updateBatchById(deviceList);
    }



//    /**
//     * 同步redis车辆最新位置
//     */
//    @Scheduled(cron = "${scheduler.job3}")
//    public void vehicleNosLocation() {
//        log.info("同步redis车辆最新位置**********");
//        Location location = null;
//        //定位数据
//        Map<String, Object> vechilesMap = RedisUtil.getMapValue(CachePre.VECHILES_LOCATION_LAST);
//        if (CollectionUtils.isEmpty(vechilesMap)) {
//            return;
//        }
//        for (String key : vechilesMap.keySet()) {
//            location = JSONObject.parseObject((String) vechilesMap.get(key), Location.class);
//            if (null == location || StringUtils.isEmpty(location.getVehicleNo()) || null == location.getLng() || null == location.getLat()) {
//                continue;
//            }
//            Location gaoDeLocation = GeoCodeUtils.getAddress("location="+location.getLng().toString()+","+location.getLat().toString());
//            if(null == gaoDeLocation){
//                continue;
//            }
//            location.setProvince(gaoDeLocation.getProvince());
//            location.setCity(gaoDeLocation.getCity());
//            location.setCityCode(gaoDeLocation.getCityCode());
//            location.setCountry(gaoDeLocation.getCountry());
//            location.setCountryCode(gaoDeLocation.getCountryCode());
//            vechilesMap.put(location.getVehicleNo(),JSONObject.toJSONString(location));
//        }
//        //缓存车辆最后定位redis
//        RedisUtil.hmset(CachePre.VECHILES_LOCATION_LAST, vechilesMap);
//    }




    public static void main(String[] arg){
        log.info(DigestUtils.md5Hex("a123456"));
    }





}
