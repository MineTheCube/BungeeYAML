package fr.onecraft.config.exception;

public abstract class ConfigurationException extends Exception {

    protected ConfigurationException() {}

    protected ConfigurationException(String msg) {
        super(msg);
    }

    protected ConfigurationException(Throwable cause) {
        super(cause);
    }

    protected ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
