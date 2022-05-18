package com.diana.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diana.mapper.AddressBookMapper;
import com.diana.pojo.AddressBook;
import com.diana.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
