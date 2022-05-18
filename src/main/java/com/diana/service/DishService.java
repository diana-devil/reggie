package com.diana.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.diana.dto.DishDto;
import com.diana.pojo.Category;
import com.diana.pojo.Dish;

/**
 * 菜品
 * service
 */
public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品和对应的口味数据，需要操作两张表：dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id来查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息和口味信息 需要操作两张表：dish,dish_flavor
    public void updateWithFlavor(DishDto dishDto);
}
