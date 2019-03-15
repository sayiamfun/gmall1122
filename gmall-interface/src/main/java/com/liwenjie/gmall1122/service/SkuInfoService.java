package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.SkuImage;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.bean.SkuSaleAttrValue;

import java.util.List;

public interface SkuInfoService {
    void saveSkuInfo(SkuInfo skuInfo);

    void deleteSkuInfo(String skuId);

    List<SkuInfo> listSkuInfo(String spuId);

    SkuInfo getSkuInfoBySkuId(String skuId);

    List<SkuImage> listSkuImageBySkuId(String skuId);

    List<SkuSaleAttrValue> listSkuSaleAttrValueBuSpuId(SkuInfo skuInfo);
}
