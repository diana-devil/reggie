package com.diana.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.diana.dto.SetmealDto;
import com.diana.pojo.Category;
import com.diana.pojo.Setmeal;

import java.util.List;

/**
 * 套餐
 * service
 */
public interface SetmealService extends IService<Setmeal> {

    //对套餐进行分页查询，得到分页对象
    public Page<SetmealDto> getMealByPage(int page,int pageSize,String name );

    //新增套餐信息，涉及到setmeal 套餐表，和setmeal_dish 套餐内部菜品表
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐  涉及到setmeal 套餐表，和setmeal_dish 套餐内部菜品表
//    public void deleteWithDish(Long[] ids); //自己实现的方法
    public void deleteWithDish(List<Long> ids);//老师实现的方法

    //修改套餐 的数据回显功能
    public SetmealDto getMealAndDish(Long id);

    //提交修改套餐的数据，并修改套餐
    public void updateMealAndDish(SetmealDto setmealDto);
}
