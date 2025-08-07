<!DOCTYPE html>
<html>
<head>
  <title>Async Report</title>
  <script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
  <style>
    .spinner {
      border: 2px solid #ccc;
      border-top: 2px solid #007bff;
      border-radius: 50%;
      width: 14px;
      height: 14px;
      animation: spin 0.8s linear infinite;
    }
    .checkmark { color: green; font-weight: bold; }
    .cross { color: red; font-weight: bold; }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
  </style>
</head>
<body>
  <form id="reportForm">
    <label for="borrower">Select Borrower:</label>
    <select id="borrower" name="borrowerId" required>
      <option value="">Loading borrowers...</option>
    </select>
    <input type="hidden" name="reportType" value="loanSummaryPDF">
    <button type="submit">Generate Report</button>
  </form>

  <table id="reportTable" border="1">
    <thead><tr><th>ID</th><th>Borrower</th><th>Status</th><th>Download</th></tr></thead>
    <tbody></tbody>
  </table>

  <script>
    $(function () {
      $.getJSON('/listBorrowers.do', function(borrowers) {
        var $select = $('#borrower');
        $select.empty().append('<option value="">-- Select Borrower --</option>');
        $.each(borrowers, function(i, b) {
          $select.append('<option value="' + b.id + '">' + b.name + '</option>');
        });
      });

      $('#reportForm').submit(function (e) {
        e.preventDefault();
        var formData = $(this).serialize();
        $.post('/generateReport.do', formData, function(report) {
          addReportRow(report);
          startPolling(report.id);
        }, 'json');
      });

      function addReportRow(report) {
        var borrowerName = $('#borrower option:selected').text();
        var rowHtml = '<tr id="report-row-' + report.id + '">' +
          '<td>' + report.id + '</td>' +
          '<td>' + borrowerName + '</td>' +
          '<td id="status-' + report.id + '"><div class="spinner"></div> ' + report.status + '</td>' +
          '<td id="download-' + report.id + '">-</td>' +
          '</tr>';
        $('#reportTable tbody').prepend(rowHtml);
      }

      function startPolling(reportId) {
        var interval = setInterval(function () {
          $.get('/checkReportStatus.do', { reportResultId: reportId }, function(status) {
            var $status = $('#status-' + reportId);
            if (status === 'completed') {
              $status.html('<span class="checkmark">&#10004;</span> completed');
              $('#download-' + reportId).html('<a href="/downloadReport.do?reportResultId=' + reportId + '">Download</a>');
              clearInterval(interval);
            } else if (status === 'failed') {
              $status.html('<span class="cross">&#10006;</span> failed');
              $('#download-' + reportId).text('Failed');
              clearInterval(interval);
            } else {
              $status.html('<div class="spinner"></div> ' + status);
            }
          });
        }, 3000);
      }
    });
  </script>
</body>
</html>
