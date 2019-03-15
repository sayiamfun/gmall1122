package com.liwenjie.gmall1122.gmallmanageservice.mapper;

import com.liwenjie.gmall1122.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> listSkuSaleAttrValueBySpu(String spuId);
}
