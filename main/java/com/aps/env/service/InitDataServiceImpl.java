package com.aps.env.service;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.JsonUtil;
import com.aps.env.communication.NettyServer;
import com.aps.env.dao.HbDataLatestMapper;
import com.aps.env.dao.HbNodeMapper;
import com.aps.env.entity.HbDataLatestExample;
import com.aps.env.entity.HbNode;
import com.aps.env.entity.HbNodeExample;
import com.aps.env.entity.HbTypeItemNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        hbNodeMapper.selectByExample(hbNodeExample).stream().forEach(node -> putHbNode(node));
        LOG.info("Completing initializes HBNODE information!");

        NettyServer.getManagedConnections().forEach((id, connection) -> {
            final List<Integer> removeNodeIds = new ArrayList<>();
            connection.getNode().forEach((nodeId, nodeMn) -> {
                if (!CommUtil.getHbNodeCache().containsKey(nodeMn)) {
                    removeNodeIds.add(nodeId);
                }
            });
            removeNodeIds.forEach(nodeId -> connection.removeNode(nodeId));
        });

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


    /**
     * @param hbNode
     */
    private void putHbNode(HbNode hbNode) {
        CommUtil.getHbNodeCache().put(hbNode.getNodeMn(), hbNode.getNodeId());
        CommUtil.getNodeNameCache().put(hbNode.getNodeId(), hbNode.getNodeName());
        if (!CommUtil.getNodeTypeItemCacheS().containsKey(hbNode.getNodeId()) || !CommUtil.getNodeTypeItemCacheS().get(hbNode.getNodeId()).equals(hbNode.getNodeItem())) {
            CommUtil.getNodeTypeItemCacheS().remove(hbNode.getNodeId());
            CommUtil.getNodeTypeItemCacheO().remove(hbNode.getNodeId());

            CommUtil.getNodeTypeItemCacheS().put(hbNode.getNodeId(), hbNode.getNodeItem());
            try {
                final Map<String, String> hbTypeItemNodeMap = JsonUtil.getObjectMapper().readValue(hbNode.getNodeItem(), Map.class);

                if (null != hbTypeItemNodeMap) {
                    final List<HbTypeItemNode> hbTypeItemNodeList = new ArrayList<>();

                    hbTypeItemNodeMap.forEach((itemId, hbTypeItem) -> {
                        try {
                            HbTypeItemNode hbTypeItemNode = JsonUtil.getObjectMapper().readValue(hbTypeItem, HbTypeItemNode.class);
                            if (null != hbTypeItemNode) {
                                hbTypeItemNodeList.add(hbTypeItemNode);
                            }
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                    });

                    CommUtil.getNodeTypeItemCacheO().put(hbNode.getNodeId(), hbTypeItemNodeList);
                }
            } catch (IOException e) {
                LOG.error(e);
            }
            LOG.info("Completing reload HB_NODE_TYPE_ITEM information!");
        }
    }
}
