package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShareItServerTest {
    @Test
    void testMain() {
        ShareItServer.main(new String[] {});
        assertTrue(true);
    }
}