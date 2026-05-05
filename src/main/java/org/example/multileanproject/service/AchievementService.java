package org.example.multileanproject.service;

import org.example.multileanproject.dto.BadgeDTO;

import java.util.List;

public interface AchievementService {
    List<BadgeDTO> getBadges(String studentEmail);
    int getCurrentStreak(String studentEmail);
}
