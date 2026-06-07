package com.example.persona.theme.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "dgtl_user_persona_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersonaHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_sn")
    private Long personaSn;

    @Column(name = "customer_key", length = 255)
    private String customerKey;

    @Column(name = "theme_code", length = 255)
    private String themeCode;

    @Column(name = "persona_code", length = 255)
    private String personaCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "previous_theme_code", length = 255)
    private String previousThemeCode;

    @Column(name = "previous_persona_code", length = 255)
    private String previousPersonaCode;

    @Column(name = "previous_accent_color", length = 255)
    private String previousAccentColor;

    @Column(name = "previous_appearance", length = 255)
    private String previousAppearance;

    @Column(name = "previous_text_size", length = 255)
    private String previousTextSize;

    @Column(name = "previous_version")
    private Integer previousVersion;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "changed_by", length = 255)
    private String changedBy;
}
