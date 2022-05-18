package com.diana.common.exception;

/**
 * 自定义业务异常
 *
 * 当删除分类包含菜品或者套餐的时候，抛出
 */
public class CustomException extends RuntimeException{

    public CustomException(String message){
        super(message);
    }
}
