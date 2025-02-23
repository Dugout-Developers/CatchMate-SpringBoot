package com.back.catchmate.domain.admin.converter;

import com.back.catchmate.domain.admin.dto.AdminResponse;
import com.back.catchmate.domain.admin.dto.AdminResponse.AdminDashboardInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.CheerStyleStatsInfo;
import com.back.catchmate.domain.admin.dto.AdminResponse.PagedUserInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.dto.ClubResponse;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameResponse;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.back.catchmate.domain.admin.dto.AdminResponse.GenderRatioDto;
import static com.back.catchmate.domain.admin.dto.AdminResponse.TeamSupportStatsInfo;

@Component
@RequiredArgsConstructor
public class AdminConverter {
    private final ClubConverter clubConverter;
    private final GameConverter gameConverter;

    public AdminDashboardInfo toAdminDashboardInfo(long totalUserCount, long totalBoardCount, long totalReportCount, long totalInquiryCount) {
        return AdminDashboardInfo.builder()
                .totalUserCount(totalUserCount)
                .totalBoardCount(totalBoardCount)
                .totalReportCount(totalReportCount)
                .totalInquiryCount(totalInquiryCount)
                .build();
    }

    public GenderRatioDto toGenderRatioDto(double maleRatio, double femaleRatio) {
        return GenderRatioDto.builder()
                .maleRatio(maleRatio)
                .femaleRatio(femaleRatio)
                .build();
    }

    public TeamSupportStatsInfo toCheerClubStatsInfo(Map<Long, Long> teamSupportCountMap) {
        return TeamSupportStatsInfo.builder()
                .teamSupportCountMap(teamSupportCountMap)
                .build();
    }

    public CheerStyleStatsInfo toCheerStyleStatsInfo(Map<String, Long> cheerStyleSupportCountMap) {
        return CheerStyleStatsInfo.builder()
                .cheerStyleCountMap(cheerStyleSupportCountMap)
                .build();
    }

    public AdminResponse.UserInfo toUserInfo(User user) {
        ClubResponse.ClubInfo clubInfo = clubConverter.toClubInfo(user.getClub());

        return AdminResponse.UserInfo.builder()
                .userId(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .nickName(user.getNickName())
                .clubInfo(clubInfo)
                .gender(user.getGender())
                .email(user.getEmail())
                .socialType(user.getProvider().getProvider())
                .joinedAt(user.getCreatedAt())
                .build();
    }

    public PagedUserInfo toPagedUserInfo(Page<User> userList) {
        List<AdminResponse.UserInfo> userInfoList = userList.stream()
                .map(this::toUserInfo)
                .toList();

        return PagedUserInfo.builder()
                .userInfoList(userInfoList)
                .totalPages(userList.getTotalPages())
                .totalElements(userList.getTotalElements())
                .isFirst(userList.isFirst())
                .isLast(userList.isLast())
                .build();
    }

    public AdminResponse.BoardInfo toBoardInfo(Board board, Game game) {
        GameResponse.GameInfo gameInfo = gameConverter.toGameInfo(game);
        AdminResponse.UserInfo userInfo = toUserInfo(board.getUser());

        return AdminResponse.BoardInfo.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .cheerClubId(board.getClub().getId())
                .currentPerson(board.getCurrentPerson())
                .maxPerson(board.getMaxPerson())
                .preferredGender(board.getPreferredGender())
                .preferredAgeRange(board.getPreferredAgeRange())
                .gameInfo(gameInfo)
                .userInfo(userInfo)
                .build();
    }

    public AdminResponse.BoardInfo toBoardInfo(Board board, Game game, List<UserChatRoom> userChatRoomList) {
        GameResponse.GameInfo gameInfo = gameConverter.toGameInfo(game);
        AdminResponse.UserInfo userInfo = toUserInfo(board.getUser());

        List<AdminResponse.UserInfo> userInfoList = userChatRoomList.stream()
                .map(userChatRoom -> toUserInfo(userChatRoom.getUser()))
                .toList();

        return AdminResponse.BoardInfo.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .cheerClubId(board.getClub().getId())
                .currentPerson(board.getCurrentPerson())
                .maxPerson(board.getMaxPerson())
                .preferredGender(board.getPreferredGender())
                .preferredAgeRange(board.getPreferredAgeRange())
                .gameInfo(gameInfo)
                .userInfo(userInfo)
                .userInfoList(userInfoList)
                .build();
    }

    public AdminResponse.PagedBoardInfo toPagedBoardInfo(Page<Board> boardList) {
        List<AdminResponse.BoardInfo> boardInfoList = boardList.stream()
                .map(board -> toBoardInfo(board, board.getGame()))
                .toList();

        return AdminResponse.PagedBoardInfo.builder()
                .boardInfoList(boardInfoList)
                .totalPages(boardList.getTotalPages())
                .totalElements(boardList.getTotalElements())
                .isFirst(boardList.isFirst())
                .isLast(boardList.isLast())
                .build();
    }
}
