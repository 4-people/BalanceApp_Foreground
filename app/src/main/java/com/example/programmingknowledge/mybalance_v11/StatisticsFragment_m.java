package com.example.programmingknowledge.mybalance_v11;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.programmingknowledge.mybalance_v11.HomeFragment.subDate;

public class StatisticsFragment_m extends Fragment {

    private ViewPager mViewPager;
    FragmentManager fm;
    FragmentTransaction tran;
    StatisticsFragment statfrag;
    Button weekButton;
    Button monthButton;
    int page = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        final DBHelper helper = new DBHelper(container.getContext());

        /*
        //페이지 갯수 세기
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(distinct date) from tb_timeline",null);
        cursor.moveToFirst();
        page = cursor.getInt(0);
        db.close();*/
        ////////////////////////일단 위에 변수로 page수 4개로 고정해놨음

        mViewPager = (ViewPager)view.findViewById(R.id.pager_week);
        StatisticsFragment_m.MyPagerAdapter adapter = new StatisticsFragment_m.MyPagerAdapter(getChildFragmentManager(), page);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(page - 1);

        //버튼 전환
        monthButton = (Button)view.findViewById(R.id.monthButton);
        monthButton.setPressed(true);

        //주별로 전환
        weekButton = (Button)view.findViewById(R.id.weekButton);
        weekButton.clearFocus();
        statfrag = new StatisticsFragment();
        weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrag(0);
            }
        });

        return view;
    }

    public void setFrag(int n){
        fm = getFragmentManager();
        tran = fm.beginTransaction();
        switch (n){
            case 0:
                tran.replace(R.id.fragment_container, statfrag);
                tran.commit();
                break;
            //main_frame자리에 현재 frame이름
        }
    }

    //히스토리 pagerAdapter
    private  static class MyPagerAdapter extends FragmentStatePagerAdapter {
        int page;

        public MyPagerAdapter(FragmentManager fm, int n) {
            super(fm);
            this.page = n;
        }

        //타임라인 히스토리 페이지 설정
        @Override
        public Fragment getItem(int position) {
            Fragment frag;
            //Date date = new Date();
            SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy/MM/dd");
            //String formatDate = sdfdate.format(date);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH,1);
            Date cur1stDay = cal.getTime();


            for (int i = 0; i < page; i++) {
                if (position == i) {
                    Calendar cal2 = Calendar.getInstance();
                    cal.setTime(cur1stDay);
                    cal.add ( cal.MONTH, (i + 1 - page) ); //i개월 전....드디어 성공...
                    cur1stDay = cal.getTime();

                    String cur1stDayStr = sdfdate.format(cur1stDay);
                    frag = MonthStatFragment.newInstance(cur1stDayStr);


                    return frag;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return page;
        }
    }

}

