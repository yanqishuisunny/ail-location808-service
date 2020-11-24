package com.ail.location.controller;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.utils.ResponseInfo;
import com.ail.location.commom.utils.ResponseUtil;
import com.ail.location.model.dto.CarDeviceMappingDto;
import com.ail.location.model.dto.PageDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.vo.DeviceVo;
import com.ail.location.service.CarDeviceMappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "车辆设备关系controller")
@RestController
@RequestMapping("/carDevice")
public class CarDeviceMappingController {

    @Autowired
    private CarDeviceMappingService carDeviceMappingService;



    @PostMapping("/addMapping")
    @ApiOperation(value = "新增车辆设备关系信息/设备列表界面绑定按钮")
    public ResponseInfo<BusCode> addMapping(@RequestBody @Validated CarDeviceMappingDto dto) {
        BusCode code = carDeviceMappingService.addMapping(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/updateMapping")
    @ApiOperation(value = "修改车辆设备关系信息")
    public ResponseInfo<BusCode> updateMapping(@RequestBody @Validated CarDeviceMappingDto dto) {
        BusCode code = carDeviceMappingService.updateMapping(dto);
        return ResponseUtil.result(code);
    }


    @GetMapping("/queryMapping")
    @ApiOperation(value = "获取车辆设备关系列表信息")
    public ResponseInfo<CarDeviceMappingEntity> queryMapping(@ApiParam(value = "设备主键", required = true) String id) {
        return ResponseUtil.success(carDeviceMappingService.queryMapping(id));
    }



    @PostMapping("/queryMappingList")
    @ApiOperation(value = "获取车辆设备关系信息")
    public ResponseInfo<List<CarDeviceMappingEntity>> queryMappingList(@RequestBody @Validated PageDto dto) {
        return ResponseUtil.success(carDeviceMappingService.queryMappingList(dto));
    }


    @DeleteMapping("/deleteMapping")
    @ApiOperation(value = "删除车辆设备关系信息")
    public ResponseInfo<BusCode> updateDevices(@ApiParam(value = "设备主键", required = true) String id) {
        BusCode code = carDeviceMappingService.deleteMapping(id);
        return ResponseUtil.result(code);
    }



    @PostMapping("/queryCarDevice")
    @ApiOperation(value = "查询车辆设备状态关系")
    public ResponseInfo<List<DeviceVo>> queryDeviceInformation(@RequestBody @Validated CarDeviceMappingDto dto){
        return ResponseUtil.success(carDeviceMappingService.queryDeviceInformation(dto));
    }




}
