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

    private List<WeekViewEvent> events = new ArrayList<>(80);

    public HorariumView(Context context) {
        this(context, null);
    }

    public HorariumView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWeekViewLoader(new WeekViewLoader() {
            @Override
            public double toWeekViewPeriodIndex(Calendar instance) {
                Log.d("WeekView", "calendar " + instance);
                return instance.get(Calendar.YEAR) * 12 + instance.get(Calendar.MONTH) + (instance.get(Calendar.DAY_OF_MONTH) - 1) / 30.0;
            }

            @Override
            public List<? extends WeekViewEvent> onLoad(int periodIndex) {
                Log.d("WeekView", "periodIndex " + periodIndex);
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
    }

}
