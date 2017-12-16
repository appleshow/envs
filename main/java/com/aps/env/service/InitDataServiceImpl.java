package com.aps.env.service;

import com.aps.env.comm.CommUtil;
import com.aps.env.dao.HbNodeMapper;
import com.aps.env.entity.HbNodeExample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    private static final Logger LOG = LogManager.getLogger(InitDataServiceImpl.class);

    @Override
    public void initHbNode() {
        HbNodeExample hbNodeExample = new HbNodeExample();
        hbNodeExample.createCriteria().andDeleteFlagEqualTo(0);
        hbNodeMapper.selectByExample(hbNodeExample).stream().forEach(node -> CommUtil.putHbNode(node));
        LOG.info("Completing initializes HBNODE information!");
    }
}
