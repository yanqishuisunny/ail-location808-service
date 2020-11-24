package com.ail.location.controller;

import com.ail.location.commom.utils.ResponseInfo;
import com.ail.location.commom.utils.ResponseUtil;
import com.ail.location.model.dto.LocationDto;
import com.ail.location.model.mongo.Location;
import com.ail.location.model.vo.TrailVo;
import com.ail.location.service.LocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Title： LocationController </p>
 * <p>Description：定位数据接口 </p>
 * <p>Company：ail </p>
 *
 * @author sujunxuan
 * @version V1.0
 * @date 2020/1/13 14:31
 */

@Api(tags = "位置定位")
@RequestMapping("/location")
@RestController
public class LocationController {

    @Autowired
    private LocationService locationService;


    @PostMapping("/last")
    @ApiOperation(value = "根据车牌号或设备号获取车辆最新定位")
    public ResponseInfo<Location> queryLast(@RequestBody @Validated LocationDto dto) {
        Location location = locationService.queryLatestLocation(dto);
        if(null != location && null == location.getSpeed()){
            location.setSpeed(0.0);
        }
        return ResponseUtil.success(location);
    }



    @PostMapping("/trailAndMileage")
    @ApiOperation(value = "根据车牌号或设备号获取一个时间段内车辆轨迹和总里程数")
    public ResponseInfo<TrailVo> queryTrailAndMileage(@RequestBody @Validated LocationDto dto) {
        TrailVo trailVo = locationService.queryTrailAndMileage(dto);
        return ResponseUtil.success(trailVo);
    }





}
