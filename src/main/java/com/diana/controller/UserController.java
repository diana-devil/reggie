package com.diana.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.diana.common.R;
import com.diana.common.utils.SMSUtils;
import com.diana.common.utils.ValidateCodeUtils;
import com.diana.pojo.User;
import com.diana.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

//    @Value("${SMS.PhoneNumbers}")
//    private  String PhoneNumbers;
//    @Value("${SMS.SignName}")
//    private  String SignName;
//    @Value("${SMS.TemplateCode}")
//    private  String TemplateCode;

    @Autowired
    private UserService userService;


    /**
     *  发送短信验证码
     *  前端传入 phone，封装进user中
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request){
        log.info(user.toString());
        //获取登陆手机号
        String phone = user.getPhone();
        if(StringUtils.hasText(phone)){
            //如果手机号不为空
            //随机生成验证码
            Integer code = ValidateCodeUtils.generateValidateCode(6);
            //将验证码存入session域中，方便后期比对  键为手机号，值为验证码
            log.info("{}",code);
            request.getSession().setAttribute(user.getPhone(),code);
            //调用阿里云提供的短信服务API发送短信
//            SMSUtils.sendMessage(SignName,TemplateCode,phone,code.toString());

            return R.success("短信发送成功！");
        }
        return R.error("手机号错误！");

    }


    /**
     * 用户登陆
     * 当把用户信息存入数据库后，会自动生成一个id，修改原user对象，
     * 即存入之前id为null，存入之后，id有值
     * @param user
     * @return
     */
    @PostMapping("/login")
    public R<User> UserLogin(@RequestBody User user,HttpServletRequest request){
        log.info(user.toString());

        //获取手机号和验证码，并去跟session域中的数据进行对比
        Integer code = (Integer) request.getSession().getAttribute(user.getPhone());
        if(!code.equals(user.getCode())){
            //如果验证码不一样
            return R.error("验证码不正确,请重新输入！");
        }
        //验证码一样
        // 如果是新用户，将用户信息存入数据库，并获取用户id；
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,user.getPhone());
        User user1 = userService.getOne(queryWrapper);

        if(user1==null){
            //将用户信息存入数据库
            userService.save(user);//这里保存之后，自动给user1对象生成了一个id------------------------------------------------------
//            log.info(user.toString());
            user1=user; //将保存好的user赋给user1 这样方便外面统一获取userId
//            log.info(user1.toString());
        }
        //如果是老用户，直接获取用户id
        Long userId = user1.getId();


        //将用户id存入session域
        request.getSession().setAttribute("user",userId);

        return R.success(user1);

    }



    /**
     * 用户登出
     * @return
     */
    @PostMapping("/loginout")
    public R<String> UserLoginout(HttpServletRequest request){
        //将session中的用户id 取消
        request.getSession().removeAttribute("user");

        return R.success("退出成功！！");
    }





}
