package com.aps.env.dao;

import com.aps.env.entity.ComPerson;
import com.aps.env.entity.ComPersonExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ComPersonMapper {
    int countByExample(ComPersonExample example);

    int deleteByExample(ComPersonExample example);

    int deleteByPrimaryKey(Integer personId);

    int insert(ComPerson record);

    int insertSelective(ComPerson record);

    List<ComPerson> selectByExample(ComPersonExample example);

    ComPerson selectByPrimaryKey(Integer personId);

    int updateByExampleSelective(@Param("record") ComPerson record, @Param("example") ComPersonExample example);

    int updateByExample(@Param("record") ComPerson record, @Param("example") ComPersonExample example);

    int updateByPrimaryKeySelective(ComPerson record);

    int updateByPrimaryKey(ComPerson record);

    //----------- additional ---------------
}