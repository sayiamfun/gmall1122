package com.liwenjie.gmall1122.service;

import com.liwenjie.gmall1122.bean.*;

import java.util.List;

public interface ManageService {

    public List<BaseCatalog1> listCatalog1();

    public List<BaseCatalog2> listCatalog2ByCataLog1Id(String catalog1Id);

    public List<BaseCatalog3> listCatalog3ByCataLog2Id(String catalog2Id);

    public List<BaseAttrInfo> listAttrInfoByCataLog3Id(String catalog3Id);

    public List<BaseAttrValue> listAttrValueByAttrInfoId(String AttrInfoId);

    void saveAttrInfo(BaseAttrInfo attrInfo);

    void deleteInfoByAttrInfoId(String attrInfoId);

    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);

}
