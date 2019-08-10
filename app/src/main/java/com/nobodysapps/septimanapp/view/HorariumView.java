package com.nobodysapps.septimanapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.WeekViewLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HorariumView extends WeekView {

    private static final int NUMBER_OF_SHOWN_DAYS_PORTRAIT = 3;
    private static final int NUMBER_OF_SHOWN_DAYS_LANDSCAPE = 8;
    public static final String TAG = "WeekView";

    private List<WeekViewEvent> events = new ArrayList<>(80);

    public HorariumView(Context context) {
        this(context, null);
    }

    public HorariumView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // set default start_date limit to today and end_date limit according to max shown days
        setupDateLimits(Calendar.getInstance());

        setupWeekLoader();
    }

    private void setupDateLimits(Calendar startDate) {
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.DAY_OF_YEAR, Math.max(NUMBER_OF_SHOWN_DAYS_PORTRAIT, NUMBER_OF_SHOWN_DAYS_LANDSCAPE) - 1);
        setMinDate(startDate);
        setMaxDate(endDate);
    }

    /**
     * Setup WeekViewLoader to always show the events specified with setEvents
     */
    private void setupWeekLoader() {
        setWeekViewLoader(new WeekViewLoader() {
            @Override
            public double toWeekViewPeriodIndex(Calendar instance) {
                return 0;
            }

            @Override
            public List<? extends WeekViewEvent> onLoad(int periodIndex) {
                Log.d(TAG, String.valueOf(events));
                return events;
            }
        });
    }

    public void changeOrientation(boolean isLandscape) {
        if (isLandscape) {
            setNumberOfVisibleDays(NUMBER_OF_SHOWN_DAYS_LANDSCAPE);
        } else {
            setNumberOfVisibleDays(NUMBER_OF_SHOWN_DAYS_PORTRAIT);
        }
    }

    public void setEvents(List<WeekViewEvent> events) {
        this.events = events;
        setShownHoursAccordingToEvents();
    }

    private void setShownHoursAccordingToEvents() {
        int earliestHour = 23;
        int latestHour = 0;
        for (WeekViewEvent event : events) {
            int eventStartHour = event.getStartTime().get(Calendar.HOUR_OF_DAY);
            int eventEndHour = event.getEndTime().get(Calendar.HOUR_OF_DAY) + 1;
            if (eventStartHour < earliestHour) {
                earliestHour = eventStartHour;
            }
            if (eventEndHour > latestHour) {
                latestHour = eventEndHour;
            }
        }
        setLimitTime(earliestHour, latestHour);
    }

}
