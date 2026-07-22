// package com.manish.device_tracker_api.controller;

// import com.manish.device_tracker_api.dto.SignalMessage;
// import com.manish.device_tracker_api.service.SignalingService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.stereotype.Controller;

// @Controller
// @RequiredArgsConstructor
// public class SignalingController {

//     private final SignalingService signalingService;

//     @MessageMapping("/signal")
//     public void signal(SignalMessage message) {

//         signalingService.sendSignal(message);

//     }

// }