package com.diana.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diana.pojo.Employee;
import com.diana.common.R;
import com.diana.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * 印象不深，加深印象
 * 1.在@PostMapping("/login")  中加路径，表示访问路径
 * 2.加注解@RequestBody ，表示接收json字符串
 * 3.将接收结果封装成 Employee对象--- 前提是json的键值和实体类的属性名称一致，数量不一样没关系，其他自动为null
 *
 */


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    /***
     * 员工登陆
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 1.将页面提交的密码进行md5加密处理
         * 2.根据页面提交的用户名username查询数据库
         * 3.如果没有查询到则返回登陆失败结果
         * 4.密码对比，如果不一致则返回登陆失败结果
         * 5.查看员工状态，如果为已禁用，则返回员工已禁用结果
         * 6.登陆成功，将员工id存入Session并返回登陆成功结果
         */

        //1.将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = service.getOne(queryWrapper);
        //3.如果没有查询到则返回登陆失败结果
        if (emp!=null){
            if (password.equals(emp.getPassword())){
                if(emp.getStatus()==1){
                    //6.登陆成功，将员工id存入Session并返回登陆成功结果
                    HttpSession session = request.getSession();
                    session.setAttribute("id",emp.getId());
                    return R.success(emp);
                }else{
                    //5.查看员工状态，如果为已禁用，则返回员工已禁用结果
                    return R.error("您已被禁用，请联系管理员！");
                }
            }else{
                //4.密码对比，如果不一致则返回登陆失败结果
                return R.error("密码不正确！");
            }
        }else{
            //3.如果没有查询到则返回登陆失败结果
            return R.error("用户名不存在！");
        }
    }

    /***
     * 员工登出
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        /**
         * 1.清理session中的id
         * 2.返回退出
         */

        //1.清理session中的id.
        request.getSession().removeAttribute("id");
        //2.返回退出
        return R.success("退出成功！！");

    }


    /**
     * 新增员工
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        //1.完善员工信息
        String idNumber = employee.getIdNumber();
        String pas = idNumber.substring(idNumber.length() - 6);
        log.info(pas);
        String password = DigestUtils.md5DigestAsHex(pas.getBytes());//进行md5加密
        employee.setPassword(password);//默认密码身份证号后6位-加密后
        employee.setStatus(1);//设置状态为1

        log.info("线程id{}",Thread.currentThread().getId());//输出当前id值

        //这些公共字段 交给mybatisplus公共类MyMetaObjectHandler 进行填充
//        employee.setCreateTime(LocalDateTime.now());//设置创建时间
//        employee.setUpdateTime(LocalDateTime.now());//设置修改时间
//        Long id = (Long)request.getSession().getAttribute("id");
//        employee.setCreateUser(id);
//        employee.setUpdateUser(id);

        log.info("员工信息{}",employee);
        //2.将员工信息添加进数据库
        service.save(employee);

        //读取一下信息，看看有没存进去
//        LambdaQueryWrapper<Employee> query=new LambdaQueryWrapper<>();
//        query.eq(Employee::getPassword,employee.getPassword());
//        Employee one = service.getOne(query);
//        log.info("保存信息{}",one);

        return R.success("添加员工成功！");
    }


    /***
     * 员工信息分页查询
     * @param page 第几页
     * @param pageSize 每页显示数量
     * @param name 查询名称
     * @return
     */
    @GetMapping("/page")
    public R<Page> getEmpByPage(int page, int pageSize,String name){
        //name 可不传， 不传为null
        log.info("{},{},{}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> lqw=new LambdaQueryWrapper<>();
        //添加过滤条件
        lqw.like(name!=null,Employee::getName,name);
        //添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);
        Page page1 = service.page(pageInfo, lqw);

//        log.info("{}",page1.getRecords());
//        log.info("{}",page1.getTotal());
        return R.success(page1);

    }


    /***
     * 根据id修改员工信息
     *
     * 雪花算法生成id Long类型，19位
     * 但是JS响应数据只能保证，前16位精确，后面3为精度缺失
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> Update(@RequestBody Employee employee,HttpServletRequest request){
        //设置更新时间和更新人
        Long id = (Long)request.getSession().getAttribute("id");
//        employee.setUpdateUser(id);
//        employee.setUpdateTime(LocalDateTime.now());
        //这个更新操作，只修改不为null的操作，为null的不会修改，放心大胆的用即可
        service.updateById(employee);
        return R.success("修改成功！！");
    }



    /**
     * 根据id查询员工数据
     * 为数据回显做准备
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> GetById(@PathVariable Long id){
        Employee employee = service.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }





}
