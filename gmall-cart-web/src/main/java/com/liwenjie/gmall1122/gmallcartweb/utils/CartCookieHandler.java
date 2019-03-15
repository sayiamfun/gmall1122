package com.liwenjie.gmall1122.gmallcartweb.utils;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.CartInfo;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.service.SkuInfoService;
import com.liwenjie.gmall1122.utils.CookieUtil;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;
    //设置一次加减的数量
    private int cartNum = 1;
    @Reference
    private SkuInfoService skuInfoService;

    /**
     * 添加商品到购物车
     * @param request
     * @param response
     * @param userId
     * @param skuId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response,String userId, String skuId, int skuNum){
        String cookieValueJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();
        boolean addFlag = true;
        //如果购物车内已经有此条信息，则直接修改
        if(cookieValueJson!=null){
            cartInfoList = JSON.parseArray(cookieValueJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {
                if(skuId.equals(cartInfo.getSkuId())){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    addFlag = false;
                    break;
                }
            }
        }
        //如果cookie没有购物车或购物车内没有此条商品信息，则直接插入
        if(addFlag){
            CartInfo cartInfoIn = new CartInfo();
            SkuInfo info = skuInfoService.getSkuInfoBySkuId(skuId);

            cartInfoIn.setSkuName(info.getSkuName());
            cartInfoIn.setUserId(userId);
            cartInfoIn.setSkuId(skuId);
            cartInfoIn.setCartPrice(info.getPrice());
            cartInfoIn.setSkuPrice(info.getPrice());
            cartInfoIn.setImgUrl(info.getSkuDefaultImg());
            cartInfoIn.setSkuNum(skuNum);

            cartInfoList.add(cartInfoIn);
        }
        //将购物车信息重新写入cookie
        String cookieCartJson = JSON.toJSONString(cartInfoList);
        CookieUtil.setCookie(request, response, cookieCartName, cookieCartJson, COOKIE_CART_MAXAGE, true);
    }

    /**
     * 得到Cookie中的购物车内所有数据
     * @param request
     * @return
     */
    public List<CartInfo> cartList(HttpServletRequest request) {
        String cookieValueJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cookieValueJson, CartInfo.class);
        return cartInfoList;
    }

    /**
     * 删除Cookie中的购物车内所有数据
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    /**
     * 修改购物车内商品是否被选中
     * @param request
     * @param response
     * @param skuId
     * @param isChecked
     */
    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //  取出购物车中的商品
        List<CartInfo> cartList = cartList(request);
        // 循环比较
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        // 保存到cookie
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);

    }
    //批量删除cookie中购物车数据
    public void deleteSkuFromCartBatch(HttpServletRequest request, HttpServletResponse response, Integer[] skuId) {
        List<CartInfo> cartInfoList = cartList(request);
        for (CartInfo cartInfo : cartInfoList) {
            for (int i = 0; i < skuId.length; i++) {
                if(cartInfo.getSkuId().equals(skuId[i].toString())){
                    deleteSkuFromCart(request, response, cartInfo.getSkuId());
                }
            }

        }
    }
    //删除cookie购物车中指定skuId的商品
    private void deleteSkuFromCart(HttpServletRequest request, HttpServletResponse response, String skuId) {
        List<CartInfo> cartInfoList = cartList(request);
        if(cartInfoList!=null && cartInfoList.size()>0) {
            for (Iterator<CartInfo> iterator = cartInfoList.iterator(); iterator.hasNext(); ) {
                CartInfo cartInfo = iterator.next();
                if (cartInfo.getSkuId().equals(skuId)){
                    iterator.remove();
                }
            }
        }
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }
    //购物车商品加号
    public void cartDec(HttpServletRequest request, HttpServletResponse response, String skuId) {
        List<CartInfo> cartInfoList = cartList(request);
        if(cartInfoList!=null && cartInfoList.size()>0) {
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()-cartNum);
                }
            }
        }
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }
    //购物车商品减号
    public void cartAdd(HttpServletRequest request, HttpServletResponse response, String skuId) {
        List<CartInfo> cartInfoList = cartList(request);
        if(cartInfoList!=null && cartInfoList.size()>0) {
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+cartNum);
                }
            }
        }
        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);
    }
}
