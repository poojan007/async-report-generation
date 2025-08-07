public class ReportGeneratorRegistry {
    private static final Map<String, Map<OutputFormat, ReportGeneratorService>> registry = new HashMap<>();

    static {
        Map<OutputFormat, ReportGeneratorService> loanSummaryMap = new HashMap<>();
        loanSummaryMap.put(OutputFormat.PDF, new LoanSummaryPdfReportService());
        loanSummaryMap.put(OutputFormat.CSV, new LoanSummaryCsvReportService());

        registry.put("loanSummary", loanSummaryMap);
    }

    public static ReportGeneratorService getService(String reportType, String outputFormat) {
        OutputFormat format = OutputFormat.fromString(outputFormat);
        Map<OutputFormat, ReportGeneratorService> formatMap = registry.get(reportType);
        if (formatMap == null || !formatMap.containsKey(format)) {
            throw new IllegalArgumentException("No service registered for " + reportType + " with format " + outputFormat);
        }
        return formatMap.get(format);
    }
}
