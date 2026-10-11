package com.jobboard.skill;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record SkillDto(Long id, String name) {

    public static SkillDto from(Skill skill) {
        return new SkillDto(skill.getId(), skill.getName());
    }

    public static List<SkillDto> sorted(Collection<Skill> skills) {
        return skills.stream()
                .map(SkillDto::from)
                .sorted(Comparator.comparing(SkillDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static List<String> names(Collection<Skill> skills) {
        return skills.stream()
                .map(Skill::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
