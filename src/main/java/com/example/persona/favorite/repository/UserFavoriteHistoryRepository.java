package com.example.persona.favorite.repository;

import com.example.persona.favorite.model.UserFavoriteHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteHistoryRepository extends JpaRepository<UserFavoriteHistory, Long> {
    List<UserFavoriteHistory> findByCustomerKey(String customerKey);

    List<UserFavoriteHistory> findByUserFavoriteId(Long userFavoriteId);

    List<UserFavoriteHistory> findByFavoriteId(Long favoriteId);
}
