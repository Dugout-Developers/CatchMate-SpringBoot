package com.back.catchmate.domain.enroll.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.*;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.user.converter.UserConverter;
import com.back.catchmate.domain.user.dto.UserResponse.UserInfo;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EnrollConverter {
    private final UserConverter userConverter;
    private final BoardConverter boardConverter;

    public Enroll toEntity(CreateEnrollRequest createEnrollRequest, User user, Board board) {
        return Enroll.builder()
                .user(user)
                .board(board)
                .acceptStatus(AcceptStatus.PENDING)
                .description(createEnrollRequest.getDescription())
                .isNew(true)
                .build();
    }

    public CreateEnrollInfo toCreateEnrollInfo(Enroll enroll) {
        return CreateEnrollInfo.builder()
                .enrollId(enroll.getId())
                .requestAt(enroll.getCreatedAt())
                .build();
    }

    public CancelEnrollInfo toCancelEnrollInfo(Enroll enroll) {
        return CancelEnrollInfo.builder()
                .enrollId(enroll.getId())
                .deletedAt(enroll.getUpdatedAt())
                .build();
    }

    public PagedEnrollRequestInfo toPagedEnrollRequestInfo(Page<Enroll> enrollList) {
        List<EnrollRequestInfo> enrollRequestInfoList = enrollList.stream()
                .map(enroll -> {
                    UserInfo userInfo = userConverter.toUserInfo(enroll.getUser());
                    BoardInfo boardInfo = boardConverter.toBoardInfo(enroll.getBoard(), enroll.getBoard().getGame());
                    return toEnrollRequestInfo(enroll, userInfo, boardInfo);
                })
                .collect(Collectors.toList());

        return PagedEnrollRequestInfo.builder()
                .enrollInfoList(enrollRequestInfoList)
                .totalPages(enrollList.getTotalPages())
                .totalElements(enrollList.getTotalElements())
                .isFirst(enrollList.isFirst())
                .isLast(enrollList.isLast())
                .build();
    }

    public EnrollRequestInfo toEnrollRequestInfo(Enroll enroll, UserInfo userInfo, BoardInfo boardInfo) {
        return EnrollRequestInfo.builder()
                .enrollId(enroll.getId())
                .acceptStatus(enroll.getAcceptStatus())
                .description(enroll.getDescription())
                .requestDate(enroll.getCreatedAt())
                .userInfo(userInfo)
                .boardInfo(boardInfo)
                .build();
    }

    public PagedEnrollReceiveInfo toPagedEnrollReceiveInfo(Page<Enroll> enrollPage) {
        Map<Board, List<EnrollInfo>> groupedByBoardId = enrollPage.stream()
                .collect(Collectors.groupingBy(
                        Enroll::getBoard,
                        Collectors.mapping(this::toEnrollInfo, Collectors.toList())
                ));

        List<EnrollReceiveInfo> enrollInfoList = groupedByBoardId.entrySet().stream()
                .map(entry -> {
                    List<EnrollInfo> sortedEnrollInfoList = entry.getValue().stream()
                            .sorted(Comparator.comparing(EnrollInfo::getRequestDate).reversed())
                            .limit(10)
                            .collect(Collectors.toList());

                    return EnrollReceiveInfo.builder()
                            .boardInfo(boardConverter.toBoardInfo(entry.getKey()))
                            .enrollReceiveInfoList(sortedEnrollInfoList)
                            .build();
                })
                .toList();

        return PagedEnrollReceiveInfo.builder()
                .enrollInfoList(enrollInfoList)
                .totalPages(enrollPage.getTotalPages())
                .totalElements(enrollPage.getTotalElements())
                .isFirst(enrollPage.isFirst())
                .isLast(enrollPage.isLast())
                .build();
    }

    public EnrollInfo toEnrollInfo(Enroll enroll) {
        return EnrollInfo.builder()
                .enrollId(enroll.getId())
                .acceptStatus(enroll.getAcceptStatus())
                .description(enroll.getDescription())
                .requestDate(enroll.getCreatedAt())
                .isNew(enroll.isNew())
                .userInfo(userConverter.toUserInfo(enroll.getUser()))
                .build();
    }

    public NewEnrollCountInfo toNewEnrollCountResponse(int enrollListCount) {
        return NewEnrollCountInfo.builder()
                .newEnrollCount(enrollListCount)
                .build();
    }

    public UpdateEnrollInfo toUpdateEnrollInfo(Enroll enroll, AcceptStatus acceptStatus) {
        return UpdateEnrollInfo.builder()
                .enrollId(enroll.getId())
                .acceptStatus(acceptStatus)
                .build();
    }

    public EnrollDescriptionInfo toEnrollDescriptionInfo(Enroll enroll) {
        return EnrollDescriptionInfo.builder()
                .enrollId(enroll.getId())
                .description(enroll.getDescription())
                .build();
    }
}
