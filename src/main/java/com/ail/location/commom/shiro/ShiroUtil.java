package com.ail.location.commom.shiro;

import com.ail.location.commom.core.AssertBuss;
import com.ail.location.commom.core.BusCode;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;

/**
 * shiro 用户登录
 *
 * @author helihui
 */

@Slf4j
public class ShiroUtil {

    /**
     * 获取当前登录人id
     * @return
     */
    public static String currentUserId(){
        return (String) SecurityUtils.getSubject().getPrincipal();
    }


    /**
     * 获取当前登录人id
     * @return
     */
    public static String getUserId(){
        String userId = currentUserId();
        if(Strings.isNullOrEmpty(userId)){
            log.warn("用户ID为空，请检查方式是否经过shiro 验证");
        }
        AssertBuss.notNull(userId, BusCode.TOKEN_LOSE);
        return userId;
    }

}
