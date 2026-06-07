package com.example.persona.pinless.config;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pinless")
public class PinlessSystemProperties {

    private System system = new System();
    private Defaults defaults = new Defaults();
    private Exchange exchange = new Exchange();
    private Cache cache = new Cache();

    @Getter
    @Setter
    public static class System {
        private Map<String, BigDecimal> caps = new HashMap<>();
        private Map<String, BigDecimal> minThresholds = new HashMap<>();

        public BigDecimal getCap(String currency) {
            return caps.getOrDefault(currency, caps.get("USD"));
        }

        public BigDecimal getMinThreshold(String currency) {
            return minThresholds.getOrDefault(currency, minThresholds.get("USD"));
        }
    }

    @Getter
    @Setter
    public static class Defaults {
        private Map<String, BigDecimal> thresholdAmount = new HashMap<>();
        private boolean thresholdEnabled = true;

        public BigDecimal getThresholdAmount(String currency) {
            return thresholdAmount.getOrDefault(currency, thresholdAmount.get("USD"));
        }
    }

    @Getter
    @Setter
    public static class Exchange {
        private Map<String, Map<String, BigDecimal>> rates = new HashMap<>();

        public BigDecimal convert(BigDecimal amount, String from, String to) {
            if (from.equals(to)) return amount;
            Map<String, BigDecimal> fromRates = rates.get(from);
            if (fromRates == null || !fromRates.containsKey(to)) {
                throw new IllegalArgumentException("No exchange rate configured for " + from + " → " + to
                        + ". Add pinless.exchange.rates." + from + "." + to + " to application.yml");
            }
            return amount.multiply(fromRates.get(to));
        }

        public boolean hasRate(String from, String to) {
            if (from.equals(to)) return true;
            Map<String, BigDecimal> fromRates = rates.get(from);
            return fromRates != null && fromRates.containsKey(to);
        }
    }

    @Getter
    @Setter
    public static class Cache {
        private long ttlMinutes = 5;
        private String keyPrefix = "";

        public String resolvedPrefix() {
            if (!StringUtils.hasText(keyPrefix)) return "";
            return keyPrefix.endsWith(":") ? keyPrefix : keyPrefix + ":";
        }
    }
}
