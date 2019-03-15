package com.liwenjie.gmall1122.gmallmanageservice.mapper;

import com.liwenjie.gmall1122.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    List<SpuSaleAttr> ListSpuSaleAttrBySpuIdAndSkuId(long spuId, long skuId);
}
