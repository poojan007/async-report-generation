public class ReportResultForm {
    private String reportType;      // e.g., "loanSummary"
    private String outputFormat;    // e.g., "PDF" or "CSV"
    private String borrowerId;
    private String currentYear;
    private String instrumentSerialNumber;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(String currentYear) {
        this.currentYear = currentYear;
    }

    public String getInstrumentSerialNumber() {
        return instrumentSerialNumber;
    }

    public void setInstrumentSerialNumber(String instrumentSerialNumber) {
        this.instrumentSerialNumber = instrumentSerialNumber;
    }

}