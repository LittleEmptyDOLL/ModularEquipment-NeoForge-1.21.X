package com.github.littleemptydoll.exoequipment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormatterTest {
    @Test
    void compactsThousandsMillionsAndNegativeValues() {
        assertEquals("950", NumberFormatter.format(950));
        assertEquals("1k", NumberFormatter.format(1_000));
        assertEquals("12.5k", NumberFormatter.format(12_500));
        assertEquals("2.45m", NumberFormatter.format(2_450_000));
        assertEquals("1.2b", NumberFormatter.format(1_200_000_000L));
        assertEquals("-2.45m", NumberFormatter.format(-2_450_000));
    }

    @Test
    void promotesRoundedBoundariesAndKeepsSmallFractions() {
        assertEquals("1m", NumberFormatter.format(999_999));
        assertEquals("0.05", NumberFormatter.format(0.05));
        assertEquals("+12.5%", NumberFormatter.signed(12.5) + "%");
        assertEquals("12.5%", NumberFormatter.percent(0.125));
    }
}
