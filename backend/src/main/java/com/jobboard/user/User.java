package com.jobboard.user;

import com.jobboard.common.BaseEntity;
import com.jobboard.company.Company;
import com.jobboard.skill.Skill;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilisateur de la plateforme. Les champs de profil (téléphone, CV, compétences…) concernent les candidats,
 * l'entreprise concerne les recruteurs.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean enabled = true;

    private String phone;

    private String city;

    private String headline;

    private String bio;

    @Column(name = "cv_original_name")
    private String cvOriginalName;

    @Column(name = "cv_storage_key")
    private String cvStorageKey;

    @Column(name = "cv_uploaded_at")
    private Instant cvUploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany
    @JoinTable(name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    protected User() {
        // requis par JPA
    }

    public User(String email, String passwordHash, Role role, String firstName, String lastName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean hasCv() {
        return cvStorageKey != null;
    }

    public void attachCv(String storageKey, String originalName) {
        this.cvStorageKey = storageKey;
        this.cvOriginalName = originalName;
        this.cvUploadedAt = Instant.now();
    }

    public void replaceSkills(Set<Skill> newSkills) {
        skills.clear();
        skills.addAll(newSkills);
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCvOriginalName() {
        return cvOriginalName;
    }

    public String getCvStorageKey() {
        return cvStorageKey;
    }

    public Instant getCvUploadedAt() {
        return cvUploadedAt;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Skill> getSkills() {
        return skills;
    }
}
