package com.diana.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.EmployeeMapper;
import com.diana.pojo.Employee;
import com.diana.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
