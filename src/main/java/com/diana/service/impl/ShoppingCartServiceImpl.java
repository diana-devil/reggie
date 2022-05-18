package com.diana.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.ShoppingCartMapper;
import com.diana.pojo.ShoppingCart;
import com.diana.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
