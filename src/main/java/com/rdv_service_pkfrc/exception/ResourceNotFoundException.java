package com.rdv_service_pkfrc.exception;



public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, String ref) {
        return new ResourceNotFoundException(
                resource + " introuvable avec la référence : " + ref
        );
    }
}
