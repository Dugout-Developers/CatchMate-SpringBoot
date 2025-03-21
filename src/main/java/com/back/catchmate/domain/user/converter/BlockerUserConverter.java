package com.back.catchmate.domain.user.converter;

import com.back.catchmate.domain.user.entity.BlockedUser;
import com.back.catchmate.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BlockerUserConverter {
    public BlockedUser toEntity(User blocker, User blockerUser) {
        return BlockedUser.builder()
                .blocker(blocker)
                .blocked(blockerUser)
                .build();
    }
}
