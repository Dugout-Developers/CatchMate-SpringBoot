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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private static final Long DEFAULT_CLUB_ID = 0L;

    private final GameRepository gameRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final EnrollRepository enrollRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final BookMarkRepository bookMarkRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final GameConverter gameConverter;
    private final BoardConverter boardConverter;
    private final ChatRoomConverter chatRoomConverter;
    private final UserChatRoomConverter userChatRoomConverter;

    @Override
    @Transactional
    public BoardInfo createOrUpdateBoard(Long userId, Long boardId, CreateOrUpdateBoardRequest request) {
        // 유저 정보 조회
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 응원 구단 및 홈 구단 및 원정 구단 조회
        Club cheerClub = findOrDefaultClub(request.getCheerClubId());
        Club homeClub = findOrDefaultClub(request.getGameRequest().getHomeClubId());
        Club awayClub = findOrDefaultClub(request.getGameRequest().getAwayClubId());

        Game game = findOrCreateGame(homeClub, awayClub, request.getGameRequest());
        Board board = (boardId != null)
                ? updateExistingBoard(user, boardId, cheerClub, game, request)
                : createNewBoardWithChatRoom(user, cheerClub, game, request);

        return boardConverter.toBoardInfo(board, game);
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
            return existingGame;
        } else {
            return createNewGame(homeClub, awayClub, createGameRequest);
        }
    }

    private LocalDateTime parseGameStartDate(String gameStartDate) {
        return (gameStartDate != null)
                ? LocalDateTime.parse(gameStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
    }

    private Game createNewGame(Club homeClub, Club awayClub, CreateGameRequest createGameRequest) {
        Game game = gameConverter.toEntity(homeClub, awayClub, createGameRequest);
        gameRepository.save(game);
        return game;
    }

    private Board updateExistingBoard(User user, Long boardId, Club cheerClub, Game game, CreateOrUpdateBoardRequest request) {
        Board board = boardRepository.findByIdAndDeletedAtIsNull(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(ErrorCode.BOARD_UPDATE_BAD_REQUEST);
        }

        boolean existsChatRoomByBoardId = chatRoomRepository.existsByBoardId(board.getId());
        if (request.getIsCompleted() && !existsChatRoomByBoardId) {
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
        Board savedBoard = boardRepository.save(board);

        if (request.getIsCompleted()) {
            createChatRoom(board, user);
        }

        return savedBoard;
    }

    private void createChatRoom(Board board, User loginUser) {
        // 채팅방 생성
        ChatRoom chatRoom = chatRoomConverter.toEntity(board);
        chatRoomRepository.save(chatRoom);

        // 채팅방 입장
        UserChatRoom userChatRoom = userChatRoomConverter.toEntity(loginUser, chatRoom);
        userChatRoomRepository.save(userChatRoom);
    }

    @Override
    public BoardInfo getBoard(Long userId, Long boardId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        validateNotBlocked(userId, board.getUser().getId());

        boolean isBookMarked = bookMarkRepository.existsByUserIdAndBoardIdAndDeletedAtIsNull(user.getId(), board.getId());
        boolean isOwnBoard = board.isWriterSameAsLoginUser(user);

        AcceptStatus acceptStatus = getLatestAcceptStatus(user.getId(), board.getId());
        boolean isInChatRoom = isUserInChatRoom(user.getId(), board);

        String buttonStatus = determineButtonStatus(isOwnBoard, isInChatRoom, acceptStatus);
        return boardConverter.toBoardInfo(board, board.getGame(), isBookMarked, buttonStatus);
    }

    private void validateNotBlocked(Long requesterId, Long boardOwnerId) {
        if (isUserBlocked(requesterId, boardOwnerId)) {
            throw new BaseException(ErrorCode.BLOCKED_USER_BOARD);
        }
    }

    private AcceptStatus getLatestAcceptStatus(Long userId, Long boardId) {
        return enrollRepository.findFirstByUserIdAndBoardIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, boardId)
                .map(Enroll::getAcceptStatus)
                .orElse(AcceptStatus.NOT_APPLIED);
    }

    private boolean isUserInChatRoom(Long userId, Board board) {
        return board.getChatRoom() != null &&
                userChatRoomRepository.existsByUserIdAndChatRoomIdAndDeletedAtIsNull(userId, board.getChatRoom().getId());
    }

    private String determineButtonStatus(boolean isOwnBoard, boolean isInChatRoom, AcceptStatus acceptStatus) {
        return (isOwnBoard || (isInChatRoom && acceptStatus == AcceptStatus.ACCEPTED)) ?
                "VIEW CHAT" :
                (acceptStatus == AcceptStatus.PENDING ? "APPLIED" : "APPLY");
    }

    @Override
    public PagedBoardInfo getBoardList(Long userId, LocalDate gameStartDate, Integer maxPerson, List<Long> preferredTeamIdList, Pageable pageable) {
        Long filteredUserId = (userId != null) ? userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND)).getId() : null;

        Page<Board> boardList = boardRepository.findFilteredBoards(filteredUserId, gameStartDate, maxPerson, preferredTeamIdList, pageable);
        return boardConverter.toPagedBoardInfoFromBoardList(boardList);
    }

    @Override
    public PagedBoardInfo getBoardListByUserId(Long loginUserId, Long userId, Pageable pageable) {
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
    public TempBoardInfo getTempBoard(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board tempBoard = boardRepository.findTopByUserIdAndIsCompletedIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.TEMP_BOARD_NOT_FOUND));

        validateBoardOwner(user, tempBoard, ErrorCode.TEMP_BOARD_BAD_REQUEST);

        return boardConverter.toTempBoardInfo(tempBoard, tempBoard.getGame());
    }

    @Override
    @Transactional
    public BoardDeleteInfo deleteBoard(Long userId, Long boardId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        validateBoardOwner(user, board, ErrorCode.BOARD_DELETE_BAD_REQUEST);

        board.deleteBoard();
        return boardConverter.toBoardDeleteInfo(boardId);
    }

    @Override
    @Transactional
    public LiftUpStatusInfo updateLiftUpDate(Long userId, Long boardId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Board board = boardRepository.findByIdAndDeletedAtIsNullAndIsCompleted(boardId)
                .orElseThrow(() -> new BaseException(ErrorCode.BOARD_NOT_FOUND));

        validateBoardOwner(user, board, ErrorCode.BOARD_LIFT_UP_BAD_REQUEST);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextLiftUpAllowed = board.getLiftUpDate().plusDays(3);

        if (nextLiftUpAllowed.isBefore(now)) {
            board.updateLiftUpDate(now);
            return boardConverter.toLiftUpStatusInfo(true, null);
        }

        long remainingMinutes = Duration.between(now, nextLiftUpAllowed).toMinutes();
        return new LiftUpStatusInfo(false, formatRemainingTime(remainingMinutes));
    }

    private void validateBoardOwner(User user, Board board, ErrorCode errorCode) {
        if (user.isDifferentUserFrom(board.getUser())) {
            throw new BaseException(errorCode);
        }
    }

    private String formatRemainingTime(long remainingMinutes) {
        long days = remainingMinutes / 1440;
        long hours = (remainingMinutes % 1440) / 60;
        long minutes = remainingMinutes % 60;

        return String.format("%d일 %02d시간 %02d분", days, hours, minutes);
    }
}
