package com.aps.env.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 *
 */
public class ManagedNode {
    private int nodeId;
    private String nodeMn;
    private String nodeName;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activeAt;
    private String activeData;

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeMn() {
        return nodeMn;
    }

    public void setNodeMn(String nodeMn) {
        this.nodeMn = nodeMn;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getActiveAt() {
        return activeAt;
    }

    public void setActiveAt(Date activeAt) {
        this.activeAt = activeAt;
    }

    public String getActiveData() {
        return activeData;
    }

    public void setActiveData(String activeData) {
        this.activeData = activeData;
    }
}
