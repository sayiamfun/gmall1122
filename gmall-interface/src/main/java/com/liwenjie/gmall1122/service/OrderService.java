package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.OrderInfo;
import com.liwenjie.gmall1122.status.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    //保存订单信息
    String saveOrder(OrderInfo orderInfo);
    //得到流水号
    String getTradeNo(String userId);
    //验证流水号
    boolean checkTradeCode(String userId,String tradeCodeNo);
    //验证库存数量
    boolean checkStock(String skuId, Integer skuNum);
    //根据orderId查询对应的订单
    OrderInfo getOrderInfoByOrderId(String orderId);
    //更新订单状态
    void updateOrderStatus(String orderId, ProcessStatus paid);
    //发送消息给库存模块，更新库存信息
    void sendOrderStatus(String orderId);
    //得到长时间不适用的订单
    List<OrderInfo> getExpiredOrderList();
    //处理时效的订单
    void execExpiredOrder(OrderInfo orderInfo);

    Map initWareOrder(OrderInfo orderInfo);

    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);
}
