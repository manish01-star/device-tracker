package com.example.devicetrackerapp.api;

import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.RegisterDeviceRequest;
import com.example.devicetrackerapp.dto.RegisterDeviceResponse;
import com.example.devicetrackerapp.dto.TrackingConfigResponse;
import com.example.devicetrackerapp.dto.UpdateLocationRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
}