package com.example.persona.setting.repository;

import com.example.persona.enums.StatusType;
import com.example.persona.setting.model.Setting;
import com.example.persona.setting.model.UserSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    List<UserSetting> findByCustomerNoAndCustomerKey(String customerNo, String customerKey);

    Optional<UserSetting> findByCustomerNoAndSetting(String customerNo, Setting setting);

    List<UserSetting> findByCustomerKeyAndStatus(String customerKey, StatusType status);

    default List<UserSetting> findByCustomerKeyAndActive(String customerKey) {
        return findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE);
    }

    Optional<UserSetting> findByCustomerKeyAndSettingAndStatus(String customerKey, Setting setting, StatusType status);

    default Optional<UserSetting> findActiveByCustomerKeyAndSetting(String customerKey, Setting setting) {
        return findByCustomerKeyAndSettingAndStatus(customerKey, setting, StatusType.ACTIVE);
    }

    @Query("""
			    SELECT us
			    FROM UserSetting us
			    JOIN us.setting s
			    WHERE us.customerKey = :customerKey
			      AND s.settingKey = :settingKey
			      AND us.channelCode = :channelCode
			      AND us.status = :status
			""")
    Optional<UserSetting> findByCustomerKeyAndSettingKeyAndChannelCodeAndStatus(
            @Param("customerKey") String customerKey,
            @Param("settingKey") String settingKey,
            @Param("channelCode") String channelCode,
            @Param("status") StatusType status);

    Optional<UserSetting> findByCustomerNoAndAccountNoAndChannelCodeAndSetting(
            String customerNo, String accountNo, String channelCode, Setting setting);

    List<UserSetting> findByCustomerNo(String customerNo);

    Optional<UserSetting> findByIdAndCustomerKeyAndStatus(
            Long userSettingId, String customerKey, StatusType statusType);
}
