package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.*;

import java.util.List;

public interface SpuInfoService {

    List<SpuInfo> listSpuInfo(String cataLog3Id);

    List<BaseSaleAttr> listBaseSaleAttr();

    List<SpuImage> listSpuImage(String spuId);

    List<SpuSaleAttr> listSpuSaleAttr(String spuId);

    void saveSpuInfo(SpuInfo spuInfo);

    void deleteSpuInfo(String spuId);

    List<SpuSaleAttrValue> listSpuSaleAttrValue(SpuSaleAttrValue spuSaleAttrValue);

    List<SpuSaleAttr> ListSpuSaleAttrBySpuIdAndSkuId(SkuInfo skuInfo);
}
