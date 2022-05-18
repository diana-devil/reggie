package com.diana.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.dto.DishDto;
import com.diana.mapper.CategoryMapper;
import com.diana.mapper.DishMapper;
import com.diana.pojo.Category;
import com.diana.pojo.Dish;
import com.diana.pojo.DishFlavor;
import com.diana.service.CategoryService;
import com.diana.service.DishFlavorService;
import com.diana.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品
 * service
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新增菜品，同时插入菜品和对应的口味数据，需要操作两张表：dish,dish_flavor
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto){
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //获取 dish_id
        Long disID = dishDto.getId(); //获取父类-菜品的id

        //将集合中每个dishId赋值--for循环方法
//        for(DishFlavor dishFlavor:dishDto.getFlavors()){
//            dishFlavor.setDishId(disID);
//        }
        //将集合中每个dishId赋值--steam流方法
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map((item)->{
            item.setDishId(disID);
            return item;
        }).collect(Collectors.toList());


        //保存菜品口味数据到菜品口味表 dish_flavor
        //批量保存
        dishFlavorService.saveBatch(flavors);
    }


    /**
     *  根据id来查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id){

        //查询出菜品基本信息
        Dish dish = this.getById(id);

        //查询出口味列表
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);

        //进行对象封装
        DishDto dishDto=new DishDto(); //新建dishDto对象
        BeanUtils.copyProperties(dish,dishDto);//copy父类dish的属性
        dishDto.setFlavors(dishFlavors);//封装自己独有的口味信息

        return dishDto;

    }






    /**
     * 更新菜品信息和口味信息 需要操作两张表：dish,dish_flavor
     * 将原来的口味信息 全部删除，在保存提交过来的新的口味信息，这样比直接修改要简单的多
     * @param dishDto
     */
//    @Transactional
    public void updateWithFlavor(DishDto dishDto){
        //更新菜品信息
        this.updateById(dishDto);

        //得到dish_id
        Long dishId = dishDto.getId();

        //删除原来的口味信息 ----删除dish-对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(queryWrapper);

        //添加提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //补充flavors中的dish_id
        flavors=flavors.stream().map((item)->{
            //将菜品口味自带的id置为null，save方法去重写生成id，这样配合逻辑删除使用，否则会造成主键冲突
            item.setId(null);
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //更新菜品口味信息
        dishFlavorService.saveBatch(flavors);
    }

}
