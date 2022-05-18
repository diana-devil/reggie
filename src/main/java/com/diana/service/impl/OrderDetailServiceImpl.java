package com.diana.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.OrderDetailMapper;
import com.diana.pojo.OrderDetail;
import com.diana.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
