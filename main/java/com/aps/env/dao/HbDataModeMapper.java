package com.aps.env.dao;

import com.aps.env.entity.HbDataMode;
import com.aps.env.entity.HbDataModeExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HbDataModeMapper {
    int countByExample(HbDataModeExample example);

    int deleteByExample(HbDataModeExample example);

    int deleteByPrimaryKey(String dataGuid);

    int insert(HbDataMode record);

    int insertSelective(HbDataMode record);

    List<HbDataMode> selectByExample(HbDataModeExample example);

    HbDataMode selectByPrimaryKey(String dataGuid);

    int updateByExampleSelective(@Param("record") HbDataMode record, @Param("example") HbDataModeExample example);

    int updateByExample(@Param("record") HbDataMode record, @Param("example") HbDataModeExample example);

    int updateByPrimaryKeySelective(HbDataMode record);

    int updateByPrimaryKey(HbDataMode record);

    //----------- additional ---------------
    int createTable(HbDataMode record);

    int insertSelectiveByNode(HbDataMode record);
}