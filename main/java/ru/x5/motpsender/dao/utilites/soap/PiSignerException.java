package ru.x5.motpsender.dao.utilites.soap;


import ru.x5.motpsender.dao.utilites.SignerException;

public class PiSignerException extends SignerException {
    public PiSignerException(String message) {
        super(message);
    }

    public PiSignerException(String message, Throwable cause) {
        super(message, cause);
    }
}
