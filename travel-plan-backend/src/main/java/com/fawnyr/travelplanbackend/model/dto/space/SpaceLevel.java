package com.fawnyr.travelplanbackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 查询空间级别
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
