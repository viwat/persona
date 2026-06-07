package com.example.persona.widget.service;

import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.utils.LanguageUtils;
import com.example.persona.widget.dto.request.AddWidgetRequest;
import com.example.persona.widget.dto.request.ParamKeyValue;
import com.example.persona.widget.dto.response.WidgetResponse;
import com.example.persona.widget.model.PersonaWidget;
import com.example.persona.widget.model.Widget;
import com.example.persona.widget.repository.PersonaWidgetRepository;
import com.example.persona.widget.repository.WidgetRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WidgetService {

    private final WidgetRepository widgetRepository;
    private final PersonaWidgetRepository personaWidgetRepository;

    // Cache key format: "segment:subSegment"
    private final Map<String, List<WidgetResponse>> widgetCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<WidgetResponse> findActiveWidgets(CustomerBaseRequest request) {
        String cacheKey = generateCacheKey(request.getCustomerSegment(), request.getCustomerSubSegment());

        // Try to get fromEntity cache first
        return widgetCache.computeIfAbsent(cacheKey, key -> {
            log.debug(
                    "Cache miss for segment: {} and subSegment: {}. Fetching fromEntity database",
                    request.getCustomerSegment(),
                    request.getCustomerSubSegment());
            return widgetRepository
                    .findActiveWidgets(request.getCustomerSegment(), request.getCustomerSubSegment(), StatusType.ACTIVE)
                    .stream()
                    .map(this::mapToWidgetResponse)
                    .collect(Collectors.toList());
        });
    }

    private WidgetResponse mapToWidgetResponse(Widget widget) {
        return WidgetResponse.builder()
                .id(widget.getId())
                .name(LanguageUtils.getLocalizedText(widget.getName()))
                .description(LanguageUtils.getLocalizedText(widget.getDescription()))
                .imageUrl(widget.getPreviewUrl())
                .deepLink(widget.getDeepLink())
                .secondaryPreviewUrl(widget.getSecondaryPreviewUrl())
                .appVersion(widget.getAppVersion())
                .param1Key(widget.getParam1Key())
                .param1Type(widget.getParam1Type())
                .param2Key(widget.getParam2Key())
                .param2Type(widget.getParam2Type())
                .param3Key(widget.getParam3Key())
                .param3Type(widget.getParam3Type())
                .param4Key(widget.getParam4Key())
                .param4Type(widget.getParam4Type())
                .param5Key(widget.getParam5Key())
                .param5Type(widget.getParam5Type())
                .build();
    }

    private String generateCacheKey(String segment, String subSegment) {
        return String.format(
                "%s:%s",
                Optional.ofNullable(segment).orElse("null"),
                Optional.ofNullable(subSegment).orElse("null"));
    }

    // Method to invalidate cache for specific segment/subSegment
    public void invalidateCache(String segment, String subSegment) {
        String cacheKey = generateCacheKey(segment, subSegment);
        widgetCache.remove(cacheKey);
        log.debug("Cache invalidated for segment: {} and subSegment: {}", segment, subSegment);
    }

    // Method to clear entire cache
    public void clearCache() {
        widgetCache.clear();
        log.debug("Entire widget cache cleared");
    }

    public WidgetResponse addWidget(AddWidgetRequest request) {
        PersonaWidget widget = new PersonaWidget();

        // Set basic fields
        widget.setCustomerNo(request.getCustomerNo());
        widget.setAccountNo(request.getAccountNo());
        widget.setPhoneNo(request.getPhoneNo());
        widget.setMasterAccountNo(request.getMasterAccountNo());
        widget.setCustomerKey(request.getCustomerKey());
        widget.setChannelCode(request.getChannelCode());
        widget.setWidgetId(request.getWidgetId());
        widget.setWidgetCode(request.getWidgetCode());
        widget.setDisplayOrder(request.getDisplayOrder());

        // Handle parameters dynamically
        Map<Integer, ParamKeyValue> parameters = request.getParameters();
        if (parameters != null) {
            for (Map.Entry<Integer, ParamKeyValue> entry : parameters.entrySet()) {
                int paramNumber = entry.getKey();
                ParamKeyValue param = entry.getValue();

                if (paramNumber >= 1 && paramNumber <= 5) {
                    String methodNameKey = String.format("setParam%dKey", paramNumber);
                    String methodNameValue = String.format("setParam%dValue", paramNumber);

                    try {
                        widget.getClass().getMethod(methodNameKey, String.class).invoke(widget, param.getKey());

                        widget.getClass()
                                .getMethod(methodNameValue, String.class)
                                .invoke(widget, param.getValue());
                    } catch (Exception e) {
                        throw new RuntimeException("Error setting parameters", e);
                    }
                }
            }
        }

        PersonaWidget savedWidget = personaWidgetRepository.save(widget);
        return mapToWidgetResponse(savedWidget);
    }

    private WidgetResponse mapToWidgetResponse(PersonaWidget widget) {
        return WidgetResponse.builder().id(widget.getId()).build();
    }

    public WidgetResponse updateWidget(AddWidgetRequest request) {
        PersonaWidget widget = personaWidgetRepository
                .findByCustomerNoAndId(request.getCustomerNo(), request.getId())
                .orElseThrow(() -> new BusinessException("Widget not found"));
        widget.setDisplayOrder(request.getDisplayOrder());
        PersonaWidget savedWidget = personaWidgetRepository.save(widget);
        return mapToWidgetResponse(savedWidget);
    }

    public WidgetResponse deleteWidget(Long sn, CustomerBaseRequest request) {
        PersonaWidget widget = personaWidgetRepository
                .findByCustomerNoAndId(request.getCustomerNo(), sn)
                .orElseThrow(() -> new BusinessException("Widget not found"));
        personaWidgetRepository.delete(widget);
        return mapToWidgetResponse(widget);
    }

    public WidgetResponse getWidgetDetail(String widgetCode, Long personalWidgetSn, CustomerBaseRequest request) {
        PersonaWidget widget = personaWidgetRepository
                .findByCustomerNoAndId(request.getCustomerNo(), personalWidgetSn)
                .orElseThrow(() -> new BusinessException("Widget not found"));
        return mapToWidgetResponse(widget);
    }

    public List<WidgetResponse> getActiveWidgetsBycustomerKey(CustomerBaseRequest request) {
        List<Widget> widgets = widgetRepository.findActiveWidgets(
                request.getCustomerSegment(), request.getCustomerSubSegment(), StatusType.ACTIVE);
        return widgets.stream().map(this::mapToWidgetResponse).collect(Collectors.toList());
    }
}
