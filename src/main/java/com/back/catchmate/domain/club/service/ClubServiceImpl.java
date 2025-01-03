package com.back.catchmate.domain.club.service;

import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.dto.ClubResponse;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {
    private final ClubRepository clubRepository;
    private final ClubConverter clubConverter;

    @Override
    @Transactional(readOnly = true)
    public ClubInfoList getClubInfoList() {
        List<Club> clubList = clubRepository.findAll();
        return clubConverter.toClubInfoList(clubList);
    }
}
