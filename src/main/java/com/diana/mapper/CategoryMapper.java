package com.diana.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diana.pojo.Category;
import org.apache.ibatis.annotations.Mapper;


/**
 * 菜品种类，套餐种类
 * mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
