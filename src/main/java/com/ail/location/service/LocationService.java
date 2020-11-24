package com.ail.location.service;

import com.ail.location.model.dto.LocationDto;
import com.ail.location.model.gis.GisResult;
import com.ail.location.model.mongo.Location;
import com.ail.location.model.vo.TrailVo;

import java.util.List;

/**
 * <p>Title： LocationService </p>
 * <p>Description： </p>
 * <p>Company：ail </p>
 *
 * @author sujunxuan
 * @version V1.0
 * @date 2020/1/13 15:50
 */
public interface LocationService {



    /**
     * 获取车辆最新定位(xinya服务接口)
     *
     * @param dto
     * @return 最新定位信息
     */
    Location xinYaLastLocation(LocationDto dto);



    /**
     * 将三方数据写入mongo
     *
     * @param result 定位数据
     * @return 定位数据
     */
     void addMongoData(List<GisResult> result);



    /**
     * 查询车辆轨迹和总里程数
     *
     * @param dto
     * @return 定位数据
     */
    TrailVo queryTrailAndMileage(LocationDto dto);



    /**
     * 根据车牌号集合查询车辆定位集合
     * @param carNos
     * @return
     */
    List<Location> queryLocationList(List<String> carNos);


    /**
     * 获取车辆最新定位
     *
     * @param dto
     * @return 最新定位信息
     */
    Location queryLatestLocation(LocationDto dto);

}
