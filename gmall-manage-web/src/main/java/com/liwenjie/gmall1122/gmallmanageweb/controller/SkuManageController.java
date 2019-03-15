package com.liwenjie.gmall1122.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liwenjie.gmall1122.bean.SkuInfo;
import com.liwenjie.gmall1122.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManageController {

    @Reference
    private SkuInfoService skuInfoService;

    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
        return "success";
    }

    @RequestMapping("deleteSkuInfo")
    @ResponseBody
    public String deleteSkuInfo(String skuId){
        skuInfoService.deleteSkuInfo(skuId);
        return "success";
    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody()
    public List<SkuInfo> skuInfoListBySpu(String spuId){
        return skuInfoService.listSkuInfo(spuId);
    }
}
