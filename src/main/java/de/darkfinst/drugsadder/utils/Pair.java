package de.darkfinst.drugsadder.utils;

import lombok.Getter;

/**
 * A pair of two values
 *
 * @param <F> The first value
 * @param <S> The second value
 */
@Getter
public class Pair<F, S> {

    public final F first;
    public final S second;

    protected Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new pair
     *
     * @param first  The first value
     * @param second The second value
     * @return The new pair
     */
    public static <F, S> Pair<F, S> of(F first, S second) {
        if (first != null && second != null) {
            return new Pair<>(first, second);
        } else {
            throw new IllegalArgumentException("Pair.of requires non null values.");
        }
    }

    /**
     * Checks if the pair is equal to another object
     *
     * @param other The other object
     * @return true, if the pair is equal to the other object otherwise false
     */
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof Pair<?, ?> rhs)) {
            return false;
        } else {
            return this.first.equals(rhs.first) && this.second.equals(rhs.second);
        }
    }

    /**
     * @return The hash code of the pair
     */
    public int hashCode() {
        return this.first.hashCode() * 37 + this.second.hashCode();
    }

}