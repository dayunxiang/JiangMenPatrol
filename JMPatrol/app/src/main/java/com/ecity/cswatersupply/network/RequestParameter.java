package com.ecity.cswatersupply.network;

import java.util.HashMap;
import java.util.Map;

import com.ecity.android.httpforandroid.http.HttpBaseServiceParameters;

public class RequestParameter extends HttpBaseServiceParameters {

    public interface IRequestParameter {
        Map<String, String> toMap();
    }

    public RequestParameter(String url) {
        this.url = url;
    }

    public RequestParameter(String url, IRequestParameter parameter) {
        this.url = url;
        this.requestParams = (parameter == null) ? new HashMap<String, String>() : parameter.toMap();
    }

    @Override
    public Map<String, String> generateRequestParams() throws Exception {
        return this.requestParams;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
