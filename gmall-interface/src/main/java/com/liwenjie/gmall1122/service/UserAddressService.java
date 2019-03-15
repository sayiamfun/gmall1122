package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.UserAddress;
import com.liwenjie.gmall1122.bean.UserInfo;

import java.util.List;

public interface UserAddressService {
    List<UserAddress> getUserAddressList(String userId);
}
