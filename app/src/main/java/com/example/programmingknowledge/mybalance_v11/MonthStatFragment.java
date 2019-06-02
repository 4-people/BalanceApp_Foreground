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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

public class MonthStatFragment extends Fragment {
    // newInstance 할 때 파라미터로 받아오는 걸 저장
    private static final String ARG_DATE = "date";
    private String date;
    private String lastDay="";
    int dayNum;
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    private static final int DEFAULT_DATA = 0;

    private ColumnChartView chart;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = true;
    private boolean hasLabelForSelected = true;
    private int dataType = DEFAULT_DATA;


    int MAX_PAGE=3;
    Fragment cur_fragment=new Fragment();

    public MonthStatFragment() {
    }

    // view pager에서 WeekStatFragment 생성할때 씀, 파라미터를 저장해줌
    public static MonthStatFragment newInstance(String date) {
        MonthStatFragment fragment = new MonthStatFragment();
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

        //해당 월의 마지막날 구하기
        lastDay = getLastDayOfMonth(date);

        setMonthRange(rootView);   //'-월-일 ~ -월-일' 텍스트뷰 변경
        generateStackedData(db); //차트 생성
        setAverageTime(rootView, db); //이주의 평균시간들 텍스트뷰 변경

        return rootView;
    }

    //해당 월의 마지막날 구하기
    public static String getLastDayOfMonth(String date) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date converted = cal.getTime();
        return dateFormat.format(converted);
    }

    //'-월' 텍스트 변경
    public void setMonthRange(View rootView){
        TextView monthRange = rootView.findViewById(R.id.range);
        monthRange.setText(date.substring(5,7)+"월");
    }

    private void generateStackedData(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select * from tb_dailybalance where date between '"+date+"' and '"+lastDay+"'",null);

        dayNum = Integer.parseInt(lastDay.substring(8,10));
        // Column can have many stacked subcolumns, here I use 4 stack subcolumn in each of 4 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();    //'1~30(31)' 표시 그래프 밑에 뜨게
        for(int i=0; i<dayNum; i++){
            axisValues.add(i, new AxisValue(i).setLabel(Integer.toString(i+1)));
            columns.add(null);
        }
        System.out.println("★getcount:"+cursor.getCount());
        for (int i=0; i<dayNum; i++) {
            values = new ArrayList<SubcolumnValue>();
            if (cursor.moveToNext()){
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("sleep")), getResources().getColor(R.color.sleep)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("work")), getResources().getColor(R.color.work)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("study")), getResources().getColor(R.color.study)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("exercise")), getResources().getColor(R.color.exercise)));
                    values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("leisure")), getResources().getColor(R.color.leisure)));
                    //values.add(new SubcolumnValue(cursor.getFloat(cursor.getColumnIndex("other")), getResources().getColor(R.color.lightGray)));

            }
            Column column = new Column(values);
            columns.set(i,column);
            System.out.println("☆☆☆☆☆testing "+i);
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
        Cursor cursor = db.rawQuery("select * from tb_dailybalance where date between '"+date+"' and '"+lastDay+"'",null);

        TextView sleepAverageValue = rootView.findViewById(R.id.sleepAverageValue);
        TextView workAverageValue = rootView.findViewById(R.id.workAverageValue);
        TextView studyAverageValue = rootView.findViewById(R.id.studyAverageValue);
        TextView exerciseAverageValue = rootView.findViewById(R.id.exerciseAverageValue);
        TextView leisureAverageValue = rootView.findViewById(R.id.leisureAverageValue);
        TextView othersAverageValue = rootView.findViewById(R.id.othersAverageValue);
        float sleep=0,work=0,study=0,exercise=0,leisure=0,others=0;
        while(cursor.moveToNext()){
            sleep += cursor.getFloat(cursor.getColumnIndex("sleep"));
            work += cursor.getFloat(cursor.getColumnIndex("work"));
            study += cursor.getFloat(cursor.getColumnIndex("study"));
            exercise += cursor.getFloat(cursor.getColumnIndex("exercise"));
            leisure += cursor.getFloat(cursor.getColumnIndex("leisure"));
            others += cursor.getFloat(cursor.getColumnIndex("other"));
        }
        sleepAverageValue.setText(getTime(sleep/dayNum));
        workAverageValue.setText(getTime(work/dayNum));
        studyAverageValue.setText(getTime(study/dayNum));
        exerciseAverageValue.setText(getTime(exercise/dayNum));
        leisureAverageValue.setText(getTime(leisure/dayNum));
        othersAverageValue.setText(getTime(others/dayNum));
    }

    public String getTime(Float time){
        float min = time%1;
        float hour = time-min;
        String res= (int)hour+"시간 "+(int)(min*60)+"분";

        return res;
    }
}
