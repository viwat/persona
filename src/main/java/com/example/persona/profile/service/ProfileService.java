package com.example.persona.profile.service;

import com.example.persona.config.UserContextHolder;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.enums.ErrorCode;
import com.example.persona.exception.BusinessException;
import com.example.persona.dto.UserContext;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.request.ProfileRequest;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.dto.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Thin facade for legacy {@code /profile/me} routes. Delegates to {@link CustomerAppProfileService}
 * using {@link UserContextHolder#customerKey} to resolve {@code customer_no}.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CustomerAppProfileService customerAppProfileService;

    public ProfileResponse getProfile() {
        String customerNo = resolveCustomerNoFromContext();
        CustomerAppProfileResponse app = customerAppProfileService.getCustomerAppProfile(customerNo);
        return mapFromCustomerAppProfile(app);
    }

    public ProfileResponse updateProfile(ProfileRequest profileRequest) {
        UserContext ctx = requireContext();
        String customerNo = CustomerBaseRequest.deriveCustomerNo(ctx.getCustomerKey());
        CustomerAppProfileRequest update = CustomerAppProfileRequest.builder()
                .customerNo(customerNo)
                .customerKey(ctx.getCustomerKey().trim())
                .customerName(profileRequest.getName())
                .email(profileRequest.getEmail())
                .phoneNo(profileRequest.getPhone())
                .build();
        CustomerAppProfileResponse updated = customerAppProfileService.updateCustomerAppProfile(update);
        return mapFromCustomerAppProfile(updated);
    }

    private static UserContext requireContext() {
        UserContext ctx = UserContextHolder.getCurrentContext();
        if (ctx == null || !StringUtils.hasText(ctx.getCustomerKey())) {
            throw new BusinessException("Missing authenticated customer context", ErrorCode.BUSINESS_ERROR);
        }
        return ctx;
    }

    private static String resolveCustomerNoFromContext() {
        return CustomerBaseRequest.deriveCustomerNo(requireContext().getCustomerKey());
    }

    private static ProfileResponse mapFromCustomerAppProfile(CustomerAppProfileResponse app) {
        if (app == null) {
            return ProfileResponse.builder().build();
        }
        return ProfileResponse.builder()
                .id(app.getCustomerNo())
                .name(app.getCustomerName())
                .email(app.getEmail())
                .phone(app.getPhoneNo())
                .isKYC(StringUtils.hasText(app.getKycStatus())
                        && !"N/A".equalsIgnoreCase(app.getKycStatus().trim()))
                .build();
    }
}
