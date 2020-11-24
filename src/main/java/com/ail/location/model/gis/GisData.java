package com.ail.location.model.gis;
import java.util.List;

public class GisData {

    private int code;
    private String message;
    private List<GisResult> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GisResult> getResult() {
        return result;
    }

    public void setResult(List<GisResult> result) {
        this.result = result;
    }

}
