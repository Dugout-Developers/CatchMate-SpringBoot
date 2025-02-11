//package com.back.catchmate.domain.topic.controller;
//
//import com.back.catchmate.domain.notification.dto.NotificationResponse;
//import com.back.catchmate.domain.notification.service.FCMService;
//import com.back.catchmate.domain.topic.dto.TopicRequest;
//import com.back.catchmate.domain.topic.dto.TopicRequest.TopicSubscribeRequest;
//import com.back.catchmate.domain.topic.dto.TopicRequest.TopicUnsubscribeRequest;
//import com.back.catchmate.global.dto.StateResponse;
//import com.back.catchmate.global.jwt.JwtValidation;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Tag(name = "구독 관련 API")
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/topics")
//public class TopicController {
//    private final FCMService fcmService;
//
//    @PostMapping("/subscribe")
//    @Operation(summary = "특정 채팅방 구독 API", description = "알림을 받기 위해 내가 접속한 채팅방 ID를 구독하는 API 입니다.")
//    public void subscribeToTopic(@JwtValidation Long userId,
//                                 @RequestBody TopicSubscribeRequest request) {
//        return fcmService.subscribeToTopic(userId, request);
//    }
//
//    @GetMapping("/receive/{notificationId}")
//    @Operation(summary = "특정 채팅방 구독 취소 API", description = "내가 접속한 채팅방 ID를 구독 취소하는 API 입니다.")
//    public void unsubscribeFromTopic(@JwtValidation Long userId,
//                                     @RequestBody TopicUnsubscribeRequest request) {
//        return fcmService.unsubscribeToTopic(userId, request);
//    }
//}
