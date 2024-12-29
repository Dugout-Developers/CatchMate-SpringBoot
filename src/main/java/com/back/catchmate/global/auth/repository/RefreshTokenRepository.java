package com.back.catchmate.global.auth.repository;

import com.back.catchmate.global.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken,String> {

}
