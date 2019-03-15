package com.liwenjie.gmall1122.gmallmanageservice.mapper;

import com.liwenjie.gmall1122.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    List<BaseAttrInfo> selectAttrInfoList(long catalog3Id);

    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("attrValueIds") String attrValueIds);
}
