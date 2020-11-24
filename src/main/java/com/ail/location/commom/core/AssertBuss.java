package com.ail.location.commom.core;

import com.ail.location.commom.exception.BusinessException;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * @author Carlos
 * 创建日期： 2019/8/7
 * 创建时间： 13:41
 * @version 1.0
 * @since 1.0
 */
public class AssertBuss {

    public static void notNull(@Nullable Object object, IBusCode busCode) {
        if (object == null) {
            throw new BusinessException(busCode);
        }
    }

}
