package com.diana.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.SetmealDishMapper;
import com.diana.pojo.SetmealDish;
import com.diana.service.SetmealDishService;
import org.springframework.stereotype.Service;


/**
 * 套餐对应菜品 信息
 */
@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
