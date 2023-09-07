package de.darkfinst.drugsadder.utils;

import lombok.Getter;

@Getter
public class Pair<F, S> {
    public final F first;

    public final S second;

    protected Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        if (first != null && second != null) {
            return new Pair<>(first, second);
        } else {
            throw new IllegalArgumentException("Pair.of requires non null values.");
        }
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (!(other instanceof Pair<?, ?> rhs)) {
            return false;
        } else {
            return this.first.equals(rhs.first) && this.second.equals(rhs.second);
        }
    }

    public int hashCode() {
        return this.first.hashCode() * 37 + this.second.hashCode();
    }

}