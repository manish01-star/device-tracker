package com.example.devicetrackerapp.dto;

public class VideoItem {

    private String videoName;

    private String videoPath;

    private long videoSize;

    private long duration;

    public VideoItem() {
    }

    public VideoItem(String videoName,
                     String videoPath,
                     long videoSize,
                     long duration) {

        this.videoName = videoName;
        this.videoPath = videoPath;
        this.videoSize = videoSize;
        this.duration = duration;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public long getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(long videoSize) {
        this.videoSize = videoSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}