package com.liwenjie.gmall1122.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liwenjie.gmall1122.bean.*;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.*;
import com.liwenjie.gmall1122.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Override
    public List<SpuInfo> listSpuInfo(String cataLog3Id) {
        SpuInfo selSpuInfo = new SpuInfo();
        selSpuInfo.setCatalog3Id(cataLog3Id);
        List<SpuInfo> spuInfoList = spuInfoMapper.select(selSpuInfo);
        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> listBaseSaleAttr() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<SpuImage> listSpuImage(String spuId) {
        //image
        SpuImage spuImageSel = new SpuImage();
        spuImageSel.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImageSel);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> listSpuSaleAttr(String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;
    }

    @Override
    public List<SpuSaleAttrValue> listSpuSaleAttrValue(SpuSaleAttrValue spuSaleAttrValue) {
        List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
        return spuSaleAttrValueList;
    }

    @Override
    public List<SpuSaleAttr> ListSpuSaleAttrBySpuIdAndSkuId(SkuInfo skuInfo) {
        return spuSaleAttrMapper.ListSpuSaleAttrBySpuIdAndSkuId(Long.parseLong(skuInfo.getSpuId()),Long.parseLong(skuInfo.getId()));
    }


    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        if(spuInfo.getId()==null || spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }
        SpuImage spuImageDe = new SpuImage();
        spuImageDe.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImageDe);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()!=0) {
            for (SpuImage spuImage : spuImageList) {
                spuImage.setId(null);
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        SpuSaleAttr spuSaleAttrDe = new SpuSaleAttr();
        spuSaleAttrDe.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttrDe);

        SpuSaleAttrValue spuSaleAttrValueDe = new SpuSaleAttrValue();
        spuSaleAttrValueDe.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValueDe);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()!=0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setId(null);
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    spuSaleAttrValue.setId(null);
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                }
            }
        }


    }

    @Override
    public void deleteSpuInfo(String spuId) {
        SpuInfo spuInfoDe = new SpuInfo();
        spuInfoDe.setId(spuId);
        spuInfoMapper.delete(spuInfoDe);

        SpuImage spuImageDe = new SpuImage();
        spuImageDe.setSpuId(spuId);
        spuImageMapper.delete(spuImageDe);

        SpuSaleAttr spuSaleAttrDe = new SpuSaleAttr();
        spuSaleAttrDe.setSpuId(spuId);
        spuSaleAttrMapper.delete(spuSaleAttrDe);

        SpuSaleAttrValue spuSaleAttrValueDe = new SpuSaleAttrValue();
        spuSaleAttrValueDe.setSpuId(spuId);
        spuSaleAttrValueMapper.delete(spuSaleAttrValueDe);
    }


}
