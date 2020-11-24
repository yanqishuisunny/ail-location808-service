package com.ail.location.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 设备表
 * </p>
 *
 * @author Carlos
 * @since 2020-01-13
 */
@Data
@ApiModel
@ToString
@EqualsAndHashCode(callSuper = false)
public class LocationDto implements Serializable {

    /**
     * 车牌号
     */
    @ApiModelProperty("车牌号")
    private String vehicleNo;

    /**
     * 设备IMEI
     */
    @ApiModelProperty("设备IMEI")
    private String imei;


    /**
     * 车牌号列表
     */
    @ApiModelProperty("车牌号列表")
    private List<String> carNos;


    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;


    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;

}
