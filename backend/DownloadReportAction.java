public class DownloadReportAction extends Action {

    @Override
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String idParam = request.getParameter("reportResultId");
        if (idParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing reportResultId");
            return null;
        }

        Long reportResultId = Long.valueOf(idParam);

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            ReportResult result = session.get(ReportResult.class, reportResultId);
            if (result == null || !"completed".equals(result.getStatus())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Report not ready");
                return null;
            }

            Document doc = result.getDocument();
            if (doc == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No document found");
                return null;
            }

            DocumentData docData = (DocumentData) session
                .createQuery("from DocumentData where document.id = :docId")
                .setParameter("docId", doc.getId())
                .uniqueResult();

            if (docData == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No document data found");
                return null;
            }

            response.setContentType(doc.getMimetype());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.getFilename() + "\"");
            response.setContentLength(docData.getPayload().length);
            response.getOutputStream().write(docData.getPayload());

            return null;

        } finally {
            session.close();
        }
    }
}
