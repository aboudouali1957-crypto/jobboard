package com.jobboard.admin;

import com.jobboard.admin.dto.AdminUserDto;
import com.jobboard.common.PageResponse;
import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.user.Role;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository users;

    public AdminUserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserDto> search(Role role, String search, Pageable pageable) {
        String term = StringUtils.hasText(search) ? search.trim() : null;
        return PageResponse.of(users.search(role, term, pageable), AdminUserDto::from);
    }

    public AdminUserDto setEnabled(Long adminId, Long userId, boolean enabled) {
        if (adminId.equals(userId) && !enabled) {
            throw new BusinessRuleException("Vous ne pouvez pas désactiver votre propre compte");
        }
        User user = users.findById(userId).orElseThrow(() -> NotFoundException.of("Utilisateur", userId));
        user.setEnabled(enabled);
        return AdminUserDto.from(user);
    }
}
