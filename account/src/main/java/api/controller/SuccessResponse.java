package api.controller;

import java.util.HashMap;
import java.util.Map;


public class SuccessResponse {

    private Map<String, Object> response = new HashMap<>();

    public SuccessResponse() {
        response.put("success", true);
    }

    public SuccessResponse put(String key, Object value) {
        response.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return response;
    }
}
