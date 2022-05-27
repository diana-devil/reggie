package com.diana.filter;

import com.alibaba.fastjson.JSON;
import com.diana.common.BaseContext;
import com.diana.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 使用过滤器
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器,可以识别通配符
    public static final AntPathMatcher PATH_MATCHER =new AntPathMatcher();


    /***
     * 完善登录功能，保证在没登录之前，无法获取数据
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest request =(HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;

        //需要放行的资源  每个资源前面都要加"/"
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//移动端发送短信
                "/user/login",//移动端登陆
                //可以在不登录的时候访问接口文档
                "/doc.html",
                "/webjars/**",
                "swagger-resources",
                "/v2/api-docs"
        };


        //1.获取本次请求的uri,直接放行登录页面；其他请求，判断session中的id，
        String requestURI = request.getRequestURI();
//        log.info("拦截到请求：{}",requestURI);
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
//        log.info("{}",check);
        //3.如果不需要处理，则直接放行
        if(check){
//            log.info("无需处理，直接放行！");
            filterChain.doFilter(request,response);
            return;
        }
        //4.1判断登录状态，如果登录完成，则直接放行---网页端 员工用户登陆
        if(request.getSession().getAttribute("id")!=null){
//            log.info("用户登录成功！！");
//            log.info("线程id{}",Thread.currentThread().getId());//输出当前id值

            //将登陆用户的id方向线程域
            Long id = (Long) request.getSession().getAttribute("id");
            BaseContext.setThreadLocal(id);


            filterChain.doFilter(request,response);
            return;
        }
        //4.2判断登录状态，如果登录完成，则直接放行---移动端，访问用户
        if(request.getSession().getAttribute("user")!=null){
//            log.info("用户登录成功！！");
//            log.info("线程id{}",Thread.currentThread().getId());//输出当前id值

            //将登陆用户的id方向线程域
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setThreadLocal(userId);

            filterChain.doFilter(request,response);
            return;
        }



        //5.如果未登录，则返回登录结果（前端定义了重定向到登录页面，所有只需要返回结果就好）
        //通过输出流的方式想客户端页面响应数据
        //在controller中不用json转换，spring会将对象自动转换为json数据，但是在外面不行，需要自己将对象转换成json数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//        log.info("返回登录页面！");
        return;
    }


    /***
     * 路径匹配，检查本次请求是否需要处理
     * @param urls
     * @param RequestURI
     * @return
     */
    public boolean check(String[] urls,String RequestURI){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url, RequestURI);
            if(match){
                return true;
            }
        }
        return false;
    }


}
