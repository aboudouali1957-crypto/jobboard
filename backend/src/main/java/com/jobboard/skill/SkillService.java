package com.jobboard.skill;

import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skills;

    public SkillService(SkillRepository skills) {
        this.skills = skills;
    }

    @Transactional(readOnly = true)
    public List<SkillDto> findAll() {
        return skills.findAllByOrderByNameAsc().stream().map(SkillDto::from).toList();
    }

    public SkillDto create(String name) {
        String trimmed = name.trim();
        if (skills.existsByNameIgnoreCase(trimmed)) {
            throw new ConflictException("La compétence « " + trimmed + " » existe déjà");
        }
        return SkillDto.from(skills.save(new Skill(trimmed)));
    }

    /**
     * Charge les compétences demandées et vérifie qu'elles existent toutes.
     */
    @Transactional(readOnly = true)
    public Set<Skill> resolve(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Long> uniqueIds = new HashSet<>(ids);
        Set<Skill> found = new HashSet<>(skills.findAllById(uniqueIds));
        if (found.size() != uniqueIds.size()) {
            throw new BusinessRuleException("Une ou plusieurs compétences sont inconnues");
        }
        return found;
    }
}
