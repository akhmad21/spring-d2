package com.example.demo.service;

import com.example.demo.util.CharCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharCounterTest {

    @Test
    void testCharCounterCounting() {
        CharCounter counter = new CharCounter("Hello World");
        assertEquals(3, counter.getJumlah('l'));
        assertEquals(2, counter.getJumlah('o'));
        assertEquals(1, counter.getJumlah('h'));
        assertEquals(1, counter.getJumlah('e'));
        assertEquals(1, counter.getJumlah('w'));
        assertEquals(1, counter.getJumlah('r'));
        assertEquals(1, counter.getJumlah('d'));
        assertEquals(0, counter.getJumlah('z'));
    }

    @Test
    void testCharCounterWithEmptyAndNull() {
        CharCounter counterNull = new CharCounter(null);
        assertEquals(0, counterNull.getJumlah('a'));

        CharCounter counterEmpty = new CharCounter("");
        assertEquals(0, counterEmpty.getJumlah('a'));
    }
}

