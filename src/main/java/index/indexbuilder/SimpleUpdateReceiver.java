package index.indexbuilder;

import report.Report;
import report.UpdateReceiver;
import utils.Utilities;

public class SimpleUpdateReceiver implements UpdateReceiver {
    private Long initTime = null;

    @Override
    public void receiveUpdate(Report report) {
        if (initTime == null) initTime = report.getLong(Report.ReportId.TIME_STAMP);

        Long passedTime = report.getLong(Report.ReportId.TIME_STAMP) - initTime;
        Double progress = report.getDouble(Report.ReportId.PROGRESS);
        Long estimatedLeft = (long) ((passedTime * (1/(progress/100))) - passedTime);


        System.out.print(report.getDouble(Report.ReportId.PROGRESS) + " % ");
        System.out.print("Read " + report.getLong(Report.ReportId.PROCESSED_ITEMS) + " records   ");
        System.out.print("Passed time: " + Utilities.millisToDate(passedTime) + "  ");
        System.out.println("Estimated remaining time: " + Utilities.millisToDate(estimatedLeft));

    }

    @Override
    public void receiveEndUpdate(Report report) {
        System.out.println("finished in " + ((report.getLong(Report.ReportId.END_TIME) - report.getLong(Report.ReportId.START_TIME))/1000) + " seconds");
    }
}