package com.example.persona.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.persona.enums.StatusType;
import com.example.persona.location.exception.LocationTypeNotFoundException;
import com.example.persona.location.mapper.LocationTypeMapper;
import com.example.persona.location.model.LocationTag;
import com.example.persona.location.model.LocationType;
import com.example.persona.location.repository.LocationTagRepository;
import com.example.persona.location.repository.LocationTypeRepository;
import com.example.persona.model.MultilingualContent;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationTypeService")
class LocationTypeServiceTest {

    @Mock
    private LocationTypeRepository locationTypeRepository;

    @Mock
    private LocationTagRepository tagRepository;

    private LocationTypeService service;

    @BeforeEach
    void setUp() {
        service = new LocationTypeService(locationTypeRepository, tagRepository, new LocationTypeMapper());
    }

    private static LocationType locationType(String code, String name) {
        return LocationType.builder()
                .code(code)
                .name(MultilingualContent.builder().en(name).build())
                .build();
    }

    @Test
    @DisplayName("listLocationTypes maps active types")
    void listLocationTypes() {
        when(locationTypeRepository.findAllByStatusWithTags(StatusType.ACTIVE))
                .thenReturn(List.of(locationType("branch", "Branch"), locationType("agent", "Agent")));

        var result = service.listLocationTypes();

        assertThat(result).extracting("code").containsExactly("branch", "agent");
    }

    @Test
    @DisplayName("getByCode returns the type when found")
    void getByCodeFound() {
        when(locationTypeRepository.findByCode("branch"))
                .thenReturn(Optional.of(locationType("branch", "Branch")));
        assertThat(service.getByCode("branch").name()).isEqualTo("Branch");
    }

    @Test
    @DisplayName("getByCode throws when missing")
    void getByCodeMissing() {
        when(locationTypeRepository.findByCode("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getByCode("nope"))
                .isInstanceOf(LocationTypeNotFoundException.class)
                .hasMessageContaining("Location type not found: nope");
    }

    @Test
    @DisplayName("listTags maps active tags")
    void listTags() {
        when(tagRepository.findByStatusOrderByCodeAsc(StatusType.ACTIVE))
                .thenReturn(List.of(LocationTag.builder()
                        .code("wifi")
                        .name(MultilingualContent.builder().en("WiFi").build())
                        .build()));

        assertThat(service.listTags()).singleElement().extracting("code").isEqualTo("wifi");
    }

    @Test
    @DisplayName("getTag throws when missing")
    void getTagMissing() {
        when(tagRepository.findByCode("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getTag("nope"))
                .isInstanceOf(LocationTypeNotFoundException.class)
                .hasMessageContaining("Tag not found: nope");
    }
}
