package com.github.littleemptydoll.exoequipment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Compact display values for tooltips, panels and HUDs. Never use for stored energy or calculations. */
public final class NumberFormatter {
    private static final String[] SUFFIXES = {"", "k", "m", "b", "t"};

    private NumberFormatter() {}

    public static String format(double value) {
        if (!Double.isFinite(value)) return Double.toString(value);

        double magnitude = Math.abs(value);
        int tier = 0;
        while (magnitude >= 1_000.0D && tier < SUFFIXES.length - 1) {
            magnitude /= 1_000.0D;
            tier++;
        }

        BigDecimal rounded = BigDecimal.valueOf(value)
                .movePointLeft(tier * 3)
                .setScale(2, RoundingMode.HALF_UP);
        if (rounded.abs().compareTo(BigDecimal.valueOf(1_000)) >= 0
                && tier < SUFFIXES.length - 1) {
            rounded = rounded.movePointLeft(3).setScale(2, RoundingMode.HALF_UP);
            tier++;
        }
        if (rounded.signum() == 0) return "0";
        return rounded.stripTrailingZeros().toPlainString() + SUFFIXES[tier];
    }

    public static String signed(double value) {
        return (value > 0 ? "+" : "") + format(value);
    }

    public static String percent(double fraction) {
        return format(fraction * 100.0D) + "%";
    }
}
