package com.back.catchmate.domain.club.converter;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.club.entity.Club;
import org.springframework.stereotype.Component;

@Component
public class ClubConverter {
    public ClubInfo toClubInfo(Club club) {
        return ClubInfo.builder()
                .name(club.getName())
                .homeStadium(club.getHomeStadium())
                .region(club.getRegion())
                .build();
    }
}

