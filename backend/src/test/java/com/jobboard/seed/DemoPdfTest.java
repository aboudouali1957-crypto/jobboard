package com.jobboard.seed;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPdfTest {

    @Test
    void generates_a_structurally_valid_pdf() {
        String pdf = new String(DemoPdf.resume("Inès Leroy", "Développeuse (front-end)", "Nantes"), StandardCharsets.US_ASCII);

        assertThat(pdf).startsWith("%PDF-1.4").endsWith("%%EOF\n");

        // startxref doit pointer exactement sur la table de références croisées
        String afterStartXref = pdf.substring(pdf.lastIndexOf("startxref\n") + "startxref\n".length());
        int xrefOffset = Integer.parseInt(afterStartXref.substring(0, afterStartXref.indexOf('\n')));
        assertThat(pdf.substring(xrefOffset)).startsWith("xref\n0 6\n");
    }

    @Test
    void strips_accents_and_escapes_parentheses() {
        String pdf = new String(DemoPdf.resume("Inès Leroy", "Développeuse (front-end)", "Nantes"), StandardCharsets.US_ASCII);

        assertThat(pdf).contains("(Ines Leroy) Tj").contains("(Developpeuse \\(front-end\\)) Tj");
    }
}
