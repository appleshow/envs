package com.aps.env.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aps.env.comm.JsonUtil;
import com.aps.env.dao.HbNodeAlarmMapper;
import com.aps.env.dao.HbNodeContactMapper;
import com.aps.env.entity.HbNodeAlarm;
import com.aps.env.entity.HbNodeAlarmExample;
import com.aps.env.entity.HbNodeContact;
import com.aps.env.entity.HbNodeContactExample;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 *
 */
@Service
public class SendMessage2AliCloud implements SendMessage {
    @Autowired
    private Environment environment;
    @Resource
    private HbNodeContactMapper hbNodeContactMapper;
    @Resource
    private HbNodeAlarmMapper hbNodeAlarmMapper;
    private static final Logger LOG = LogManager.getLogger(SendMessage2AliCloud.class);

    @Override
    public String sendMessage() throws UnsupportedEncodingException {
        final Date now = new Date();
        HbNodeContactExample hbNodeContactExample = new HbNodeContactExample();
        hbNodeContactExample.createCriteria().andPrflagEqualTo(1);
        int total = 0, success = 0;

        List<HbNodeContact> hbNodeContactList = hbNodeContactMapper.selectByExample(hbNodeContactExample);
        if (null == hbNodeContactList) {
            hbNodeContactList = new ArrayList<>();
        }

        final Map<Integer, String> nodePhones = new HashMap<>();
        hbNodeContactList.forEach(hbNodeContact -> {
            if (nodePhones.containsKey(hbNodeContact.getNodeId())) {
                nodePhones.put(hbNodeContact.getNodeId(), String.format("%s,%s", nodePhones.get(hbNodeContact.getNodeId()), hbNodeContact.getPersonPhone()));
            } else {
                nodePhones.put(hbNodeContact.getNodeId(), hbNodeContact.getPersonPhone());
            }
        });

        HbNodeAlarmExample hbNodeAlarmExample = new HbNodeAlarmExample();
        hbNodeAlarmExample.createCriteria().andPrflagEqualTo(0);
        List<HbNodeAlarm> hbNodeAlarmList = hbNodeAlarmMapper.selectByExample(hbNodeAlarmExample);
        if (null != hbNodeAlarmList && hbNodeAlarmList.size() > 0) {
            final String accessKeyId = environment.getProperty("accessKeyId");
            final String accessKeySecret = environment.getProperty("accessKeySecret");
            final String signName = environment.getProperty("signName");
            final String templateCode = environment.getProperty("templateCode");

            total = hbNodeAlarmList.size();
            for (HbNodeAlarm hbNodeAlarm : hbNodeAlarmList) {
                if (aliDsmsApi(hbNodeAlarm, nodePhones, accessKeyId, accessKeySecret, signName, templateCode, now)) {
                    success++;
                }
            }
        }

        return total == 0 ? "There are no SMS to send." : String.format("Send SMS %d, success %d, failure %d", total, success, total - success);
    }

    /**
     * @param hbNodeAlarm
     * @param nodePhones
     * @param accessKeyId
     * @param accessKeySecret
     * @param signName
     * @param templateCode
     * @param now
     * @return
     */
    private boolean aliDsmsApi(HbNodeAlarm hbNodeAlarm, Map<Integer, String> nodePhones, String accessKeyId, String accessKeySecret, String signName, String templateCode, Date now) {
        final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
        final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
        final ObjectNode jsonPar = JsonUtil.getObjectNodeInstance();
        boolean state;

        try {
            HbNodeAlarm hbNodeAlarmUpdate = new HbNodeAlarm();
            hbNodeAlarmUpdate.setGuid(hbNodeAlarm.getGuid());
            hbNodeAlarmUpdate.setPrflag(1);
            if (nodePhones.containsKey(hbNodeAlarm.getNodeId())) {
                final String phones = nodePhones.get(hbNodeAlarm.getNodeId());
                //初始化ascClient,暂时不支持多region（请勿修改）
                IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId,
                        accessKeySecret);
                DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
                IAcsClient acsClient = new DefaultAcsClient(profile);
                //组装请求对象
                SendSmsRequest request = new SendSmsRequest();
                //使用post提交
                request.setMethod(MethodType.POST);
                //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
                request.setPhoneNumbers(phones);
                //必填:短信签名-可在短信控制台中找到
                request.setSignName(signName);
                //必填:短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
                request.setTemplateCode(templateCode);
                //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
                //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
                jsonPar.put("nodeName", hbNodeAlarm.getNodeName());
                jsonPar.put("nodePar", hbNodeAlarm.getItemName());
                jsonPar.put("parValue", hbNodeAlarm.getItemValue());
                jsonPar.put("parTime", hbNodeAlarm.getItemDate());
                jsonPar.put("parLevel", hbNodeAlarm.getItemAlarm());
                request.setTemplateParam(jsonPar.toString());
                //可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
                //request.setSmsUpExtendCode("90997");
                //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
                //request.setOutId("yourOutId");
                //请求失败这里会抛ClientException异常
                SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

                if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
                    state = true;
                } else {
                    hbNodeAlarmUpdate.setPrinf(sendSmsResponse.getMessage());
                    state = false;
                }
            } else {
                state = false;
                hbNodeAlarmUpdate.setPrinf("Unable to find contact information.");
            }
            hbNodeAlarmUpdate.setUtime(now);

            hbNodeAlarmMapper.updateByPrimaryKeySelective(hbNodeAlarmUpdate);
        } catch (ClientException e) {
            LOG.error(e.getMessage());
            state = false;
        }

        return state;
    }
}
