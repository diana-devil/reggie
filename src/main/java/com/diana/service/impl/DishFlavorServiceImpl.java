package com.diana.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.DishFlavorMapper;
import com.diana.pojo.DishFlavor;
import com.diana.service.DishFlavorService;
import com.diana.service.DishService;
import org.springframework.stereotype.Service;

/**
 * 菜品口味
 * service
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
