package com.rdv_service_pkfrc.exception;

public class CapaciteRdvException extends BusinessException {

    public CapaciteRdvException() {
        super("Un RDV ne peut accueillir que 2 personnes maximum.");
    }
}