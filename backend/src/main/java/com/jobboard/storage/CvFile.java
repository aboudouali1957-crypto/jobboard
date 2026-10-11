package com.jobboard.storage;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/**
 * CV prêt à être renvoyé au client, avec son nom de fichier d'origine.
 */
public record CvFile(Resource resource, String originalName) {

    public ResponseEntity<Resource> toResponse() {
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(originalName != null ? originalName : "cv.pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
