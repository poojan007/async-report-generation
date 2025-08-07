import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateReportAction extends Action {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ReportResultForm reportForm = (ReportResultForm) form;

        ReportResult result = new ReportResult();
        result.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        result.setCreatedBy((String) request.getSession().getAttribute("loggedInUserId"));
        result.setStatus("pending");
        result.setReportType(reportForm.getReportType());
        result.setVersionNumber(1);
        result.setProgress(0);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(result);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }

        executor.submit(new ReportGeneratorTask(result.getId(), reportForm));

        ObjectMapper objectMapper = new ObjectMapper();

        // Create a map to represent the response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", result.getId());
        responseMap.put("reportType", result.getReportType());
        responseMap.put("status", result.getStatus());
        responseMap.put("createdDate", result.getCreatedDate().toString());

        if ("completed".equalsIgnoreCase(result.getStatus()) && result.getDocument() != null) {
            responseMap.put("downloadUrl", "/downloadReport.do?reportResultId=" + result.getId());
        }

        String json = objectMapper.writeValueAsString(responseMap);

        response.setContentType("application/json");
        response.getWriter().write(json);

        return null;
    }
}
