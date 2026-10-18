package com.digitaldownpour.atouchofclass;

import java.util.Objects;

/** Checked, non-null test preconditions for entities and recipe values. */
public final class GameTestChecks {
    private GameTestChecks() {}

    public static <T> T requireType(@org.jetbrains.annotations.Nullable Object value, Class<T> type) {
        return Objects.requireNonNull(type.cast(value), "Expected a non-null " + type.getSimpleName());
    }
}
