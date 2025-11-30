package com.back.catchmate.global.error;

import com.back.catchmate.domain.admin.service.AdminService;
import com.back.catchmate.domain.board.service.BoardService;
import com.back.catchmate.domain.board.service.BookMarkService;
import com.back.catchmate.domain.chat.service.ChatRoomService;
import com.back.catchmate.domain.chat.service.ChatService;
import com.back.catchmate.domain.chat.service.UserChatRoomService;
import com.back.catchmate.domain.club.service.ClubService;
import com.back.catchmate.domain.enroll.service.EnrollService;
import com.back.catchmate.domain.inquiry.service.InquiryService;
import com.back.catchmate.domain.notice.service.NoticeService;
import com.back.catchmate.domain.notification.service.NotificationService;
import com.back.catchmate.domain.report.service.ReportService;
import com.back.catchmate.domain.user.service.BlockedUserService;
import com.back.catchmate.domain.user.service.UserService;
import com.back.catchmate.global.auth.service.AuthService;
import com.back.catchmate.global.auth.service.AuthServiceImpl;
import com.back.catchmate.global.error.exception.BaseException;
import com.back.catchmate.global.error.exception.clientError.BadRequestException;
import com.back.catchmate.global.error.exception.serverError.DataNotFoundException;
import com.back.catchmate.global.error.exception.serverError.InternalServerException;
import com.back.catchmate.global.jwt.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- 모든 컨트롤러 의존성 Mocking ---
    @MockBean private JwtService jwtService;
    @MockBean private AdminService adminService;
    @MockBean private BlockedUserService blockedUserService;
    @MockBean private BoardService boardService;
    @MockBean private BookMarkService bookMarkService;
    @MockBean private ClubService clubService;
    @MockBean private EnrollService enrollService;
    @MockBean private NotificationService notificationService;
    @MockBean private UserService userService;
    @MockBean private InquiryService inquiryService;
    @MockBean private NoticeService noticeService;
    @MockBean private ReportService reportService;
    @MockBean private UserChatRoomService userChatRoomService;
    @MockBean private ChatService chatService;
    @MockBean private ChatRoomService chatRoomService;
    @MockBean private AuthService authService;


    /**
     * 예외 발생 시나리오를 위한 테스트용 컨트롤러 (빈 등록을 위해 public static class로 선언)
     */
    @RestController
    public static class TestController {
        @PostMapping("/test/bind")
        public void testBind(@RequestBody @Valid TestDto dto) {
        }

        @GetMapping("/test/type-mismatch")
        public void testTypeMismatch(@RequestParam Integer param) {
            // param에 문자열 전달 시 TypeMismatchException 발생 유도
        }

        @GetMapping("/test/bad-request")
        public void testBadRequest() {
            throw new BadRequestException(ErrorCode.BAD_REQUEST);
        }

        @GetMapping("/test/internal-server")
        public void testInternalServer() {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        @GetMapping("/test/data-not-found")
        public void testDataNotFound() {
            throw new DataNotFoundException(ErrorCode.BOARD_NOT_FOUND);
        }

        @GetMapping("/test/base-exception")
        public void testBaseException() {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }

        @GetMapping("/test/runtime-exception")
        public void testRuntimeException() {
            throw new RuntimeException("Unexpected Error");
        }
    }

    /**
     * Validation 테스트용 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestDto {
        @NotNull(message = "must not be null")
        private String field;
    }

    // --- 1. Validation/Parameter Exception Handlers ---

    @Test
    @DisplayName("BindException (Validation 실패) 처리 테스트")
    @WithMockUser
    void handleBindException() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/test/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 확인
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("field : must not be null")));
    }

    @Test
    @DisplayName("TypeMismatchException (파라미터 타입 불일치) 처리 테스트")
    @WithMockUser
    void handleTypeMismatchException() throws Exception {
        String invalidParam = "abc";

        mockMvc.perform(get("/test/type-mismatch")
                        .param("param", invalidParam))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 확인
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));
    }

    @Test
    @DisplayName("MissingServletRequestParameterException (필수 파라미터 누락) 처리 테스트")
    @WithMockUser
    void handleMissingParameterException() throws Exception {
        mockMvc.perform(get("/test/type-mismatch")) // param 없이 호출
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400 확인
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));
    }

    // --- 2. BaseException Integration Handlers ---

    @Test
    @DisplayName("BadRequestException 처리 테스트 (통합 핸들러)")
    @WithMockUser
    void handleBadRequestException() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.BAD_REQUEST.getMessage()));
    }

    @Test
    @DisplayName("InternalServerException 처리 테스트 (통합 핸들러)")
    @WithMockUser
    void handleInternalServerException() throws Exception {
        mockMvc.perform(get("/test/internal-server"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    @DisplayName("DataNotFoundException 처리 테스트 (통합 핸들러)")
    @WithMockUser
    void handleDataNotFoundException() throws Exception {
        mockMvc.perform(get("/test/data-not-found"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.BOARD_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("BaseException 처리 테스트 (USER_NOT_FOUND)")
    @WithMockUser
    void handleBaseException() throws Exception {
        mockMvc.perform(get("/test/base-exception"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    // --- 3. Catch-all Handler ---

    @Test
    @DisplayName("알 수 없는 RuntimeException 처리 테스트 (Default Handler)")
    @WithMockUser
    void handleRuntimeException() throws Exception {
        mockMvc.perform(get("/test/runtime-exception"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
