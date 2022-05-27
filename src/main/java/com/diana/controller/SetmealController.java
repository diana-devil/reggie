package com.diana.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diana.common.R;
import com.diana.dto.SetmealDto;
import com.diana.pojo.Category;
import com.diana.pojo.Setmeal;
import com.diana.pojo.SetmealDish;
import com.diana.service.CategoryService;
import com.diana.service.SetmealDishService;
import com.diana.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@Api(tags = "套餐接口")
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     *  套餐分类 分页查询
     * @param page  当前页
     * @param pageSize 页面显示数量
     * @param name 套餐名称
     * @return  R<Page<SetmealDto>>
     */
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page" ,value = "当前页数", required = true),//说明参数
            @ApiImplicitParam(name="pageSize" ,value = "每页显示个数", required = true),//说明参数
            @ApiImplicitParam(name="name" ,value = "套餐名称", required = false)//说明参数
    })//说明多个参数
    public R<Page<SetmealDto>> getMealByPage(int page,int pageSize,String name){

        //调用service层方法，完成分页查询
        Page<SetmealDto> mealByPage = setmealService.getMealByPage(page, pageSize, name);

        return R.success(mealByPage);
    }


    /**
     *  新增套餐信息，涉及到setmeal 套餐表，和setmeal_dish 套餐内部菜品表
     * @param setmealDto  dto对象 其继承了父类Setmeal的全部属性，又封装了菜系名称和菜品列表
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)//删除当前分类下所有缓存
    @PostMapping
    @ApiOperation(value = "新增套装接口")
    @ApiImplicitParam(name="setmealDto" ,value = "套餐dto对象", required = true)//说明参数
    public R<String> addSetMeal(@RequestBody SetmealDto setmealDto){
        //调用service层方法
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }


    /**
     * 删除套餐
     * 删除套餐前应该先删除套餐对应的菜品表 即 stemeal_dish中对应的数据
     *然后在删除 表setmeal的数据， 开事务
     * @param ids
     * @return
     */
    @CacheEvict(value = "setmealCache",allEntries = true)//删除当前分类下所有缓存
    @DeleteMapping
    @ApiOperation(value = "套装删除")
    @ApiImplicitParam(name="ids" ,value = "套餐ids", required = true)//说明参数
    public R<String> deleteMeal(@RequestParam List<Long> ids){
//    public R<String> deleteMeal(Long[] ids){
        //调用service 层方法
        setmealService.deleteWithDish(ids);
        return R.success("删除成功！");
    }


    /**
     * 修改套餐的 数据回显
     * 使用dto对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "套装数据回显")
    @ApiImplicitParam(name="id" ,value = "套餐id", required = true)//说明参数
    public R<SetmealDto> getMealAndDish(@PathVariable Long id){

        //调用service层方法
        SetmealDto setmealDto = setmealService.getMealAndDish(id);

        return R.success(setmealDto);
    }



    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId+'_'+#setmealDto.status")
    @PutMapping
    @ApiOperation(value = "套餐修改")
    @ApiImplicitParam(name="setmealDto" ,value = "套餐dto对象", required = true)//说明参数
    public R<String> updateMealAndDish(@RequestBody SetmealDto setmealDto){

        setmealService.updateMealAndDish(setmealDto);
        return R.success("修改成功");

    }


    /**
     * 修改销售状态
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改销售状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ids" ,value = "套餐ids", required = true),//说明参数
            @ApiImplicitParam(name="status" ,value = "销售状态", required = true)//说明参数
    })//说明多个参数
    public R<String> updateStatus(Long[] ids,@PathVariable int status){

        for(Long id:ids){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status); //修改销售状态
            setmealService.updateById(setmeal);//保存修改的信息
        }

        return R.success("修改状态成功！");
    }



    /**
     * 在移动端 展示套餐菜品的时候用到
     * 点击套餐种类可获得该种类下的所有售卖套餐
     * 获取套餐的全部菜品
     * @param setmeal  套餐对象
     * @return
     */
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
//    @ApiOperation(value = "暂时未做出来")  //有问题
//    @ApiImplicitParam(name="setmeal" ,value = "套餐对象", required = true)//说明参数
    public R<List<Setmeal>> getSetMeal(Setmeal setmeal){ //问号接参数
        log.info(setmeal.toString());
        //根据 categoryId和status查询套餐的菜品
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId())
                    .eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());//1表示在售
        //排序条件 按更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);

        return R.success(setmeals);

    }


    /**
     * 移动端点击套餐后，弹出套餐对应菜品详情
     * 显示菜品
     * @param setmealId   套餐id
     * @return  返回套餐信息和套餐对应的菜品   dto对象list集合  R<SetmealDto>
     */
    @GetMapping("dish/{setmealId}")
    @ApiOperation(value = "查询套餐对应菜品")//说明方法的用途
    @ApiImplicitParam(name="setmealId" ,value = "套餐id", required = true)//说明参数
    public R<List<SetmealDish>> getSetMeal(@PathVariable Long setmealId){//路径参数 直接接

//        //新建 SetmealDto 对象
//        SetmealDto setmealDto=new SetmealDto();

        //根据套餐id 查询对应菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmealId!=null,SetmealDish::getSetmealId,setmealId);
        //根据修改时间降序排列,根据sort升序排列
        queryWrapper.orderByDesc(SetmealDish::getUpdateTime).orderByAsc(SetmealDish::getSort);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
//        //封装套餐内菜品信息
//        setmealDto.setSetmealDishes(setmealDishes);


//        //根据套餐id，查询套餐信息
//        Setmeal setmeal = setmealService.getById(setmealId);
//        //将套餐信息copy到dto对象中
//        BeanUtils.copyProperties(setmeal,setmealDto);
//        return R.success(setmealDto);

        return R.success(setmealDishes);


    }























}
