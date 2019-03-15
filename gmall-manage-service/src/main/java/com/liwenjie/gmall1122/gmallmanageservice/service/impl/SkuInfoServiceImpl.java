package com.liwenjie.gmall1122.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.SkuAttrValue;
import com.liwenjie.gmall1122.bean.SkuImage;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.bean.SkuSaleAttrValue;
import com.liwenjie.gmall1122.config.utils.RedisUtil;
import com.liwenjie.gmall1122.gmallmanageservice.constant.ManageConst;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.SkuAttrValueMapper;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.SkuImageMapper;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.SkuInfoMapper;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.SkuSaleAttrValueMapper;
import com.liwenjie.gmall1122.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    //保存skuInfo信息
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        if(skuInfo.getId()==null || skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else{
            skuInfoMapper.updateByPrimaryKey(skuInfo);
        }
        //image
        SkuImage skuImageDe = new SkuImage();
        skuImageDe.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImageDe);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList!=null && skuImageList.size()>0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setId(null);
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
        //attrValue
        SkuAttrValue skuAttrValueDe = new SkuAttrValue();
        skuAttrValueDe.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValueDe);
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size()>0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setId(null);
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
        //saleAttrValue
        SkuSaleAttrValue skuSaleAttrValueDe = new SkuSaleAttrValue();
        skuSaleAttrValueDe.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValueDe);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setId(null);
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
    }

    //删除skuInfo信息
    @Override
    public void deleteSkuInfo(String skuId) {

        skuInfoMapper.deleteByPrimaryKey(skuId);
        //image
        SkuImage skuImageDe = new SkuImage();
        skuImageDe.setSkuId(skuId);
        skuImageMapper.delete(skuImageDe);
        //attrValue
        SkuAttrValue skuAttrValueDe = new SkuAttrValue();
        skuAttrValueDe.setSkuId(skuId);
        skuAttrValueMapper.delete(skuAttrValueDe);
        //saleAttrValue
        SkuSaleAttrValue skuSaleAttrValueDe = new SkuSaleAttrValue();
        skuSaleAttrValueDe.setSkuId(skuId);
        skuSaleAttrValueMapper.delete(skuSaleAttrValueDe);
    }

    //根据spuId得到所有的skuInfo信息
    @Override
    public List<SkuInfo> listSkuInfo(String spuId) {
        SkuInfo skuInfoSe = new SkuInfo();
        skuInfoSe.setSpuId(spuId);
        return skuInfoMapper.select(skuInfoSe);
    }

    //根据skuId得到skuInfo信息  从redis中
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        SkuInfo skuInfo = null;
        String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
        try {
            Jedis jedis = redisUtil.getJedis();
            if(jedis.exists(skuInfoKey)){
                skuInfo = JSON.parseObject(jedis.get(skuInfoKey), SkuInfo.class);
            }else{
                String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockKey)){
                    skuInfo = getSkuInfoDB(skuId);
                    String skuInsoJson = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, skuInsoJson);
                }else{
                    Thread.sleep(1000);
                    return getSkuInfoBySkuId(skuId);
                }
            }
            jedis.close();
        }catch (Exception e){
            e.printStackTrace();
            //如果Redis宕机则直接从数据库中查找信息
            skuInfo = getSkuInfoDB(skuId);
        }
        return skuInfo;
    }

    //根据skuId得到skuInfo信息  从mysql中
    public SkuInfo getSkuInfoDB(String skuId){
        // 单纯的信息
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        // 查询图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> imageList = skuImageMapper.select(skuImage);
        // 将查询出来所有图片赋予对象
        skuInfo.setSkuImageList(imageList);
        // 查询属性值
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        // 将查询出来所有商品属性值赋给对象
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        //SkuAttrValue
        SkuAttrValue skuAttrValueSe = new SkuAttrValue();
        skuAttrValueSe.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValueSe);
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }

    //根据skuId查询skuImage信息
    @Override
    public List<SkuImage> listSkuImageBySkuId(String skuId) {
        SkuImage skuImageSe = new SkuImage();
        skuImageSe.setSkuId(skuId);
        return skuImageMapper.select(skuImageSe);
    }

    //根据skuId查询skuSaleAttrValue信息
    @Override
    public List<SkuSaleAttrValue> listSkuSaleAttrValueBuSpuId(SkuInfo skuInfo) {
        return skuSaleAttrValueMapper.listSkuSaleAttrValueBySpu(skuInfo.getSpuId());
    }


}
