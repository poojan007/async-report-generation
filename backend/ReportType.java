public enum ReportType {
    LOAN_SUMMARY("loanSummary");

    private final String code;

    ReportType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReportType fromCode(String code) {
        for (ReportType type : values()) {
            if (type.code.equalsIgnoreCase(code)) return type;
        }
        throw new IllegalArgumentException("Invalid report type: " + code);
    }
}
