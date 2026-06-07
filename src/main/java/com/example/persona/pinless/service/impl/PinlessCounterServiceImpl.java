package com.example.persona.pinless.service.impl;

import com.example.persona.pinless.config.PinlessSystemProperties;
import com.example.persona.pinless.model.dto.PinlessConfigData;
import com.example.persona.pinless.model.enums.BreachReason;
import com.example.persona.pinless.service.PinlessCounterService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PinlessCounterServiceImpl implements PinlessCounterService {

    private static final DateTimeFormatter DAILY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTHLY = DateTimeFormatter.ofPattern("yyyyMM");
    private static final BigDecimal CENTS = BigDecimal.valueOf(100);
    private static final int SCALE = 2;
    private static final long TTL_DAY = 48L * 3600;
    private static final long TTL_MON = 62L * 24 * 3600;

    // ── Lua script ────────────────────────────────────────────────────────────
    //
    // KEYS : [dCountKey, mCountKey, dAmountKey, mAmountKey]
    // ARGV : [amountCents,
    //         dCntOn(0/1), dCntLim,
    //         mCntOn(0/1), mCntLim,
    //         dAmtOn(0/1), dAmtLim,
    //         mAmtOn(0/1), mAmtLim,
    //         ttlDay, ttlMon]
    //
    private static final String ATOMIC_SCRIPT = """
        local dCount = tonumber(redis.call('GET', KEYS[1]) or '0') or 0
        local mCount = tonumber(redis.call('GET', KEYS[2]) or '0') or 0
        local dAmt   = tonumber(redis.call('GET', KEYS[3]) or '0') or 0
        local mAmt   = tonumber(redis.call('GET', KEYS[4]) or '0') or 0

        local amount = tonumber(ARGV[1])

        local dCntOn = tonumber(ARGV[2]);  local dCntLim = tonumber(ARGV[3])
        local mCntOn = tonumber(ARGV[4]);  local mCntLim = tonumber(ARGV[5])
        local dAmtOn = tonumber(ARGV[6]);  local dAmtLim = tonumber(ARGV[7])
        local mAmtOn = tonumber(ARGV[8]);  local mAmtLim = tonumber(ARGV[9])
        local ttlDay = tonumber(ARGV[10])
        local ttlMon = tonumber(ARGV[11])

        if dCntOn == 1 and dCount >= dCntLim         then return 'DAILY_COUNT_EXCEEDED'   end
        if mCntOn == 1 and mCount >= mCntLim         then return 'MONTHLY_COUNT_EXCEEDED' end
        if dAmtOn == 1 and (dAmt + amount) > dAmtLim then return 'DAILY_AMOUNT_EXCEEDED'  end
        if mAmtOn == 1 and (mAmt + amount) > mAmtLim then return 'MONTHLY_AMOUNT_EXCEEDED' end

        if dCntOn == 1 then
            redis.call('INCRBY', KEYS[1], 1);     redis.call('EXPIRE', KEYS[1], ttlDay)
        end
        if mCntOn == 1 then
            redis.call('INCRBY', KEYS[2], 1);     redis.call('EXPIRE', KEYS[2], ttlMon)
        end
        if dAmtOn == 1 then
            redis.call('INCRBY', KEYS[3], amount); redis.call('EXPIRE', KEYS[3], ttlDay)
        end
        if mAmtOn == 1 then
            redis.call('INCRBY', KEYS[4], amount); redis.call('EXPIRE', KEYS[4], ttlMon)
        end

        return 'OK'
        """;

    private static final DefaultRedisScript<String> REDIS_SCRIPT =
            new DefaultRedisScript<>(ATOMIC_SCRIPT, String.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplate;

    private final PinlessSystemProperties systemProperties;

    public PinlessCounterServiceImpl(
            ObjectProvider<StringRedisTemplate> redisTemplate, PinlessSystemProperties systemProperties) {
        this.redisTemplate = redisTemplate;
        this.systemProperties = systemProperties;
    }

    // ── Atomic check + increment ──────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "pinlessRedis", fallbackMethod = "checkAndIncrementFallback")
    public BreachReason checkAndIncrement(String customerNo, BigDecimal amount, PinlessConfigData config) {
        if (customerNo == null || amount == null || config == null) {
            log.warn("[PINLESS] checkAndIncrement called with null args: customer={}", customerNo);
            return BreachReason.PINLESS_DISABLED;
        }

        if (!config.isDailyCountEnabled()
                && !config.isMonthlyCountEnabled()
                && !config.isDailyAmountEnabled()
                && !config.isMonthlyAmountEnabled()) {
            log.debug("[PINLESS] All counters disabled, skipping Redis: customer={}", customerNo);
            return null;
        }

        String currency = config.getCurrency() != null ? config.getCurrency() : "USD";
        long amountCents = toCents(amount);

        List<String> keys = List.of(
                dailyCountKey(customerNo, currency),
                monthlyCountKey(customerNo, currency),
                dailyAmountKey(customerNo, currency),
                monthlyAmountKey(customerNo, currency));

        List<String> args = List.of(
                String.valueOf(amountCents),
                flag(config.isDailyCountEnabled()),
                limitArg(config.getDailyCountLimit()),
                flag(config.isMonthlyCountEnabled()),
                limitArg(config.getMonthlyCountLimit()),
                flag(config.isDailyAmountEnabled()),
                amountArg(config.getDailyAmountLimit()),
                flag(config.isMonthlyAmountEnabled()),
                amountArg(config.getMonthlyAmountLimit()),
                String.valueOf(TTL_DAY),
                String.valueOf(TTL_MON));

        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) {
            log.warn("[PINLESS] Redis not configured — pinless counters unavailable: customer={}", customerNo);
            return BreachReason.PINLESS_DISABLED;
        }

        String result = template.execute(REDIS_SCRIPT, keys, args.toArray(String[]::new));

        if ("OK".equals(result)) {
            log.debug("[PINLESS] Counters OK: customer={} currency={} cents={}", customerNo, currency, amountCents);
            return null;
        }

        BreachReason reason = BreachReason.valueOf(result);
        log.debug("[PINLESS] Breach: customer={} currency={} reason={}", customerNo, currency, reason);
        return reason;
    }

    public BreachReason checkAndIncrementFallback(
            String customerNo, BigDecimal amount, PinlessConfigData config, Throwable ex) {
        log.error("[PINLESS] Circuit open for customer={}: {}", customerNo, ex.getMessage());
        return BreachReason.PINLESS_DISABLED;
    }

    @Override
    public void warmUp(
            String customerNo,
            String currency,
            long dailyCount,
            long monthlyCount,
            BigDecimal dailyAmount,
            BigDecimal monthlyAmount) {
        if (customerNo == null) return;
        String cur = currency != null ? currency : "USD";

        redisTemplate.ifAvailable(template -> {
            try {
                if (dailyCount > 0)
                    setIfAbsent(template, dailyCountKey(customerNo, cur), String.valueOf(dailyCount), TTL_DAY);
                if (monthlyCount > 0)
                    setIfAbsent(template, monthlyCountKey(customerNo, cur), String.valueOf(monthlyCount), TTL_MON);
                if (dailyAmount != null && dailyAmount.compareTo(BigDecimal.ZERO) > 0)
                    setIfAbsent(
                            template, dailyAmountKey(customerNo, cur), String.valueOf(toCents(dailyAmount)), TTL_DAY);
                if (monthlyAmount != null && monthlyAmount.compareTo(BigDecimal.ZERO) > 0)
                    setIfAbsent(
                            template,
                            monthlyAmountKey(customerNo, cur),
                            String.valueOf(toCents(monthlyAmount)),
                            TTL_MON);
                log.debug("[PINLESS] Warmed: customer={} currency={}", customerNo, cur);
            } catch (Exception ex) {
                log.warn("[PINLESS] Warmup failed for customer={} currency={}: {}", customerNo, cur, ex.getMessage());
            }
        });
        if (redisTemplate.getIfAvailable() == null) {
            log.debug("[PINLESS] Redis not configured — warmup skipped: customer={}", customerNo);
        }
    }

    // ── Key builders ──────────────────────────────────────────────────────────

    private String dailyCountKey(String c, String currency) {
        return prefix() + "pinless:count:daily:" + c + ":" + currency + ":"
                + LocalDate.now().format(DAILY);
    }

    private String monthlyCountKey(String c, String currency) {
        return prefix() + "pinless:count:monthly:" + c + ":" + currency + ":"
                + LocalDate.now().format(MONTHLY);
    }

    private String dailyAmountKey(String c, String currency) {
        return prefix() + "pinless:amount:daily:" + c + ":" + currency + ":"
                + LocalDate.now().format(DAILY);
    }

    private String monthlyAmountKey(String c, String currency) {
        return prefix() + "pinless:amount:monthly:" + c + ":" + currency + ":"
                + LocalDate.now().format(MONTHLY);
    }

    private String prefix() {
        return systemProperties.getCache().resolvedPrefix();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String flag(boolean enabled) {
        return enabled ? "1" : "0";
    }

    private String limitArg(Integer limit) {
        return limit != null ? String.valueOf(limit) : "0";
    }

    private String amountArg(BigDecimal limit) {
        return limit != null ? String.valueOf(toCents(limit)) : "0";
    }

    private long toCents(BigDecimal amount) {
        if (amount == null) return 0L;
        return amount.setScale(SCALE, RoundingMode.HALF_UP).multiply(CENTS).longValue();
    }

    private void setIfAbsent(StringRedisTemplate template, String key, String value, long ttlSeconds) {
        template.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
    }
}
