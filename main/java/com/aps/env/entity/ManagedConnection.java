package com.aps.env.entity;

import com.aps.env.comm.DateUtil;

import java.util.*;

/**
 *
 */
public class ManagedConnection {
    private String id;
    private Map<Integer, String> node;
    private String startAt;
    private String activeAt;

    public ManagedConnection(String id) {
        this.id = id;
        this.node = new HashMap<>();
        this.startAt = DateUtil.formatString(new Date(), DateUtil.SIMPLE_DATE_FORMAT1);
    }

    public boolean hasNodeId(int nodeId) {
        return this.node.containsKey(nodeId);
    }

    public String getNodeMn(int nodeId) {
        return this.node.get(nodeId);
    }

    public ManagedConnection removeNode(int nodeId) {
        this.node.remove(nodeId);
        return this;
    }

    public String getId() {
        return id;
    }

    public ManagedConnection setId(String id) {
        this.id = id;
        return this;
    }

    public ManagedConnection addNode(int nodeId, String nodeMn) {
        this.node.put(nodeId, nodeMn);
        return this;
    }

    public Map<Integer, String> getNode() {
        return this.node;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getActiveAt() {
        return activeAt;
    }

    public ManagedConnection setActiveAt(String activeAt) {
        this.activeAt = activeAt;
        return this;
    }
}
