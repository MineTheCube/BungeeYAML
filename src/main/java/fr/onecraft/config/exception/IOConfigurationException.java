package fr.onecraft.config.exception;

import fr.onecraft.config.FileConfiguration;

/**
 * Exception thrown when attempting to load or save a {@link FileConfiguration}
 */
public class IOConfigurationException extends ConfigurationException {

    /**
     * Creates a new instance of IOConfigurationException without a
     * message or cause.
     */
    public IOConfigurationException() {}

    /**
     * Constructs an instance of IOConfigurationException with the
     * specified message.
     *
     * @param msg The details of the exception.
     */
    public IOConfigurationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of IOConfigurationException with the
     * specified cause.
     *
     * @param cause The cause of the exception.
     */
    public IOConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of IOConfigurationException with the
     * specified message and cause.
     *
     * @param cause The cause of the exception.
     * @param msg   The details of the exception.
     */
    public IOConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
