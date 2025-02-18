package com.back.catchmate.global.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ENTITY_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자 ID 입니다."),
    USER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 가입된 사용자입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 클럽
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 구단입니다."),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게임입니다."),

    // 신청
    ENROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 직관 신청입니다."),
    ENROLL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 보낸 직관 신청입니다."),
    ENROLL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글에는 직관 신청을 할 수 없습니다."),
    ENROLL_CANCEL_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 취소할 권한이 없습니다."),
    ENROLL_ACCEPT_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 수락할 권한이 없습니다."),
    ENROLL_REJECT_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 거절할 권한이 없습니다."),
    ENROLL_GET_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 조회할 권한이 없습니다."),
    ENROLL_ALREADY_ACCEPTED(HttpStatus.BAD_REQUEST, "이미 수락된 신청입니다."),

    // 게시글
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    TEMP_BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "임시 저장된 글이 존재하지 않습니다."),
    TEMP_BOARD_BAD_REQUEST(HttpStatus.NOT_FOUND, "임시 저장된 글을 불러올 권한이 없습니다."),
    BOARD_DELETED(HttpStatus.NOT_FOUND, "삭제된 게시글이거나 잘못된 접근입니다."),
    BOARD_UPDATE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글만 수정할 수 있습니다."),
    BOARD_DELETE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글만 삭제할 수 있습니다."),
    BOARD_LIFT_UP_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글만 삭제할 수 있습니다."),
    BOARD_NOT_ALLOWED_UPDATE_LIFT_UPDATE(HttpStatus.BAD_REQUEST, "마지막으로 끌어올리기 한 지 3일이 지나야 업데이트 가능합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ALREADY_BOOKMARK(HttpStatus.BAD_REQUEST, "이미 찜한 게시글입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 찜입니다."),
    BOOKMARK_BAD_REQUEST(HttpStatus.BAD_REQUEST, "본인 게시글은 찜할 수 없습니다."),
    FULL_PERSON(HttpStatus.BAD_REQUEST, "해당 게시글은 마감되었습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),
    EMPTY_FCM_RESPONSE(HttpStatus.BAD_REQUEST, "알림 데이터가 존재하지 않습니다."),
    FCM_TOPIC_SEND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "토픽 알람 전송중 에러가 발생했습니다."),
    FCM_TOKEN_SEND_BAD_REQUEST(HttpStatus.BAD_REQUEST, "토픽 알람 전송중 에러가 발생했습니다."),
    FCM_SUBSCRIBE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "토픽 구독중 에러가 발생했습니다."),
    FCM_UNSUBSCRIBE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "토픽 구독 취소중 에러가 발생했습니다."),

    // 채팅방
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅방입니다."),
    USER_CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자가 해당 채팅방에 참여하지 않았습니다"),

    // 파일
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "파일 업로드를 실패했습니다."),
    IMAGE_UPDATE_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "채팅방 이미지를 수정할 권한이 없습니다."),
    KICK_CHATROOM_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "채팅방에서 내보낼 권한이 없습니다."),

    // 유저 차단
    USER_BLOCK_FAILED(HttpStatus.BAD_REQUEST, "해당 유저를 이미 차단했습니다."),
    SELF_BLOCK_FAILED(HttpStatus.BAD_REQUEST, "자기 자신을 차단할 수 없습니다."),
    USER_UNBLOCK_FAILED(HttpStatus.BAD_REQUEST, "해당 유저를 차단한 이력이 없습니다."),
    BLOCKED_USER_BOARD(HttpStatus.BAD_REQUEST, "내가 차단한 유저의 게시글입니다."),
    BLOCKED_USER_BOARD_LIST(HttpStatus.BAD_REQUEST, "내가 차단한 유저의 게시글 리스트입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "클라이언트 에러입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다."),;

    private final HttpStatus httpStatus;
    private final String message;
}
