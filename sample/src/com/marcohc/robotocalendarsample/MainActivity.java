package com.marcohc.robotocalendarsample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;


import com.marcohc.robotocalendar.RobotoCalendarView;
import com.marcohc.robotocalendar.RobotoCalendarView.RobotoCalendarListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Sample Activity
 *
 * @author Marco Hernaiz Cao
 */
public class MainActivity extends Activity implements RobotoCalendarListener {

    private RobotoCalendarView robotoCalendarView;
    private int currentMonthIndex;
    private Calendar currentCalendar;
    private Date Today;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Gets the calendar from the view
        robotoCalendarView = (RobotoCalendarView) findViewById(R.id.robotoCalendarPicker);

        // Set listener, in this case, the same activity
        robotoCalendarView.setRobotoCalendarListener(this);

        // Initialize the RobotoCalendarPicker with the current index and date
        currentMonthIndex = 0;
        currentCalendar = Calendar.getInstance(Locale.getDefault());

        // Mark current day
        robotoCalendarView.markDayAsCurrentDay(currentCalendar.getTime());
        Today = currentCalendar.getTime();


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDateSelected(Date date) {
        //點選事件監聽器

        //讓畫面顯示點選哪天
        robotoCalendarView.markDayAsSelectedDay(date);

        // Mark that day with random colors
        final Random random = new Random(System.currentTimeMillis());
        final int style = random.nextInt(3);
        switch (style) {
            case 0:
                robotoCalendarView.markFirstUnderlineWithStyle(RobotoCalendarView.BLUE_COLOR, date);
                break;
            case 1:
                robotoCalendarView.markSecondUnderlineWithStyle(RobotoCalendarView.GREEN_COLOR, date);
                break;
            case 2:
                robotoCalendarView.markFirstUnderlineWithStyle(RobotoCalendarView.RED_COLOR, date);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRightButtonClick() {
        currentMonthIndex++;
        updateCalendar();
    }

    @Override
    public void onLeftButtonClick() {
        currentMonthIndex--;
        updateCalendar();
    }

    private void updateCalendar() {
        currentCalendar = Calendar.getInstance(Locale.getDefault());
        currentCalendar.add(Calendar.MONTH, currentMonthIndex); //月的加減
        robotoCalendarView.initializeCalendar(currentCalendar);
        if (currentMonthIndex == 0) { //等於零等於今天這個月份
            robotoCalendarView.markDayAsCurrentDay(Today);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentMonthIndex == 0) { //等於零等於今天這個月份
            robotoCalendarView.markDayAsCurrentDay(Today);
        }
    }


}
