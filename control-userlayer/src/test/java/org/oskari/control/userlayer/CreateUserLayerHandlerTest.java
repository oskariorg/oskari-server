package org.oskari.control.userlayer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserLayerHandlerTest {

    @Test
    void isFileIgnored() {
        Assertions.assertFalse(CreateUserLayerHandler.isFileIgnored("test.mif"));
        Assertions.assertFalse(CreateUserLayerHandler.isFileIgnored("some/test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored(".test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored(".some/test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored("some/.test.mif"));
    }
}