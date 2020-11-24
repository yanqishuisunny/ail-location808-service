package com.ail.location.commom.cache;

public interface CachePre {

    /**
     * 登录jwt
     */
    String LOING_SHIRO_JWT_ID = "login:shiro:jwt:id:";

    /**
     * gps定位数据集合
     */
    String GPS_LOCATION_LIST_RESULT = "gps:location:list:restlt";

    /**
     * 车辆最后定位数据
     */
    String VECHILES_LOCATION_LAST = "gps:vechiles:location";




}
