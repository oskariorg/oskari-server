package org.oskari.user.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class UserHelperTest {
    @Test
    public void isValidEmail() {
        List<String> goodEmails = Arrays.asList(
            "username@domain.com",
            "user.name@domain.com",
            "user-name@domain.com",
            "user-name@domain-test.com",
            "username@domain.co.in",
            "user_name@domain.com");
        goodEmails.forEach(email -> assertTrue(email, UserHelper.isValidEmail(email)));
    }
    @Test
    public void isNotValidEmail() {
        List<String> badEmails = Arrays.asList(
                "username.@domain.com",
                ".user.name@domain.com",
                "user-name@domain.com.",
                "username@.com");
        badEmails.forEach(email -> assertFalse(email, UserHelper.isValidEmail(email)));
    }
}