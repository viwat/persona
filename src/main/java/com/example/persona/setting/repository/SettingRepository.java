package com.example.persona.setting.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.setting.model.Setting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findByCode(String code);

    List<Setting> findByStatus(StatusType status);

    default List<Setting> findActive() {
        return findByStatus(StatusType.ACTIVE);
    }

    Optional<Setting> findBySettingKey(String settingKey);

    Optional<Setting> findBySettingKeyAndStatus(String settingKey, StatusType status);

    default Optional<Setting> findBySettingKeyAndActive(String settingKey) {
        return findBySettingKeyAndStatus(settingKey, StatusType.ACTIVE);
    }

    boolean existsByCode(String code);

    boolean existsBySettingKey(String settingKey);

    @Query("""
			    select s.settingKey
			    from Setting s
			    where s.settingKey in :settingKeys
			""")
    List<String> findExistingSettingKeys(@Param("settingKeys") List<String> settingKeys);

    List<Setting> findAllByStatus(StatusType statusType);
}
