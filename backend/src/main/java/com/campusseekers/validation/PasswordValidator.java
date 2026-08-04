package com.campusseekers.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        boolean hasMinLength = password.length() >= 8;
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);

        return hasMinLength && hasDigit && hasUppercase && hasLowercase;
    }
}
