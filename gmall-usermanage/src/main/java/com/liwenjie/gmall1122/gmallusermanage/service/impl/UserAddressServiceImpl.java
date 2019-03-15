package com.liwenjie.gmall1122.gmallusermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liwenjie.gmall1122.bean.UserAddress;
import com.liwenjie.gmall1122.config.utils.RedisUtil;
import com.liwenjie.gmall1122.gmallusermanage.mapper.UserAddressMapper;
import com.liwenjie.gmall1122.gmallusermanage.mapper.UserInfoMapper;
import com.liwenjie.gmall1122.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;
    public String USERINFOKEY_PREFIX="user:";
    public String USERINFOKEY_SUFFIX=":info";
    public int USERINFOKEY_TIMEOUT=60*60*24;


    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

}
