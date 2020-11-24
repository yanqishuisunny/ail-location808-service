package com.ail.location.commom.utils;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.core.IBusCode;

/**
 * @description: 返回消息对象封装
 * @author: Carlos
 * @Date: 2019/7/8 15:23
 */
public class ResponseUtil {

    private ResponseUtil(){}
    /**
     * @description: 返回成功消息体
     * @param: Object 获取的数据
     * @return: ResponseInfo
     * @author: kingJing
     * @date: 2019/7/8 15:26
     **/
    public static <T> ResponseInfo<T> success(T object) {
        ResponseInfo<T> respInfo = new ResponseInfo<>();
        respInfo.setCode(BusCode.SUCCESS.getCode());
        respInfo.setMessage(BusCode.SUCCESS.getMsg());
        respInfo.setData(object);
        return respInfo;
    }

    public static ResponseInfo success() {
        return success(null);
    }

    /**
     * @description: 返回错误信息消息体
     * @param: messge 消息信息
     * @return: ResponseInfo
     * @author: kingJing
     * @date: 2019/7/8 15:30
     **/
    public static ResponseInfo error(String messge) {
        ResponseInfo respInfo = new ResponseInfo();
        respInfo.setCode(BusCode.FAILURE.getCode());
        respInfo.setMessage(messge);
        return respInfo;
    }


    /**
     * @description: 返回错误信息消息体
     * @param: messge 消息信息
     * @return: ResponseInfo
     * @author: kingJing
     * @date: 2019/7/8 15:30
     **/
    public static ResponseInfo error() {
        ResponseInfo respInfo = new ResponseInfo();
        respInfo.setCode(BusCode.FAILURE.getCode());
        respInfo.setMessage(BusCode.FAILURE.getMsg());
        return respInfo;
    }

    public static ResponseInfo result(boolean flg) {
        return flg ? ResponseUtil.success() : ResponseUtil.error();
    }

    public static ResponseInfo result(Object obj) {
        ResponseInfo<Object> respInfo = new ResponseInfo<>();
        respInfo.setCode(BusCode.SUCCESS.getCode());
        respInfo.setMessage(BusCode.SUCCESS.getMsg());
        respInfo.setData(obj);
        return respInfo;
    }

    public static <T>  ResponseInfo<T> result(IBusCode code) {
        ResponseInfo<T> respInfo = new ResponseInfo<>();
        respInfo.setCode(code.getCode());
        respInfo.setMessage(code.getMsg());
        return respInfo;
    }

    public static <T>  ResponseInfo<T> result(IBusCode code,String msg) {
        ResponseInfo<T> respInfo = new ResponseInfo<>();
        respInfo.setCode(code.getCode());
        respInfo.setMessage(msg);
        return respInfo;
    }

    public static <T>  ResponseInfo<T> result(IBusCode code,T t) {
        ResponseInfo<T> respInfo = new ResponseInfo<>();
        respInfo.setCode(code.getCode());
        respInfo.setMessage(code.getMsg());
        respInfo.setData(t);
        return respInfo;
    }


    public static <T>  ResponseInfo<T> resultSuccess(T t) {
        ResponseInfo<T> respInfo = new ResponseInfo<>();
        respInfo.setCode(BusCode.SUCCESS.getCode());
        respInfo.setMessage(BusCode.SUCCESS.getMsg());
        respInfo.setData(t);
        return respInfo;
    }


}
