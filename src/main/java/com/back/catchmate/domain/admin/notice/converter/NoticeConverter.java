package com.back.catchmate.domain.admin.notice.converter;

import com.back.catchmate.domain.admin.notice.dto.NoticeRequest;
import com.back.catchmate.domain.admin.notice.dto.NoticeResponse;
import com.back.catchmate.domain.admin.notice.entity.Notice;
import com.back.catchmate.domain.admin.notice.repository.NoticeRepository;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoticeConverter {
    private final NoticeRepository noticeRepository;
    private final UserConverter userConverter;

    public Notice toEntity(User user, NoticeRequest.CreateNoticeRequest request) {
        return Notice.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    public Notice toUpdatedEntity(Notice notice, NoticeRequest.UpdateNoticeRequest request) {
        return Notice.builder()
                .id(notice.getId())  // 기존 ID 유지
                .user(notice.getUser())  // 기존 유저 유지
                .title(request.getTitle())
                .content(request.getContent())
                .build();
    }

    public NoticeResponse.NoticeInfo toNoticeInfo(Notice notice) {
        return NoticeResponse.NoticeInfo.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public NoticeResponse.PagedNoticeInfo toPagedNoticeInfo(Page<Notice> noticePage) {
        List<NoticeResponse.NoticeInfo> noticeInfos = noticePage.getContent().stream()
                .map(this::toNoticeInfo)
                .collect(Collectors.toList());

        return NoticeResponse.PagedNoticeInfo.builder()
                .notices(noticeInfos)
                .totalPages(noticePage.getTotalPages())
                .totalElements(noticePage.getTotalElements())
                .currentPage(noticePage.getNumber())
                .size(noticePage.getSize())
                .build();
    }
}
