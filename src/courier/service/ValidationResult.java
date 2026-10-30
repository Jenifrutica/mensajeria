package courier.service;

public record ValidationResult(
        boolean valid,
        int position,
        String expectedGuide,
        String actualGuide,
        String message) {

    public static ValidationResult valid(String message) {
        return new ValidationResult(true, -1, null, null, message);
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, -1, null, null, message);
    }

    public static ValidationResult mismatch(int position, String expected, String actual, String message) {
        return new ValidationResult(false, position, expected, actual, message);
    }
}
