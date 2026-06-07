package com.example.persona.widget.dto.request;

import com.example.persona.dto.CustomerBaseRequest;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWidgetRequest extends CustomerBaseRequest {
    private Long id;
    private Long widgetId;
    private String widgetCode;
    private Integer displayOrder;
    private Map<Integer, ParamKeyValue> parameters;
}
