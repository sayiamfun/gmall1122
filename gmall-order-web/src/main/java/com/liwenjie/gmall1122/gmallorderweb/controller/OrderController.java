package com.liwenjie.gmall1122.gmallorderweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liwejie.gmall1122.LoginRequire;
import com.liwenjie.gmall1122.bean.*;
import com.liwenjie.gmall1122.service.CartService;
import com.liwenjie.gmall1122.service.OrderService;
import com.liwenjie.gmall1122.service.PaymentService;
import com.liwenjie.gmall1122.service.UserInfoService;
import com.liwenjie.gmall1122.status.OrderStatus;
import com.liwenjie.gmall1122.status.ProcessStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Reference
    private CartService cartService;
    @Reference
    private UserInfoService userInfoService;
    @Reference
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;


    /**
     * 结算页面数据初始化
     * @param request
     * @return
     */
    @RequestMapping("tradeInit")
    @LoginRequire(autoRedirect = true)
    public String tradeInit(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        // 得到选中的购物车列表
        userId = "1";
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        // 收货人地址
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);
        // 订单信息集合
        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        request.setAttribute("orderDetailArrayList",orderDetailList);
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("userAddressList", userAddressList);

        // 获取TradeCode号
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeCode",tradeNo);
        return  "trade";

    }

    /**
     * 生成订单结算
     * @param orderInfo
     * @param request
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
        // 检查tradeCode
        String userId = (String) request.getAttribute("userId");

        String tradeCode = (String) request.getParameter("tradeNo");
        if(tradeCode!=null && !"".equals(tradeCode)){
            boolean reslut = orderService.checkTradeCode(userId, tradeCode);
            if(!reslut){
                request.setAttribute("errMsg","该页面已失效，请刷新页面后重新结算!");
                return "tradeFail";
            }
        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            // 从订单中去购物skuId，数量
            boolean result = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!result){
                request.setAttribute("errMsg",orderDetail.getSkuNum()+"商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }

        // 保存
        String orderId = orderService.saveOrder(orderInfo);
        // 重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }

    /**
     * 验证支付是否成功
     * @param request
     * @return
     */
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        boolean result = paymentService.checkPayment(paymentInfo);
        return ""+request;
    }
    /**
     * 拆单
     */
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 定义订单集合
        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMap);
        List<Map<String, Object>> wareMapList=new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);

    }
}
