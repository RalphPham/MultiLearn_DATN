package org.example.multileanproject.service;


import org.example.multileanproject.entity.Section;

import java.util.List;

public interface SectionService {

    Section createSection(Long courseId, Section section);

    Section updateSection(Long sectionId, Section section);

    void deleteSection(Long sectionId);

    List<Section> getSectionsByCourse(Long courseId);

    Section getSectionDetail(Long sectionId);

    Integer countSectionsByCourse(Long courseId);
}

