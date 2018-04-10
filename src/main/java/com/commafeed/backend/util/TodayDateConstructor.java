package com.commafeed.backend.util;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class TodayDateConstructor {

    /**
     * Returns a string containing the datetime of today at the start of the day. Used to fix a bug in a dependency
     * where the date returned is always in 1970 and commafeed tries to refresh nonstop the related feed.
     * @return
     */
    public String constructDate() {
        ZonedDateTime today = LocalDate.now().atStartOfDay(ZoneOffset.UTC);
        Date dateToReturn = Date.from(today.toInstant());
        return dateToReturn.toString();
    }
}
