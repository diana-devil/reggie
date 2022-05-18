package com.diana.common;

/**
 * 基于ThreadLocal 封装工具类，用户保存和获取当前登陆用户的id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();

    public static  void  setThreadLocal(Long id){
        threadLocal.set(id);
    }

    public static  Long getCurrentId(){
        return  threadLocal.get();
    }
}
