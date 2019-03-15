package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.PaymentInfo;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    boolean checkPayment(PaymentInfo paymentInfo);
    //发送消息给支付模块验证知否是否成功
    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);
    //关闭订单
    void closePayment(String id);
}
