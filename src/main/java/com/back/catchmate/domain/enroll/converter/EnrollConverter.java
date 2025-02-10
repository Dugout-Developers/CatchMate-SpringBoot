package com.back.catchmate.domain.enroll.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollDescriptionInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.EnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.NewEnrollCountInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollReceiveInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.PagedEnrollRequestInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.UpdateEnrollInfo;
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

    public PagedEnrollReceiveInfo toPagedEnrollReceiveInfo(Page<Enroll> enrollList) {
        // boardId 기준으로 그룹화 (Map<BoardId, List<EnrollInfo>>)
        Map<Long, List<EnrollResponse.EnrollInfo>> groupedByBoardId = enrollList.stream()
                .collect(Collectors.groupingBy(
                        enroll -> enroll.getBoard().getId(), // Key: boardId
                        Collectors.mapping(this::toEnrollInfo, Collectors.toList()) // Value: EnrollInfo 리스트
                ));

        // 각 boardId에 대한 EnrollReceiveInfo 생성
        List<EnrollReceiveInfo> enrollInfoList = groupedByBoardId.entrySet().stream()
                .map(entry -> {
                    List<EnrollResponse.EnrollInfo> sortedEnrollInfoList = entry.getValue().stream()
                            .sorted(Comparator.comparing(EnrollResponse.EnrollInfo::getRequestDate).reversed())
                            .limit(10)  // 최근 10개만 포함
                            .collect(Collectors.toList());

                    // boardId와 그에 해당하는 EnrollReceiveInfo 목록을 생성
                    return EnrollReceiveInfo.builder()
                            .boardInfo(boardConverter.toBoardInfo(entry.getKey())) // boardId에 해당하는 BoardInfo 객체 생성
                            .enrollReceiveInfoList(sortedEnrollInfoList)
                            .build();
                })
                .toList();

        // PagedEnrollReceiveInfo 반환
        return PagedEnrollReceiveInfo.builder()
                .enrollInfoList(enrollInfoList) // boardId 단위로 그룹화된 신청 리스트
                .totalPages(enrollList.getTotalPages())
                .totalElements(enrollList.getTotalElements())
                .isFirst(enrollList.isFirst())
                .isLast(enrollList.isLast())
                .build();
    }

    public EnrollResponse.EnrollInfo toEnrollInfo(Enroll enroll) {
        return EnrollResponse.EnrollInfo.builder()
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
