package com.back.catchmate.domain.club.service;

import com.back.catchmate.domain.club.dto.ClubResponse;
import com.back.catchmate.domain.club.entity.Club;

public interface ClubService {
    ClubResponse.ClubInfoList getClubInfoList();
    Club getClub(Long clubId);
}
