package com.rdv_service_pkfrc.exception;


public class DelaiRdvException extends BusinessException {

    public DelaiRdvException() {
        super("Un RDV doit être pris au moins 2 jours avant sa date.");
    }
}
