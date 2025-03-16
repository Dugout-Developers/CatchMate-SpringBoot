package com.back.catchmate.global.scheduler;

import com.back.catchmate.domain.board.entity.Board;
import com.back.catchmate.domain.board.repository.BoardRepository;
import com.back.catchmate.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardCleanScheduler {
    private final BoardRepository boardRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    @Scheduled(cron = "0 9 13 * * ?", zone = "Asia/Seoul")
    public void softDeleteOldBoardsAndChats() {
        LocalDateTime deleteThreshold = LocalDateTime.now().minusDays(7);

        List<Board> oldBoardList = boardRepository.findBoardsByGameStartDatePlusSevenAndDeletedAtIsNull(deleteThreshold);
        oldBoardList.forEach(board -> {
            board.deleteBoard();
            chatMessageRepository.deleteAllByChatRoomId(board.getChatRoom().getId());
        });
    }
}
