package com.liwenjie.gmall1122.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liwenjie.gmall1122.bean.*;
import com.liwenjie.gmall1122.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SpuManageController {

    @Reference
    private SpuInfoService spuInfoService;

    @RequestMapping("spuInfoList")
    @ResponseBody
    public List<SpuInfo> spuInfoList(String cataLog3Id){
        return spuInfoService.listSpuInfo(cataLog3Id);
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        return spuInfoService.listBaseSaleAttr();
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        return spuInfoService.listSpuImage(spuId);
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuInfoService.listSpuSaleAttr(spuId);
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }
        return spuSaleAttrList;
    }

    @RequestMapping("spuSaleAttrValueList")
    @ResponseBody
    public List<SpuSaleAttrValue> spuSaleAttrValueList(String spuId,String saleAttrId){
        SpuSaleAttrValue spuSaleAttrValue=new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuId);
        spuSaleAttrValue.setSaleAttrId(saleAttrId);
        List<SpuSaleAttrValue> spuSaleAttrValueList = spuInfoService.listSpuSaleAttrValue(spuSaleAttrValue);
        return spuSaleAttrValueList;
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        spuInfoService.saveSpuInfo(spuInfo);
        return "success";
    }

    @RequestMapping("deleteSpuInfo")
    @ResponseBody
    public String deleteSpuInfo(String spuId){
        spuInfoService.deleteSpuInfo(spuId);
        return "success";
    }

}
