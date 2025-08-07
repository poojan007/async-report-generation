public class LoanSummaryPdfReportService implements ReportGeneratorService {

    private static final String TEMPLATE_NAME = "loan_summary_template.tom";
    private static final String DEFAULT_TEMPLATE_PATH = "src/main/docmosis"; // for local dev

    static {
        try {
            SystemManager.initialise();

            // Load template path from system property or fallback to default
            String templatePath = System.getProperty("docmosis.template.path", DEFAULT_TEMPLATE_PATH);
            File templateDir = new File(templatePath);

            DropStoreHelper helper = new DropStoreHelper(TemplateStoreFactory.getStore());
            helper.process(templateDir);
        } catch (Exception e) {
            e.printStackTrace(); // You can replace with proper logging
        }
    }

    @Override
    public byte[] generateReport(ReportResultForm form) throws Exception {
        Long borrowerId = Long.valueOf(form.getBorrowerId());

        Session session = HibernateUtil.getSessionFactory().openSession();
        Borrower borrower;
        List<Loan> loans;
        try {
            borrower = session.get(Borrower.class, borrowerId);
            loans = session.createQuery("from Loan where borrower.id = :borrowerId", Loan.class)
                           .setParameter("borrowerId", borrowerId)
                           .list();
        } finally {
            session.close();
        }

        // Prepare data
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("borrowerName", borrower.getName());
        dataMap.put("borrowerAddress", borrower.getAddress());
        dataMap.put("loanCount", loans.size());
        dataMap.put("loans", loans);

        DataProviderBuilder dpb = new DataProviderBuilder();
        dpb.addMap(dataMap, "data");
        DataProvider dataProvider = dpb.getDataProvider();

        TemplateIdentifier template = new TemplateIdentifier(TEMPLATE_NAME);
        ConversionInstruction instruction = new ConversionInstruction();
        instruction.setConversionFormats(new ConversionFormat[]{ConversionFormat.FORMAT_PDF});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DocumentProcessor.renderDoc(template, dataProvider, instruction, baos);

        return baos.toByteArray();
    }

    @Override
    public String getMimeType() {
        return "application/pdf";
    }

    @Override
    public String getFileName(ReportResultForm form) {
        return "loan_summary_" + form.getBorrowerId() + "_" + System.currentTimeMillis() + ".pdf";
    }
}
