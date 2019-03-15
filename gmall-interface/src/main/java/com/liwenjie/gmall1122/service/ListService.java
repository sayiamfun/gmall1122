package com.liwenjie.gmall1122.service;


import com.liwenjie.gmall1122.bean.SkuLsInfo;
import com.liwenjie.gmall1122.bean.SkuLsParams;
import com.liwenjie.gmall1122.bean.SkuLsResult;

import java.util.List;

public interface ListService {

    void saveSkuInfo(SkuLsInfo skuLsInfo);

    SkuLsResult search(SkuLsParams skuLsParams);

    void incrHotScore(String skuId);
}
