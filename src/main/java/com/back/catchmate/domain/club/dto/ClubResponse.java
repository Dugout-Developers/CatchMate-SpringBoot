package com.back.catchmate.domain.club.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public abstract class ClubResponse {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClubInfo {
        private String name; // 팀 이름
        private String homeStadium; // 홈 구장 이름
        private String region; // 지역명
    }
}
