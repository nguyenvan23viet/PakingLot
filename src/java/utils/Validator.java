package utils;

import java.sql.Date;
import java.util.regex.Pattern;

public class Validator {

    // Khong cho phep khoang trang
    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " khong duoc de trong.";
        }
        return null;
    }

    // Username
    public static String validateUsername(String username) {

        if (username == null || username.trim().isEmpty()) {
            return "Username khong duoc de trong.";
        }

        if (!Pattern.matches("^[A-Za-z0-9_]{3,30}$", username)) {
            return "Username phai tu 3 den 30 ky tu.";
        }

        return null;
    }

    // Password
    public static String validatePassword(String password) {

        if (password == null || password.trim().isEmpty()) {
            return "Password khong duoc de trong.";
        }

        if (password.length() < 6) {
            return "Password phai co it nhat 6 ky tu.";
        }

        return null;
    }

    // Ho ten
    public static String validateFullName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return "Ho ten khong duoc de trong.";
        }

        if (name.trim().length() < 2 || name.trim().length() > 50) {
            return "Ho ten phai tu 2 den 50 ky tu.";
        }

        return null;
    }

    // Email
    public static String validateEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            return "Email khong duoc de trong.";
        }

        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", email)) {
            return "Email khong dung dinh dang.";
        }

        return null;
    }

    // So dien thoai
    public static String validatePhone(String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            return "So dien thoai khong duoc de trong.";
        }

        if (!Pattern.matches("^0\\d{9}$", phone)) {
            return "So dien thoai phai gom 10 chu so, va bat dau bang so 0. VD: 0852560026";
        }

        return null;
    }

    // Owner ID
    public static String validateOwnerID(String ownerID) {

        if (ownerID == null || ownerID.trim().isEmpty()) {
            return "Owner ID khong duoc de trong.";
        }

        if (ownerID.length() > 30) {
            return "Owner ID qua dai.";
        }

        return null;
    }

    // Bien so xe
    public static String validateLicensePlate(String plate) {
    if (plate == null || plate.trim().isEmpty()) {
        return "Bien so xe khong duoc de trong.";
    }
    String p = plate.trim().toUpperCase();
    // Oto: 51H-678.90   |   Xe may: 59A1-12345 hoac 59A1-123.45
    boolean valid = Pattern.matches("^[0-9]{2}[A-Z][0-9A-Z]?-[0-9]{3}\\.?[0-9]{2}$", p);
    if (!valid) {
        return "Bien so xe khong dung dinh dang (VD: 51H-678.90 hoac 59A1-12345, 29AA-23416).";
    }
    return null;
}

    // So nguyen duong
    public static String validatePositiveInt(String value, String fieldName) {

        try {

            int number = Integer.parseInt(value);

            if (number <= 0) {
                return fieldName + " phai lon hon 0.";
            }

        } catch (Exception e) {
            return fieldName + " phai la so nguyen.";
        }

        return null;
    }

    // So thuc duong
    public static String validatePositiveDouble(String value, String fieldName) {

        try {

            double number = Double.parseDouble(value);

            if (number <= 0) {
                return fieldName + " phai lon hon 0.";
            }

        } catch (Exception e) {
            return fieldName + " phai la so.";
        }

        return null;
    }

    // Ngay
    public static String validateDate(String value, String fieldName) {

        try {

            Date.valueOf(value);

        } catch (Exception e) {

            return fieldName + " khong hop le.";

        }

        return null;
    }

}