package com.diana.common.exception;

import com.diana.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;


/**
 *
 * 全局异常处理类
 */

//拦截加了这些controller的类
@RestControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {

    /***
     * SQL
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler_SQL(SQLIntegrityConstraintViolationException ex){

       log.error(ex.getMessage());

       if(ex.getMessage().contains("Duplicate entry")){
           String username = ex.getMessage().split(" ")[2];
            username = username.substring(1, username.length()-1);
           return R.error(username+"已存在");
       }

       return R.error("其他类型错误");
    }

    /**
     * 捕获 自定义异常
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler_Cus(CustomException ex){

        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }















}
