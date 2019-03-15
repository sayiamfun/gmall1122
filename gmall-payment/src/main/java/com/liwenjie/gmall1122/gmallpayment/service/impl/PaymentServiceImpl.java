package com.liwenjie.gmall1122.gmallpayment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.liwenjie.gmall1122.bean.PaymentInfo;
import com.liwenjie.gmall1122.config.utils.ActiveMQUtil;
import com.liwenjie.gmall1122.gmallpayment.mapper.PaymentInfoMapper;
import com.liwenjie.gmall1122.service.PaymentService;
import com.liwenjie.gmall1122.status.PaymentStatus;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    //根据实体类的属性名修改支付订单信息
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD) {
        Example example = new Example(PaymentInfo.class);
        // outTradeNo 实体类的属性名 ，而不是数据库的字段名
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUPD,example);
    }

    //按发送消息给订单模块，支付完毕
    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        //获取连接
        Connection connection = activeMQUtil.getConnection();
        //打开连接
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建消息队列
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建消息生产者
            MessageProducer producer = session.createProducer(queue);
            //准备要发送的消息
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", paymentInfo.getOrderId());
            mapMessage.setString("result", result);
            //发送消息
            producer.send(mapMessage);
            //提交
            session.commit();
            //关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    //手动检查知否是否成功
    @Override
    public boolean checkPayment(PaymentInfo paymentInfo) {
        //根据orderId查询相应的支付订单信息
        PaymentInfo paymentInfoQuery = getPaymentInfo(paymentInfo);
        //如果订单信息支付状态显示已支付或者已关闭则支付成功
        if (paymentInfoQuery.getPaymentStatus()== PaymentStatus.PAID || paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
//        request.setBizContent("{" +
//                "\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\"" +
//                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                //  IPAD
                System.out.println("支付成功");
                // 改支付状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoUpd);
                sendPaymentResult(paymentInfo,"success");
                return true;
            }else {
                System.out.println("支付失败");
                return false;
            }
        }
        System.out.println("支付失败");
        return false;
    }
    //发送消息给支付模块验证支付是否成功
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        //得到连接
        Connection connection = activeMQUtil.getConnection();
        //打开连接
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true,Session.SESSION_TRANSACTED);
            //创建消息队列
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            //创建消息生产者
            MessageProducer producer = session.createProducer(queue);
            //封装消息
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo", outTradeNo);
            mapMessage.setInt("delaySec", delaySec);
            mapMessage.setInt("checkCount", checkCount);
            // 设置延迟多少时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            //发送消息
            producer.send(mapMessage);
            //提交
            session.commit();
            //关闭连接
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }

}
