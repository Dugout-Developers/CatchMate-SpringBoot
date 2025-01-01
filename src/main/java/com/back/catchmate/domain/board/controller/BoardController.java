package com.back.catchmate.domain.board.controller;

import com.back.catchmate.domain.board.dto.BoardRequest.*;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.domain.board.service.BookMarkService;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@Tag(name = "게시글 관련 API")
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final BookMarkService bookMarkService;

    @PostMapping
    @Operation(summary = "게시글 등록 API", description = "게시글을 등록합니다.")
    public BoardInfo createBoard(@JwtValidation Long userId,
                                 @Valid @RequestBody CreateBoardRequest request) {
        return boardService.createBoard(userId, request);
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 단일 조회 API", description = "게시글을 단일 조회하는 API 입니다.")
    public BoardInfo getBoard(@JwtValidation Long userId,
                              @PathVariable Long boardId) {
        return boardService.getBoard(userId, boardId);
    }

    @GetMapping("/list")
    @Operation(summary = "게시글 리스트 전체 조회 API", description = "게시글 리스트를 전체 조회하는 API 입니다.")
    public PagedBoardInfo getBoardList(@JwtValidation Long userId,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate gameStartDate,
                                       @RequestParam(required = false) Integer maxPerson,
                                       @RequestParam(required = false) Long preferredTeamId,
                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                       @Parameter(hidden = true) Pageable pageable) {
        return boardService.getBoardList(userId, gameStartDate, maxPerson, preferredTeamId, pageable);
    }

    @GetMapping("/list/{userId}")
    @Operation(summary = "상대방이 작성한 게시글 조회 API", description = "상대방이 작성한 게시글을 조회하는 API 입니다.")
    public PagedBoardInfo getBoardListByUserId(@JwtValidation Long loginUserId,
                                               @PathVariable Long userId,
                                               @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                               @Parameter(hidden = true) Pageable pageable) {
        return boardService.getBoardListByUserId(loginUserId, userId, pageable);
    }

    @PostMapping("/bookmark/{boardId}")
    @Operation(summary = "원하는 게시글을 찜하는 API", description = "원하는 게시글을 찜하는 API 입니다.")
    public StateResponse addBookMark(@JwtValidation Long userId,
                                     @PathVariable Long boardId) {
        return bookMarkService.addBookMark(userId, boardId);
    }

    @DeleteMapping("/bookmark/{boardId}")
    @Operation(summary = "원하는 게시글을의 찜을 삭제하는 API", description = "원하는 게시글을의 찜을 삭제하는 API 입니다.")
    public StateResponse removeBookMark(@JwtValidation Long userId,
                                        @PathVariable Long boardId) {
        return bookMarkService.removeBookMark(userId, boardId);
    }

    @PatchMapping("/{boardId}")
    @Operation(summary = "게시글 수정 API", description = "게시글을 수정합니다.")
    public BoardInfo updateBoard(@JwtValidation Long userId,
                                 @PathVariable Long boardId,
                                 @Valid @RequestBody UpdateBoardRequest request) {
        return boardService.updateBoard(userId, boardId, request);
    }

    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시글 삭제 API", description = "게시글을 삭제합니다.")
    public BoardDeleteInfo deleteBoard(@JwtValidation Long userId,
                                       @PathVariable Long boardId) {
        return boardService.deleteBoard(userId, boardId);
    }
}
