package com.liwenjie.gmall1122.gmallcartserviceone.mapper;

import com.liwenjie.gmall1122.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
