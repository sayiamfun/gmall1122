package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.CartInfo;

import java.util.List;

public interface CartService {
    void  addToCart(String skuId,String userId,Integer skuNum);

    List<CartInfo> CartListByUserId(String userId);

    List<CartInfo> selectCartListWithCurPrice(String userId);

    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoFromCookie, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);

    void deleteSkuFromCartBatch(Integer[] skuId, String userId);

    void cartAdd(String skuId, String userId);

    void cartDec(String skuId, String userId);
}
