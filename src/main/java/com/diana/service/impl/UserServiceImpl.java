package com.diana.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.UserMapper;
import com.diana.pojo.User;
import com.diana.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
