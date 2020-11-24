package com.ail.location.schedule;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.exception.BusinessException;
import com.ail.location.commom.gpsUtils.SignUtils;
import com.ail.location.model.entity.SupplierInfoEntity;
import com.ail.location.model.gis.GisData;
import com.ail.location.model.gis.GisResult;
import com.ail.location.service.LocationService;
import com.ail.location.service.impl.SupplierInfoServiceImpl;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class CoordinateScheduler {

    @Autowired
    private LocationService locationService;

    @Autowired
    private SupplierInfoServiceImpl supplierInfoServiceImpl;

    private static final String jimi_penapi_url = "http://open.aichezaixian.com/route/rest";

    private static final String jimi_login_token_url = "jimi.oauth.token.get";

    private static final String jimi_new_location_url = "jimi.device.location.get";

    private static final String jimi_all_location_url = "jimi.user.device.location.list";

    private static final String jimi_need_sign = "1.0";

    private static final String jimi_not_need_sign = "0.9";


    @Value("${jimi.expires_in}")
    private String jimi_expires_in;


    @Value("${jimi.mapType_google}")
    private String jimi_mapType_google;

    @Value("${scheduler.close}")
    private boolean scheduler_close;


    /**
     * 拉取车辆定位信息写入mongo
     */
    @Scheduled(cron = "${scheduler.job1}")
    public void runTimer() {
        if(scheduler_close){
            return;
        }
        //查询几米供应商
        Wrapper supplierWrapper = new EntityWrapper<SupplierInfoEntity>();
        supplierWrapper.eq("supplier_code","jimi001");
        supplierWrapper.eq("enable",1);
        SupplierInfoEntity supplierInfoEntity = supplierInfoServiceImpl.selectOne(supplierWrapper);
        if(null == supplierInfoEntity){
            return;
        }
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> paramMap = new HashMap<String, String>();
        // 公共参数
        paramMap.put("app_key", supplierInfoEntity.getAppKey());
        paramMap.put("v", jimi_need_sign);
        paramMap.put("timestamp", getCurrentDate());
        paramMap.put("sign_method", "md5");
        paramMap.put("format", "json");
        paramMap.put("method", jimi_login_token_url);
        // 私有参数
        paramMap.put("user_id", supplierInfoEntity.getUserName());
        paramMap.put("user_pwd_md5", DigestUtils.md5Hex(supplierInfoEntity.getUserPwd()));
        paramMap.put("expires_in", jimi_expires_in);
        // 计算签名
        String sign = "";
        try {
            sign = SignUtils.signTopRequest(paramMap, supplierInfoEntity.getAppSecret(), "md5");
            paramMap.put("sign", sign);
        } catch (IOException e) {
            System.err.println(e);
        }
        //入参
        log.info("获取登录数据入参:" + JSONObject.toJSONString(paramMap));
        GisData gisData = sendPost(headerMap, paramMap);
        //出参
        log.info("获取登录数据出参:" + JSONObject.toJSONString(gisData));
        if (null == gisData.getResult() || gisData.getResult().isEmpty()) {
            throw new BusinessException(BusCode.LOGIN_JIMI_ERROE);
        }
        Map<String, String> gisParamMap = new HashMap<String, String>();
        // 公共参数
        gisParamMap.put("app_key", supplierInfoEntity.getAppKey());
        gisParamMap.put("v", jimi_need_sign);
        gisParamMap.put("timestamp", getCurrentDate());
        gisParamMap.put("sign_method", "md5");
        gisParamMap.put("format", "json");
        gisParamMap.put("method", jimi_all_location_url);
        // 私有参数
        gisParamMap.put("user_id", supplierInfoEntity.getUserName());
        gisParamMap.put("user_pwd_md5", DigestUtils.md5Hex(supplierInfoEntity.getUserPwd()));
        gisParamMap.put("expires_in", jimi_expires_in);
        gisParamMap.put("access_token", gisData.getResult().get(0).getAccessToken());
        gisParamMap.put("target", supplierInfoEntity.getUserName());
        gisParamMap.put("map_type", jimi_mapType_google);
        String gitSign = "";
        try {
            gitSign = SignUtils.signTopRequest(gisParamMap, supplierInfoEntity.getAppSecret(), "md5");
            gisParamMap.put("sign", gitSign);

        } catch (IOException e) {
            System.err.println(e);
        }
        //入参
        log.info("获取定位数据入参:" + JSONObject.toJSONString(gisParamMap));
        gisData = sendPost(headerMap, gisParamMap);
        //数据写入mongo数据库中
        if (!CollectionUtils.isEmpty(gisData.getResult())) {
            locationService.addMongoData(gisData.getResult());
        }
    }


    private static GisData sendPost(Map<String, String> headerMap, Map<String, String> paramMap) {
        List<GisResult> result = new ArrayList<>();
        GisData gisData = new GisData();
        try {
            HttpPost post = new HttpPost(jimi_penapi_url);
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (String key : paramMap.keySet()) {
                list.add(new BasicNameValuePair(key, paramMap.get(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            if (null != headerMap) {
                post.setHeaders(assemblyHeader(headerMap));
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            String resultutil = EntityUtils.toString(entity, "utf-8");
            gisData = JSONObject.parseObject(resultutil, GisData.class);// jsonStr 是String类型。
        } catch (IOException e) {
            System.err.println(e);
        }
        return gisData;
    }


    /**
     * 组装头部信息
     *
     * @param headers
     * @return
     */
    private static Header[] assemblyHeader(Map<String, String> headers) {
        Header[] allHeader = new BasicHeader[headers.size()];
        int i = 0;
        for (String str : headers.keySet()) {
            allHeader[i] = new BasicHeader(str, headers.get(str));
            i++;
        }
        return allHeader;
    }


    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = formatter.format(new Date());
        return result;
    }


}
