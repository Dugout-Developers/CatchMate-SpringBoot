package com.back.catchmate.domain.club.controller;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.service.ClubService;
import com.back.catchmate.global.jwt.JwtService; // [추가]
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClubControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClubService clubService;
    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("구단 정보 리스트 조회 API 테스트")
    @WithMockUser
    void getClubInfoList_Success() throws Exception {
        // given
        ClubInfo kia = ClubInfo.builder()
                .id(1L)
                .name("KIA Tigers")
                .homeStadium("Champions Field")
                .region("Gwangju")
                .build();

        ClubInfo doosan = ClubInfo.builder()
                .id(2L)
                .name("Doosan Bears")
                .homeStadium("Jamsil Baseball Stadium")
                .region("Seoul")
                .build();

        ClubInfoList response = new ClubInfoList(List.of(kia, doosan));

        given(clubService.getClubInfoList()).willReturn(response);

        // when & then
        mockMvc.perform(get("/clubs/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubInfoList").isArray())
                .andExpect(jsonPath("$.clubInfoList[0].name").value("KIA Tigers"))
                .andExpect(jsonPath("$.clubInfoList[0].region").value("Gwangju"))
                .andExpect(jsonPath("$.clubInfoList[1].name").value("Doosan Bears"))
                .andExpect(jsonPath("$.clubInfoList[1].region").value("Seoul"));
    }
}
