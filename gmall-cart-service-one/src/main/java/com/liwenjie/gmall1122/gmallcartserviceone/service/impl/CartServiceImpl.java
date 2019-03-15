package com.liwenjie.gmall1122.gmallcartserviceone.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.CartInfo;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.config.utils.RedisUtil;
import com.liwenjie.gmall1122.config.utils.CartConst;
import com.liwenjie.gmall1122.gmallcartserviceone.mapper.CartInfoMapper;
import com.liwenjie.gmall1122.service.CartService;
import com.liwenjie.gmall1122.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    private int cartNum = 1;

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private SkuInfoService skuInfoService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void cartDec(String skuId, String userId) {
        //修改数据库内数据
        CartInfo cartInfoSe = new CartInfo();
        cartInfoSe.setSkuId(skuId);
        cartInfoSe.setUserId(userId);
        CartInfo cartInfo = cartInfoMapper.selectOne(cartInfoSe);
        if(cartInfo!=null){
            cartInfo.setSkuNum(cartInfo.getSkuNum()-cartNum);
            cartInfoMapper.updateByPrimaryKey(cartInfoSe);
        }
        //修改jedis内购物车数据
        Jedis jedis = redisUtil.getJedis();
        String userCarKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        String hget = jedis.hget(userCarKey, skuId);
        if(hget!=null){
            CartInfo cartInfo1 = JSON.parseObject(hget, CartInfo.class);
            cartInfo1.setSkuNum(cartInfo1.getSkuNum()-cartNum);
            jedis.hset(userCarKey,skuId,JSON.toJSONString(cartInfo1));
            //修改购物车内已选中商品数据
            String userCartCheckKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            if("1".equals(cartInfo1.getIsChecked())){
                String hget1 = jedis.hget(userCartCheckKey, skuId);
                if(hget1!=null){
                    CartInfo cartInfo2 = JSON.parseObject(hget1, CartInfo.class);
                    cartInfo2.setSkuNum(cartInfo2.getSkuNum()-cartNum);
                    jedis.hset(userCartCheckKey, skuId, JSON.toJSONString(cartInfo2));
                }
            }
        }
        jedis.close();
    }

    /**
     * 购物车加号
     * @param skuId
     * @param userId
     */
    @Override
    public void cartAdd(String skuId, String userId) {
        //修改数据库内数据
        CartInfo cartInfoSe = new CartInfo();
        cartInfoSe.setSkuId(skuId);
        cartInfoSe.setUserId(userId);
        CartInfo cartInfo = cartInfoMapper.selectOne(cartInfoSe);
        if(cartInfo!=null){
            cartInfo.setSkuNum(cartInfo.getSkuNum()+cartNum);
            cartInfoMapper.updateByPrimaryKey(cartInfoSe);
        }
        //修改jedis内购物车数据
        Jedis jedis = redisUtil.getJedis();
        String userCarKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        String hget = jedis.hget(userCarKey, skuId);
        if(hget!=null){
            CartInfo cartInfo1 = JSON.parseObject(hget, CartInfo.class);
            cartInfo1.setSkuNum(cartInfo1.getSkuNum()+cartNum);
            jedis.hset(userCarKey,skuId,JSON.toJSONString(cartInfo1));
            //修改购物车内已选中商品数据
            String userCartCheckKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            if("1".equals(cartInfo1.getIsChecked())){
                String hget1 = jedis.hget(userCartCheckKey, skuId);
                if(hget1!=null){
                    CartInfo cartInfo2 = JSON.parseObject(hget1, CartInfo.class);
                    cartInfo2.setSkuNum(cartInfo2.getSkuNum()+cartNum);
                    jedis.hset(userCartCheckKey, skuId, JSON.toJSONString(cartInfo2));
                }
            }
        }
        jedis.close();
    }

    /**
     * 批量删除购物车数据
     * @param skuId
     * @param userId
     */
    @Override
    public void deleteSkuFromCartBatch(Integer[] skuId, String userId) {
        //删除数据库内数据
        for (int i = 0; i < skuId.length; i++) {
            CartInfo cartInfoDe = new CartInfo();
            cartInfoDe.setSkuId(skuId[i].toString());
            cartInfoMapper.delete(cartInfoDe);
        }
        //删除jedis内购物车数据
        Jedis jedis = redisUtil.getJedis();
        String userCarKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartInfos = jedis.hvals(userCarKey);
        if(cartInfos!=null && cartInfos.size()>0) {
            for (String cartInfoStr : cartInfos) {
                CartInfo cartInfo = JSON.parseObject(cartInfoStr, CartInfo.class);
                for (int i = 0; i < skuId.length; i++) {
                    String skuIdStr = skuId[i].toString();
                    if (cartInfo.getSkuId().equals(skuIdStr)) {
                        jedis.hdel(userCarKey, skuIdStr);
                    }
                }
            }
        }
        //删除购物车内已选中商品数据
        String userCartCheckKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> userCartChecks = jedis.hvals(userCartCheckKey);
        if(userCartChecks!=null && userCartChecks.size()>0) {
            for (String userCartCheck : userCartChecks) {
                CartInfo cartInfo = JSON.parseObject(userCartCheck, CartInfo.class);
                for (int i = 0; i < skuId.length; i++) {
                    if (cartInfo.getSkuId().equals(skuId[i].toString())) {
                        jedis.hdel(userCartCheckKey, cartInfo.getSkuId());
                    }
                }
            }
        }
        jedis.close();
    }

    /**
     * 添加商品到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //判断数据库内是否已经存在此商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoSe = cartInfoMapper.selectOne(cartInfo);

        if(cartInfoSe!=null){
            //如果存在，直接修改数据即可
            cartInfoSe.setSkuNum(cartInfoSe.getSkuNum()+skuNum);
            cartInfoMapper.updateByPrimaryKey(cartInfoSe);
        }else{
            //如果不存在，则将数据插入数据库
            SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoSe = cartInfo;
        }
        //插入数据库后将数据保存到redis
        String userCarKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Boolean exists = jedis.exists(userCarKey);
        if(!exists){
            loadCartCache(userId);
        }
        String cartInfoJson = JSON.toJSONString(cartInfoSe);
        jedis.hset(userCarKey, skuId, cartInfoJson);
        //将购物车数据信息的过期时间设置成与userInfo信息的过期时间一致
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCarKey,ttl.intValue());
        jedis.close();
    }

    /**
     * 根据userId查询购物车信息
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> CartListByUserId(String userId) {
        //先从redis获取数据
        String userCarKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartList = jedis.hvals(userCarKey);
        List<CartInfo> cartInfos = new ArrayList<>();
        if(cartList!=null && cartList.size()>0) {
            //redis中有数据
            for (String cartInfo : cartList) {
                CartInfo cartInfo1 = JSON.parseObject(cartInfo, CartInfo.class);
                cartInfos.add(cartInfo1);
            }
        }else{
            //如果redis没有则从数据库获取数据，并且存入redis一份
            cartInfos = loadCartCache(userId);
        }
        return cartInfos;
    }

    /**
     * 从数据库中加载购物车数据并存入redis中
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        Map<String,String> checkMap = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
            if(cartInfo.getIsChecked().equals("1")){
                checkMap.put(cartInfo.getSkuId(),cartJson);
            }
        }
        // 将java list - redis hash
        //将所有的购物车数据重新放入redis中
        if(map.size()>0) {
            jedis.hmset(userCartKey, map);
        }
        //将选中的商品数据重新放入redis中
        if(checkMap.size()>0) {
            String userCartCheckKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            jedis.hmset(userCartCheckKey, checkMap);
        }
        jedis.close();
        return  cartInfoList;

    }
    /**
     * 根据userId查询对应的购物车内数据和所有商品对应的实时价格
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> selectCartListWithCurPrice(String userId) {
        return cartInfoMapper.selectCartListWithCurPrice(userId);
    }

    /**
     * 得到redis中的购物车选中的商品集合
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;

    }

    /**
     * 合并Cookie中的购物车信息到数据库
     * @param cartInfoFromCookie
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 循环开始匹配
        for (CartInfo cartInfoCk : cartInfoFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){
                    if(cartInfoCk.getIsChecked().equals("1")){
                        cartInfoDB.setIsChecked(cartInfoCk.getIsChecked());
                    }
                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            // 数据库中没有购物车，则直接将cookie中购物车添加到数据库
            if (!isMatch){
                cartInfoCk.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCk);
            }
        }
        // 从新在数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        return cartInfoList;

    }

    /**
     * 修改购物车中商品是否被选中
     * @param skuId
     * @param isChecked
     * @param userId
     */
    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        //修改购物车中商品选中属性
        cartInfo.setIsChecked(isChecked);
        //修改完毕重新放入redis
        String cartCheckdJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);

        // 新增到已选中购物车中
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            //如果修改为选中，添加
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else{
            //如果修改为不选中，删除
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }
}
