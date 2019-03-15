package com.liwenjie.gmall1122.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liwenjie.gmall1122.bean.UserInfo;
import com.liwenjie.gmall1122.service.UserInfoService;
import com.liwenjie.gmall1122.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Value("${token.key}")
    String signKey;



    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("index")
    public String login(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
         //取得ip地址
        String remoteAddr  = request.getHeader("x-forwarded-for");
        UserInfo info = userInfoService.getUserInfoByUser(userInfo);
        if(info!=null){
            Map<String,Object> map = new HashMap<>();
            map.put("userId", info.getId());
            map.put("nickName", info.getNickName());
            String token = JwtUtil.encode(signKey, map, remoteAddr);
            return token;
        }
         return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("newToken");
        String currentIp = request.getParameter("currentIp");
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if(map!=null){
            String userId = (String) map.get("userId");
            UserInfo userInfo = userInfoService.verify(userId);
            if(userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }
}
