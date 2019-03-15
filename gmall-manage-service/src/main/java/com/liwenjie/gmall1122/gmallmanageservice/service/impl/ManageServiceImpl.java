package com.liwenjie.gmall1122.gmallmanageservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.liwenjie.gmall1122.bean.*;
import com.liwenjie.gmall1122.gmallmanageservice.mapper.*;
import com.liwenjie.gmall1122.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseCatalog1> listCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> listCatalog2ByCataLog1Id(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> listCatalog3ByCataLog2Id(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> listAttrInfoByCataLog3Id(String catalog3Id) {
        return baseAttrInfoMapper.selectAttrInfoList(Long.parseLong(catalog3Id));
    }

    @Override
    public List<BaseAttrValue> listAttrValueByAttrInfoId(String attrInfoId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrInfoId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo attrInfo) {
          System.out.println(attrInfo.getId()+"  "+attrInfo.getAttrName()+"  "+attrInfo.getCatalog3Id());
        //如果有主键就进行更新，如果没有就插入
        if(attrInfo.getId()!=null&&attrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(attrInfo);
        }else{
            //防止主键被赋上一个空字符串
            if(attrInfo.getId().length()==0){
                attrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(attrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(attrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性值
        if(attrInfo.getAttrValueList()!=null&&attrInfo.getAttrValueList().size()>0) {
            for (BaseAttrValue attrValue : attrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if(attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(attrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

    }

    @Override
    public void deleteInfoByAttrInfoId(String attrInfoId) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setId(attrInfoId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrInfoId);
        baseAttrValueMapper.delete(baseAttrValue);
        baseAttrInfoMapper.delete(baseAttrInfo);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        return baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
    }

}
