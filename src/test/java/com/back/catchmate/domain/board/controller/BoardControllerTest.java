package com.back.catchmate.domain.board.controller;

import com.back.catchmate.domain.board.dto.BoardRequest.CreateOrUpdateBoardRequest;
import com.back.catchmate.domain.board.dto.BoardResponse.*;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.domain.board.service.BookMarkService;
import com.back.catchmate.domain.game.dto.GameRequest.CreateGameRequest;
import com.back.catchmate.global.dto.StateResponse;
import com.back.catchmate.global.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private BookMarkService bookMarkService;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // JwtValidation 리졸버가 동작하도록 Mock 설정
        given(jwtService.parseJwtToken(anyString())).willReturn(1L);
    }

    @Test
    @DisplayName("게시글 등록 API 테스트")
    @WithMockUser
    void createBoard_Success() throws Exception {
        // given
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .homeClubId(1L).awayClubId(2L).gameStartDate("2024-11-29 18:30:00").location("Seoul").build();

        CreateOrUpdateBoardRequest request = CreateOrUpdateBoardRequest.builder()
                .title("제목")
                .content("내용")
                .maxPerson(4)
                .cheerClubId(1L)
                .gameRequest(gameRequest)
                // [수정] 필수 필드 추가
                .preferredGender("M")
                .preferredAgeRange(List.of("20s"))
                .isCompleted(true)
                .build();

        BoardInfo response = BoardInfo.builder().boardId(10L).title("제목").build();

        given(boardService.createOrUpdateBoard(any(), eq(null), any())).willReturn(response);
        // when & then
        mockMvc.perform(post("/boards")
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(10L));
    }

    @Test
    @DisplayName("게시글 단일 조회 API 테스트")
    @WithMockUser
    void getBoard_Success() throws Exception {
        // given
        Long boardId = 10L;
        BoardInfo response = BoardInfo.builder().boardId(boardId).title("상세 조회").build();

        given(boardService.getBoard(any(), eq(boardId))).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/{boardId}", boardId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("상세 조회"));
    }

    @Test
    @DisplayName("게시글 리스트 전체 조회 API 테스트 (필터링)")
    @WithMockUser
    void getBoardList_Success() throws Exception {
        // given
        PagedBoardInfo response = PagedBoardInfo.builder().totalElements(5L).build();

        // OptionalJwtValidation이므로 토큰이 있어도 되고 없어도 됨 (여기선 있는 경우 테스트)
        given(boardService.getBoardList(any(), any(), any(), any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/list")
                        .header("AccessToken", "token")
                        .param("gameStartDate", LocalDate.now().toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5L));
    }

    @Test
    @DisplayName("상대방이 작성한 게시글 조회 API 테스트")
    @WithMockUser
    void getBoardListByUserId_Success() throws Exception {
        // given
        Long targetUserId = 20L;
        PagedBoardInfo response = PagedBoardInfo.builder().totalElements(3L).build();

        given(boardService.getBoardListByUserId(any(), eq(targetUserId), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/list/{userId}", targetUserId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3L));
    }

    @Test
    @DisplayName("임시저장된 게시글 단일 조회 API 테스트")
    @WithMockUser
    void getTempBoard_Success() throws Exception {
        // given
        TempBoardInfo response = TempBoardInfo.builder().boardId(5L).title("임시글").build();

        given(boardService.getTempBoard(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/temp")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("임시글"));
    }

    @Test
    @DisplayName("게시글 수정 API 테스트")
    @WithMockUser
    void updateBoard_Success() throws Exception {
        // given
        Long boardId = 10L;
        CreateGameRequest gameRequest = CreateGameRequest.builder()
                .homeClubId(1L).awayClubId(2L).gameStartDate("2024-11-30 18:30:00").location("Seoul").build();

        CreateOrUpdateBoardRequest request = CreateOrUpdateBoardRequest.builder()
                .title("수정 제목")
                // [수정] 필수 필드들을 모두 채워야 @Valid를 통과합니다.
                .content("수정 내용")
                .maxPerson(4)
                .cheerClubId(1L)
                .preferredGender("F")
                .preferredAgeRange(List.of("30s"))
                .isCompleted(true)
                .gameRequest(gameRequest)
                .build();

        BoardInfo response = BoardInfo.builder().boardId(boardId).title("수정 제목").build();

        given(boardService.createOrUpdateBoard(any(), eq(boardId), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/boards/{boardId}", boardId)
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 제목"));
    }

    @Test
    @DisplayName("게시글 끌어올리기 API 테스트")
    @WithMockUser
    void updateLiftUpDate_Success() throws Exception {
        // given
        Long boardId = 10L;
        LiftUpStatusInfo response = new LiftUpStatusInfo(true, null);

        given(boardService.updateLiftUpDate(any(), eq(boardId))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/boards/{boardId}/lift-up", boardId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("게시글 삭제 API 테스트")
    @WithMockUser
    void deleteBoard_Success() throws Exception {
        // given
        Long boardId = 10L;
        BoardDeleteInfo response = BoardDeleteInfo.builder().boardId(boardId).build();

        given(boardService.deleteBoard(any(), eq(boardId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/boards/{boardId}", boardId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(boardId));
    }

    @Test
    @DisplayName("게시글 찜하기 API 테스트")
    @WithMockUser
    void addBookMark_Success() throws Exception {
        // given
        Long boardId = 10L;
        StateResponse response = new StateResponse(true);

        given(bookMarkService.addBookMark(any(), eq(boardId))).willReturn(response);

        // when & then
        mockMvc.perform(post("/boards/bookmark/{boardId}", boardId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("찜한 게시글 조회 API 테스트")
    @WithMockUser
    void getBookMarkBoardList_Success() throws Exception {
        // given
        PagedBoardInfo response = PagedBoardInfo.builder().totalElements(2L).build();

        given(bookMarkService.getBookMarkBoardList(any(), any(Pageable.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/bookmark")
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2L));
    }

    @Test
    @DisplayName("찜 취소 API 테스트")
    @WithMockUser
    void removeBookMark_Success() throws Exception {
        // given
        Long boardId = 10L;
        StateResponse response = new StateResponse(true);

        given(bookMarkService.removeBookMark(any(), eq(boardId))).willReturn(response);

        // when & then
        mockMvc.perform(delete("/boards/bookmark/{boardId}", boardId)
                        .header("AccessToken", "token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(true));
    }

    @Test
    @DisplayName("게시글 등록 시 필수 값이 누락되면 400 예외가 발생한다")
    @WithMockUser
    void createBoard_Fail_Validation() throws Exception {
        // given
        // 필수 값인 title, content, cheerClubId 등을 비우거나 null로 설정
        CreateOrUpdateBoardRequest invalidRequest = CreateOrUpdateBoardRequest.builder()
                .title("") // @NotEmpty 위반
                .content("") // @NotEmpty 위반
                .maxPerson(-1) // @Positive, @Range 위반
                .cheerClubId(null) // @NotNull 위반
                .preferredGender(null) // @NotEmpty 위반
                .isCompleted(null) // @NotNull 위반
                .build();

        // when & then
        mockMvc.perform(post("/boards")
                        .header("AccessToken", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 확인
                .andExpect(jsonPath("$.statusCode").value(400))
                // 에러 메시지에 구체적인 필드명이 포함되어 있는지 검증 (GlobalExceptionHandler 로직에 따라 다를 수 있음)
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("title")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("content")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cheerClubId")));
    }
}
