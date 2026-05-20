package com.liang.bbs.common.distributedid;

import lombok.Data;

@Data
public class SnowflakeIdResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
