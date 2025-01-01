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

    // 클럽
    CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 구단입니다."),

    // 신청
    ENROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 직관 신청입니다."),
    ENROLL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 보낸 직관 신청입니다."),
    ENROLL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글에는 직관 신청을 할 수 없습니다."),
    ENROLL_CANCEL_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 취소할 권한이 없습니다."),
    ENROLL_ACCEPT_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 수락할 권한이 없습니다."),
    ENROLL_REJECT_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 거절할 권한이 없습니다."),
    ENROLL_GET_INVALID(HttpStatus.BAD_REQUEST, "직관 신청을 조회할 권한이 없습니다."),

    // 게시글
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    BOARD_DELETED(HttpStatus.NOT_FOUND, "삭제된 게시글이거나 잘못된 접근입니다."),
    BOARD_BAD_REQUEST(HttpStatus.BAD_REQUEST, "자신의 게시글만 수정할 수 있습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    // 파일
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "파일 업로드를 실패했습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "클라이언트 에러입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다."),;

    private final HttpStatus httpStatus;
    private final String message;
}
