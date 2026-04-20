package com.digivahan.other;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRatioUtil {

    public static class TimeRatio {
        public int passed;
        public int remaining;

        public TimeRatio(int passed, int remaining) {
            this.passed = passed;
            this.remaining = remaining;
        }
    }

    public static TimeRatio calculateTimeRatio(
            String startDateStr,
            String endDateStr
    ) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
            Date today = new Date();

            // Safety checks
            if (today.before(startDate)) {
                return new TimeRatio(0, 100);
            }

            if (today.after(endDate)) {
                return new TimeRatio(100, 0);
            }

            long totalDuration = endDate.getTime() - startDate.getTime();
            long passedDuration = today.getTime() - startDate.getTime();

            double passedRatio = (double) passedDuration / totalDuration * 100;
            double remainingRatio = 100 - passedRatio;

            return new TimeRatio(
                    (int) Math.round(passedRatio),
                    (int) Math.round(remainingRatio)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new TimeRatio(0, 0);
        }
    }
}
