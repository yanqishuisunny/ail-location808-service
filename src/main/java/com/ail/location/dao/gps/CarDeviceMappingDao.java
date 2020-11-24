package com.ail.location.dao.gps;

import com.ail.location.model.dto.CarDeviceMappingDto;
import com.ail.location.model.entity.CarDeviceMappingEntity;
import com.ail.location.model.vo.DeviceVo;
import com.ail.location.model.vo.ProDistributionVo;
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
public interface CarDeviceMappingDao extends BaseMapper<CarDeviceMappingEntity> {

    List<DeviceVo> queryDeviceInformation(@Param("p") CarDeviceMappingDto dto);

}