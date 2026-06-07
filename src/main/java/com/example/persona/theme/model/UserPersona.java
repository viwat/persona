package com.example.persona.theme.model;

import com.example.persona.model.BaseCustomerEntity;
import com.example.persona.theme.StringListConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dgtl_user_persona")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserPersona extends BaseCustomerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_code", length = 255)
    private String personaCode;

    @Column(name = "channel_code", length = 255)
    private String channelCode;

    @Column(name = "theme_code", length = 255)
    private String themeCode;

    @Column(name = "theme_category", length = 255)
    private String themeCategory;

    @Column(name = "accent_color", length = 255)
    private String accentColor;

    @Column(name = "app_icon", length = 255)
    private String appIcon;

    @Column(name = "appearance", length = 255)
    private String appearance;

    @Column(name = "text_size", length = 255)
    private String textSize;

    @Column(name = "metadata", length = 1000)
    private String metadata;

    @Column(name = "control_attr1", length = 255)
    private String controlAttr1;

    @Column(name = "control_attr2", length = 255)
    private String controlAttr2;

    @Column(name = "control_attr3", length = 255)
    private String controlAttr3;

    @Column(name = "control_attr4", length = 255)
    private String controlAttr4;

    @Column(name = "control_attr5", length = 255)
    private String controlAttr5;

    @Column(name = "service_order", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> serviceOrders;

    @Column(name = "account_order", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> accountOrders;

    @Column(name = "card_order", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> cardOrders;

    @Column(name = "widget_order", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> widgetOrders;

    @Column(name = "suggested_widget", length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> suggestedWidgets;

    @Column(name = "version")
    private Integer version;
}
