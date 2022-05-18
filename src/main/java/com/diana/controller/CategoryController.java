package com.diana.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diana.common.R;
import com.diana.pojo.Category;
import com.diana.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * 1-菜品分类
     * 2-套餐分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> addCategory(@RequestBody Category category){
        log.info(category.toString());
        categoryService.save(category);
        return R.success("新增分类成功");
    }


    /***
     * 分类管理
     * 分页查询
     * @return
     */
    @GetMapping("/page")
    public R<IPage<Category>> getCategoryByPage(int page, int pageSize){
        log.info("{},{}",page,pageSize);

        //创建分页对象   传入 当前页page    当前页面大小 pageSize
        IPage<Category> page1=new Page<Category>(page,pageSize);

        //定义查询条件
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort)
                    .orderByAsc(Category::getUpdateTime); //按排名排序,排名一样的按修改时间排序，都是生序
        //调用分页查询方法
        IPage<Category> page2 = categoryService.page(page1, queryWrapper);

        log.info(page2.toString());

        return R.success(page2);
    }


    /**
     * 删除分类--在service层使用了
     * 需要使用逻辑外键，判断分类是否关联了菜品，
     * @return
     */
    @DeleteMapping
    public R<String> deleteCategory(Long ids){
        categoryService.remove(ids);
        return R.success("删除成功！！");
    }


    /**
     * 通过前端vue框架 实现了数据回显
     *  修改分类
     * @return
     */
    @PutMapping
    public R<String> updateCategory(@RequestBody Category category){
        log.info(category.toString());

        if (categoryService.updateById(category)){
            return R.success("修改成功！");
        }else{
            return R.error("修改失败！");
        }

    }


    /**
     * 套餐管理中添加套餐 的套餐分类查询 根据传入的type查询套餐的分类
     * 复用： 移动端展示 菜系和套餐
     * 根据传入type类型  查询分类列表
     * @param category  使用菜品对象，后期复用性强，主要获取菜品类别 1表示菜品分类，2表示套餐分裂
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> getCategoryList(Category category){
        //构造分类条件
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        //添加分类条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());//查询type等值
        //添加排序条件-- 排名从小到大（升序排名）， 因为1最高，-- 修改时间，从高到低（降序排列），因为最近修改的值大
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //调用list方法  获取数据   记住！！！！
        List<Category> list = categoryService.list(queryWrapper);

        return  R.success(list);
    }








}
