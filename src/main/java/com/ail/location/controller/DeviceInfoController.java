package com.ail.location.controller;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.utils.ResponseInfo;
import com.ail.location.commom.utils.ResponseUtil;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.vo.*;
import com.ail.location.service.DeviceInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(tags = "设备信息controller")
@RestController
@RequestMapping("/device")
public class DeviceInfoController {

    @Autowired
    private DeviceInfoService deviceInfoService;


    @PostMapping("/addDevice")
    @ApiOperation(value = "新增设备信息")
    public ResponseInfo<BusCode> addDevices(@RequestBody DeviceInfoDto dto) {
        BusCode code = deviceInfoService.addDevices(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/updateDevice")
    @ApiOperation(value = "修改设备信息")
    public ResponseInfo<BusCode> updateDevices(@RequestBody @Validated DeviceInfoDto dto) {
        BusCode code = deviceInfoService.updateDevices(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/queryDeviceList")
    @ApiOperation(value = "获取设备列表信息")
    public ResponseInfo<DeviceTotalVo> queryDeviceList(@RequestBody @Validated DeviceInfoDto dto) {
        return ResponseUtil.success(deviceInfoService.queryDeviceInfoList(dto));
    }


    @GetMapping("/queryDevice")
    @ApiOperation(value = "获取某一个设备信息，根据设备主键查询")
    public ResponseInfo<DeviceInfoVo> queryDevice(@ApiParam(value = "设备主键", required = true) String deviceId) {
        return ResponseUtil.success(deviceInfoService.queryDeviceInfo(deviceId));
    }


    @DeleteMapping("/deleteDevice")
    @ApiOperation(value = "删除设备信息")
    public ResponseInfo<BusCode> updateDevices(@ApiParam(value = "设备主键", required = true) String id) {
        BusCode code = deviceInfoService.deleteDeviceInfo(id);
        return ResponseUtil.result(code);
    }


    @GetMapping("/queryProDistribution")
    @ApiOperation(value = "查询省区设备区域分布")
    public ResponseInfo<DeviceDistributionVo> queryProvinceDistribution() {
        return ResponseUtil.success(deviceInfoService.queryProDistribution());
    }

    @GetMapping("/queryCyDistribution")
    @ApiOperation(value = "查询某省内设备区域分布")
    public ResponseInfo<DeviceDistributionVo> queryCyDistribution(@ApiParam(value = "省份名称", required = true) String province) {
        return ResponseUtil.success(deviceInfoService.queryCyDistribution(province));
    }


    @GetMapping("/queryCarList")
    @ApiOperation(value = "查询未被绑定的车辆")
    public ResponseInfo<List<CarVo>> queryCarList(@ApiParam(value = "车牌号", required = true) String vehicleNo) {
        return ResponseUtil.success(deviceInfoService.queryCarList(vehicleNo));
    }


    @GetMapping("/queryCarInformation")
    @ApiOperation(value = "根据车牌号查询车辆信息")
    public ResponseInfo<CarVo> queryCarInformation(@ApiParam(value = "车牌号", required = true) String vehicleNo) {
        return ResponseUtil.success(deviceInfoService.queryCarInformation(vehicleNo));
    }


    @PostMapping("/importExcel")
    @ApiOperation(value = "导入")
    public ResponseInfo importExcel(@RequestParam("file")  MultipartFile file){
        deviceInfoService.importExcel(file);
        return ResponseUtil.success();
    }


    @PostMapping("/oneTransactional")
    public ResponseInfo<BusCode> updateTransactional(@RequestBody @Validated DeviceInfoDto dto) {
        BusCode code = deviceInfoService.oneTransactional(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/twoTransactionals")
    public ResponseInfo<BusCode> updateTransactionals(@RequestBody @Validated DeviceInfoDto dto) {
        BusCode code = deviceInfoService.twoTransactional(dto);
        return ResponseUtil.result(code);
    }


}
