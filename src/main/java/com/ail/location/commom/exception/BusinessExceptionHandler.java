package com.ail.location.commom.exception;

import com.ail.location.commom.core.IBusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class BusinessExceptionHandler {

    private static  final Logger logger= LoggerFactory.getLogger(BusinessExceptionHandler.class);

     @ExceptionHandler(BusinessException.class)
     public RestResult businessExceptionHandler(BusinessException ex){

         IBusCode iBusCode=ex.getBusCode();
        if(ex.getBusCode()==null){
            logger.warn("------>BusinessException code:{},message:{}",ex.getCode(),ex.getMessage());
            return RestResultBuider.builder().failure("FAILURE").message(ex.getMessage()).build();
        }
        logger.warn("------>BusinessException code:{},message:{}",iBusCode.getCode(),iBusCode.getMsg());
        return RestResultBuider.builder().errorCode(ex.getBusCode()).build();
    }

}
