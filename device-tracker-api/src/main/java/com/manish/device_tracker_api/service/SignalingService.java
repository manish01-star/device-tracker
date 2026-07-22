// package com.manish.device_tracker_api.service;

// import com.manish.device_tracker_api.dto.SignalMessage;
// import lombok.RequiredArgsConstructor;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// public class SignalingService {

//     private final SimpMessagingTemplate messagingTemplate;

//     public void sendSignal(SignalMessage message) {

//         messagingTemplate.convertAndSend(
//                 "/topic/" + message.getDeviceId(), message);

//     }

// }