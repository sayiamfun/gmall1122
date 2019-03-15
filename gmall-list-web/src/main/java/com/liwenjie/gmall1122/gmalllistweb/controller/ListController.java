package com.liwenjie.gmall1122.gmalllistweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.liwenjie.gmall1122.bean.BaseAttrInfo;
import com.liwenjie.gmall1122.bean.BaseAttrValue;
import com.liwenjie.gmall1122.bean.SkuLsParams;
import com.liwenjie.gmall1122.bean.SkuLsResult;
import com.liwenjie.gmall1122.service.ListService;
import com.liwenjie.gmall1122.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
//    @ResponseBody 第一个：可以将数据直接显示到页面，第二个作用：@ResponseBody返回Json字符串 它底层使用的哪个jar包来实现的？ jackson！gson！
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){

        try {
            // 设置每页显示的条数
            // skuLsParams.setPageSize(2);
            SkuLsResult skuLsResult = listService.search(skuLsParams);


            //  显示平台属性名称，平台属性值名称 skuLsResult.getAttrValueIdList()
            List<String> attrValueIdList = skuLsResult.getAttrValueIdList();

            //  制作一个连接url;
            String urlParam = makeUrlParam(skuLsParams);

            // 存储面包屑的集合
            ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

            //  通过平台属性值Id 查询数据
            List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);
        /*
            baseAttrInfoList = 存储的是页面显示的平台属性，属性值！
            点击一次，将点击的平台属性值，从该集合中移除！
            关键两点：
                第一点我怎么知道你点击的是谁？ 通过valueId
                第二点如何处理 集合在循环过程中，就删除数据！使用迭代器！
         */
            //  获取集合中的所有数据进行遍历 baseAttrInfoList 使用迭代器遍历 itco ， itar ，iter。
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                // 取出所有平台属性值的集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                if (attrValueList!=null && attrValueIdList.size()>0){
                    if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                        for (BaseAttrValue baseAttrValue : attrValueList) {
                            //   baseAttrValue.getId(); 平台属性值Id 与 skuLsParams中的valueId 做比较
                            for (String valueId : skuLsParams.getValueId()) {
                                if (valueId.equals(baseAttrValue.getId())){
                                    iterator.remove();
                                    // 声明一个平台属性值对象
                                    BaseAttrValue baseAttrValueSelect = new BaseAttrValue();
                                    // setValueName 改为面包屑的值！属性名：属性值名
                                    baseAttrValueSelect.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                                    // 将 urlParam 里面的url参数从新赋值！{将valueId} 进行数据移除！
                                    String newUrlParam = makeUrlParam(skuLsParams, baseAttrValue.getId());
                                    // 放入最新的参数
                                    baseAttrValueSelect.setUrlParam(newUrlParam);
                                    // 将面包屑添加到集合中！
                                    baseAttrValueArrayList.add(baseAttrValueSelect);
                                }
                            }
                        }
                    }
                }
            }


            // 从第几页开始查询
            request.setAttribute("pageNo",skuLsParams.getPageNo());
            // 保存总页数
            request.setAttribute("totalPages",skuLsResult.getTotalPages());
            // 保存查询条件的URL
            request.setAttribute("urlParam",urlParam);

            // 存储平台属性
            request.setAttribute("baseAttrInfoList",baseAttrInfoList);

            // 保存面包屑的集合
            request.setAttribute("baseAttrValuesList",baseAttrValueArrayList);

            // 将skuLsInfo 的集合进行保存
            request.setAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return "list";
        }
    }


    // 新增一个字段{valueId}  String... 可变参数
    private String makeUrlParam(SkuLsParams skuLsParams,String...excludeValueIds) {
        String urlParam = "";
        // http://list.gmall.com/list.html?keyword=小米
        // 拼接关键字
        if (skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        // 三级分类Id
        // http://list.gmall.com/list.html?keyword=小米&catalog3Id=61
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            // 添加一个& 符号
            if (urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        // 平台属性值Id
        // http://list.gmall.com/list.html?keyword=小米&catalog3Id=61&valueId=83&valueId=84
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

            // 循环遍历集合中的值，进行拼接
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];

                if (excludeValueIds!=null && excludeValueIds.length>0){
                    // 取出里面的数据
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;
                    }
                }
                // 准备开始拼接
                if (urlParam.length()>0){
                    urlParam+="&";
                }
                urlParam+="valueId="+valueId;
            }
        }
        return urlParam;
    }
}
