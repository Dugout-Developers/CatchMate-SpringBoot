package com.back.catchmate.domain.notice.converter;

import com.back.catchmate.domain.admin.dto.AdminRequest;
import com.back.catchmate.domain.notice.dto.NoticeResponse;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NoticeConverter {
    private final UserConverter userConverter;

    public Notice toEntity(User user, AdminRequest.CreateNoticeRequest request) {
        return Notice.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    public NoticeResponse.NoticeInfo toNoticeInfo(Notice notice) {
        UserResponse.UserInfo userInfo = userConverter.toUserInfo(notice.getUser());

        return NoticeResponse.NoticeInfo.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .userInfo(userInfo)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public NoticeResponse.PagedNoticeInfo toPagedNoticeInfo(Page<Notice> noticeList) {
        List<NoticeResponse.NoticeInfo> noticeInfoList = noticeList.getContent().stream()
                .map(this::toNoticeInfo)
                .toList();

        return NoticeResponse.PagedNoticeInfo.builder()
                .noticeInfoList(noticeInfoList)
                .totalPages(noticeList.getTotalPages()) // 전체 페이지 수
                .totalElements(noticeList.getTotalElements()) // 전체 요소 수
                .isFirst(noticeList.isFirst()) // 첫 번째 페이지 여부
                .isLast(noticeList.isLast()) // 마지막 페이지 여부
                .build();
    }
}
