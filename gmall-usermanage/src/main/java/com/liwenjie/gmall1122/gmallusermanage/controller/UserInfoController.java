package com.liwenjie.gmall1122.gmallusermanage.controller;

import com.liwenjie.gmall1122.bean.UserInfo;
import com.liwenjie.gmall1122.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ResponseBody
    @RequestMapping("getUserInfoList")
    public List<UserInfo> getUserInfoList(){
        List<UserInfo> userInfoList = userInfoService.getUserInfoList();
        return userInfoList;
    }
}
