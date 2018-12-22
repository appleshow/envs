package com.aps.env.comm;

import com.aps.env.entity.ComOrgFormRights;
import com.aps.env.entity.HbNode;
import com.aps.env.entity.HbTypeItemNode;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * <dl>
 * <dt>com.aps.env.comm.CommUtil</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2017/11/7</dd>
 * </dl>
 *
 * @author appleshow
 */
public class CommUtil {
    public final static String CONTEXT_PATH = "ctx";
    public final static String LOCK_WORD = "jmzfc369";
    public final static String LOADED = "loaded";
    public final static String SESSION_VERIFICATION = "verification";
    public final static String SESSION_PERSON_ID = "personId";
    public final static String SESSION_USER_ID = "userId";
    public final static String SESSION_USER_NAME = "userName";
    public final static String SESSION_USER_PERMISSIONS = "userPermissions";

    public final static String REQUEST_SQL_DML_TYPE = "_type";

    public final static String MODIFY_TYPE_INSERT = "I";
    public final static String MODIFY_TYPE_UPDATE = "U";
    public final static String MODIFY_TYPE_DELETE = "D";

    public final static String HB_DATA_CUR = "HB_DATA_CUR";
    public final static String HB_DATA_HIS = "HB_DATA_HIS";
    public final static String HB_DATA_RTD212 = "-Rtd";
    public final static String HB_DATA_AVG212 = "-Avg";

    public final static Integer AVAILABLE = 0;
    public final static Integer DELETE = 1;

    private final static HashMap<String, Integer> HB_NODE_MN = new HashMap<>();
    private final static HashMap<Integer, String> HB_NODE_NAME = new HashMap<>();
    private final static HashMap<Integer, String> HB_NODE_ITEM_S = new HashMap<>();
    private final static HashMap<Integer, List<HbTypeItemNode>> HB_NODE_ITEM_O = new HashMap<>();

    /**
     * @throws @since 1.0.0
     * @Title: updatePermissoned
     * @Description: TODO
     * @param: @param
     * httpSession
     * @param: @param
     * personPermissions
     * @return: void
     */
    public static void updatePermissoned(HttpSession httpSession, List<ComOrgFormRights> personPermissions) {
        httpSession.removeAttribute(SESSION_USER_PERMISSIONS);
        if (null != personPermissions) {
            httpSession.setAttribute(SESSION_USER_PERMISSIONS, personPermissions);
        }
    }

    /**
     * @throws @since 1.0.0
     * @Title: isPermissoned
     * @Description: TODO
     * @param: @param
     * httpSession
     * @param: @param
     * formId
     * @param: @param
     * action
     * @param: @param
     * responseData
     * @param: @return
     * @return: boolean
     */
    public static boolean isPermissoned(HttpSession httpSession, int formId, String action, ResponseData responseData) {
        int permissionedCount = 0;
        boolean isPermissioined = false;
        Object sessionPermissions = httpSession.getAttribute(SESSION_USER_PERMISSIONS);

        if (null == httpSession.getAttribute(SESSION_PERSON_ID)) {
            responseData.setCode(-201);
            responseData.setMessage("未登录，您无法访问此功能！");

            isPermissioined = false;
        } else if (null == sessionPermissions) {
            responseData.setCode(-202);
            responseData.setMessage("权限不足，您无法访问此功能！");

            isPermissioined = false;
        } else {
            @SuppressWarnings("unchecked")
            List<ComOrgFormRights> personPermissions = (List<ComOrgFormRights>) sessionPermissions;

            for (ComOrgFormRights orgFormRights : personPermissions) {
                if (orgFormRights.getFormId() == formId && orgFormRights.getRightId().equals(action)) {
                    if (1 == CommUtil.nvl(orgFormRights.getRea())) {
                        isPermissioined = false;
                        responseData.setCode(-203);
                        responseData.setMessage("功能已被锁定，您无法访问此功能！");
                        return isPermissioined;
                    }
                    permissionedCount += CommUtil.nvl(orgFormRights.getRel());
                }
            }

            if (0 == permissionedCount) {
                responseData.setCode(-204);
                responseData.setMessage("权限不足，您无法访问此功能！");

                isPermissioined = false;
            } else {
                isPermissioined = true;
            }
        }

        return isPermissioined;
    }

    /**
     * @throws @since 1.0.0
     * @Title: nvl
     * @Description: TODO
     * @param: @param
     * value
     * @param: @param
     * defaule
     * @param: @return
     * @return: int
     */
    public static int nvl(Integer value, int defaule) {
        return null == value ? defaule : value;
    }

    /**
     * @throws @since 1.0.0
     * @Title: nvl
     * @Description: TODO
     * @param: @param
     * value
     * @param: @return
     * @return: int
     */
    public static int nvl(Integer value) {
        return nvl(value, 0);
    }

    /**
     * @throws @since 1.0.0
     * @Title: nvl
     * @Description: TODO
     * @param: @param
     * value
     * @param: @param
     * defaule
     * @param: @return
     * @return: String
     */
    public static String nvl(String value, String defaule) {
        return null == value ? defaule : value;
    }

    /**
     * @throws @since 1.0.0
     * @Title: nvl
     * @Description: TODO
     * @param: @param
     * value
     * @param: @return
     * @return: String
     */
    public static String nvl(String value) {
        return nvl(value, "");
    }

    /**
     * @return
     * @Title: getHbNodeCache
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public static HashMap<String, Integer> getHbNodeCache() {
        return HB_NODE_MN;
    }

    /**
     * @return
     */
    public static HashMap<Integer, String> getNodeNameCache() {
        return HB_NODE_NAME;
    }

    /**
     * @return
     */
    public static HashMap<Integer, String> getNodeTypeItemCacheS() {
        return HB_NODE_ITEM_S;
    }

    /**
     * @return
     */
    public static HashMap<Integer, List<HbTypeItemNode>> getNodeTypeItemCacheO() {
        return HB_NODE_ITEM_O;
    }

    /**
     * @param e
     * @return String
     * @Title: getMessageFromException
     * @Description: TODO
     * @throws:
     * @since 1.0.0
     */
    public static String getMessageFromException(Exception e) {
        String message = "";
        int messageLength = 100;
        if (e != null && !StringUtil.isNullOrEmpty(e.getMessage())) {
            message = e.getMessage();
            message.replace('\r', ' ');
            String[] messages = message.split("\n");
            for (int index = 0; index < message.length(); index++) {
                if (!StringUtil.isNullOrEmpty(messages[index].trim())) {
                    message = messages[index];
                    break;
                }
            }
            if (message.length() > messageLength) {
                message = message.substring(0, messageLength);
            }
        }
        return message;
    }

    /**
     * @param list
     * @return
     */
    public static boolean isEmptyList(List<?> list) {
        return list == null ? true : list.size() == 0 ? true : false;
    }
}
