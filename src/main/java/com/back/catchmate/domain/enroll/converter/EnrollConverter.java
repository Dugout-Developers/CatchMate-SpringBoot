package com.back.catchmate.domain.enroll.converter;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.enroll.dto.EnrollRequest.CreateEnrollRequest;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CancelEnrollInfo;
import com.back.catchmate.domain.enroll.dto.EnrollResponse.CreateEnrollInfo;
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
        // Board 기준으로 그룹화 (Map<BoardInfo, List<EnrollReceiveInfo>>)
        Map<BoardInfo, List<EnrollResponse.EnrollInfo>> groupedByBoard = enrollList.stream()
                .collect(Collectors.groupingBy(
                        enroll -> boardConverter.toBoardInfo(enroll.getBoard(), enroll.getBoard().getGame()), // Key: BoardInfo
                        Collectors.mapping(enroll -> {
                            UserInfo userInfo = userConverter.toUserInfo(enroll.getUser());
                            return toEnrollInfo(enroll, userInfo);
                        }, Collectors.toList()) // Value: EnrollReceiveInfo 리스트
                ));

        // BoardInfo + 해당 Board에 대한 신청 리스트를 포함하는 구조로 변환
        List<EnrollResponse.EnrollReceiveInfo> enrollReceiveInfoList = groupedByBoard.entrySet().stream()
                .map(entry -> EnrollResponse.EnrollReceiveInfo.builder()
                        .boardInfo(entry.getKey()) // BoardInfo 설정
                        .enrollReceiveInfoList(entry.getValue()) // Board에 대한 신청 리스트 설정
                        .build())
                .toList();

        return PagedEnrollReceiveInfo.builder()
                .enrollInfoList(enrollReceiveInfoList) // Board 단위로 그룹화된 신청 리스트
                .totalPages(enrollList.getTotalPages())
                .totalElements(enrollList.getTotalElements())
                .isFirst(enrollList.isFirst())
                .isLast(enrollList.isLast())
                .build();
    }

    public EnrollResponse.EnrollInfo toEnrollInfo(Enroll enroll, UserInfo userInfo) {
        return EnrollResponse.EnrollInfo.builder()
                .enrollId(enroll.getId())
                .acceptStatus(enroll.getAcceptStatus())
                .description(enroll.getDescription())
                .requestDate(enroll.getCreatedAt())
                .isNew(enroll.isNew())
                .userInfo(userInfo)
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
}
