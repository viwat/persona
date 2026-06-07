package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.widget.dto.request.AddWidgetRequest;
import com.example.persona.widget.dto.response.WidgetResponse;
import com.example.persona.widget.service.WidgetService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@Tag(name = "Widget API", description = "APIs for managing widget data")
public class WidgetController {

    private final WidgetService widgetService;

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<List<WidgetResponse>>> getActiveWidgets(
            @RequestBody CustomerBaseRequest request) {
        List<WidgetResponse> widgets = widgetService.findActiveWidgets(request);
        return ResponseEntity.ok(ApiResponse.success(widgets));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WidgetResponse>> addWidget(@RequestBody AddWidgetRequest request) {
        WidgetResponse widget = widgetService.addWidget(request);
        return ResponseEntity.ok(ApiResponse.success(widget));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<WidgetResponse>> updateWidget(@RequestBody AddWidgetRequest request) {
        WidgetResponse widget = widgetService.updateWidget(request);
        return ResponseEntity.ok(ApiResponse.success(widget));
    }

    @PostMapping("/delete/{sn}")
    public ResponseEntity<ApiResponse<WidgetResponse>> deleteWidget(
            @PathVariable Long sn, @RequestBody CustomerBaseRequest request) {
        WidgetResponse widget = widgetService.deleteWidget(sn, request);
        return ResponseEntity.ok(ApiResponse.success(widget));
    }

    @PostMapping("/my-widget")
    public ResponseEntity<ApiResponse<List<WidgetResponse>>> getActiveWidgetsBycustomerKey(
            CustomerBaseRequest request) {
        List<WidgetResponse> widgets = widgetService.getActiveWidgetsBycustomerKey(request);
        return ResponseEntity.ok(ApiResponse.success(widgets));
    }

    @PostMapping("/detail/{widgetCode}/{personalWidgetSn}")
    public ResponseEntity<ApiResponse<WidgetResponse>> getWidgetDetail(
            @PathVariable String widgetCode,
            @PathVariable Long personalWidgetSn,
            @RequestBody CustomerBaseRequest request) {
        WidgetResponse widget = widgetService.getWidgetDetail(widgetCode, personalWidgetSn, request);
        return ResponseEntity.ok(ApiResponse.success(widget));
    }
}
