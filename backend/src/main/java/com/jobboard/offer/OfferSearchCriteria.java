package com.jobboard.offer;

/**
 * Filtres optionnels de la recherche d'offres. Un critère null est ignoré.
 *
 * @param keyword      mot-clé recherché dans le titre, la description et le nom de l'entreprise
 * @param city         ville exacte (insensible à la casse)
 * @param contractType type de contrat
 * @param minSalary    salaire annuel brut minimum souhaité (en euros)
 * @param skillId      compétence demandée
 * @param remote       si true, uniquement les offres compatibles télétravail
 */
public record OfferSearchCriteria(
        String keyword,
        String city,
        ContractType contractType,
        Integer minSalary,
        Long skillId,
        Boolean remote) {
}
