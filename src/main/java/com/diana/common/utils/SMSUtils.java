package com.diana.common.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sound.sampled.Line;

/**
 * 短信发送工具类
 */
@Slf4j
@Component

public class SMSUtils {

	//从配置文件读取数据
	@Value("${SMS.accessKeyId}")
	private String accessKeyId1;
	@Value("${SMS.secret}")
	private String secret1;

	//声明静态变量，静态方法只能调用静态变量
	private static  String accessKeyId;
	private static String secret;

	//通过get方法，给静态变量赋值
	@PostConstruct
	public void getAccessKeyId(){
		accessKeyId=this.accessKeyId1;
	}
	@PostConstruct
	public void getSecret(){
		secret=this.secret1;
	}


	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){

		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, secret);
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		log.info(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}
	}

}
