package com.diana.service;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.diana.pojo.Category;

/**
 * 菜品种类，套餐种类
 * service
 */
public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
