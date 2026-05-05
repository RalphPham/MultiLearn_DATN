package org.example.multileanproject.service;

import org.example.multileanproject.dto.BundleDTO;
import org.example.multileanproject.entity.Bundle;
import java.util.List;

public interface BundleService {
    Bundle createBundle(BundleDTO request, String instructorEmail);
    List<Bundle> getMyBundles(String instructorEmail);
    BundleDTO getBundleDetail(Long id);
    void deleteBundle(Long bundleId);
}