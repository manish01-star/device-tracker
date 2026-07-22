package com.example.devicetrackerapp.dto;

public class AudioItem {

    private String audioName;

    private String audioPath;

    private long audioSize;

    private long duration;

    public AudioItem() {
    }

    public AudioItem(String audioName,
                     String audioPath,
                     long audioSize,
                     long duration) {

        this.audioName = audioName;
        this.audioPath = audioPath;
        this.audioSize = audioSize;
        this.duration = duration;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public long getAudioSize() {
        return audioSize;
    }

    public void setAudioSize(long audioSize) {
        this.audioSize = audioSize;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}