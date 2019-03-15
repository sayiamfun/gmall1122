package com.liwenjie.gmall1122.mq;

import com.alibaba.fastjson.JSON;

import com.liwenjie.gmall1122.bean.WareOrderTask;
import com.liwenjie.gmall1122.enums.TaskStatus;
import com.liwenjie.gmall1122.mapper.WareOrderTaskDetailMapper;
import com.liwenjie.gmall1122.mapper.WareOrderTaskMapper;
import com.liwenjie.gmall1122.mapper.WareSkuMapper;
import com.liwenjie.gmall1122.service.GwareService;

import com.liwenjie.gmall1122.config.ActiveMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;
import java.util.*;

/**
 * @param
 * @return
 */
@Component
public class WareConsumer {


    @Autowired
    WareOrderTaskMapper wareOrderTaskMapper;
    @Autowired
    WareOrderTaskDetailMapper wareOrderTaskDetailMapper;
    @Autowired
    WareSkuMapper wareSkuMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    GwareService gwareService;



    @JmsListener(destination = "ORDER_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void receiveOrder(TextMessage textMessage) throws JMSException {
        String orderTaskJson = textMessage.getText();
        WareOrderTask wareOrderTask = JSON.parseObject(orderTaskJson, WareOrderTask.class);
        wareOrderTask.setTaskStatus(TaskStatus.PAID);
        gwareService.saveWareOrderTask(wareOrderTask);
        textMessage.acknowledge();

        List<WareOrderTask> wareSubOrderTaskList = gwareService.checkOrderSplit(wareOrderTask);
        if (wareSubOrderTaskList != null && wareSubOrderTaskList.size() >= 2) {
            for (WareOrderTask orderTask : wareSubOrderTaskList) {
                gwareService.lockStock(orderTask);
            }
        } else {
            gwareService.lockStock(wareOrderTask);
        }
    }


}
