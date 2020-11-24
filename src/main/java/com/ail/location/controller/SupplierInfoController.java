package com.ail.location.controller;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.gpsUtils.StringUtil;
import com.ail.location.commom.utils.ResponseInfo;
import com.ail.location.commom.utils.ResponseUtil;
import com.ail.location.model.dto.SupplierInfoDto;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.vo.SupplierVo;
import com.ail.location.service.SupplierInfoService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "供应商信息controller")
@RestController
@RequestMapping("/supplier")
public class SupplierInfoController {

    @Autowired
    private SupplierInfoService supplierInfoService;



    @PostMapping("/addSupplier")
    @ApiOperation(value = "新增供应商信息")
    public ResponseInfo<BusCode> addSupplier(@RequestBody @Validated SupplierInfoDto dto) {
        BusCode code = supplierInfoService.addSupplier(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/updateSupplier")
    @ApiOperation(value = "修改供应商信息")
    public ResponseInfo<BusCode> updateSupplier(@RequestBody @Validated SupplierInfoDto dto) {
        BusCode code = supplierInfoService.updateSupplier(dto);
        return ResponseUtil.result(code);
    }


    @PostMapping("/selectSupplierList")
    @ApiOperation(value = "获取供应商列表信息")
    public ResponseInfo<SupplierVo> selectSupplierList(@RequestBody @Validated SupplierInfoDto dto) {
        return ResponseUtil.success(supplierInfoService.selectSupplierList(dto));
    }



    @GetMapping("/querySupplier")
    @ApiOperation(value = "根据供应商主键获取供应商信息")
    public ResponseInfo<SupplierInfoEntity> querySupplier(@ApiParam(value = "主键ID", required = true) String id) {
        return ResponseUtil.success(supplierInfoService.querySupplier(id));
    }


    @DeleteMapping("/deleteSupplier")
    @ApiOperation(value = "删除供应商信息")
    public ResponseInfo<BusCode> deleteSupplier(@ApiParam(value = "设备主键", required = true) String id) {
        BusCode code = supplierInfoService.deleteSupplier(id);
        return ResponseUtil.result(code);
    }



    @GetMapping("/selectAllSupplierName")
    @ApiOperation(value = "查询所以供应商名称列表")
    public ResponseInfo<List<SupplierInfoEntity>> selectAllSupplierName(){
        return ResponseUtil.success(supplierInfoService.selectAllSupplierName());
    }


    @PostMapping("/checkSupplier")
    @ApiOperation(value = "校验供应商是否存在 返回值")
    public ResponseInfo<Boolean> checkSupplier(@RequestBody @Validated SupplierInfoDto dto){
        Wrapper wrapper = new EntityWrapper<SupplierInfoEntity>();
        if(null == dto.getSupplierId()){
            wrapper.eq("supplier_name", dto.getSupplierName());
            wrapper.eq("enable", 1);
        }else{
            wrapper.ne("supplier_id", dto.getSupplierId());
            wrapper.eq("supplier_name", dto.getSupplierName());
            wrapper.eq("enable", 1);
        }
        List<SupplierInfoEntity> list = supplierInfoService.selectList(wrapper);
        if(CollectionUtils.isEmpty(list)){
            return ResponseUtil.success();
        }else{
            return ResponseUtil.error();
        }
    }




}
