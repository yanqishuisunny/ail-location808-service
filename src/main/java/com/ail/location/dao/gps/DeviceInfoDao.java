package com.ail.location.dao.gps;

import com.ail.location.model.dto.CarDeviceMappingDto;
import com.ail.location.model.dto.DeviceInfoDto;
import com.ail.location.model.entity.DeviceInfoEntity;
import com.ail.location.model.vo.*;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
  * 接口前台访问日志 Mapper 接口
 * </p>
 *
 * @author Carlos
 * @since 2020-1-13
 */
@Repository
public interface DeviceInfoDao extends BaseMapper<DeviceInfoEntity> {


    List<ProDistributionVo>  queryProDistribution();


    List<CyDistributionVo>  queryCyDistribution(@Param("province")String province);


    /**
     * 不根据省份查
     * @return
     */
    List<StatusVo>  queryStatusDib();

    /**
     * 根据省份查
     * @return
     */
    List<StatusVo>  queryStatusDibByPro(@Param("province")String province);


    /**
     * 查询设备列表数据(分页)
     * @return
     */
    List<DeviceVo>  queryDeviceList(@Param("p") DeviceInfoDto dto);


    /**
     * 查询设备列表数据(不分页)
     * @return
     */
    List<DeviceVo>  queryDeviceAllList(@Param("p") DeviceInfoDto dto);

}