package org.example.multileanproject.service;

import org.example.multileanproject.dto.MarketInsightRequest;
import org.example.multileanproject.dto.MarketInsightResponse;

public interface MarketInsightService {
    MarketInsightResponse analyze(MarketInsightRequest request);
}
