package com.example.devicetrackerapp.dto;

import android.net.Uri;

public class AudioItem {

    private String name;

    private Uri uri;

    private long size;

    private String bucketId;

    private String bucketName;

    private long duration;

    public AudioItem(
            String name,
            Uri uri,
            long size,
            String bucketId,
            String bucketName,
            long duration) {

        this.name = name;
        this.uri = uri;
        this.size = size;
        this.bucketId = bucketId;
        this.bucketName = bucketName;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public Uri getUri() {
        return uri;
    }

    public long getSize() {
        return size;
    }

    public String getBucketId() {
        return bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public long getDuration() {
        return duration;
    }
}