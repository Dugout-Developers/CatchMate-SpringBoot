package com.back.catchmate.domain.club.converter;

import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfo;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.entity.Club;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClubConverter {
    public ClubInfo toClubInfo(Club club) {
        return ClubInfo.builder()
                .id(club.getId())
                .name(club.getName())
                .homeStadium(club.getHomeStadium())
                .region(club.getRegion())
                .build();
    }

    public ClubInfoList toClubInfoList(List<Club> clubList) {
        List<ClubInfo> clubInfoList = clubList.stream()
                .map(this::toClubInfo)
                .toList();

        return new ClubInfoList(clubInfoList);
    }
}

