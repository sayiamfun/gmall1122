package com.liwenjie.gmall1122.gmallpayment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.liwejie.gmall1122.LoginRequire;
import com.liwenjie.gmall1122.bean.OrderInfo;
import com.liwenjie.gmall1122.bean.PaymentInfo;
import com.liwenjie.gmall1122.gmallpayment.config.AlipayConfig;
import com.liwenjie.gmall1122.service.OrderService;
import com.liwenjie.gmall1122.service.PaymentService;
import com.liwenjie.gmall1122.status.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;
    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    @LoginRequire(autoRedirect = true)
    public String index(HttpServletRequest request){
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);
        request.setAttribute("orderId", orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        return "index";
    }

    @RequestMapping("alipay/submit")
    @LoginRequire(autoRedirect = true)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){

        // 获取订单Id
        String orderId = request.getParameter("orderId");
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfoByOrderId(orderId);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getOrderComment());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        // 保存信息
        paymentService.savePaymentInfo(paymentInfo);
        // 支付宝参数
        //创建API对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //在公共参数中设置回跳和通知地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        // 声明一个Map
        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);
        String form="";
        try {
            //调用SDK生成表单
            form = alipayClient.pageExecute(alipayRequest).getBody();
            System.err.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        // 代码追后面 15 执行一次，总共需要执行3次。
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    // 支付宝同步回调
    @RequestMapping("alipay/callback/return")
    public String callback(){
        return "redirect:"+AlipayConfig.return_order_url;
    }
    // 异步回调 通知电商的支付结果  success 时才能成功！面试题：fail 失败！{ 网络异常！}
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        // 将异步通知中收到的所有参数都存放到map中 url 上的所有参数，我们可以使用springMVC的那个注解得？ springmvc 接收传值方式有几种？
        // <input type="texy" name = "userName1">  UserInfo ()
//        Map<String, String> paramsMap = ... //将异步通知中收到的所有参数都存放到map中
        PaymentInfo paymentInfo = new PaymentInfo();
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,AlipayConfig.charset , AlipayConfig.sign_type); //调用SDK验证签名
        if(flag){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            // 获取交易状态
            String trade_status = paramMap.get("trade_status"); // 校验交易状态
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                // 根据交易第三方编号查询当前的交易记录
                String out_trade_no = paramMap.get("out_trade_no");
                // select *  from paymentInfo where out_trade_no=？
                paymentInfo.setOutTradeNo(out_trade_no);
                // 调用方法
                PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                // 假如说交易状态是成功的！但是，在交易记录表中，该条记录的支付状态或者是进程状态已经结束了，或者是已经付款完成，那么失败！
                if (paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED || paymentInfoQuery.getPaymentStatus()==PaymentStatus.PAID ){
                    return "fail";
                }
                // 更新支付交易表中的PaymentStatus= PaymentStatus.PAID
                // update paymentInfo set PaymentStatus = PaymentStatus.PAID where out_trade_no = ?
                PaymentInfo paymentInfoUPD = new PaymentInfo();
                // 给要更新的对象进行赋值
                paymentInfoUPD.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUPD.setCallbackTime(new Date());
//                paymentInfoUPD.setOutTradeNo(out_trade_no);
//                paymentInfoUPD.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUPD);
                paymentService.sendPaymentResult(paymentInfo,"success");
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            paymentService.sendPaymentResult(paymentInfo,"fail");
            return "fail";
        }
        return "fail";
    }

    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(String orderId, String result){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentService.sendPaymentResult(paymentInfo, result);
        return "OK";
    }
}
