package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.UserAddress;
import com.liwenjie.gmall1122.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> getUserInfoList();

    UserInfo getUserInfoByUser(UserInfo userInfo);

    UserInfo verify(String userId);

    List<UserAddress> getUserAddressList(String userId);
}
