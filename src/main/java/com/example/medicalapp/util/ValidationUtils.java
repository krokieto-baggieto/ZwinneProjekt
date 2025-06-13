package com.example.medicalapp.util;

import java.util.regex.Pattern;
import com.example.medicalapp.model.User;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{9}$");
    private static final Pattern PESEL_PATTERN = Pattern.compile("^[0-9]{11}$");

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPesel(String pesel, User.Role role) {
        // Dla administratora pomijamy walidację PESEL
        if (role == User.Role.ADMIN) {
            return true;
        }

        return isValidPesel(pesel);
    }

    public static boolean isValidPesel(String pesel) {
        if (pesel == null || pesel.isEmpty()) {
            return false;
        }
        if (!PESEL_PATTERN.matcher(pesel).matches()) {
            return false;
        }
        
        // Algorytm sprawdzający sumę kontrolną PESEL
        try {
            int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
            int sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Integer.parseInt(pesel.substring(i, i + 1)) * weights[i];
            }
            int checkDigit = (10 - (sum % 10)) % 10;
            return checkDigit == Integer.parseInt(pesel.substring(10));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPassword(String password, String confirmPassword) {
        return password != null && !password.isEmpty() && password.equals(confirmPassword);
    }

    public static String validateUserInput(String email, String phone, String pesel) {
        if (email != null && !email.isEmpty() && !isValidEmail(email)) {
            return "Nieprawidłowy format adresu email";
        }
        if (phone != null && !phone.isEmpty() && !isValidPhone(phone)) {
            return "Numer telefonu musi składać się z 9 cyfr";
        }
        return null;
    }

    public static String validateUserInput(String email, String phone, String pesel, User.Role role) {
        if (!isValidEmail(email)) {
            return "Nieprawidłowy format adresu email";
        }
        
        // Waliduj PESEL tylko dla pacjentów
        if (role == User.Role.PATIENT) {
            if (!isValidPesel(pesel)) {
                return "Nieprawidłowy numer PESEL";
            }
        }
        
        // Waliduj telefon tylko dla pacjentów i lekarzy
        if (role != User.Role.ADMIN && !isValidPhone(phone)) {
            return "Numer telefonu musi składać się z 9 cyfr";
        }
        
        return null;
    }

    // Metoda specjalnie dla rejestracji pacjenta
    public static String validatePatientRegistration(String email, String phone, String pesel) {
        if (!isValidEmail(email)) {
            return "Nieprawidłowy format adresu email";
        }
        if (!isValidPhone(phone)) {
            return "Numer telefonu musi składać się z 9 cyfr";
        }
        if (!isValidPesel(pesel)) {
            return "Nieprawidłowy numer PESEL";
        }
        return null;
    }
}
