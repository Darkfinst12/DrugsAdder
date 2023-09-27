package de.darkfinst.drugsadder.exceptions;

public class DamageToolException extends RuntimeException {

    /**
     * Constructs an {@code ShopModifyStorageException} with no
     * detail message.
     */
    public DamageToolException() {
        super();
    }

    /**
     * Constructs an {@code ShopModifyStorageException} with the
     * specified detail message.
     *
     * @param message The detail message.
     */
    public DamageToolException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method).  (A {@code null} value
     *                is permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public DamageToolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwable (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).
     *              <br>(A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public DamageToolException(Throwable cause) {
        super(cause);
    }


}
