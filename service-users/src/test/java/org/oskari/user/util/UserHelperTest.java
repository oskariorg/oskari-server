package org.oskari.user.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
        goodEmails.forEach(email -> Assertions.assertTrue(UserHelper.isValidEmail(email), email));
    }
    @Test
    public void isNotValidEmail() {
        List<String> badEmails = Arrays.asList(
                "username.@domain.com",
                ".user.name@domain.com",
                "user-name@domain.com.",
                "username@.com");
        badEmails.forEach(email -> Assertions.assertFalse(UserHelper.isValidEmail(email), email));
    }
}