public class ListReportResultsAction extends Action {

    @Override
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String userId = (String) request.getSession().getAttribute("loggedInUserId");
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        String startParam = request.getParameter("startDate");
        String endParam = request.getParameter("endDate");

        Timestamp startDate = (startParam != null) ? Timestamp.valueOf(startParam + " 00:00:00") : null;
        Timestamp endDate = (endParam != null) ? Timestamp.valueOf(endParam + " 23:59:59") : null;

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            String hql = "from ReportResult where createdBy = :userId";
            if (startDate != null) hql += " and createdDate >= :startDate";
            if (endDate != null) hql += " and createdDate <= :endDate";

            Query query = session.createQuery(hql);
            query.setParameter("userId", userId);
            if (startDate != null) query.setParameter("startDate", startDate);
            if (endDate != null) query.setParameter("endDate", endDate);

            List<ReportResult> results = query.list();

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            out.write("[");
            for (int i = 0; i < results.size(); i++) {
                ReportResult r = results.get(i);
                out.write(toJson(r));
                if (i < results.size() - 1) out.write(",");
            }
            out.write("]");
            return null;

        } finally {
            session.close();
        }
    }

    private String toJson(ReportResult r) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a map to represent the response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", r.getId());
        responseMap.put("reportType", r.getReportType());
        responseMap.put("status", r.getStatus());
        responseMap.put("createdDate", r.getCreatedDate().toString());

        if ("completed".equalsIgnoreCase(r.getStatus()) && r.getDocument() != null) {
            responseMap.put("downloadUrl", "/downloadReport.do?reportResultId=" + r.getId());
        }

        return objectMapper.writeValueAsString(responseMap);
    }

}
