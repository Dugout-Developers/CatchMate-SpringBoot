package com.back.catchmate.domain.board.service;

import com.back.catchmate.domain.board.converter.BoardConverter;
import com.back.catchmate.domain.board.dto.BoardRequest.CreateOrUpdateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardDeleteInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.BoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.LiftUpStatusInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.PagedBoardInfo;
import com.back.catchmate.domain.board.dto.BoardResponse.TempBoardInfo;
import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.board.repository.BookMarkRepository;
import com.back.catchmate.domain.chat.converter.ChatRoomConverter;
import com.back.catchmate.domain.chat.converter.UserChatRoomConverter;
import com.back.catchmate.domain.chat.entity.ChatRoom;
import com.back.catchmate.domain.chat.entity.UserChatRoom;
import com.back.catchmate.domain.chat.repository.ChatRoomRepository;
import com.back.catchmate.domain.chat.repository.UserChatRoomRepository;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.domain.enroll.entity.AcceptStatus;
import com.back.catchmate.domain.enroll.entity.Enroll;
import com.back.catchmate.domain.enroll.repository.EnrollRepository;
import com.back.catchmate.domain.game.converter.GameConverter;
import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import com.back.catchmate.domain.game.entity.Game;
import com.back.catchmate.domain.game.repository.GameRepository;
import com.back.catchmate.domain.user.entity.User;
import com.back.catchmate.domain.user.repository.BlockedUserRepository;
import com.back.catchmate.domain.user.repository.UserRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private static final Long DEFAULT_CLUB_ID = 0L;
    private final BoardRepository boardRepository;
    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final BookMarkRepository bookMarkRepository;
    private final EnrollRepository enrollRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final BoardConverter boardConverter;
    private final GameConverter gameConverter;
    private final ChatRoomConverter chatRoomConverter;
    private final UserChatRoomConverter userChatRoomConverter;

    @Override
    @Transactional
    public BoardInfo createOrUpdateBoard(Long userId, Long boardId, CreateOrUpdateBoardRequest request) {
        // 유저 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        // 응원 구단 조회
        Club cheerClub = findOrDefaultClub(request.getCheerClubId());

        // 홈 구단 및 원정 구단 조회
        Club homeClub = findOrDefaultClub(request.getGameRequest().getHomeClubId());
        Club awayClub = findOrDefaultClub(request.getGameRequest().getAwayClubId());

        // Game 조회 또는 생성
        Game game = findOrCreateGame(homeClub, awayClub, request.getGameRequest());

        Board board = (boardId != null)
                ? updateExistingBoard(user, boardId, cheerClub, game, request)
                : createNewBoardWithChatRoom(user, cheerClub, game, request);

        return boardConverter.toBoardInfo(board, game);
    }

    private Board updateExistingBoard(User user, Long boardId, Club cheerClub, Game game, CreateOrUpdateBoardRequest request) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_UPDATE_BAD_REQUEST);
        }

        boolean existsByBoardId = chatRoomRepository.existsByBoardId(board.getId());
        if (request.getIsCompleted() && !existsByBoardId) {
            createChatRoom(board, user);
        }

        board.updateBoard(cheerClub, game, request);
        return board;
    }

    private Board createNewBoardWithChatRoom(User user, Club cheerClub, Game game, CreateOrUpdateBoardRequest request) {
        // 기존 임시 저장 데이터 조회
        Optional<Board> existingTempBoard = boardRepository.findTopByUserIdAndIsCompletedIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId());

        // 기존 임시 저장 데이터가 존재하면 삭제 처리
        existingTempBoard.ifPresent(Board::deleteTempBoard);

        Board board = boardConverter.toEntity(user, game, cheerClub, request);
        boardRepository.save(board);

        if (request.getIsCompleted()) {
            createChatRoom(board, user);
        }
        return board;
    }

    private void createChatRoom(Board board, User loginUser) {
        // 채팅방 생성
        ChatRoom chatRoom = chatRoomConverter.toEntity(board);
        chatRoomRepository.save(chatRoom);

        // 채팅방 입장
        UserChatRoom userChatRoom = userChatRoomConverter.toEntity(loginUser, chatRoom);
        userChatRoomRepository.save(userChatRoom);
    }

    private Club findOrDefaultClub(Long clubId) {
        return (clubId != 0)
                ? clubRepository.findById(clubId).orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND))
                : getDefaultClub();
    }

    private Club getDefaultClub() {
        return clubRepository.findById(DEFAULT_CLUB_ID)
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
    }

    private Game findOrCreateGame(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        LocalDateTime gameStartDate = parseGameStartDate(createGameRequest.getGameStartDate());

        Game existingGame = gameRepository.findByHomeClubAndAwayClubAndGameStartDate(homeClub, awayClub, gameStartDate);

        if (existingGame != null) {
            return updateExistingGame(existingGame, homeClub, awayClub, gameStartDate);
        } else {
            return createNewGame(homeClub, awayClub, createGameRequest);
        }
    }

    private LocalDateTime parseGameStartDate(String gameStartDate) {
        return (gameStartDate != null)
                ? LocalDateTime.parse(gameStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
    }

    private Game updateExistingGame(Game game, Club homeClub, Club awayClub, LocalDateTime gameStartDate) {
        game.updateGame(homeClub, awayClub, gameStartDate);
        return game;
    }

    private Game createNewGame(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        Game game = gameConverter.toEntity(homeClub, awayClub, createGameRequest);
        gameRepository.save(game);
        return game;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardInfo getBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (isUserBlocked(user.getId(), board.getUser().getId())) {
            throw new BaseException(ErrorCode.BLOCKED_USER_BOARD);
        }

        boolean isBookMarked = bookMarkRepository.existsByUserIdAndBoardIdAndDeletedAtIsNull(user.getId(), board.getId());
        boolean isOwnBoard = board.isWriterSameAsLoginUser(user);

        // 신청 정보 가져오기
        Optional<Enroll> enroll = enrollRepository.findFirstByUserIdAndBoardIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId(), board.getId());

        boolean isAccepted = enroll.map(e -> e.getAcceptStatus() == AcceptStatus.ACCEPTED).orElse(false);
        boolean isPending = enroll.map(e -> e.getAcceptStatus() == AcceptStatus.PENDING).orElse(false);
        boolean isRejected = enroll.map(e -> e.getAcceptStatus() == AcceptStatus.REJECTED).orElse(false);
        boolean isInChatRoom = userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(user.getId(), board.getChatRoom().getId());

        String buttonStatus;
        if (isOwnBoard) {
            buttonStatus = "VIEW CHAT"; // 본인 게시물
        } else {
            if (isInChatRoom && isAccepted) {
                buttonStatus = "VIEW CHAT"; // 채팅방 참여 중
            } else if (isPending) {
                buttonStatus = "APPLIED"; // 신청 승인됨
            } else if (isRejected) {
                buttonStatus = "APPLY"; // 거절된 경우 다시 신청 가능
            } else {
                buttonStatus = "APPLY"; // 미신청 상태
            }
        }

        return boardConverter.toBoardInfo(board, board.getGame(), isBookMarked, buttonStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardList(Long userId, LocalDate gameStartDate, Integer maxPerson, List<Long> preferredTeamIdList, Pageable pageable) {
        Long filteredUserId = null;

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
            filteredUserId = user.getId();
        }

        Page<Board> boardList = boardRepository.findFilteredBoards(filteredUserId, gameStartDate, maxPerson, preferredTeamIdList, pageable);
        return boardConverter.toPagedBoardInfoFromBoardList(boardList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable) {
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (isUserBlocked(loginUserId, userId)) {
            throw new BaseException(ErrorCode.BLOCKED_USER_BOARD_LIST);
        }

        Page<Board> boardList = boardRepository.findAllByUserIdAndDeletedAtIsNullAndIsCompletedIsTrue(user.getId(), pageable);
        return boardConverter.toPagedBoardInfoFromBoardList(boardList);
    }

    private boolean isUserBlocked(Long blockerId, Long blockedId) {
        return blockedUserRepository.existsByBlockerIdAndBlockedIdAndDeletedAtIsNull(blockerId, blockedId);
    }

    @Override
    @Transactional(readOnly = true)
    public TempBoardInfo getTempBoard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board tempBoard = boardRepository.findTopByUserIdAndIsCompletedIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.TEMP_BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(tempBoard.getUser())) {
            throw new BaseException(ErrorCode.TEMP_BOARD_BAD_REQUEST);
        }

        return boardConverter.toTempBoardInfo(tempBoard, tempBoard.getGame());
    }

    @Override
    @Transactional
    public BoardDeleteInfo deleteBoard(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));


        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_DELETE_BAD_REQUEST);
        }

        board.deleteBoard();
        return boardConverter.toBoardDeleteInfo(boardId);
    }

    @Override
    @Transactional
    public LiftUpStatusInfo updateLiftUpDate(Long userId, Long boardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_LIFT_UP_BAD_REQUEST);
        }

        // note: 3일 간격으로 수정할 수 있음.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextLiftUpAllowed = board.getLiftUpDate().plusDays(3);

        if (nextLiftUpAllowed.isBefore(now)) {
            board.updateLiftUpDate(now);
            return boardConverter.toLiftUpStatusInfo(true, null);
        } else {
            // 남은 시간 계산
            long remainingMinutes = Duration.between(now, nextLiftUpAllowed).toMinutes();
            return new LiftUpStatusInfo(false, formatRemainingTime(remainingMinutes));  // 실패 시 200 응답, 상태와 남은 시간 포함
        }
    }

    private String formatRemainingTime(long remainingMinutes) {
        long days = remainingMinutes / (24 * 60);  // 1일은 1440분
        long hours = (remainingMinutes % (24 * 60)) / 60;  // 나머지 분에서 시간 계산
        long minutes = remainingMinutes % 60;  // 나머지 분 계산

        return String.format("%d일 %02d시간 %02d분", days, hours, minutes);
    }
}
