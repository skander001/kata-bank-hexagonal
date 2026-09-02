/**
 * Adapter primaire Kafka : les consumers.
 *
 * <p>Un {@code @KafkaListener} joue exactement le meme role qu'un controleur REST :
 * il traduit un message en appel de port entrant. Le domaine ne sait pas d'ou vient
 * la commande.
 */
package fr.shelali.tech.consulting.bank.adapter.in.kafka;
