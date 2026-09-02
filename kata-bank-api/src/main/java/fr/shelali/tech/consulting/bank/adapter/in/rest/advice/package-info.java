/**
 * Traduction des exceptions en reponses HTTP.
 *
 * <p>C'est le seul endroit qui connait a la fois les exceptions metier
 * ({@code fr.shelali.tech.consulting.bank.domain.exception}) et les codes HTTP :
 * le domaine ignore qu'il existe un statut 409.
 */
package fr.shelali.tech.consulting.bank.adapter.in.rest.advice;
