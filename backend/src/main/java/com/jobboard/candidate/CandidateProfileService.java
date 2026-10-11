package com.jobboard.candidate;

import com.jobboard.candidate.dto.CandidateProfileDto;
import com.jobboard.candidate.dto.UpdateProfileRequest;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.skill.SkillService;
import com.jobboard.storage.CvFile;
import com.jobboard.storage.FileStorageService;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class CandidateProfileService {

    private static final int MAX_FILE_NAME_LENGTH = 255;

    private final UserRepository users;
    private final SkillService skillService;
    private final FileStorageService storage;

    public CandidateProfileService(UserRepository users, SkillService skillService, FileStorageService storage) {
        this.users = users;
        this.skillService = skillService;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public CandidateProfileDto getProfile(Long candidateId) {
        return CandidateProfileDto.from(candidate(candidateId));
    }

    public CandidateProfileDto updateProfile(Long candidateId, UpdateProfileRequest request) {
        User user = candidate(candidateId);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(blankToNull(request.phone()));
        user.setCity(blankToNull(request.city()));
        user.setHeadline(blankToNull(request.headline()));
        user.setBio(blankToNull(request.bio()));
        user.replaceSkills(skillService.resolve(request.skillIds()));
        return CandidateProfileDto.from(user);
    }

    public CandidateProfileDto uploadCv(Long candidateId, MultipartFile file) {
        User user = candidate(candidateId);
        String previousKey = user.getCvStorageKey();
        String newKey = storage.storePdf(file);
        user.attachCv(newKey, sanitizeFileName(file.getOriginalFilename()));
        storage.delete(previousKey);
        return CandidateProfileDto.from(user);
    }

    @Transactional(readOnly = true)
    public CvFile loadCv(Long candidateId) {
        User user = candidate(candidateId);
        if (!user.hasCv()) {
            throw new NotFoundException("Aucun CV n'a encore été déposé");
        }
        return new CvFile(storage.load(user.getCvStorageKey()), user.getCvOriginalName());
    }

    private User candidate(Long candidateId) {
        return users.findById(candidateId).orElseThrow(() -> NotFoundException.of("Utilisateur", candidateId));
    }

    private static String sanitizeFileName(String originalName) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(originalName == null ? "" : originalName));
        if (!StringUtils.hasText(name)) {
            return "cv.pdf";
        }
        return name.length() > MAX_FILE_NAME_LENGTH ? name.substring(name.length() - MAX_FILE_NAME_LENGTH) : name;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
