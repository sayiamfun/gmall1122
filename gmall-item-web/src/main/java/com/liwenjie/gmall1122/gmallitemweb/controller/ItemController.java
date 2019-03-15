package com.liwenjie.gmall1122.gmallitemweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liwejie.gmall1122.LoginRequire;
import com.liwenjie.gmall1122.bean.SkuImage;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.bean.SkuSaleAttrValue;
import com.liwenjie.gmall1122.bean.SpuSaleAttr;
import com.liwenjie.gmall1122.service.ListService;
import com.liwenjie.gmall1122.service.SkuInfoService;
import com.liwenjie.gmall1122.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private SkuInfoService skuInfoService;
    @Reference
    private SpuInfoService spuInfoService;
    @Reference
    private ListService listService;

    @LoginRequire(autoRedirect = false)
    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable("skuId")String skuId , Map<String , Object> map){
        //取得skuInfo的数据
        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
        //取得skuImage的数据
        List<SkuImage> skuImageList = skuInfoService.listSkuImageBySkuId(skuId);
        //设置默认图片
        for (SkuImage skuImage : skuImageList) {
            if(skuImage.getIsDefault().equals("1")){
                skuInfo.setSkuDefaultImg(skuImage.getImgUrl());
            }
        }
        //spuSaleAttrs
        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.ListSpuSaleAttrBySpuIdAndSkuId(skuInfo);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfoService.listSkuSaleAttrValueBuSpuId(skuInfo);
        String skuSaleAttrValeJson = "";
        Map<String , String> saleAttrValueMap = new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
             if((i+2)%2 != 0){
                 skuSaleAttrValeJson += "|";
             }
             skuSaleAttrValeJson += skuSaleAttrValueList.get(i).getSaleAttrValueId();
             if((i+1)<skuSaleAttrValueList.size()){
                 if(!skuSaleAttrValueList.get(i).getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                     saleAttrValueMap.put(skuSaleAttrValeJson , skuSaleAttrValueList.get(i).getSkuId());
                     skuSaleAttrValeJson = "";
                 }
             }else{
                 saleAttrValueMap.put(skuSaleAttrValeJson , skuSaleAttrValueList.get(i).getSkuId());
             }

        }
        //把map变成json串
        String valuesSkuJson = JSON.toJSONString(saleAttrValueMap);


        skuInfo.setSkuImageList(skuImageList);
        map.put("skuInfo" , skuInfo);
        map.put("spuSaleAttrs" , spuSaleAttrs);
        map.put("valuesSkuJson" , valuesSkuJson);

        listService.incrHotScore(skuId);
        return "item";
    }
}
