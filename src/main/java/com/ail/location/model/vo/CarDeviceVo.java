package com.ail.location.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 车辆表
 * </p>
 *
 * @author Carlos
 * @since 2019-08-06
 */
@Getter
@Setter
@ToString
public class CarDeviceVo implements Serializable {

    private static final long serialVersionUID = 1L;


    private List<CarVo> list;


}
