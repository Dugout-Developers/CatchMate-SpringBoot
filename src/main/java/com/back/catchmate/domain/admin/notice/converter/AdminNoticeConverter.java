package com.back.catchmate.domain.admin.notice.converter;

import com.back.catchmate.domain.admin.converter.AdminConverter;
import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.domain.notice.entity.Notice;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminNoticeConverter {
    private final AdminConverter adminConverter;

    public Notice toEntity(User user, NoticeRequest.CreateNoticeRequest request) {
        return Notice.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    public NoticeResponse.NoticeInfo toNoticeInfo(Notice notice) {
        AdminResponse.UserInfo userInfo = adminConverter.toUserInfo(notice.getUser());

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
                .notices(noticeInfoList)
                .totalPages(noticeList.getTotalPages()) // 전체 페이지 수
                .totalElements(noticeList.getTotalElements()) // 전체 요소 수
                .isFirst(noticeList.isFirst()) // 첫 번째 페이지 여부
                .isLast(noticeList.isLast()) // 마지막 페이지 여부
                .build();
    }
}
