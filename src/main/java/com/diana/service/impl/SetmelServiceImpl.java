package com.diana.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.common.exception.CustomException;
import com.diana.dto.SetmealDto;
import com.diana.mapper.CategoryMapper;
import com.diana.mapper.SetmealMapper;
import com.diana.pojo.Category;
import com.diana.pojo.Setmeal;
import com.diana.pojo.SetmealDish;
import com.diana.service.CategoryService;
import com.diana.service.SetmealDishService;
import com.diana.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品
 * service
 */
@Slf4j
@Service
public class SetmelServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired  //这里注入mapper 调用mapper 的方法，因为注入service 会发生循环调用
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 对套餐进行分页查询，得到分页对象
     *
     * @param page 当前页
     * @param pageSize 每页显示数量
     * @param name 套餐名称 进行模糊查询
     * @return 返回分页对象
     */
    public Page<SetmealDto> getMealByPage(int page, int pageSize, String name ){
        //新建Page<Setmeal>对象
        Page<Setmeal> pageinfo=new Page<>(page,pageSize);
        //查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //分页查询
        this.page(pageinfo,queryWrapper);


        //新建Page<SetmealDto>对象，不光要继承原page对象的属性，还有加入categoryName
        Page<SetmealDto> dtoPage=new Page<>();
        BeanUtils.copyProperties(pageinfo,dtoPage,"records");//复制原属性，排除records
        List<Setmeal> records = pageinfo.getRecords();//得到原records对象
        List<SetmealDto> setmealDtos=records.stream().map((item)->{
            //得到 categoryName
            Long categoryId = item.getCategoryId();
            Category category = categoryMapper.selectById(categoryId);
            String categoryName = category.getName();
//            String categoryName ="儿童套餐";

            //将 categoryName 存入对象SetmealDto,并将records中其他属性copy到该对象中
            SetmealDto setmealDto=new SetmealDto();
            setmealDto.setCategoryName(categoryName);
            BeanUtils.copyProperties(item,setmealDto);

            return setmealDto;
        }).collect(Collectors.toList());

        //将setmealDtos作为dtoPage的records
        dtoPage.setRecords(setmealDtos);
        return dtoPage;
    }


    /**
     * 新增套餐信息，涉及到setmeal 套餐表，和setmeal_dish 套餐内部菜品表
     *
     * @param setmealDto dto对象 其继承了父类Setmeal的全部属性，又封装了菜系名称和菜品列表
     */
    public void saveWithDish(SetmealDto setmealDto){
        //将其继承的父类属性，保存到套餐分类 表setmeal
        this.save(setmealDto);

        //得到setmeal_id
        Long setmealId = setmealDto.getId();

        //需要设置 setmeal_id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

//        log.info(setmealDishes.toString());
        //将套餐对应的菜品信息存入setmeal_dish表中
        setmealDishService.saveBatch(setmealDishes);
    }



//    /**
//     * 删除套餐1
//     * 删除套餐前应该先删除表stemeal_dish中对应的数据
//     *然后在删除 表setmeal的数据， 开事务
//     * @param ids
//     * @return
//     */
//    @Transactional
//    public void deleteWithDish(Long[] ids){
//        for (Long id:ids){
//            //删除表 stemeal_dish 中套餐对应的菜品信息
//            LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
//            queryWrapper.eq(SetmealDish::getSetmealId,id);
//            setmealDishService.remove(queryWrapper);
//            //删除表 stemeal 中的套餐信息
//            this.removeById(id);
//        }
//
//    }

    /**
     * 删除套餐1
     * 删除前，判断一下菜品是否在售卖中，如果没有在售卖在删除
     * 删除套餐前应该先删除表stemeal_dish中对应的数据
     *然后在删除 表setmeal的数据， 开事务
     * @param ids
     * @return
     */
    @Transactional
    public void deleteWithDish(List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //查询状态为1 的且 id在被批量删除的ids中的
        queryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,1);
        //查询满足上述要求的数量
        long count = this.count(queryWrapper);
        if(count>0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中,不能删除！");
        }
        //能删除，删除表setmeal,直接批量删除
        this.removeByIds(ids);
        //删除表 setmeal_dish ,直接批量删除
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);

    }


    /**
     * 修改套餐的数据回显功能
     * @param id
     */
    public SetmealDto getMealAndDish(Long id){
        //创建一个setmealDto 对象
        SetmealDto setmealDto=new SetmealDto();

        //1、查询setmeal表,并封装到dto对象中
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal,setmealDto);

        //2、查询setmeal_dish表，并封装到dto对象中
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;


    }




    /**
     * 提交修改套餐的数据，并修改套餐
     * @param setmealDto
     */
    public void updateMealAndDish(SetmealDto setmealDto){

        //修改父类的属性信息
        this.updateById(setmealDto);

        //得到setmeal_id
        Long setmealId = setmealDto.getId();

        //删除原有的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);


        //需要设置 setmeal_id,同时将 id设为null
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes=setmealDishes.stream().map((item)->{
            item.setId(null);
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());


        //将套餐对应的菜品信息存入setmeal_dish表中
        setmealDishService.saveBatch(setmealDishes);

    }




}
