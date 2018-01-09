package com.aps.env.processing;

import com.aps.env.comm.CommUtil;
import com.aps.env.comm.DateUtil;
import com.aps.env.comm.JsonUtil;
import com.aps.env.dao.HbDataModeMapper;
import com.aps.env.dao.HbDataRecordMapper;
import com.aps.env.entity.HbDataMode;
import com.aps.env.entity.HbDataRecord;
import com.aps.env.entity.KeyValue;
import com.aps.env.entity.Message;
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
    private HbDataRecordMapper hbDataRecordMapper;
    @Resource
    private HbDataModeMapper hbDataModeMapper;
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
        Date nowDate = new Date();
        String recordId = UUID.randomUUID().toString();
        String nodeData = message.getMessageBodyTailor();
        String checkMessageError = null;
        int nodeId;

        hbDataMode.setDataGuid(recordId);
        hbDataMode.setProperty0(message.getFromHost());
        if (nodeData.length() > 2 && nodeData.startsWith("##") && nodeData.indexOf("&&") > 0 && nodeData.indexOf("DataTime=") > 0) {
            List<String[]> dataList = Arrays.asList(nodeData.split("&&")).stream().map(item -> item.split(";")).collect(Collectors.toList());
            if (dataList.size() >= 2) {
                Arrays.asList(dataList.get(0)).stream().filter(item -> item.startsWith("MN=")).findFirst().ifPresent(item -> {
                    format2KeyValue(item).ifPresent(keyValue -> hbDataMode.setNodeMn(keyValue.getValue()));
                });
                if (CommUtil.getHbNodeCache().containsKey(hbDataMode.getNodeMn())) {
                    nodeId = CommUtil.getHbNodeCache().get(hbDataMode.getNodeMn());
                    hbDataMode.setNodeMn(String.valueOf(nodeId));

                    Arrays.asList(dataList.get(0)).stream().filter(item -> item.startsWith("CN=")).findFirst().ifPresent(item -> {
                        format2KeyValue(item).ifPresent(keyValue -> hbDataMode.setDataType(keyValue.getValue()));
                    });
                    Arrays.asList(dataList.get(1)).stream().filter(item -> item.startsWith("DataTime=")).findFirst().ifPresent(item -> {
                        format2KeyValue(item)
                                .ifPresent(keyValue -> hbDataMode.setDataTime(DateUtil.fromString(keyValue.getValue(), DateUtil.SIMPLE_DATE_FORMAT2)));
                    });
                    final ObjectNode jsonPar = JsonUtil.getObjectNodeInstance();
                    for (String item : dataList.get(1)) {
                        if (item.indexOf(",") > 0) {
                            Arrays.asList(item.split(",")).stream().forEach(par -> {
                                format2KeyValue(par).ifPresent(keyValue -> {
                                    jsonPar.put(keyValue.getKey(), keyValue.getValue());
                                });
                            });
                        } else if (!item.startsWith("DataTime=")) {
                            format2KeyValue(item).ifPresent(keyValue -> {
                                jsonPar.put(keyValue.getKey(), keyValue.getValue());
                            });

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
                    checkMessageError = "Can not find MN: [" + hbDataMode.getNodeMn() + "]!";
                }
            } else {
                checkMessageError = "Can not format data using DealMessage212,can not find key word [;]!";
            }
        } else {
            checkMessageError = "Can not format data using DealMessage212!";
        }

        if (null == checkMessageError) {
            hbDataModeMapper.insertSelectiveByNode(hbDataMode);
            LOG.debug("Processed message successfully from " + message.getFromHost());
        } else {
            HbDataRecord hbDataRecord = new HbDataRecord();

            hbDataRecord.setRecordGuid(recordId);
            hbDataRecord.setRecordData(nodeData);
            hbDataRecord.setPrexp(checkMessageError);
            hbDataRecord.setProperty0(message.getFromHost());
            hbDataRecord.setItime(nowDate);
            hbDataRecord.setUtime(nowDate);
            hbDataRecordMapper.insertSelective(hbDataRecord);
            LOG.info("Analysis message failed from " + message.getFromHost());
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
}
