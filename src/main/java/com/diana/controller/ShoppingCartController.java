package com.diana.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diana.common.R;
import com.diana.pojo.ShoppingCart;
import com.diana.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 得到购物车信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getShoppingCartList(HttpServletRequest request){
        //根据当前用户id 查询 其购物车信息
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCarts);
    }


    /**
     * 传入菜品和套餐公用一个方法
     * 往购物车中添加菜品
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> addDish(@RequestBody ShoppingCart shoppingCart, HttpServletRequest request){
        log.info(shoppingCart.toString());
        //添加userId
        Long userId = (Long)request.getSession().getAttribute("user");
        shoppingCart.setUserId(userId);
        //添加创建时间
        //因为表中只有创建时间，没有修改时间，所以只能用这种方法来插入时间
        shoppingCart.setCreateTime(LocalDateTime.now());


        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);//当前 用户下
        //查询当前菜品或者套餐是否在购物车中  两个id只传入一个
        //查询套餐
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        //查询菜品,不考虑口味，前端没考虑
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());


        ShoppingCart setmeal_dish = shoppingCartService.getOne(queryWrapper);
        if(setmeal_dish!=null){
            //说明已经有了当前套餐，数量加一
            setmeal_dish.setNumber(setmeal_dish.getNumber()+1);
            shoppingCartService.updateById(setmeal_dish);
        }else{
            //如果不存在，则添加到购物车中，数量默认是1
            shoppingCartService.save(shoppingCart);
            setmeal_dish=shoppingCart;
        }

        return R.success(setmeal_dish);
    }


    /**
     *  从购物车中减少 菜品或套餐数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> subDish(@RequestBody ShoppingCart shoppingCart,HttpServletRequest request){
        log.info(shoppingCart.toString());
        //添加userId
        Long userId = (Long)request.getSession().getAttribute("user");
        shoppingCart.setUserId(userId);


        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);//当前 用户下
        //查询当前菜品或者套餐是否在购物车中  两个id只传入一个
        //查询套餐
        queryWrapper.eq(shoppingCart.getSetmealId()!=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        //查询菜品,不考虑口味，前端没考虑
        queryWrapper.eq(shoppingCart.getDishId()!=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        ShoppingCart setmeal_dish = shoppingCartService.getOne(queryWrapper);


        if(setmeal_dish!=null){
            Integer number = setmeal_dish.getNumber();
            log.info("{}",number);
            if(number>1){ //1 这个数字很精髓，不是0 是1,  因为数量为1的时候在点就不要减了，直接删除就好了
                //说明当前套餐存在，数量减一
                setmeal_dish.setNumber(setmeal_dish.getNumber()-1);
                shoppingCartService.updateById(setmeal_dish);
            }else{
                //如果当前套餐数量小于等于0 直接删除对应购物车信息
                shoppingCartService.removeById(setmeal_dish);
            }
        }else{
            return R.error("购物车中已没有当前菜品!");
        }
        return R.success(setmeal_dish);
    }


    /**
     * 清空购物车
     * @param request
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> deleteShoppingCart(HttpServletRequest request){

        //查询当前userId
        Long userId = (Long)request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);//没有removeBaths方法， 会清除所有满足要求的数据

        return R.success("成功清空购物车！！");

    }
}
