public class LoanSummaryCsvReportService implements ReportGeneratorService {
    public byte[] generateReport(ReportResultForm form) throws Exception {
        // Logic to generate CSV
        return new byte[0];
    }

    public String getMimeType() {
        return "text/csv";
    }

    public String getFileName(ReportResultForm form) {
        return "loan_summary_" + form.getBorrowerId() + ".csv";
    }
}
