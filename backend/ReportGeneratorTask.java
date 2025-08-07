public class ReportGeneratorTask implements Runnable {

    private final Long reportResultId;
    private final ReportResultForm form;

    public ReportGeneratorTask(Long reportResultId, ReportResultForm form) {
        this.reportResultId = reportResultId;
        this.form = form;
    }


    @Override
    public void run() {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Step 1: Load ReportResult from DB
            ReportResult result = session.get(ReportResult.class, reportResultId);
            if (result == null) {
                throw new IllegalArgumentException("ReportResult not found with ID: " + reportResultId);
            }

            result.setStatus("in_progress");
            session.update(result);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            handleFailure(reportResultId, e);
            return;
        } finally {
            if (session != null) session.close();
        }

        byte[] reportData;
        String mimeType;
        String fileName;

        try {
            // Step 2: Lookup report generator by reportType
            ReportGeneratorService generator = ReportGeneratorRegistry.getService(
                                                    form.getReportType(), form.getOutputFormat()
                                                );
            if (generator == null) {
                throw new UnsupportedOperationException("No generator found for report type: " + form.getReportType());
            }

            reportData = generator.generateReport(form);
            mimeType = generator.getMimeType();
            fileName = generator.getFileName(form);

        } catch (Exception e) {
            handleFailure(reportResultId, e);
            return;
        }

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Step 3: Create and persist Document
            Document doc = new Document();
            doc.setCreatedBy(form.getRequestedBy());
            doc.setCreatedDate(new Timestamp(System.currentTimeMillis()));
            doc.setFilename(fileName);
            doc.setMimetype(mimeType);
            doc.setDatasize((long) reportData.length);
            doc.setVersion(1);

            session.save(doc);

            // Step 4: Store binary data
            DocumentData docData = new DocumentData();
            docData.setDocument(doc);
            docData.setPayload(reportData);
            docData.setVersionNumber(1);

            session.save(docData);

            // Step 5: Update ReportResult with status and document link
            ReportResult result = session.get(ReportResult.class, reportResultId);
            result.setStatus("completed");
            result.setDocument(doc);
            result.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
            result.setLastModifiedBy(form.getRequestedBy());

            session.update(result);
            tx.commit();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            handleFailure(reportResultId, e);
        } finally {
            if (session != null) session.close();
        }
    }

    private void handleFailure(Long id, Exception e) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            ReportResult result = session.get(ReportResult.class, id);
            if (result != null) {
                result.setStatus("failed");
                result.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
                result.setLastModifiedBy(form.getRequestedBy());
                session.update(result);
            }

            tx.commit();
        } catch (Exception ignored) {
            if (tx != null) tx.rollback();
        } finally {
            if (session != null) session.close();
        }

        e.printStackTrace(); // Or log it using your logging framework
    }
}
