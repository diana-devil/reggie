package com.diana.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diana.common.R;
import com.diana.dto.DishDto;
import com.diana.mapper.DishMapper;
import com.diana.pojo.Category;
import com.diana.pojo.Dish;
import com.diana.pojo.DishFlavor;
import com.diana.service.CategoryService;
import com.diana.service.DishFlavorService;
import com.diana.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分页查询
     * 这里不能简单的返回Page<Dish>, 因为这里面没有对应的 分类名称只有分类id
     * 所以创建一个Page<DishDto>对象，将Page<Dish>的数据进行简单处理。
     * @param page 第几页
     * @param pageSize 每页显示多少
     * @return  page对象
     */
    @GetMapping("/page")
    public R<Page<DishDto>> getDishByPage(int page,int pageSize,String name){

        //创建分页对象
        Page<Dish> pageinfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPageinfo=new Page<>();

        //创建查询条件
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);

        //创建排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //查询数据
//        Page<Dish> page1 = dishService.page(pageinfo, queryWrapper);
//        log.info(page1.getRecords().toString());

        //这里不用返回一个page对象，pageinfo对象就已经是我们需要的对象了，数据都存好了
        dishService.page(pageinfo, queryWrapper);
        log.info(pageinfo.getRecords().toString());


        //对Page<DishDto> 进行处理
        //1.对象copy，忽略records
        BeanUtils.copyProperties(pageinfo,dishDtoPageinfo,"records");//忽略records，即copy除这个属性以外的所有值
        //2.从原来的records中获取categoryId，进一步获取分类名称
        List<Dish> records = pageinfo.getRecords();

        List<DishDto> dtoList=records.stream().map((item)->{
            //创建dishdto对象，将其他属性从records 中copy到dishdto对象中
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //得到分类id
            Long categoryId = item.getCategoryId();

            //根据分类id 获取分类名称，并将dishDto进行设置
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());//将集合转换成list集合对象，并收集起来 赋值给dtolist

        //3.将设置好的records存入 Page<DishDto>对象
        dishDtoPageinfo.setRecords(dtoList);//将dtolist设置为新的records

        return  R.success(dishDtoPageinfo);
    }



    /**
     * 添加菜品
     * @return
     */
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return  R.success("添加成功");
    }


    /**
     * 根据id查询 菜品信息，和菜品对应口味信息
     * 查两个表
     * 修改菜品，回显请求
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id){
        log.info("{}",id);

        //调用service 层方法
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }



    /**
     * 修改菜品 提交请求
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto){

        log.info(dishDto.toString());
        //调用service 方法
        dishService.updateWithFlavor(dishDto);

        return R.success("修改成功！！");
    }


    /**
     * 修改菜品销售状态
     * 这里前端给我们传递了要修改的状态值，直接用即可
     *
     * 起售，停售，批量起售，批量停售 复用一个方法
     * 对于选中的多个 进行批量起售，可以直接设置所选的为起售，不需要考虑原来的状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,Long[] ids){
        for(Long id:ids){
            //根据id查出dish对象
            Dish dish = dishService.getById(id);

            //修改status  前端没传信息 ，自己从数据库中获取信息，并修改
//        Integer status = dish.getStatus();
//        dish.setStatus((status==1) ? 0:1); //如果状态是1，改为0；如果状态是0，改为1
            //修改status,前端传了信息，直接修改
            dish.setStatus(status);
            dishService.updateById(dish);
        }

        //修改成功
        return R.success("修改状态成功！");
    }


    /**
     * 删除菜品信息
     * 删除单个与批量删除 复用一个方法
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional
    public R<String> deleteDish(Long[] ids){
        //如果没有删除成功
        //多人操作，一个人删除了，另外一个人没有刷新页面，也点击删除，就会出现删除失败的现象
        //这种请求 删除不需要报错，因为关心的是删了吗，谁删都一样
        for(Long id:ids){
//            //如果删除有一个失败 就返回失败，终止操作
//            if(!dishService.removeById(id)){
//                return R.error("当前菜品已删除，请刷新后重试！！");
//            }
            dishService.removeById(id);
        }
        //全部删除完成，没有任何问题，返回删除成功
        return R.success("删除成功！！");
    }



    /**
     * 原本 后台，新增套餐时使用
     * 复用移动端，点击左侧 菜系名称时 生效
     *  根据categoryId,查询菜品名称
     * @param dish Dish 对象，现阶段只有categoryId一个值，但是复用性更好,这不是后期复用了status 查询状态
     * @return R<List<Dish>>  对应菜系id的全部菜品信息
     * @return R<List<DishDto>>  返回对应菜系id的全部菜品信息，同时返回菜品对应的口味信息，修改
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishList(Dish dish){

        //在后台管理页面 需要获取菜品信息，  这些就够了
        //创建查询对象
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //根据当前的categoryId 查询所有 在售卖阶段的菜品
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        //排序条件
        queryWrapper.orderByAsc(Dish::getSort);//按排序 越小越好
        queryWrapper.orderByDesc(Dish::getUpdateTime);//按更新时间，越大越好
        //查询所有满足条件的排列好的数据
        List<Dish> dishes = dishService.list(queryWrapper);


        //在移动端展示的时候，需要展示菜品对应口味信息，所以要补加这些
        //新建dishDto对象，copy dishes的属性
//        List<DishDto> dishDtos=new ArrayList<>();
//        BeanUtils.copyProperties(dishes,dishDtos);---->这个copy方法不能直接作用于list集合


        //遍历每一个菜品，封装其口味信息
        List<DishDto> dishDtos=dishes.stream().map((item)->{
            //新建dishDto对象，并copy dish的值
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //得到 dish_id
            Long dishId = item.getId();
            //查表 dish_flavor,得到口味信息
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            //保存每个菜品的口味信息
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());


        log.info(dishDtos.toString());
        return R.success(dishDtos);
    }















}
