package com.example.persona.schedule.mapper;

import com.example.persona.schedule.dto.response.UserScheduleHistoryResponse;
import com.example.persona.schedule.model.UserScheduleHistory;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserScheduleHistoryMapper {
    @Mapping(source = "userSchedule.id", target = "userScheduleId")
    UserScheduleHistoryResponse fromEntity(UserScheduleHistory history);

    default Page<@NonNull UserScheduleHistoryResponse> fromPageEntity(Page<@NonNull UserScheduleHistory> history) {
        return history.map(this::fromEntity);
    }

    default List<UserScheduleHistoryResponse> fromEntityList(List<UserScheduleHistory> histories) {
        return histories.stream().map(this::fromEntity).toList();
    }
}
