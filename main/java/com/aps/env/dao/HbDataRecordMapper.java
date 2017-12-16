package com.aps.env.dao;

import com.aps.env.entity.HbDataRecord;
import com.aps.env.entity.HbDataRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HbDataRecordMapper {
    int countByExample(HbDataRecordExample example);

    int deleteByExample(HbDataRecordExample example);

    int deleteByPrimaryKey(String recordGuid);

    int insert(HbDataRecord record);

    int insertSelective(HbDataRecord record);

    List<HbDataRecord> selectByExample(HbDataRecordExample example);

    HbDataRecord selectByPrimaryKey(String recordGuid);

    int updateByExampleSelective(@Param("record") HbDataRecord record, @Param("example") HbDataRecordExample example);

    int updateByExample(@Param("record") HbDataRecord record, @Param("example") HbDataRecordExample example);

    int updateByPrimaryKeySelective(HbDataRecord record);

    int updateByPrimaryKey(HbDataRecord record);
}