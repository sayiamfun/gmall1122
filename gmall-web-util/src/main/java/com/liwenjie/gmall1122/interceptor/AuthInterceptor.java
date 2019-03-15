package com.liwenjie.gmall1122.interceptor;

import com.alibaba.fastjson.JSON;
import com.liwejie.gmall1122.LoginRequire;
import com.liwenjie.gmall1122.utils.CookieUtil;
import com.liwenjie.gmall1122.utils.HttpClientUtil;
import com.liwenjie.gmall1122.utils.WebConst;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");
        if(token!=null){
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }
        if(token==null){
            token = CookieUtil.getCookieValue(request, "token", false);
        }
        if(token!=null){
            Map<String, Object> map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }

        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
//        Class<?> aClass = handler.getClass();
//        LoginRequire methodAnnotation = aClass.getAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            String remoteAddr = request.getHeader("x-forwarded-for");
            String result =
                    HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?newToken=" + token + "&currentIp=" + remoteAddr);
            if("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            }else {
                if(methodAnnotation.autoRedirect()){
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    private Map<String,Object> getUserMapByToken(String token) {

        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenByts = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenByts,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);

        return map;
    }
}
