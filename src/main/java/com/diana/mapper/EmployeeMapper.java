package com.diana.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.diana.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;

/***
 * 员工mapper
 */

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
