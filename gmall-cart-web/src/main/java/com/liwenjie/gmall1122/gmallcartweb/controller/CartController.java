package com.liwenjie.gmall1122.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liwejie.gmall1122.LoginRequire;
import com.liwenjie.gmall1122.bean.CartInfo;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.gmallcartweb.utils.CartCookieHandler;
import com.liwenjie.gmall1122.service.CartService;
import com.liwenjie.gmall1122.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.plugin2.message.JavaScriptBaseMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    private CartService cartService;
    @Reference
    private SkuInfoService skuInfoService;
    @Autowired
    private CartCookieHandler cartCookieHandler;

    /**
     * 添加商品到购物车
     * @param request
     * @param response
     * @param skuId
     * @param skuNum
     * @return
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("addToCart")
    public String addCartInfo(HttpServletRequest request, HttpServletResponse response, String skuId, String skuNum){
        // 获取userId，skuId，skuNum
        //已经通过拦截器将userId放入request域里(注意：如果想取得userId必须添加LoginRequer注解)
        String userId = (String) request.getAttribute("userId");
        if(userId!=null){
            //如果已经登录，数据存在数据库和redis
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        }else {
            //如果未登录，数据存入cookie
            //说明用户没有登录没有登录放到cookie中
            cartCookieHandler.addToCart(request,response,userId,skuId,Integer.parseInt(skuNum));
        }
        // 取得sku信息对象
        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    /**
     * 显示购物车内所有商品信息
     * @param request
     * @param response
     * @param map
     * @return
     */
    @LoginRequire(autoRedirect = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map){
        //判断用户是否登录
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();
        if(userId!=null){
            List<CartInfo> cartInfoFromCookie = cartCookieHandler.cartList(request);
            if(cartInfoFromCookie!=null && cartInfoFromCookie.size()>0){
                cartInfoList = cartService.mergeToCartList(cartInfoFromCookie,userId);
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                cartInfoList = cartService.CartListByUserId(userId);
            }
        }else {
             cartInfoList = cartCookieHandler.cartList(request);
        }
        map.put("cartInfos", cartInfoList);
        return "cartList";
    }

    /**
     * 修改购物车内商品是否被选中状态
     * @param request
     * @param response
     */
    @RequestMapping("checkCart")
    @ResponseBody()
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId=(String) request.getAttribute("userId");
        if (userId!=null){
            cartService.checkCart(skuId,isChecked,userId);
        }else{
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }
    }

    /**
     * 去结算页面（如果添加购物车时未登录，则将cookie的数据更新到数据库）
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cookieHandlerCartList = cartCookieHandler.cartList(request);
        if (cookieHandlerCartList!=null && cookieHandlerCartList.size()>0){
            cartService.mergeToCartList(cookieHandlerCartList, userId);
            cartCookieHandler.deleteCartCookie(request,response);
        }
        return "redirect://order.gmall.com/tradeInit";
    }
    /**
     * 批量删除商品
     */
    @RequestMapping("moveFromCartBatch")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public String moveFromCartBatch(HttpServletRequest request, HttpServletResponse response, Integer[] skuId){
        String userId = (String) request.getAttribute("userId");
        if(userId!=null){
            cartService.deleteSkuFromCartBatch(skuId, userId);
        }else{
            cartCookieHandler.deleteSkuFromCartBatch(request, response,skuId);
        }
        return "true";
    }
    //购物车商品数量加
    @RequestMapping("cartAdd")
    @ResponseBody
    public String cartAdd(HttpServletRequest request, HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        if(userId!=null){
            cartService.cartAdd(skuId, userId);
        }else{
            cartCookieHandler.cartAdd(request, response, skuId);
        }
        return "true";
    }
    //购物车商品数量减
    @RequestMapping("cartDec")
    @ResponseBody
    public String cartDec(HttpServletRequest request, HttpServletResponse response){
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        if(userId!=null){
            cartService.cartDec(skuId, userId);
        }else{
            cartCookieHandler.cartDec(request, response, skuId);
        }
        return "true";
    }
}
