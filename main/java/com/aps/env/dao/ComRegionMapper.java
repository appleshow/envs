package com.aps.env.dao;

import com.aps.env.entity.ComRegion;
import com.aps.env.entity.ComRegionExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ComRegionMapper {
    int countByExample(ComRegionExample example);

    int deleteByExample(ComRegionExample example);

    int deleteByPrimaryKey(String regionId);

    int insert(ComRegion record);

    int insertSelective(ComRegion record);

    List<ComRegion> selectByExample(ComRegionExample example);

    ComRegion selectByPrimaryKey(String regionId);

    int updateByExampleSelective(@Param("record") ComRegion record, @Param("example") ComRegionExample example);

    int updateByExample(@Param("record") ComRegion record, @Param("example") ComRegionExample example);

    int updateByPrimaryKeySelective(ComRegion record);

    int updateByPrimaryKey(ComRegion record);
}