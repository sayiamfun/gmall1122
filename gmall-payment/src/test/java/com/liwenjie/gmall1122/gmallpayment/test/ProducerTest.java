package com.liwenjie.gmall1122.gmallpayment.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerTest {
    public static void main(String[] args) throws JMSException {
        //创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.44.100:61616");
        //得到连接
        Connection connection = connectionFactory.createConnection();
        //开启连接
        connection.start();
        //创建session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建消息队列
        Queue queue = session.createQueue("Atguigu");
        //创建消息生产者
        MessageProducer producer = session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
        textMessage.setText("你好Liwenjie111");
        //发送消息
        producer.send(textMessage);
        //只有创建session时第一个参数为true，才需要提交事务
        //session.commit();
        //关闭
        producer.close();
        session.close();
        connection.close();
    }
}
