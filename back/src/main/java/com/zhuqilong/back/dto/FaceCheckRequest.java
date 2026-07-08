package com.zhuqilong.back.dto;

public class FaceCheckRequest {
    private String result;
    private String photoData;
    
    public FaceCheckRequest() {}
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getPhotoData() {
        return photoData;
    }
    
    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }
}
