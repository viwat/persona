package com.example.persona.setting.aspect;

import com.example.persona.config.UserContextHolder;
import com.example.persona.setting.annotation.SettingValue;
import com.example.persona.setting.service.SettingService;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class SettingAspect {

    private final SettingService settingService;

    @Around("@annotation(com.example.persona.setting.annotation.SettingValue)")
    public Object getSettingValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SettingValue annotation = method.getAnnotation(SettingValue.class);
        String settingName = annotation.value();
        String defaultValue = annotation.defaultValue();
        boolean isCustomerSetting = annotation.isCustomerSetting();

        String settingValue;
        if (isCustomerSetting) {
            settingValue = settingService.getSettingValue(
                    settingName, UserContextHolder.getCurrentContext().getCustomerKey(), "MOBAPP");
            if (settingValue == null) {
                settingValue = settingService.getSettingValue(settingName, defaultValue, "MOBAPP");
            }
        } else {
            settingValue = settingService.getSettingValue(settingName, defaultValue, "MOBAPP");
        }

        return settingValue;
    }
}
