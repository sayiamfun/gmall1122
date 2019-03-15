package com.liwenjie.gmall1122.gmallmanageservice.constant;

public class ManageConst {

    //skuInfoKey前缀
    public static final String SKUKEY_PREFIX="sku:";
    //skuInfoKey后缀
    public static final String SKUKEY_SUFFIX=":info";
    //skuInfoKey存在超时销毁时间
    public static final int SKUKEY_TIMEOUT=24*60*60;
    //skuInfoKey请求连接超时时间
    public static final int SKULOCK_EXPIRE_PX=10000;
    //skuInfoLock的后缀
    public static final String SKULOCK_SUFFIX=":lock";


}
