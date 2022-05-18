package com.diana.controller;

import com.diana.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class FileController {

    @Value("${reggie.filepath}") //读取配置文件中的定义的路径值
    private String basePath;


    /**
     * 文件上传
     *
     * @param file 参数类型必须是 MultipartFile，是Spirng-web封装好的处理文件的类型
     * @return
     */

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){ //属性名与 表单提交文件的name名一致
        //file 是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会自动消失

        //获取文件后缀名
        String originalFilename =file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
//        log.info(suffix);

        //使用uuid重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        //处理目录对象
        File dir=new File(basePath);
        if(!dir.exists()){
            //如果目录不存在，创建目录
            dir.mkdir();
        }

        //将文件转存到指定位置
        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  R.success(fileName);

    }


    /**
     * 文件下载
     * 方式1： 以附件的形式下载，保存到本地
     * 方式2:  以流的方式输出到浏览器，直接在浏览器中打开
     *
     * 这里采用的是 方式2 在浏览器展示图片
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
//        log.info(name);

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //输出流，通过输出流写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            //设置响应格式  图片格式
//            response.setContentType("image/jpeg");

            //手动copy流
//            int len=0;
//            byte[] bytes =new byte[1024];
//            while((len=fileInputStream.read(bytes)) !=-1){//将输入流读入 字节数组，一次读入1024个字节，最后一次不足1024，在读就是-1
//                log.info("{}",len);
//                outputStream.write(bytes,0,len);
//                outputStream.flush();
//            }

            //使用方法copy  ----commons-io  方法内部跟上面自己手动实现copy流基本一样
            IOUtils.copy(fileInputStream,outputStream); //完成从输入流到输出流的copy

            //关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
