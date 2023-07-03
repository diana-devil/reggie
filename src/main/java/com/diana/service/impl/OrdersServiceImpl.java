package com.diana.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.common.BaseContext;
import com.diana.common.exception.CustomException;
import com.diana.mapper.OrdersMapper;
import com.diana.pojo.*;
import com.diana.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService{

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * /提交订单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders){
        //查询用户id
        Long userId = BaseContext.getCurrentId();


        //补充用户数据
        User user = userService.getById(userId);
//        if(user.getName()!=null){
//            orders.setUserName(user.getName());//用户名
//        }
//        if(user.getPhone()!=null){
//            orders.setPhone(user.getPhone());//手机号
//        }


        //补充地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook==null){
            //仿小人，为空不能下单
            throw  new CustomException("用户地址信息有误，不能下单");
        }
        orders.setConsignee(addressBook.getConsignee()); //收获人
        orders.setAddress((addressBook.getProvinceName()==null ? "":addressBook.getProvinceName())
                +(addressBook.getCityName()==null ? "":addressBook.getCityName())
                +(addressBook.getDistrictName()==null? "" :addressBook.getDistrictName())
                +(addressBook.getDetail()==null? "":addressBook.getDetail()));//详细地址


        //查询当前购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts==null || shoppingCarts.size()==0){
            //仿小人，为空不能下单
            throw  new CustomException("购物车为空,不能下单");
        }


//        //补充钱数
//        float num=0;//总钱数
//        for(ShoppingCart shoppingCart:shoppingCarts){
//            Integer number = shoppingCart.getNumber();
//            float value = shoppingCart.getAmount().floatValue();
//            num=num+number*(value);
//        }
//        log.info("{}",num);
//        orders.setAmount(BigDecimal.valueOf(num));


        //补充基本信息

        orders.setUserId(userId);//用户id
        long orderId = IdWorker.getId();
        orders.setNumber(String.valueOf(orderId));//订单号
        orders.setId(orderId);//直接将订单号设置为id  不用自己生成
        orders.setStatus(2);//2表示代派送
        orders.setOrderTime(LocalDateTime.now());//下单时间
        orders.setCheckoutTime(LocalDateTime.now());//支付时间


        //计算钱数及补充订单详情信息
        AtomicInteger amount=new AtomicInteger(0);//原子操作，可以保证并发安全
        List<OrderDetail> orderDetails=shoppingCarts.stream().map((item)->{
            //设置订单详情
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setOrderId(orderId);//设置订单id
            //这个方法会copy所有相同的属性，自动忽略没有的属性，不会报错
            //这里item 多了id，user_Id,create_time, 在复制的时候自动忽略了
            //这里orderDetail 少了 order_id,id
            BeanUtils.copyProperties(item,orderDetail,"id");//忽略id，让其自动生成
            log.info(orderDetail.toString());

            //顺便计算钱数
            //钱数用 BigDecimal 类型  amount是这个类型，将number转成这个类型
            //乘完之后，用 intValue获取值，在用addAndGet 自增赋值给amount
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());

            return orderDetail;
        }).collect(Collectors.toList());
        
        //将订单信息保存
        orders.setAmount(new BigDecimal(amount.get()));//得到数值，转换成BigDecimal类型
        this.save(orders);


        //将订单详情保存
        orderDetailService.saveBatch(orderDetails);

        //删除购物车信息
        shoppingCartService.remove(queryWrapper);

    }
}
