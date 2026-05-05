package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BadgeDTO;
import org.example.multileanproject.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /** GET /api/student/achievements */
    @GetMapping("/achievements")
    public ResponseEntity<?> getAchievements(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        return ResponseEntity.ok(Map.of(
                "badges", achievementService.getBadges(email),
                "currentStreak", achievementService.getCurrentStreak(email)
        ));
    }
}
