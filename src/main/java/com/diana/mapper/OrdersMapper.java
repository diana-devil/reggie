package com.diana.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diana.pojo.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
