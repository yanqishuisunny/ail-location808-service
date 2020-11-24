package com.ail.location.controller;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.utils.ResponseInfo;
import com.ail.location.commom.utils.ResponseUtil;
import com.ail.location.model.dto.DeviceAlarmDto;
import com.ail.location.model.dto.LocationDto;
import com.ail.location.model.mongo.Location;
import com.ail.location.model.vo.TrailVo;
import com.ail.location.service.DeviceInfoService;
import com.ail.location.service.LocationService;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "对外接口controller")
@RestController
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private LocationService locationService;




    @PostMapping("/deviceAlarm")
    @ApiOperation(value = "告警推送(19:拆除报警,135:超速报警)")
    public ResponseInfo<BusCode> deviceAlarm(@RequestParam(value = "data") String data) {
        if(StringUtils.isNullOrEmpty(data)){
            return ResponseUtil.error("data没有数据");
        }
        DeviceAlarmDto dto = JSONObject.parseObject(data, DeviceAlarmDto.class);
        BusCode busCode = deviceInfoService.deviceAlarm(dto);
        return ResponseUtil.result(busCode);
    }


    @PostMapping("/last")
    @ApiOperation(value = "根据车牌号获取车辆最新定位")
    public ResponseInfo<Location> queryLast(@RequestBody @Validated LocationDto dto) {
        Location location = locationService.xinYaLastLocation(dto);
        return ResponseUtil.success(location);
    }




    @PostMapping("/trailAndMileage")
    @ApiOperation(value = "根据车牌号或设备号获取一个时间段内车辆轨迹和总里程数")
    public ResponseInfo<TrailVo> queryTrailAndMileage(@RequestBody @Validated LocationDto dto) {
        TrailVo trailVo = locationService.queryTrailAndMileage(dto);
        return ResponseUtil.success(trailVo);
    }


    @PostMapping("/queryLocationList")
    @ApiOperation(value = "根据车牌号或设备号获取一个时间段内车辆轨迹和总里程数")
    public ResponseInfo<List<Location>> queryLocationList(@RequestBody @Validated LocationDto dto) {
        return ResponseUtil.success(locationService.queryLocationList(dto.getCarNos()));
    }



}
