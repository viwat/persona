package com.example.persona.migration.util;

import com.example.persona.dto.response.AccountDetail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.util.StringUtils;

/**
 * Migration field helpers
 */
public final class CustomerProfileMigrationFields {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter SIMPLE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CustomerProfileMigrationFields() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Prefer CDP contact numbers over Oracle: legacy Oracle often stores masked or stale
     * {@code phoneNumber} while CDP has {@code mobile_number} / {@code msisdn}.
     */
    public static String resolvePhoneForMigration(String oraclePhone, AccountDetail.AccountDetailData cdp) {
        if (cdp != null) {
            if (StringUtils.hasText(cdp.getMobileNumber())) {
                return cdp.getMobileNumber().trim();
            }
            if (StringUtils.hasText(cdp.getMsisdn())) {
                return cdp.getMsisdn().trim();
            }
            if (StringUtils.hasText(cdp.getTelPhone())) {
                return cdp.getTelPhone().trim();
            }
        }
        return StringUtils.hasText(oraclePhone) ? oraclePhone.trim() : null;
    }

    /**
     * Uses CDP {@code address} when set; otherwise builds a line from street / address lines.
     */
    public static String resolveCurrentAddress(AccountDetail.AccountDetailData cdp) {
        if (cdp == null) {
            return null;
        }
        if (StringUtils.hasText(cdp.getAddress())) {
            return cdp.getAddress().trim();
        }
        StringBuilder sb = new StringBuilder();
        appendAddressPart(sb, cdp.getStreetHouse());
        appendAddressPart(sb, cdp.getAddressLine1());
        appendAddressPart(sb, cdp.getAddressLine2());
        appendAddressPart(sb, cdp.getAddressLine3());
        appendAddressPart(sb, cdp.getAddressLine4());
        return !sb.isEmpty() ? sb.toString() : null;
    }

    /**
     * Parses CDP {@code unique_id_exp} (typically a date string) into a timestamp for
     * {@code profile_identification_expired}.
     */
    public static LocalDateTime parseUniqueIdExpiration(String uniqueIdExp) {
        if (!StringUtils.hasText(uniqueIdExp)) {
            return null;
        }
        String t = uniqueIdExp.trim();
        try {
            return LocalDate.parse(t, ISO_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDate.parse(t, SIMPLE_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static void appendAddressPart(StringBuilder sb, String part) {
        if (!StringUtils.hasText(part)) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(", ");
        }
        sb.append(part.trim());
    }

    /**
     * Customer name to persist during migration: CDP {@code full_name}, or composed
     * first/middle/last, or {@code customer_name1}. Oracle {@code USER_NAME} is not used.
     */
    public static String resolveCustomerNameForMigration(AccountDetail.AccountDetailData cdp) {
        return primaryCdpPersonName(cdp);
    }

    /** CDP {@code full_name}, or composed first/middle/last, or {@code customer_name1}. */
    static String primaryCdpPersonName(AccountDetail.AccountDetailData cdp) {
        if (cdp == null) {
            return null;
        }
        if (StringUtils.hasText(cdp.getName())) {
            return cdp.getName().trim();
        }
        String composed = joinLatinNameParts(cdp.getFirstName(), cdp.getMiddleName(), cdp.getLastName());
        if (StringUtils.hasText(composed)) {
            return composed;
        }
        return StringUtils.hasText(cdp.getCustomerName1())
                ? cdp.getCustomerName1().trim()
                : null;
    }

    private static String joinLatinNameParts(String first, String middle, String last) {
        String a = StringUtils.hasText(first) ? first.trim() : "";
        String m = StringUtils.hasText(middle) ? middle.trim() : "";
        String b = StringUtils.hasText(last) ? last.trim() : "";
        StringBuilder sb = new StringBuilder();
        if (!a.isEmpty()) {
            sb.append(a);
        }
        if (!m.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(m);
        }
        if (!b.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(b);
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /** Joins Khmer first/last names for {@code customer_name_kh} when CDP provides them. */
    public static String joinKhmerNames(String firstNameInKhmer, String lastNameInKhmer) {
        String a = StringUtils.hasText(firstNameInKhmer) ? firstNameInKhmer.trim() : "";
        String b = StringUtils.hasText(lastNameInKhmer) ? lastNameInKhmer.trim() : "";
        if (a.isEmpty()) {
            return b.isEmpty() ? null : b;
        }
        if (b.isEmpty()) {
            return a;
        }
        return a + " " + b;
    }
}
