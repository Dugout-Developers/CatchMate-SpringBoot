package com.back.catchmate.domain.club.controller;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "구단 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService clubService;

    @GetMapping("/list")
    @Operation(summary = "구단 정보 리스트 조회 API", description = "구단 정보를 리스트로 조회하는 API 입니다.")
    public ClubInfoList getNotification() {
        return clubService.getClubInfoList();
    }
}
