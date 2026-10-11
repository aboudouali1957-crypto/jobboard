package com.jobboard.storage;

import com.jobboard.common.exception.BusinessRuleException;
import com.jobboard.common.exception.NotFoundException;
import com.jobboard.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/**
 * Stockage des CV sur le système de fichiers local.
 * Les fichiers sont renommés avec un UUID : le nom d'origine n'est jamais utilisé comme chemin.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final Path root;

    public FileStorageService(AppProperties properties) throws IOException {
        this.root = Path.of(properties.storage().cvDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Stockage des CV dans {}", root);
    }

    /**
     * Enregistre un PDF envoyé par un utilisateur après avoir vérifié sa signature binaire.
     *
     * @return la clé de stockage du fichier
     */
    public String storePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Le fichier est vide");
        }
        if (!hasPdfSignature(file)) {
            throw new BusinessRuleException("Seuls les fichiers PDF sont acceptés");
        }
        String key = newKey();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, resolve(key), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'enregistrer le fichier", e);
        }
        return key;
    }

    /**
     * Enregistre un contenu PDF généré côté serveur (jeu de données de démonstration).
     */
    public String storePdf(byte[] content) {
        String key = newKey();
        try {
            Files.write(resolve(key), content);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'enregistrer le fichier", e);
        }
        return key;
    }

    public Resource load(String key) {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("Fichier introuvable");
        }
        return new FileSystemResource(path);
    }

    public void delete(String key) {
        if (key == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier {} : {}", key, e.getMessage());
        }
    }

    private static boolean hasPdfSignature(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return Arrays.equals(in.readNBytes(PDF_SIGNATURE.length), PDF_SIGNATURE);
        } catch (IOException e) {
            return false;
        }
    }

    private static String newKey() {
        return UUID.randomUUID() + ".pdf";
    }

    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Clé de fichier invalide");
        }
        return path;
    }
}
