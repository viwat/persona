package com.example.persona.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.util.StringUtils;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@MappedSuperclass
public class CustomerBaseRequest implements Serializable {
    @JsonProperty("master_account_no")
    private String masterAccountNo;

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("phone_no")
    private String phoneNo;

    @JsonProperty("customer_segment")
    private String customerSegment;

    @JsonProperty("customer_sub_segment")
    private String customerSubSegment;

    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("customer_key")
    private String customerKey;

    /** True when the client sent a non-blank {@code customer_key} (not inferred from customer_no / account_no). */
    public boolean hasProvidedCustomerKey() {
        return StringUtils.hasText(customerKey);
    }

    /** Trimmed {@code customer_key} from the request body only; {@code null} if absent or blank. */
    public String getProvidedCustomerKey() {
        return hasProvidedCustomerKey() ? customerKey.trim() : null;
    }

    /**
     * Canonical customer key: explicit {@link #customerKey} when present, otherwise
     * {@code customerNo + "_" + accountNo} when both are set, otherwise whichever side is present.
     */
    public String getCustomerKey() {
        if (hasProvidedCustomerKey()) {
            return customerKey.trim();
        }
        if (StringUtils.hasText(customerNo) && StringUtils.hasText(accountNo)) {
            return customerNo.trim() + "_" + accountNo.trim();
        }
        if (StringUtils.hasText(customerNo)) {
            return customerNo.trim();
        }
        if (StringUtils.hasText(accountNo)) {
            return accountNo.trim();
        }
        return null;
    }

    /**
     * Returns the customer number segment from a composite {@code customer_key} ({@code customerNo_accountNo}
     * or {@code customerNo_accountNo_masterId}). If there is no {@code '_'}, returns the trimmed whole key.
     */
    public static String deriveCustomerNo(String customerKey) {
        if (!StringUtils.hasText(customerKey)) {
            return null;
        }
        String key = customerKey.trim();
        int sep = key.indexOf('_');
        return sep > 0 ? key.substring(0, sep) : key;
    }
}
