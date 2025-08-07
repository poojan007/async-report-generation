public interface ReportGeneratorService {
    byte[] generateReport(ReportResultForm form) throws Exception;
    String getMimeType();
    String getFileName(ReportResultForm form);
}
