package com.ail.location.commom.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static String xinya_xsungroupurl;

    @Value("${xinya.xsungroupurl}")
    public void setXinyaXsungroupurl(String url) {
        this.xinya_xsungroupurl = url;
    }


    public static String sendPostJson(Map<String, String> paramMap, String url) {
        String resultUtil = "";
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            String gpsUrl = xinya_xsungroupurl + url;
            HttpPost post = new HttpPost(gpsUrl);
            post.setHeader("Content-Type", "application/json;charset=UTF-8");
            post.setHeader("Connection", "Close");
            String jsonString = JSON.toJSONString(paramMap);
            StringEntity se = new StringEntity(jsonString, Charset.forName("UTF-8"));
            se.setContentEncoding("UTF-8");
            se.setContentType("application/json");
            post.setEntity(se);
            CloseableHttpResponse response = client.execute(post);
            resultUtil = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(resultUtil);
            if (null != jsonObject.getString("data")) {
                resultUtil = jsonObject.getString("data");
            } else {
                resultUtil = "";
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultUtil;
    }


    public static String sendPost(Map<String, String> paramMap, String url) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        String resultUtil = "";
        try {
            String gpsUrl = xinya_xsungroupurl + url;
            HttpPost post = new HttpPost(gpsUrl);
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
            resultUtil = EntityUtils.toString(entity, "utf-8");
            JSONObject jsonObject = JSON.parseObject(resultUtil);
            if (null != jsonObject.getString("data")) {
                resultUtil = JSON.parseObject(jsonObject.getString("data")).toJSONString();
            } else {
                resultUtil = "";
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return resultUtil;
    }


    public static String sendGet(Map<String, String> paramMap, String url) {
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        String resultUtil = "";
        String gpsUrl = xinya_xsungroupurl + url;
        System.out.println("gpsUrl" + "--------------------------" + gpsUrl);
        try {
            URIBuilder uriBuilder = new URIBuilder(gpsUrl);
            /** 第二种添加参数的形式 */
            List<NameValuePair> list = new LinkedList<>();
            for (String key : paramMap.keySet()) {
                list.add(new BasicNameValuePair(key, paramMap.get(key)));
            }
            uriBuilder.setParameters(list);
            // 根据带参数的URI对象构建GET请求对象
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            resultUtil = EntityUtils.toString(entity, "utf-8");
            JSONObject jsonObject = JSON.parseObject(resultUtil);
            if (null != jsonObject.getString("data")) {
                resultUtil = JSON.parseObject(jsonObject.getString("data")).toJSONString();
            } else {
                resultUtil = "";
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return resultUtil;
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
