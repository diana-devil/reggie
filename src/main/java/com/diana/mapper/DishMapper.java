package com.diana.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diana.pojo.Category;
import com.diana.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;


/**
 * 菜品
 * mapper
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
