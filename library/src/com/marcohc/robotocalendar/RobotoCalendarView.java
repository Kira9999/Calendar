/*
 * Copyright (C) 2015 Marco Hernaiz Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marcohc.robotocalendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * The roboto calendar view
 *
 * @author Marco Hernaiz Cao
 */
public class RobotoCalendarView extends LinearLayout {

    // ************************************************************************************************************************************************************************
    // * Attributes
    // ************************************************************************************************************************************************************************

    private static final String DAY_OF_WEEK = "dayOfWeek";
    // View
    private Context context;
    private TextView dateTitle;
    private ImageView leftButton;
    private ImageView rightButton;
    private View view;

    // Class
    private RobotoCalendarListener robotoCalendarListener;
    private Calendar currentCalendar;
    private Locale locale;

    // Style
    private int monthTitleColor;
    private int dayOfWeekColor;
    private int dayOfMonthColor;

    private Date lastCurrentDay;
    private Date lastSelectedDay;

    public static final int RED_COLOR = R.color.red;
    public static final int GREEN_COLOR = R.color.green;
    public static final int BLUE_COLOR = R.color.blue;
    public static final int WHITE_COLOR = R.color.white;

    private static final String DAY_OF_MONTH_TEXT = "dayOfMonthText";
    private static final String DAY_OF_MONTH_BACKGROUND = "dayOfMonthBackground";
    private static final String DAY_OF_MONTH_CONTAINER = "dayOfMonthContainer";
    private static final String FIRST_UNDERLINE = "firstUnderlineView";
    private static final String SECOND_UNDERLINE = "secondUnderlineView";

    // ************************************************************************************************************************************************************************
    // * Initialization methods
    // ************************************************************************************************************************************************************************

    public RobotoCalendarView(Context context) {
        super(context);
        this.context = context;
        onCreateView();
    }

    public RobotoCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode()) {
            return;
        }
        getAttributes(context, attrs);
        onCreateView();
    }

    //基本的title bar 設定位子
    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RobotoCalendarView, 0, 0);
        monthTitleColor = typedArray.getColor(R.styleable.RobotoCalendarView_monthTitleColor, R.color.month_title);
        dayOfWeekColor = typedArray.getColor(R.styleable.RobotoCalendarView_dayOfWeekColor, R.color.day_of_week_color);
        dayOfMonthColor = typedArray.getColor(R.styleable.RobotoCalendarView_dayOfMonthColor, R.color.day_of_month);
        typedArray.recycle();
    }

    public View onCreateView() {

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflate.inflate(R.layout.roboto_calendar_picker_layout, this, true);
        //true 是指日曆是否顯示

        findViewsById(view);
        //左右邊的按鈕

        initializeEventListeners();
        //左右邊日曆按鈕監聽器

        initializeComponentBehavior();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        return view;
    }

    private void findViewsById(View view) {
        leftButton = (ImageView) view.findViewById(R.id.leftButton);
        rightButton = (ImageView) view.findViewById(R.id.rightButton);
        dateTitle = (TextView) view.findViewById(R.id.dateTitle);
    }

    private void initializeEventListeners() {

        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (robotoCalendarListener == null) {
                    throw new IllegalStateException("You must assing a valid RobotoCalendarListener first!");
                }

                robotoCalendarListener.onLeftButtonClick();
            }
        });

        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (robotoCalendarListener == null) {
                    throw new IllegalStateException("You must assing a valid RobotoCalendarListener first!");
                }

                robotoCalendarListener.onRightButtonClick();
            }
        });
    }

    private void initializeComponentBehavior() {
        // Initialize calendar for current month
        Locale locale = context.getResources().getConfiguration().locale;
        //getConfiguration().locale 抓取語言，來顯示裡面文字內容

        Calendar currentCalendar = Calendar.getInstance(locale);
        initializeCalendar(currentCalendar);
    }

    // ************************************************************************************************************************************************************************
    // * Initialization UI methods
    // ************************************************************************************************************************************************************************

    @SuppressLint("DefaultLocale")
    private void initializeTitleLayout() {

        // Apply styles
        int color = getResources().getColor(R.color.red);
        dateTitle.setTextColor(color);
        //title的顏色內容

        String dateText = new DateFormatSymbols(locale).getMonths()[currentCalendar.get(Calendar.MONTH)].toString();
        dateText = dateText.substring(0, 1).toUpperCase() + dateText.subSequence(1, dateText.length());
        //dateText = 幾月 "8月"
        Calendar calendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {//判別是否過今年
            //今年內都顯示 幾月
            dateTitle.setText(dateText);
        } else {
            //前年或是後年都顯示 月+年
            dateTitle.setText(dateText + " " + currentCalendar.get(Calendar.YEAR));
        }
        //日期的顯示
    }

    @SuppressLint("DefaultLocale")
    private void initializeWeekDaysLayout() {

        // Apply styles
        int color = getResources().getColor(dayOfWeekColor);

        TextView dayOfWeek;
        String dayOfTheWeekString;
        String[] weekDaysArray = new DateFormatSymbols(locale).getShortWeekdays();
        for (int i = 1; i < weekDaysArray.length; i++) {

            dayOfWeek = (TextView) view.findViewWithTag(DAY_OF_WEEK + getWeekIndex(i, currentCalendar));
            dayOfTheWeekString = weekDaysArray[i];

            dayOfTheWeekString = checkSpecificLocales(dayOfTheWeekString, i);
            //Log.d("", "title 日期顯示 = " + weekDaysArray[i]);
            dayOfWeek.setText(dayOfTheWeekString);
            //把title的所有質放入dayOfWeek，依照1-7顯示
            dayOfWeek.setTextColor(color);
            //顯示月下面那一行，星期幾
        }
    }

    @SuppressLint("DefaultLocale")
    private String checkSpecificLocales(String dayOfTheWeekString, int i) {
        // Set Wednesday as "X" in Spanish locale
        if (i == 4 && locale.getCountry().equals("ES")) { //西班牙國家時，星期三等於X
            dayOfTheWeekString = "X";
        } else {
            //dayOfTheWeekString = dayOfTheWeekString.substring(0, 3).toUpperCase();//取多少字串，並且大寫
        }
        return dayOfTheWeekString;
    }

    private void initializeDaysOfMonthLayout() {

        // Apply styles
        int color = getResources().getColor(dayOfMonthColor);
        TextView dayOfMonthText;
        View firstUnderline;
        View secondUnderline;
        ViewGroup dayOfMonthContainer;
        ViewGroup dayOfMonthBackground;

        for (int i = 1; i < 43; i++) { //一頁顯示天數

            //月曆基本的設定
            dayOfMonthContainer = (ViewGroup) view.findViewWithTag(DAY_OF_MONTH_CONTAINER + i);
            dayOfMonthBackground = (ViewGroup) view.findViewWithTag(DAY_OF_MONTH_BACKGROUND + i);
            dayOfMonthText = (TextView) view.findViewWithTag(DAY_OF_MONTH_TEXT + i);

            //顯示圖示
            firstUnderline = (View) view.findViewWithTag(FIRST_UNDERLINE + i);
            secondUnderline = (View) view.findViewWithTag(SECOND_UNDERLINE + i);

            //跨次日的不可見
            dayOfMonthText.setVisibility(View.INVISIBLE);

            //日期下有條底線，基本預設值全部不顯示
            firstUnderline.setVisibility(View.INVISIBLE);
            secondUnderline.setVisibility(View.INVISIBLE);

            // Apply styles
            dayOfMonthText.setTextColor(color);
            dayOfMonthText.setBackgroundResource(android.R.color.transparent);
            dayOfMonthContainer.setBackgroundResource(android.R.color.transparent);
            dayOfMonthContainer.setOnClickListener(null);
            dayOfMonthBackground.setBackgroundResource(android.R.color.transparent);
        }
    }

    private void setDaysInCalendar() {
        Calendar auxCalendar = Calendar.getInstance(locale); //使用指定語系建立Calendar實體
        auxCalendar.setTime(currentCalendar.getTime());//寫入現在時間
        auxCalendar.set(Calendar.DAY_OF_MONTH, 1); //設置當前日期為本月的第一天

        Log.d("", "取的時間 " + String.valueOf(auxCalendar.getTime()));
        int firstDayOfMonth = auxCalendar.get(Calendar.DAY_OF_WEEK);//從日曆中找尋第一天星期幾(get)
        Log.d("", "aaaaaa = " + String.valueOf(firstDayOfMonth));
        Log.d("", "aaaaaa = " + String.valueOf(auxCalendar.getFirstDayOfWeek()));
        // Calculate dayOfMonthIndex 讓Calendar 日曆對應索引值
        int dayOfMonthIndex = getWeekIndex(firstDayOfMonth, auxCalendar);


        TextView dayOfMonthText;
        ViewGroup dayOfMonthContainer;

        //自動抓取最大天數，超過當月時則自動換月
        for (int i = 1; i <= auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++, dayOfMonthIndex++) {
            Log.d("", "月 = " + String.valueOf(dayOfMonthIndex));
            dayOfMonthContainer = (ViewGroup) view.findViewWithTag(DAY_OF_MONTH_CONTAINER + dayOfMonthIndex); //用tag來指定哪一個view + 數字
            //dayOfMonthIndexn 是方便讓直指定所指定的位置
            dayOfMonthText = (TextView) view.findViewWithTag(DAY_OF_MONTH_TEXT + dayOfMonthIndex);//用tag來指定哪一個view + 數字
            if (dayOfMonthText == null) {
                break;
            }
            //日曆內的點擊事件
            dayOfMonthContainer.setOnClickListener(onDayOfMonthClickListener);
            dayOfMonthText.setVisibility(View.VISIBLE);
            dayOfMonthText.setText(String.valueOf(i));
        }

        // If the last week row has no visible days, hide it or show it in case
        ViewGroup weekRow = (ViewGroup) view.findViewWithTag("weekRow6");
        dayOfMonthText = (TextView) view.findViewWithTag("dayOfMonthText36");
        //最後一排 roboto_calendar_week_6.xml
        //如果第一個直視顯示，則整排顯示，如果沒有則隱藏
        if (dayOfMonthText.getVisibility() == INVISIBLE) {
            weekRow.setVisibility(GONE);
        } else {
            weekRow.setVisibility(VISIBLE);
        }

    }

    private void clearDayOfTheMonthStyle(Date currentDate) {

        if (currentDate != null) {
            Calendar calendar = getCurrentCalendar();
            calendar.setTime(currentDate);
            ViewGroup dayOfMonthBackground = getDayOfMonthBackground(calendar);
            dayOfMonthBackground.setBackgroundResource(android.R.color.transparent);
        }
    }

    // ************************************************************************************************************************************************************************
    // * Getter methods
    // ************************************************************************************************************************************************************************

    private ViewGroup getDayOfMonthBackground(Calendar currentCalendar) {
        return (ViewGroup) getView(DAY_OF_MONTH_BACKGROUND, currentCalendar);
    }

    private TextView getDayOfMonthText(Calendar currentCalendar) {
        return (TextView) getView(DAY_OF_MONTH_TEXT, currentCalendar);
    }

    private View getFirstUnderline(Calendar currentCalendar) {
        return getView(FIRST_UNDERLINE, currentCalendar);
    }

    private View getSecondUnderline(Calendar currentCalendar) {
        return getView(SECOND_UNDERLINE, currentCalendar);
    }

    private int getDayIndexByDate(Calendar currentCalendar) {
        int monthOffset = getMonthOffset(currentCalendar);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int index = currentDay + monthOffset;
        return index;
    }

    private int getMonthOffset(Calendar currentCalendar) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentCalendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayWeekPosition = calendar.getFirstDayOfWeek();
        int dayPosition = calendar.get(Calendar.DAY_OF_WEEK);

        //今天日期的判斷
        if (firstDayWeekPosition == 1) {
            return dayPosition - 1;
        } else {

            if (dayPosition == 1) {
                return 6;
            } else {
                return dayPosition - 2;
            }
        }

    }

    private int getWeekIndex(int weekIndex, Calendar currentCalendar) {
        //第一天開始值，這一定會是 1 ，weekIndex = 7 因為一個禮拜為七天
        int firstDayWeekPosition = currentCalendar.getFirstDayOfWeek();

        if (firstDayWeekPosition == 1) {
            return weekIndex;
        } else {

            if (weekIndex == 1) {
                return 7;
            } else {
                return weekIndex - 1;
            }
        }
    }

    private View getView(String key, Calendar currentCalendar) {
        int index = getDayIndexByDate(currentCalendar);
        View childView = (View) view.findViewWithTag(key + index);
        return childView;
    }

    private Calendar getCurrentCalendar() {
        Calendar currentCalendar = Calendar.getInstance(context.getResources().getConfiguration().locale);
        return currentCalendar;
    }

    // ************************************************************************************************************************************************************************
    // * Public calendar methods
    // ************************************************************************************************************************************************************************

    @SuppressLint("DefaultLocale")
    public void initializeCalendar(Calendar currentCalendar) {

        this.currentCalendar = currentCalendar;
        locale = context.getResources().getConfiguration().locale;

        //主title
        initializeTitleLayout();

        //一到星期天
        initializeWeekDaysLayout();

        //日曆版面的相關設定
        initializeDaysOfMonthLayout();

        //天設定到日曆內
        setDaysInCalendar();
    }

    public void markDayAsCurrentDay(Date currentDate) {
        if (currentDate != null) {
            lastCurrentDay = currentDate;
            Calendar currentCalendar = getCurrentCalendar();
            currentCalendar.setTime(currentDate);
            TextView dayOfMonth = getDayOfMonthText(currentCalendar);
            //讓那一天有圓圈圈框住
            ViewGroup dayOfMonthBackground = getDayOfMonthBackground(currentCalendar);
            dayOfMonthBackground.setBackgroundResource(R.drawable.circle);
            dayOfMonth.setTextColor(context.getResources().getColor(R.color.current_day_of_month)); //設定當天顏色
        }
    }

    public void markDayAsSelectedDay(Date currentDate) {

        // Initialize attributes
        Calendar currentCalendar = getCurrentCalendar();
        currentCalendar.setTime(currentDate);

        // Clear previous marks
        clearDayOfTheMonthStyle(lastSelectedDay);

        markDayAsCurrentDay(lastCurrentDay);

        // Store current values as last values
        storeLastValues(currentDate);

        // Mark current day as selected
        ViewGroup dayOfMonthBackground = getDayOfMonthBackground(currentCalendar);
        dayOfMonthBackground.setBackgroundResource(R.drawable.circle);
    }

    private void storeLastValues(Date currentDate) {
        lastSelectedDay = currentDate;
    }

    public void markFirstUnderlineWithStyle(int style, Date currentDate) {
        Locale locale = context.getResources().getConfiguration().locale;
        Calendar currentCalendar = Calendar.getInstance(locale);
        currentCalendar.setTime(currentDate);
        View underline = getFirstUnderline(currentCalendar);

        // Draw day with style
        underline.setVisibility(View.VISIBLE);
        underline.setBackgroundResource(style);
    }

    public void markSecondUnderlineWithStyle(int style, Date currentDate) {
        Locale locale = context.getResources().getConfiguration().locale;
        Calendar currentCalendar = Calendar.getInstance(locale);
        currentCalendar.setTime(currentDate);
        View underline = getSecondUnderline(currentCalendar);

        // Draw day with style
        underline.setVisibility(View.VISIBLE);
        underline.setBackgroundResource(style);
    }

    // ************************************************************************************************************************************************************************
    // * Public interface
    // ************************************************************************************************************************************************************************

    public interface RobotoCalendarListener {

        void onDateSelected(Date date);

        void onRightButtonClick();

        void onLeftButtonClick();
    }

    public void setRobotoCalendarListener(RobotoCalendarListener robotoCalendarListener) {
        this.robotoCalendarListener = robotoCalendarListener;
    }

    // ************************************************************************************************************************************************************************
    // * Event handler methods
    // ************************************************************************************************************************************************************************

    private OnClickListener onDayOfMonthClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // Extract day selected
            ViewGroup dayOfMonthContainer = (ViewGroup) view;
            String tagId = (String) dayOfMonthContainer.getTag();
            tagId = tagId.substring(DAY_OF_MONTH_CONTAINER.length(), tagId.length());
            TextView dayOfMonthText = (TextView) view.findViewWithTag(DAY_OF_MONTH_TEXT + tagId);

            // Fire event
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentCalendar.getTime());
            //currentCalendar.getTime() 抓取現在時間

            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfMonthText.getText().toString()));
            //dayOfMonthText.getText().toString() 你所點選的日期
            Log.i("", "選取日期時間 = " + dayOfMonthText.getText().toString());

            if (robotoCalendarListener == null) {
                throw new IllegalStateException("You must assing a valid RobotoCalendarListener first!");
            } else {
                //傳直給主畫面
                robotoCalendarListener.onDateSelected(calendar.getTime());
                Log.i("", "點選事件觸發" + String.valueOf(calendar.getTime()));

            }
        }
    };
}
