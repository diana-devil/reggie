package com.diana;

import com.diana.common.utils.SMSUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootTest
@EnableTransactionManagement
class ReggieApplicationTests {

    @Autowired
    private SMSUtils smsUtils;


    @Test
    void contextLoads() {
//        smsUtils.get();

    }

}
