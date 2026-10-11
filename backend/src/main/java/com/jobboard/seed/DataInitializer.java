package com.jobboard.seed;

import com.jobboard.application.ApplicationStatus;
import com.jobboard.application.JobApplication;
import com.jobboard.application.JobApplicationRepository;
import com.jobboard.company.Company;
import com.jobboard.company.CompanyRepository;
import com.jobboard.config.AppProperties;
import com.jobboard.offer.ContractType;
import com.jobboard.offer.Offer;
import com.jobboard.offer.OfferRepository;
import com.jobboard.offer.OfferStatus;
import com.jobboard.skill.Skill;
import com.jobboard.skill.SkillRepository;
import com.jobboard.storage.FileStorageService;
import com.jobboard.user.Role;
import com.jobboard.user.User;
import com.jobboard.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Au démarrage : garantit l'existence du compte administrateur et, si activé
 * ({@code app.seed.demo-data=true}) et que la base est vide, insère un jeu de données de démonstration.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    static final String DEMO_PASSWORD = "Password123!";

    private final AppProperties properties;
    private final UserRepository users;
    private final CompanyRepository companies;
    private final OfferRepository offers;
    private final JobApplicationRepository applications;
    private final SkillRepository skills;
    private final FileStorageService storage;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppProperties properties, UserRepository users, CompanyRepository companies,
                           OfferRepository offers, JobApplicationRepository applications, SkillRepository skills,
                           FileStorageService storage, PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.companies = companies;
        this.offers = offers;
        this.applications = applications;
        this.skills = skills;
        this.storage = storage;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureAdmin();
        if (properties.seed().demoData() && users.countByRole(Role.RECRUITER) == 0) {
            seedDemoData();
        }
    }

    private void ensureAdmin() {
        String email = properties.admin().email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            return;
        }
        users.save(new User(email, passwordEncoder.encode(properties.admin().password()), Role.ADMIN, "Admin", "JobBoard"));
        log.info("Compte administrateur créé : {}", email);
    }

    // ------------------------------------------------------------------ Jeu de démonstration

    private record CompanySeed(String name, String city, String website, String description) {
    }

    private record OfferSeed(int company, String title, String city, ContractType contract, Integer salaryMin,
                             Integer salaryMax, boolean remote, int daysAgo, boolean closed, List<String> skills) {
    }

    private void seedDemoData() {
        Instant now = Instant.now();
        Random random = new Random(42);
        String hash = passwordEncoder.encode(DEMO_PASSWORD);

        List<CompanySeed> companySeeds = List.of(
                new CompanySeed("Nimbus Tech", "Paris", "https://nimbus-tech.example",
                        "Éditeur SaaS de solutions RH, 120 collaborateurs, produit utilisé par plus de 900 entreprises."),
                new CompanySeed("Atlas Conseil", "Lyon", "https://atlas-conseil.example",
                        "ESN à taille humaine qui accompagne les grands comptes de la région lyonnaise."),
                new CompanySeed("Océane Digital", "Nantes", "https://oceane-digital.example",
                        "Agence produit qui conçoit des applications web et mobiles pour des start-up."));

        List<Company> savedCompanies = new ArrayList<>();
        for (CompanySeed seed : companySeeds) {
            Company company = new Company(seed.name(), seed.city());
            company.setWebsite(seed.website());
            company.setDescription(seed.description());
            company.setCreatedAt(now.minus(Duration.ofDays(200)));
            savedCompanies.add(companies.save(company));
        }

        List<User> recruiters = List.of(
                recruiter("recruteur@jobboard.dev", "Sophie", "Bernard", savedCompanies.get(0), hash),
                recruiter("recruteur2@jobboard.dev", "Julien", "Moreau", savedCompanies.get(1), hash),
                recruiter("recruteur3@jobboard.dev", "Claire", "Petit", savedCompanies.get(2), hash));

        List<User> candidates = List.of(
                candidate("candidat@jobboard.dev", "Camille", "Martin", "Lyon",
                        "Développeuse full stack Java / Angular", hash,
                        List.of("Java", "Spring Boot", "Angular", "TypeScript", "MySQL", "Git")),
                candidate("candidat2@jobboard.dev", "Thomas", "Dubois", "Paris",
                        "Développeur back-end Java", hash,
                        List.of("Java", "Spring Boot", "Hibernate / JPA", "PostgreSQL", "Docker", "Kafka")),
                candidate("candidat3@jobboard.dev", "Inès", "Leroy", "Nantes",
                        "Développeuse front-end Angular", hash,
                        List.of("Angular", "TypeScript", "HTML / CSS", "UX / UI Design", "Tests unitaires")),
                candidate("candidat4@jobboard.dev", "Lucas", "Garcia", "Bordeaux",
                        "Étudiant en Master informatique, recherche une alternance", hash,
                        List.of("Java", "Python", "SQL", "Git", "Agile / Scrum")));

        List<OfferSeed> offerSeeds = List.of(
                new OfferSeed(0, "Développeur Full Stack Java / Angular", "Paris", ContractType.CDI, 45000, 55000, true, 170, false,
                        List.of("Java", "Spring Boot", "Angular", "TypeScript", "MySQL")),
                new OfferSeed(0, "Lead Developer Java", "Paris", ContractType.CDI, 60000, 72000, true, 150, false,
                        List.of("Java", "Spring Boot", "Microservices", "Kafka", "Kubernetes")),
                new OfferSeed(0, "Développeur Front-end Angular", "Paris", ContractType.CDI, 42000, 50000, false, 120, false,
                        List.of("Angular", "TypeScript", "HTML / CSS", "Tests unitaires")),
                new OfferSeed(0, "Alternance - Développeur Java", "Paris", ContractType.ALTERNANCE, 14000, 18000, false, 60, false,
                        List.of("Java", "SQL", "Git")),
                new OfferSeed(0, "Ingénieur DevOps", "Paris", ContractType.CDI, 55000, 65000, true, 160, true,
                        List.of("Docker", "Kubernetes", "AWS", "CI/CD", "Linux")),
                new OfferSeed(1, "Consultant Développeur Java Spring", "Lyon", ContractType.CDI, 40000, 48000, false, 140, false,
                        List.of("Java", "Spring Boot", "Spring Security", "REST API")),
                new OfferSeed(1, "Développeur Back-end Java - Mission banque", "Lyon", ContractType.FREELANCE, null, null, true, 100, false,
                        List.of("Java", "Spring Boot", "PostgreSQL", "Microservices")),
                new OfferSeed(1, "Data Analyst junior", "Lyon", ContractType.CDD, 32000, 36000, false, 80, false,
                        List.of("SQL", "Python", "Data Analysis")),
                new OfferSeed(1, "Stage - Développeur Full Stack", "Lyon", ContractType.STAGE, 7200, 7200, false, 45, false,
                        List.of("Java", "Angular", "Git")),
                new OfferSeed(1, "Chef de projet technique", "Lyon", ContractType.CDI, 50000, 58000, false, 130, true,
                        List.of("Gestion de projet", "Agile / Scrum", "Anglais courant")),
                new OfferSeed(2, "Développeur Angular confirmé", "Nantes", ContractType.CDI, 43000, 50000, true, 110, false,
                        List.of("Angular", "TypeScript", "JavaScript", "HTML / CSS")),
                new OfferSeed(2, "Développeur Node.js", "Nantes", ContractType.CDD, 38000, 44000, true, 70, false,
                        List.of("Node.js", "TypeScript", "MySQL", "Docker")),
                new OfferSeed(2, "UX / UI Designer", "Bordeaux", ContractType.FREELANCE, null, null, true, 30, false,
                        List.of("UX / UI Design", "HTML / CSS")),
                new OfferSeed(2, "Développeur Full Stack React / Java", "Toulouse", ContractType.CDI, 44000, 52000, false, 20, false,
                        List.of("React", "Java", "Spring Boot", "MySQL")),
                new OfferSeed(2, "Ingénieur Cloud Azure", "Lille", ContractType.CDI, 52000, 62000, true, 10, false,
                        List.of("Azure", "Docker", "CI/CD", "Linux")));

        List<Offer> savedOffers = new ArrayList<>();
        for (OfferSeed seed : offerSeeds) {
            Offer offer = new Offer(recruiters.get(seed.company()), savedCompanies.get(seed.company()));
            offer.setTitle(seed.title());
            offer.setDescription(description(seed, companySeeds.get(seed.company()).name()));
            offer.setCity(seed.city());
            offer.setContractType(seed.contract());
            offer.setSalaryMin(seed.salaryMin());
            offer.setSalaryMax(seed.salaryMax());
            offer.setRemote(seed.remote());
            offer.setStatus(seed.closed() ? OfferStatus.CLOSED : OfferStatus.OPEN);
            offer.replaceSkills(skillsNamed(seed.skills()));
            offer.setCreatedAt(now.minus(Duration.ofDays(seed.daysAgo())));
            savedOffers.add(offers.save(offer));
        }

        // Candidatures réparties sur plusieurs mois pour alimenter les graphiques
        int[][] plan = {
                {0, 0, 2, 5, 13},   // Camille
                {1, 0, 1, 4, 6},    // Thomas
                {2, 2, 10, 11},     // Inès
                {3, 3, 8, 7}        // Lucas
        };
        ApplicationStatus[] statuses = {
                ApplicationStatus.INTERVIEW, ApplicationStatus.RECEIVED, ApplicationStatus.ACCEPTED,
                ApplicationStatus.REJECTED, ApplicationStatus.INTERVIEW, ApplicationStatus.RECEIVED
        };
        int count = 0;
        for (int[] row : plan) {
            User candidate = candidates.get(row[0]);
            for (int i = 1; i < row.length; i++) {
                Offer offer = savedOffers.get(row[i]);
                OfferSeed seed = offerSeeds.get(row[i]);
                JobApplication application = new JobApplication(offer, candidate,
                        "Bonjour, votre offre correspond exactement au poste que je recherche. "
                                + "Je serais ravi(e) d'échanger avec vous sur ma candidature.");
                applyStatus(application, statuses[count % statuses.length]);
                int daysAfterPublication = 1 + random.nextInt(Math.max(1, seed.daysAgo() - 1));
                application.setCreatedAt(now.minus(Duration.ofDays(seed.daysAgo() - daysAfterPublication)));
                applications.save(application);
                count++;
            }
        }

        log.info("Jeu de démonstration créé : {} entreprises, {} offres, {} candidatures (mot de passe : {})",
                savedCompanies.size(), savedOffers.size(), count, DEMO_PASSWORD);
    }

    private User recruiter(String email, String firstName, String lastName, Company company, String hash) {
        User user = new User(email, hash, Role.RECRUITER, firstName, lastName);
        user.setCompany(company);
        user.setCreatedAt(Instant.now().minus(Duration.ofDays(190)));
        return users.save(user);
    }

    private User candidate(String email, String firstName, String lastName, String city, String headline,
                           String hash, List<String> skillNames) {
        User user = new User(email, hash, Role.CANDIDATE, firstName, lastName);
        user.setCity(city);
        user.setHeadline(headline);
        user.setPhone("06 12 34 56 78");
        user.setBio("Passionné(e) par le développement d'applications web robustes et bien testées.");
        user.replaceSkills(skillsNamed(skillNames));
        String key = storage.storePdf(DemoPdf.resume(firstName + " " + lastName, headline, city));
        user.attachCv(key, "CV-" + firstName + "-" + lastName + ".pdf");
        user.setCreatedAt(Instant.now().minus(Duration.ofDays(180)));
        return users.save(user);
    }

    private Set<Skill> skillsNamed(List<String> names) {
        Set<Skill> result = new HashSet<>();
        for (String name : names) {
            skills.findByNameIgnoreCase(name).ifPresent(result::add);
        }
        return result;
    }

    private static void applyStatus(JobApplication application, ApplicationStatus target) {
        switch (target) {
            case RECEIVED -> { /* statut initial */ }
            case INTERVIEW -> application.moveTo(ApplicationStatus.INTERVIEW,
                    "Seriez-vous disponible mardi prochain à 10h pour un entretien en visio ?");
            case ACCEPTED -> {
                application.moveTo(ApplicationStatus.INTERVIEW, null);
                application.moveTo(ApplicationStatus.ACCEPTED, "Bienvenue dans l'équipe ! Nous revenons vers vous pour la suite.");
            }
            case REJECTED -> application.moveTo(ApplicationStatus.REJECTED, null);
        }
    }

    private static String description(OfferSeed seed, String companyName) {
        return """
                %s recrute un(e) %s pour renforcer ses équipes à %s.

                Vos missions :
                - Concevoir et développer de nouvelles fonctionnalités avec l'équipe produit
                - Participer aux revues de code et à l'amélioration continue de la qualité
                - Contribuer aux choix techniques et à la documentation

                Votre profil :
                - Maîtrise de %s
                - Goût pour le travail en équipe et la qualité logicielle
                %s
                """.formatted(companyName, seed.title(), seed.city(), String.join(", ", seed.skills()),
                seed.remote() ? "\nTélétravail possible jusqu'à 3 jours par semaine." : "").strip();
    }
}
