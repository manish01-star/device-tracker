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

    // Video Sync
    private Boolean refreshVideos;
    private Boolean videosUploaded;

    //Audio
    private Boolean refreshAudios;
    private Boolean audiosUploaded;

    //Mic Recording
    private Boolean refreshMic;
    private Boolean micUploaded;
    private Integer micDuration;

    private Boolean refreshCamera;

    private Boolean cameraStreaming;

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
}