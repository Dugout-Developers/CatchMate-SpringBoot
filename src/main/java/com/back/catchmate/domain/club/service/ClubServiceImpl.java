package com.back.catchmate.domain.club.service;

import com.back.catchmate.domain.club.converter.ClubConverter;
import com.back.catchmate.domain.club.dto.ClubResponse.ClubInfoList;
import com.back.catchmate.domain.club.entity.Club;
import com.back.catchmate.domain.club.repository.ClubRepository;
import com.back.catchmate.global.error.ErrorCode;
import com.back.catchmate.global.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {
    private static final Long DEFAULT_CLUB_ID = 0L;
    private final ClubRepository clubRepository;
    private final ClubConverter clubConverter;

    @Override
    @Transactional(readOnly = true)
    public ClubInfoList getClubInfoList() {
        List<Club> clubList = clubRepository.findAll();
        return clubConverter.toClubInfoList(clubList);
    }

    @Override
    @Transactional(readOnly = true)
    public Club getClub(Long clubId) {
        if (clubId == null || clubId.equals(DEFAULT_CLUB_ID)) {
            return clubRepository.findById(DEFAULT_CLUB_ID)
                    .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
        }
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BaseException(ErrorCode.CLUB_NOT_FOUND));
    }
}
