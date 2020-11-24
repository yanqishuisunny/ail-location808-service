package com.ail.location.commom.exception;

import com.ail.location.commom.core.BusCode;
import com.ail.location.commom.core.IBusCode;

public class BusinessException extends RuntimeException{
    protected int code = 500;

    protected IBusCode busCode;

    public IBusCode getCode() {
        return this.busCode;
    }

    public IBusCode getBusCode() {
        return this.busCode;
    }

    public BusinessException(IBusCode busCode) {
        super(formatMsg(busCode));
        this.code=busCode.getCode();
        this.busCode = busCode;

    }
    public BusinessException(Throwable cause, IBusCode busCode) {
        super(formatMsg(busCode),cause);
        this.code=busCode.getCode();
        this.busCode = busCode;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = BusCode.FAILURE.getCode();
    }


    public final static String formatMsg(IBusCode busCode){
        return String.format("%s-%s",busCode.getCode(),busCode.getMsg());

    }

    public String formatMsg(String message){
        return String.format("%s-%s",this.code,message);
    }

    public String formatMsg(int code, String message){
        return String.format("%s-%s",code,message);
    }

}
