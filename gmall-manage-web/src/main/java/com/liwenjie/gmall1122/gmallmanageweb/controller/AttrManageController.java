package com.liwenjie.gmall1122.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.liwenjie.gmall1122.bean.*;
import com.liwenjie.gmall1122.service.ListService;
import com.liwenjie.gmall1122.service.ManageService;
import com.liwenjie.gmall1122.service.SkuInfoService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrManageController {

    @Reference
    private ManageService manageService;
    @Reference
    private SkuInfoService skuInfoService;
    @Reference
    private ListService listService;

    @RequestMapping("cataLog1List")
    @ResponseBody
    public List<BaseCatalog1> cataLog1List(){
        return manageService.listCatalog1();
    }

    @RequestMapping("cataLog2List")
    @ResponseBody
    public List<BaseCatalog2> cataLog2List(String cataLog1Id){
        return manageService.listCatalog2ByCataLog1Id(cataLog1Id);
    }

    @RequestMapping("cataLog3List")
    @ResponseBody
    public List<BaseCatalog3> cataLog3List(String cataLog2Id){
        return manageService.listCatalog3ByCataLog2Id(cataLog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.listAttrInfoByCataLog3Id(catalog3Id);
    }

    @RequestMapping("attrValueList")
    @ResponseBody
    public List<BaseAttrValue> attrValueList(String attrInfoId){
        return manageService.listAttrValueByAttrInfoId(attrInfoId);
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo attrInfo){
        manageService.saveAttrInfo(attrInfo);
        return "success";
    }

    @RequestMapping("deleteAttrInfo")
    @ResponseBody
    public String deleteAttrInfo(String attrInfoId){
        manageService.deleteInfoByAttrInfoId(attrInfoId);
        return "success";
    }

    @RequestMapping("onSale")
    @ResponseBody
    public void onSale(String skuId){
        SkuInfo skuInfo = skuInfoService.getSkuInfoBySkuId(skuId);
       SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }


}
