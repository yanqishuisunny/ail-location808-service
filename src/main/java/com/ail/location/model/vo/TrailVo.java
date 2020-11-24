package com.ail.location.model.vo;

import com.ail.location.model.mongo.Location;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TrailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总里程数
     */
    @ApiModelProperty("总里程数")
    private String mileage;

    /**
     * 行驶轨迹
     */
    @ApiModelProperty("行驶轨迹")
    private List<Location> LocationList;

}
