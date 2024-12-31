package com.back.catchmate.domain.board.controller;

import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "게시글 관련 API")
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping()
    @Operation(summary = "게시글 등록 API", description = "게시글을 등록합니다.")
    public BoardInfo createBoard(@JwtValidation Long userId,
                                 @Valid @RequestBody CreateBoardRequest request) {
        return boardService.createBoard(userId, request);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시글 삭제 API", description = "게시글을 삭제합니다.")
    public BoardDeleteInfo deleteBoard(@JwtValidation Long userId,
                                     @PathVariable Long boardId) {
        return boardService.deleteBoard(userId, boardId);
    }
}
