package com.example.persona.setting.repository;

import com.example.persona.setting.model.UserSettingHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingHistoryRepository extends JpaRepository<UserSettingHistory, Long> {

    List<UserSettingHistory> findByCustomerKey(String customerKey);
}
