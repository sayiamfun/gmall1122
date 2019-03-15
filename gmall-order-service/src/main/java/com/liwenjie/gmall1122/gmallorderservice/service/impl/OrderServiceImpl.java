package com.liwenjie.gmall1122.gmallorderservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.OrderDetail;
import com.liwenjie.gmall1122.bean.OrderInfo;
import com.liwenjie.gmall1122.config.utils.ActiveMQUtil;
import com.liwenjie.gmall1122.config.utils.RedisUtil;
import com.liwenjie.gmall1122.config.utils.HttpclientUtil;
import com.liwenjie.gmall1122.gmallorderservice.mapper.OrderDetailMapper;
import com.liwenjie.gmall1122.gmallorderservice.mapper.OrderInfoMapper;
import com.liwenjie.gmall1122.service.OrderService;
import com.liwenjie.gmall1122.service.PaymentService;
import com.liwenjie.gmall1122.status.PaymentStatus;
import com.liwenjie.gmall1122.status.ProcessStatus;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Reference
    private PaymentService paymentService;

    //保存订单信息
    @Override
    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;
    }
    //判断数据库内库存数量是否满足购买数量
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpclientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)){
            return  true;
        }else {
            return  false;
        }

    }
    // 生成流水号
    public  String getTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,10*60,tradeCode);
        jedis.close();
        return tradeCode;
    }
    // 验证流水号
    public  boolean checkTradeCode(String userId,String tradeCodeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode!=null && tradeCode.equals(tradeCodeNo)){
            return  true;
        }else{
            return false;
        }
    }
    //根据orderId查询订单信息
    @Override
    public OrderInfo getOrderInfoByOrderId(String orderId) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;
    }

    // 删除流水号
    public void  delTradeCode(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }
    //更新数据库内订单的状态信息
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }
    //发送消息给库存模块，更新库存信息
    @Override
    public void sendOrderStatus(String orderId) {
        //得到连接
        Connection connection = activeMQUtil.getConnection();
        // 得到要发送的消息
        String orderJson = initWareOrder(orderId);
        //创建session
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建消息队列
            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建消息生产者
            MessageProducer producer = session.createProducer(queue);
            //封装消息
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(orderJson);
            //发送消息
            producer.send(textMessage);
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
    //得到失效的订单
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus", PaymentStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);
    }
    //处理失效的订单
    @Async
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 订单信息
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        // 付款信息
        paymentService.closePayment(orderInfo.getId());

    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        // 1 先查询原始订单
        OrderInfo orderInfoOrigin = getOrderInfoByOrderId(orderId);
        // 2 wareSkuMap 反序列化
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        // 3 遍历拆单方案
        for (Map map : maps) {
            String wareId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            // 4 生成订单主表，从原始订单复制，新的订单号，父订单
            OrderInfo subOrderInfo = new OrderInfo();
            try {
                BeanUtils.copyProperties(subOrderInfo,orderInfoOrigin);
            } catch (Exception e) {
                e.printStackTrace();
            }
            subOrderInfo.setId(null);
            // 5 原来主订单，订单主表中的订单状态标志为拆单
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
            subOrderInfo.setWareId(wareId);

            // 6 明细表 根据拆单方案中的skuids进行匹配，得到那个的子订单
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            // 创建一个新的订单集合
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                for (String skuId : skuIds) {
                    if (skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }
            }
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            subOrderInfo.sumTotalAmount();
            // 7 保存到数据库中
            saveOrder(subOrderInfo);
            subOrderInfoList.add(subOrderInfo);
        }
        updateOrderStatus(orderId,ProcessStatus.SPLIT);
        // 8 返回一个新生成的子订单列表
        return subOrderInfoList;

    }

    //得到要发送给库存模块的消息
    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfoByOrderId(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);

    }
    //封装orderInfo内的orderDetailList为map
    public Map initWareOrder(OrderInfo orderInfo) {

        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getOrderComment());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","1");
        map.put("wareId",orderInfo.getWareId());

        // 组合json
        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }
        map.put("details",detailList);
        return map;
    }

}
