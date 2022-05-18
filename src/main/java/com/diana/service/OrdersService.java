package com.diana.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.diana.pojo.Orders;

public interface OrdersService extends IService<Orders> {

    //提交订单
    public void submit(Orders orders);
}
