package com.diana.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diana.common.R;
import com.diana.pojo.Orders;
import com.diana.pojo.ShoppingCart;
import com.diana.service.OrdersService;
import com.diana.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {


    @Autowired
    private OrdersService ordersService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 提交订单
     * 点击去支付按钮后生效
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info(orders.toString());
        ordersService.submit(orders);

        return R.success("下单成功！");
    }
}
