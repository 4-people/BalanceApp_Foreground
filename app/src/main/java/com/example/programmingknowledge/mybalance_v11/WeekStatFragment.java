package com.example.programmingknowledge.mybalance_v11;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static java.lang.Integer.parseInt;

public class WeekStatFragment extends Fragment {
    // newInstance 할 때 파라미터로 받아오는 걸 저장
    private static final String ARG_DATE = "date";
    private String date;

    private static final int DEFAULT_DATA = 0;

    private ColumnChartView chart;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = true;
    private boolean hasLabelForSelected = true;
    private int dataType = DEFAULT_DATA;

    List<String> weeks = new ArrayList<String>(Arrays.asList("월","화","수","목","금","토","일"));
    String monday= "";
    String sunday="";

    int MAX_PAGE=3;
    Fragment cur_fragment=new Fragment();

    public WeekStatFragment() {
    }

    // view pager에서 WeekStatFragment 생성할때 씀, 파라미터를 저장해줌
    public static WeekStatFragment newInstance(String date) {
        WeekStatFragment fragment = new WeekStatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    // view pager에서 WeekStatFragment 생성할때 씀, 파라미터를 저장해줌
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_stat, container, false);

        chart = (ColumnChartView) rootView.findViewById(R.id.chart);
        //fragment_stat.setOnValueTouchListener(new ValueTouchListener());   //차트를 선택했을때 값이 팝업으로 뜨는 함수
        chart.setZoomEnabled(false);

        //db읽기
        DBHelper helper = new DBHelper(container.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            monday = subDate(date,0);
            sunday = subDate(date,6);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Cursor cursor = db.rawQuery("select * from tb_dailybalance where date between '"+monday+"' and '"+sunday+"'",null);

        setWeekRange(rootView);   //'-월-일 ~ -월-일' 텍스트뷰 변경
        generateStackedData(db); //차트 생성
        setAverageTime(rootView, db); //이주의 평균시간들 텍스트뷰 변경

        return rootView;
    }

    //날짜 빼는 메소드
    public static String subDate(String dt, int d) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        Calendar cal = Calendar.getInstance();
        Date date =  new SimpleDateFormat("yyyy/MM/dd").parse(dt);
        cal.setTime(date);
        cal.add(Calendar.DATE, d);

        return format.format(cal.getTime());
    }

    public void setWeekRange(View rootView){
        TextView weekRange = rootView.findViewById(R.id.range);
        String mondayMonth = date.substring(5,7);
        String mondayDay = date.substring(8,10);
        String sundayMonth = sunday.substring(5,7);
        String sundayDay = sunday.substring(8,10);
        weekRange.setText(mondayMonth+"월 "+mondayDay+"일 ~ "+sundayMonth+"월 "+sundayDay+"일");
    }

/*
    public void setMonthAndNthWeek(View rootView){
            TextView nthweek = rootView.findViewById(R.id.nthweek);
            //Cursor cursor = db.rawQuery("select * from tb_dailybalance where date="+date , null);
            //cursor.moveToNext();
            //String month = (cursor.getString(cursor.getColumnIndex("date"))).substring(5, 7);
            //int nth = getNthWeek(cursor.getString(cursor.getColumnIndex("date")));
            String month = (date).substring(5, 7);
            int nth = getNthWeek(date,null);
            nthweek.setText(month+"월 "+nth+"주차");
    }*/
    /*
    private static int getNthWeek(String thisDate, String option) {
        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
        //SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy/MM/dd");
        //String thisDate = sdfdate.format(date);
        //String thisDate = "2019/07/07";
        System.out.println("넣은날짜: "+thisDate);

        String todayYY = thisDate.substring(0, 4); //0123
        String todayMM = thisDate.substring(5, 7); //56
        String todayDD = thisDate.substring(8, 10);  //89

        // 오늘 일자를 받아서 int 형으로 치환 한다.
        int year = parseInt(todayYY);
        int month = parseInt(todayMM)-1;   // 월은 0 부터 시작이기 때문에 -1 을 해준다.
        int day = parseInt(todayDD);
        int monday = 0;
        int friday = 0;

        // calendar 선언.
        Calendar to_day = Calendar.getInstance();
        //오늘 일자를 setup 한다.
        to_day.set(year, month, day);
        // 오늘을 기준으로 해당 주의 월요일 과 금요일을 구한다.
        int today_week =  to_day.get(Calendar.DAY_OF_WEEK); // 오늘이 무슨 요일인지 int형으로 반환. /(1-7)/1은 SUNDAY 7은 SATURDAY

        if(today_week == Calendar.MONDAY){ // 먼저 월요일을 구하기 위해 오늘이 월요일인지 체크 한다.
            monday = day;
            friday = day + (Calendar.FRIDAY - Calendar.MONDAY);
            // 오늘이 월요일이면 오늘 일자에  금요일과 월요일의 일차수를 뺀 만큼 더하면 금요일 일자가 나온다.
        }
        else if(today_week == Calendar.FRIDAY) { // 오늘 일자가 금요일인지 체크 한다.
            // 오늘이 금요일이면 오늘 일자에  금요일과 월요일의 일차수를 뺀 만큼 빼면 월요일 일자가 나온다.
            monday = day - (Calendar.FRIDAY - Calendar.MONDAY);
            friday = day;
        }
        else if(today_week == Calendar.SUNDAY){
            // today_week 가 일요일 이면 오늘 일자에 sunday(1)를 더하여 monday 일자를 구함.
            monday = day + Calendar.SUNDAY;
            friday = day + (Calendar.FRIDAY - Calendar.SUNDAY);
            // 오늘이 일요일이면 friday(6) 에 sunday(1)을 뺀 만큼 더하여 금요일 일자를 구함.
        }
        else if(today_week == Calendar.SATURDAY){
            // 오늘이 토요일이면 saturday(7) 에서 monday(2)를 뺀 후 오늘일자에서 뺀 뒤 월요일 일자를 구함.
            monday = day - (Calendar.SATURDAY - Calendar.MONDAY);
            // 오늘이 토요일이면 saturday(7) 에서 friday(6)을 뺀 후 오늘일자에서 뺀 뒤 금요일 일자를 구함.
            friday = day - (Calendar.SATURDAY - Calendar.FRIDAY);
        }
        else {  // 화 , 수 , 목 일 때
            // 오늘 일자 표시 에서 monday(2) 를 뺀 후 오늘 일자에서 해당 일자를 뺀다.
            monday = day - (today_week - Calendar.MONDAY);
            // friday(6) 에서 오늘 일자 표시를 뺀 후 오늘일자에 해당 일자를 더한다.
            friday = day + (Calendar.FRIDAY - today_week);
        }
        //이번주의 주차를 구한다.
        //주차는 월요일을 기준으로 해당 달의 주차를 표시 한다.
        to_day = Calendar.getInstance(); // to_day 를 초기화 한다.
        to_day.set(year, month, friday); // 월요일에 해당하는 주차 구하기 위해 월요일 일자를 입력. //월요일 넣으나 금요일 넣으나 똑같은거 같음

        int this_week =  to_day.get(Calendar.WEEK_OF_MONTH); // 이번 주의 월요일에 해당하는 주차를 가져온다.
        System.out.println("이번주의 월요일에 해당하는주차: "+this_week);

        if(option=="monday") return monday;
        return this_week;
        //this_week가 우리가 필요한 값
    }
    */
    private void generateStackedData(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select * from tb_dailybalance where date between '"+monday+"' and '"+sunday+"'",null);

        int categoryNum = 5;   //스택의 개수
        int dayNum = 7;   //일주일(7일) 그래프
        // Column can have many stacked subcolumns, here I use 4 stack subcolumn in each of 4 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();    //'월화수목금토일' 표시 그래프에 뜨게
        for(int i=0; i<dayNum; i++){
            axisValues.add(i, new AxisValue(i).setLabel(weeks.get(i)));
            columns.add(null);
        }
        for (int i=0; i<7; i++) {
            values = new ArrayList<SubcolumnValue>();
            if (cursor.moveToNext()){
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("rest")), getResources().getColor(R.color.rest)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("work")), getResources().getColor(R.color.work)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("study")), getResources().getColor(R.color.study)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("exercise")), getResources().getColor(R.color.exercise)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("leisure")), getResources().getColor(R.color.leisure)));
                    //values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("other")), getResources().getColor(R.color.lightGray)));

            }
            Column column = new Column(values);
            //column.setHasLabels(hasLabels);
            //column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.set(i,column);
        }


        data = new ColumnChartData(columns);

        // Set stacked flag.
        data.setStacked(true);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);

            axisX.setValues(axisValues);

            axisX.setTextColor(Color.BLACK);
            axisY.setTextColor(Color.BLACK);

            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        chart.setColumnChartData(data);
    }

    private void setAverageTime(View rootView, SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select * from tb_dailybalance where date between '"+monday+"' and '"+sunday+"'",null);

        TextView restAverageValue = rootView.findViewById(R.id.restAverageValue);
        TextView workAverageValue = rootView.findViewById(R.id.workAverageValue);
        TextView studyAverageValue = rootView.findViewById(R.id.studyAverageValue);
        TextView exerciseAverageValue = rootView.findViewById(R.id.exerciseAverageValue);
        TextView leisureAverageValue = rootView.findViewById(R.id.leisureAverageValue);
        TextView othersAverageValue = rootView.findViewById(R.id.othersAverageValue);
        float rest=0,work=0,study=0,exercise=0,leisure=0,others=0;
        while(cursor.moveToNext()){
            rest += cursor.getFloat(cursor.getColumnIndex("rest"));
            work += cursor.getFloat(cursor.getColumnIndex("work"));
            study += cursor.getFloat(cursor.getColumnIndex("study"));
            exercise += cursor.getFloat(cursor.getColumnIndex("exercise"));
            leisure += cursor.getFloat(cursor.getColumnIndex("leisure"));
            others += cursor.getFloat(cursor.getColumnIndex("other"));
        }
        restAverageValue.setText(getTime(rest/7));
        workAverageValue.setText(getTime(work/7));
        studyAverageValue.setText(getTime(study/7));
        exerciseAverageValue.setText(getTime(exercise/7));
        leisureAverageValue.setText(getTime(leisure/7));
        othersAverageValue.setText(getTime(others/7));
    }

    public String getTime(Float time){
        float min = time%1;
        float hour = time-min;
        String res= (int)hour+"시간 "+(int)(min*60)+"분";

        return res;
    }
}
