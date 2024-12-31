package com.back.catchmate.domain.board.controller;

import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.domain.enroll.dto.EnrollResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/{boardId}}")
    @Operation(summary = "내가 보낸 직관 신청 목록 조회 API", description = "내가 보낸 직관 신청 목록을 조회하는 API 입니다.")
    public BoardInfo getBoard(@JwtValidation Long userId,
                              @PathVariable Long boardId) {
        return boardService.getBoard(userId, boardId);
    }
}
