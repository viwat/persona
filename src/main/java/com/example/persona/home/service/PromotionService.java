package com.example.persona.home.service;

import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.home.dto.AnnouncementResponse;
import com.example.persona.home.dto.PromotionResponse;
import com.example.persona.home.model.Announcement;
import com.example.persona.home.model.Promotional;
import com.example.persona.home.repository.AnnouncementRepository;
import com.example.persona.home.repository.PromotionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final AnnouncementRepository announcementRepository;
    private final PromotionalRepository promotionalRepository;
    private final CacheManager cacheManager;

    public List<AnnouncementResponse> getAnnouncement(CustomerBaseRequest request) {

        List<Announcement> allAnnouncements = Optional.ofNullable(cacheManager.getCache("active_announcements"))
                .map(cache -> cache.get(
                        "active_announcements",
                        () -> announcementRepository.findByEffectiveFromBeforeAndEffectiveToAfter(
                                java.time.LocalDateTime.now(), java.time.LocalDateTime.now())))
                .orElseGet(() -> announcementRepository.findByEffectiveFromBeforeAndEffectiveToAfter(
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()));
        if (allAnnouncements == null || allAnnouncements.isEmpty()) {
            return List.of();
        }
        return allAnnouncements.stream()
                .filter(announcement -> announcement.getTargetAudience() == null
                        || announcement.getTargetAudience().equals("{}")
                        || announcement.getTargetAudience().contains(request.getCustomerSegment()))
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    public List<PromotionResponse> getPromotion(CustomerBaseRequest request) {
        List<Promotional> allPromotions = Optional.ofNullable(cacheManager.getCache("active_promotions"))
                .map(cache -> cache.get(
                        "active_promotions",
                        () -> promotionalRepository.findByEffectiveFromBeforeAndEffectiveToAfter(
                                java.time.LocalDateTime.now(), java.time.LocalDateTime.now())))
                .orElseGet(() -> promotionalRepository.findByEffectiveFromBeforeAndEffectiveToAfter(
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()));
        if (allPromotions == null || allPromotions.isEmpty()) {
            return List.of();
        }
        return allPromotions.stream()
                .filter(promotion -> promotion.getTargetAudience() == null
                        || promotion.getTargetAudience().equals("{}")
                        || promotion.getTargetAudience().contains(request.getCustomerSegment()))
                .map(PromotionResponse::from)
                .collect(Collectors.toList());
    }
}
