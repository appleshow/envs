package com.aps.env.service;

import com.aps.env.comm.CommUtil;
import com.aps.env.dao.HbDataLatestMapper;
import com.aps.env.dao.HbNodeMapper;
import com.aps.env.entity.HbDataLatestExample;
import com.aps.env.entity.HbNode;
import com.aps.env.entity.HbNodeExample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <dl>
 * <dt>com.aps.env.service.InitDataServiceImpl</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2017/12/13</dd>
 * </dl>
 *
 * @author appleshow
 */
@Service
public class InitDataServiceImpl implements InitDataService {
    @Resource
    private HbNodeMapper hbNodeMapper;
    @Resource
    private HbDataLatestMapper hbDataLatestMapper;

    private static final Logger LOG = LogManager.getLogger(InitDataServiceImpl.class);

    @Override
    public void initHbNode() {
        HbNodeExample hbNodeExample = new HbNodeExample();
        hbNodeExample.createCriteria().andDeleteFlagEqualTo(0);
        hbNodeMapper.selectByExample(hbNodeExample).stream().forEach(node -> CommUtil.putHbNode(node));
        LOG.info("Completing initializes HBNODE information!");
    }

    @Override
    public void initHbNodeStatus() {
        HbNodeExample hbNodeExample = new HbNodeExample();
        hbNodeExample.createCriteria().andNodeIdIsNotNull();
        HbNode hbNode = new HbNode();
        hbNode.setPrflag(0);
        hbNode.setUtime(new Date());

        hbNodeMapper.updateByExampleSelective(hbNode, hbNodeExample);
    }

    @Override
    public void delHbDataLatest() {
        Date nowDate = new Date();
        int hours = 12;
        HbDataLatestExample hbDataLatestExample = new HbDataLatestExample();

        hbDataLatestExample.createCriteria().andDataTimeLessThan(new Date(nowDate.getTime() - 1000 * 60 * 60 * hours));
        hbDataLatestMapper.deleteByExample(hbDataLatestExample);
    }
}
