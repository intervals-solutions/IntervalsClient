package com.intervals.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalsTest {

    private final Intervals intervals = new Intervals();

    @Test
    void testAdd() {
        assertEquals(11, intervals.add(3, 8));
    }
}
