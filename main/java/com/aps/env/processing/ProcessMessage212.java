package com.aps.env.processing;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.DateUtil;
import com.aps.env.comm.JsonUtil;
import com.aps.env.comm.StringUtil;
import com.aps.env.communication.NettyServer;
import com.aps.env.dao.HbDataModeMapper;
import com.aps.env.dao.HbDataRecordMapper;
import com.aps.env.dao.HbNodeAlarmMapper;
import com.aps.env.dao.HbNodeMapper;
import com.aps.env.entity.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: DealMessage212
 * @Description:TODO
 * @author: AppleShow
 * @date: 2016年11月2日 下午9:45:21
 * @since 1.0.0
 */
@Service
public class ProcessMessage212 implements ProcessMessage {
    @Resource
    private HbNodeMapper hbNodeMapper;
    @Resource
    private HbDataRecordMapper hbDataRecordMapper;
    @Resource
    private HbDataModeMapper hbDataModeMapper;
    @Resource
    private HbNodeAlarmMapper hbNodeAlarmMapper;

    private static final Logger LOG = LogManager.getLogger(ProcessMessage212.class);


    /**
     * @param message
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public void saveMessage(Message message) {
        insertMessage(message);
    }

    /**
     * @param message
     */
    private void insertMessage(Message message) {
        final HbDataMode hbDataMode = new HbDataMode();
        final Date nowDate = new Date();
        final String recordId = UUID.randomUUID().toString();
        final String nodeData = message.getMessageBodyTailor();
        final ObjectNode jsonPar = JsonUtil.getObjectNodeInstance();

        String checkMessageError = null;
        String mn = null;
        int nodeId = 0;


        hbDataMode.setDataGuid(recordId);
        hbDataMode.setProperty0(String.format("%s @ %s", message.getFromHost(), message.getReceiveDate()));
        if (nodeData.length() > 2 && nodeData.startsWith("##") && nodeData.indexOf("&&") > 0 && nodeData.indexOf("DataTime=") > 0) {
            List<String[]> dataList = Arrays.asList(nodeData.split("&&")).stream().map(item -> item.split(";")).collect(Collectors.toList());
            if (dataList.size() >= 2) {
                Arrays.asList(dataList.get(0)).stream().filter(
                        item -> item.startsWith("MN=")).findFirst().ifPresent(
                        item -> format2KeyValue(item).ifPresent(keyValue -> hbDataMode.setNodeMn(keyValue.getValue())));
                if (CommUtil.getHbNodeCache().containsKey(hbDataMode.getNodeMn())) {
                    mn = hbDataMode.getNodeMn();
                    nodeId = CommUtil.getHbNodeCache().get(mn);

                    NettyServer.findManagedConnection(message.getId()).ifPresent(connection -> {
                                String thisNodeMn = hbDataMode.getNodeMn();
                                int thisNodeId = CommUtil.getHbNodeCache().get(thisNodeMn);
                                String thisNodeName = CommUtil.getNodeNameCache().get(thisNodeId);

                                if (connection.getManagedNodeMap().containsKey(thisNodeId)) {
                                    connection.getManagedNodeMap().get(thisNodeId).setNodeName(thisNodeName);
                                    connection.getManagedNodeMap().get(thisNodeId).setNodeMn(thisNodeMn);
                                    connection.getManagedNodeMap().get(thisNodeId).setActiveAt(nowDate);
                                    connection.getManagedNodeMap().get(thisNodeId).setActiveData(message.getMessageBody());
                                } else {
                                    ManagedNode managedNode = new ManagedNode();
                                    managedNode.setNodeId(thisNodeId);
                                    managedNode.setNodeMn(thisNodeMn);
                                    managedNode.setNodeName(thisNodeName);
                                    managedNode.setActiveAt(nowDate);
                                    managedNode.setActiveData(message.getMessageBody());

                                    connection.addManagedNode(managedNode);
                                }
                            }
                    );

                    hbDataMode.setNodeMn(String.valueOf(nodeId));
                    Arrays.asList(dataList.get(0)).stream().filter(
                            item -> item.startsWith("CN=")).findFirst().ifPresent(
                            item -> format2KeyValue(item).ifPresent(keyValue -> hbDataMode.setDataType(keyValue.getValue())));
                    Arrays.asList(dataList.get(1)).stream().filter(item -> item.startsWith("DataTime=")).findFirst().ifPresent(
                            item -> format2KeyValue(item)
                                    .ifPresent(
                                            keyValue -> hbDataMode.setDataTime(DateUtil.fromString(keyValue.getValue(), DateUtil.SIMPLE_DATE_FORMAT2))));
                    for (String item : dataList.get(1)) {
                        if (item.indexOf(",") > 0) {
                            Arrays.asList(item.split(",")).stream().forEach(
                                    par -> format2KeyValue(par).ifPresent(
                                            keyValue -> jsonPar.put(keyValue.getKey(), keyValue.getValue())));
                        } else if (!item.startsWith("DataTime=")) {
                            format2KeyValue(item).ifPresent(keyValue -> jsonPar.put(keyValue.getKey(), keyValue.getValue()));
                        }
                    }
                    if (jsonPar.size() > 0) {
                        switch (hbDataMode.getDataType()) {
                            case "2011":// 实时数据
                                hbDataMode.setNodeData(jsonPar.toString().replace(CommUtil.HB_DATA_RTD212, ""));
                                break;
                            case "2051":// 分钟数据（5分钟或10分钟）
                                hbDataMode.setNodeData(jsonPar.toString().replace(CommUtil.HB_DATA_AVG212, ""));
                                break;
                            case "2061":// 小时数据
                                hbDataMode.setNodeData(jsonPar.toString().replace(CommUtil.HB_DATA_AVG212, ""));
                                break;
                            default:
                                hbDataMode.setNodeData(jsonPar.toString());
                                break;
                        }
                        hbDataMode.setPrstatus(0);
                        hbDataMode.setPrflag(nodeId);
                        hbDataMode.setItime(nowDate);
                        hbDataMode.setUtime(nowDate);
                    } else {
                        checkMessageError = "Can not find item data!";
                    }
                } else {
                    checkMessageError = "Can not find MN: [" + mn + "]!";
                }
            } else {
                checkMessageError = "Can not format data using DealMessage212,can not find key word [;]!";
            }
        } else {
            checkMessageError = "Can not format data using DealMessage212!";
        }

        if (null == checkMessageError) {
            hbDataModeMapper.insertSelectiveByNode(hbDataMode);
            LOG.debug(String.format("Processed message successfully from %s,%s", message.getFromHost(), mn));

            // 小时数据异常检查
            if ("2061".equals(hbDataMode.getDataType())) {
                if (CommUtil.getNodeTypeItemCacheO().containsKey(nodeId)) {
                    checkNodeData(nodeId, CommUtil.getNodeNameCache().get(nodeId), mn, jsonPar, hbDataMode.getDataTime(), CommUtil.getNodeTypeItemCacheO().get(nodeId));
                }
            }
        } else {
            HbDataRecord hbDataRecord = new HbDataRecord();

            hbDataRecord.setRecordGuid(recordId);
            hbDataRecord.setRecordData(nodeData);
            hbDataRecord.setPrexp(checkMessageError);
            hbDataRecord.setProperty0(message.getFromHost());
            hbDataRecord.setItime(nowDate);
            hbDataRecord.setUtime(nowDate);
            hbDataRecordMapper.insertSelective(hbDataRecord);
            LOG.info(String.format("Analysis message failed from %s,%s", message.getFromHost(), mn));
        }
    }

    /**
     * @param data
     * @return Optional<KeyValue>
     * @Title: format2KeyValue
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    private Optional<KeyValue> format2KeyValue(String data) {
        Optional<KeyValue> optional;

        if (data.indexOf("=") > 0) {
            String[] array = data.split("=");
            KeyValue keyValue;
            if (array.length >= 2) {
                keyValue = new KeyValue(array[0], array[1]);
            } else {
                keyValue = new KeyValue(array[0], "");
            }
            optional = Optional.of(keyValue);
        } else {
            optional = Optional.ofNullable(null);
        }

        return optional;
    }

    /**
     * @param nodeId
     * @param nodeName
     * @param nodeMN
     * @param nodeItemValue
     * @param valueDate
     * @param hbTypeItemNodeList
     */
    private void checkNodeData(int nodeId, String nodeName, String nodeMN, ObjectNode nodeItemValue, Date valueDate, List<HbTypeItemNode> hbTypeItemNodeList) {
        Date now = new Date();

        for (HbTypeItemNode hbTypeItemNode : hbTypeItemNodeList) {
            if (hbTypeItemNode.isSelectMonitorItem() && hbTypeItemNode.isAlarmItem()) {
                String itemId = String.format("%s%s", hbTypeItemNode.getItemId(), CommUtil.HB_DATA_AVG212);
                if (nodeItemValue.has(itemId)) {
                    String itemValueString = nodeItemValue.get(itemId).asText();

                    if (!StringUtil.isNullOrEmpty(itemValueString)) {
                        String alarm = "";
                        float itemValueFloat = Float.valueOf(itemValueString);

                        if (null != hbTypeItemNode.getItemVmin() && Float.valueOf(hbTypeItemNode.getItemVmin().toString()) > itemValueFloat) {
                            alarm = String.format("下限%s%s", hbTypeItemNode.getItemVmin(), null != hbTypeItemNode.getItemUnit() ? hbTypeItemNode.getItemUnit() : "");
                        } else if (null != hbTypeItemNode.getItemVmax() && Float.valueOf(hbTypeItemNode.getItemVmax().toString()) < itemValueFloat) {
                            alarm = String.format("上限%s%s", hbTypeItemNode.getItemVmax(), null != hbTypeItemNode.getItemUnit() ? hbTypeItemNode.getItemUnit() : "");
                        } else {
                            if (null != hbTypeItemNode.getItemVala3() && Float.valueOf(hbTypeItemNode.getItemVala3().toString()) < itemValueFloat) {
                                alarm = String.format("三级阀值%s%s", hbTypeItemNode.getItemVala3(), null != hbTypeItemNode.getItemUnit() ? hbTypeItemNode.getItemUnit() : "");
                            } else if (null != hbTypeItemNode.getItemVala2() && Float.valueOf(hbTypeItemNode.getItemVala2().toString()) < itemValueFloat) {
                                alarm = String.format("二级阀值%s%s", hbTypeItemNode.getItemVala2(), null != hbTypeItemNode.getItemUnit() ? hbTypeItemNode.getItemUnit() : "");
                            } else if (null != hbTypeItemNode.getItemVala1() && Float.valueOf(hbTypeItemNode.getItemVala1().toString()) < itemValueFloat) {
                                alarm = String.format("一级阀值%s%s", hbTypeItemNode.getItemVala1(), null != hbTypeItemNode.getItemUnit() ? hbTypeItemNode.getItemUnit() : "");
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(alarm)) {
                            HbNodeAlarm hbNodeAlarm = new HbNodeAlarm();

                            hbNodeAlarm.setGuid(UUID.randomUUID().toString());
                            hbNodeAlarm.setNodeId(nodeId);
                            hbNodeAlarm.setNodeMn(nodeMN);
                            hbNodeAlarm.setNodeName(nodeName);
                            hbNodeAlarm.setItemCode(hbTypeItemNode.getItemId());
                            hbNodeAlarm.setItemName(hbTypeItemNode.getItemName());
                            hbNodeAlarm.setItemValue(itemValueString);
                            hbNodeAlarm.setItemDate(DateUtil.formatString(valueDate, DateUtil.SIMPLE_DATE_FORMAT1));
                            hbNodeAlarm.setItemAlarm(alarm);
                            hbNodeAlarm.setPrflag(0);
                            hbNodeAlarm.setItime(now);

                            hbNodeAlarmMapper.insertSelective(hbNodeAlarm);
                        }
                    }
                }
            }
        }
    }
}
