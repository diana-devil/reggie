package com.diana.dto;

import com.diana.pojo.Dish;
import com.diana.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于封装页面提交的数据
 *
 * dto   即数据传输对象
 * 一般用于展示层与服务层之间的数据传输
 *
 */
@Data
public class DishDto extends Dish {

    //口味列表
    private List<DishFlavor> flavors = new ArrayList<>();

    //菜品分类名称  Dish里面是id
    private String categoryName;

    //
    private Integer copies;
}
