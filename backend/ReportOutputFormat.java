public enum ReportOutputFormat {
    PDF, CSV;

    public static ReportOutputFormat fromString(String value) {
        try {
            return ReportOutputFormat.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid output format: " + value);
        }
    }
}
