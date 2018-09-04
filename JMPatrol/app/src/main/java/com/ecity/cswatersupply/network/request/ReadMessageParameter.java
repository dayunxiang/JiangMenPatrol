package com.ecity.cswatersupply.network.request;

import java.util.HashMap;
import java.util.Map;

import com.ecity.cswatersupply.network.RequestParameter.IRequestParameter;

public class ReadMessageParameter implements IRequestParameter {
    private int messageId;

    public ReadMessageParameter(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("gid", String.valueOf(messageId));

        return map;
    }
}