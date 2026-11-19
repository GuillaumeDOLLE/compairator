package com.will.compairator.user.persistence;

import com.will.compairator.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    /** Nombre de comptes inscrits avant la date exclue. */
    @Query("select count(account) from UserEntity account where account.createdAt < :to")
    long countRegisteredBefore(Instant to);
    /** Comptes inscrits entre from inclus et to exclu. */
    @Query("select account from UserEntity account where account.createdAt >= :from and account.createdAt < :to")
    List<UserEntity> findCreatedBetween(Instant from, Instant to);
}
