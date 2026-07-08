package com.zhuqilong.back.dto;

public class RecheckRequest {
    private Long elderId;
    private String result;
    private String photoData;
    private Boolean adminAssisted;
    private Integer originalRecordId;
    private String recheckReason;
    
    public RecheckRequest() {}
    
    public Long getElderId() {
        return elderId;
    }
    
    public void setElderId(Long elderId) {
        this.elderId = elderId;
    }
    
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
    
    public Boolean getAdminAssisted() {
        return adminAssisted;
    }
    
    public void setAdminAssisted(Boolean adminAssisted) {
        this.adminAssisted = adminAssisted;
    }
    
    public Integer getOriginalRecordId() {
        return originalRecordId;
    }
    
    public void setOriginalRecordId(Integer originalRecordId) {
        this.originalRecordId = originalRecordId;
    }
    
    public String getRecheckReason() {
        return recheckReason;
    }
    
    public void setRecheckReason(String recheckReason) {
        this.recheckReason = recheckReason;
    }
}
