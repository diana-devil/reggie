package com.diana.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.common.exception.CustomException;
import com.diana.mapper.CategoryMapper;
import com.diana.pojo.Category;
import com.diana.pojo.Dish;
import com.diana.pojo.Setmeal;
import com.diana.service.CategoryService;
import com.diana.service.DishService;
import com.diana.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 菜品种类，套餐种类
 * service
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {



    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 根据id删除分类，删除之前需要判断
     * @param id
     */
    public void remove(Long id){

        //查询当前分类是否关联了菜品，如果关联了，则抛出异常
        LambdaQueryWrapper<Dish> queryWrapper_Dish=new LambdaQueryWrapper<>();
        queryWrapper_Dish.eq(Dish::getCategoryId,id);
        long count1 = dishService.count(queryWrapper_Dish);
        log.info("{}",count1);
        if(count1>0){
            //抛出业务异常
            throw new CustomException("所选分类关联了菜品，不能删除！");
        }
        //查询当前分类是否关联了套餐，如果关联了，则抛出异常
        LambdaQueryWrapper<Setmeal> queryWrapper_Setmeal=new LambdaQueryWrapper<>();
        queryWrapper_Setmeal.eq(Setmeal::getCategoryId,id);
        long count2 = dishService.count(queryWrapper_Dish);
        log.info("{}",count2);
        if(count2>0){
            //抛出业务异常
            throw new CustomException("所选分类关联了套餐，不能删除！");
        }
        //逻辑删除分类
        super.removeById(id);//调用父类————categoryService.removeById(id);
    }

}
