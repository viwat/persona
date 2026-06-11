package com.example.persona.location.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Location domain")
class LocationDomainTest {

    private static Coordinate coord() {
        return new Coordinate(11.556, 104.928);
    }

    private static Address address() {
        return Address.builder().province("Phnom Penh").country("Cambodia").build();
    }

    private static Location.Draft.DraftBuilder validDraft() {
        return Location.Draft.builder()
                .name("  Wing Bank - Main  ")
                .type("branch")
                .coordinate(coord())
                .address(address());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("sets defaults, trims name, and copies attributes")
        void setsDefaults() {
            Location.Draft draft = validDraft()
                    .availableServices(List.of("Transfer"))
                    .avgRating(4.5)
                    .action(ActionLink.builder().label("Open").url("https://x").build())
                    .build();

            Location location = Location.create(draft, "actor-1");

            assertThat(location.getId()).isNotNull();
            assertThat(location.getName()).isEqualTo("Wing Bank - Main"); // trimmed
            assertThat(location.getStatus()).isEqualTo(LocationStatus.ACTIVE);
            assertThat(location.isTemporarilyClosed()).isFalse();
            assertThat(location.getCreatedBy()).isEqualTo("actor-1");
            assertThat(location.getUpdatedBy()).isEqualTo("actor-1");
            assertThat(location.getCreatedAt()).isNotNull();
            assertThat(location.getType()).isEqualTo("branch");
            assertThat(location.getAvgRating()).isEqualTo(4.5);
            assertThat(location.getAction().getLabel()).isEqualTo("Open");
            assertThat(location.getAvailableServices()).containsExactly("Transfer");
        }

        @Test
        @DisplayName("defaults availableServices to empty when null")
        void defaultsServicesEmpty() {
            Location location = Location.create(validDraft().build(), "a");
            assertThat(location.getAvailableServices()).isEmpty();
        }

        @Test
        @DisplayName("rejects missing required fields")
        void rejectsMissingRequired() {
            assertThatThrownBy(() -> Location.create(validDraft().name(null).build(), "a"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Location.create(validDraft().type(null).build(), "a"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(
                            () -> Location.create(validDraft().coordinate(null).build(), "a"))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Location.create(validDraft().address(null).build(), "a"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("null components leave existing values unchanged")
        void partialUpdateKeepsExisting() {
            Location original =
                    Location.create(validDraft().branchCode("BR1").build(), "creator");

            Location updated =
                    original.update(Location.Draft.builder().name("New Name").build(), "editor");

            assertThat(updated.getName()).isEqualTo("New Name");
            assertThat(updated.getBranchCode()).isEqualTo("BR1"); // unchanged
            assertThat(updated.getType()).isEqualTo("branch"); // unchanged
            assertThat(updated.getUpdatedBy()).isEqualTo("editor");
            assertThat(updated.getCreatedBy()).isEqualTo("creator"); // preserved
            assertThat(updated.getId()).isEqualTo(original.getId());
        }

        @Test
        @DisplayName("overrides only the provided components")
        void overridesProvided() {
            Location original = Location.create(validDraft().avgRating(3.0).build(), "c");

            Location updated = original.update(
                    Location.Draft.builder()
                            .avgRating(4.8)
                            .branchName("Main Branch")
                            .build(),
                    "e");

            assertThat(updated.getAvgRating()).isEqualTo(4.8);
            assertThat(updated.getBranchName()).isEqualTo("Main Branch");
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("closeTemporarily and reopen toggle operating status without changing LocationStatus")
        void closeAndReopen() {
            Location active = Location.create(validDraft().build(), "c");
            var until = java.time.Instant.parse("2026-12-31T17:00:00Z");

            Location closed = active.closeTemporarily(until, "e");
            assertThat(closed.isTemporarilyClosed()).isTrue();
            assertThat(closed.getClosedUntil()).isEqualTo(until);
            assertThat(closed.getStatus()).isEqualTo(LocationStatus.ACTIVE);

            Location reopened = closed.reopen("e");
            assertThat(reopened.isTemporarilyClosed()).isFalse();
            assertThat(reopened.getClosedUntil()).isNull();
        }
    }
}
