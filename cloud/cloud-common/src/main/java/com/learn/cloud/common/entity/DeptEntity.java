package com.learn.cloud.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author will
 */
@Data
public class DeptEntity implements Serializable {
    private static final long serialVersionUID = -1369760518990842409L;

    private Integer id = 1;

    private Date createTime = new Date();

}
