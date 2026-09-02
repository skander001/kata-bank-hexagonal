package fr.shelali.tech.consulting.bank.domain.exception;

/**
 * Leve lorsqu'un montant fourni a une operation (depot, retrait, ...) est invalide :
 * nul, negatif, nul en valeur (zero) ou d'une echelle non supportee.
 */
public class InvalidAmountException extends RuntimeException {

    private final String reason;

    public InvalidAmountException(String reason) {
        super(reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
