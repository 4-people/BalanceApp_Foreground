package com.example.programmingknowledge.mybalance_v11;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private ViewPager mViewPager;
    FragmentManager fm;
    FragmentTransaction tran;
    HomeFragment_p frag1;
    Button button;
    Button button2;
    int page;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        final DBHelper helper = new DBHelper(container.getContext());

        //db insert 버튼
        button = (Button)view.findViewById(R.id.insert);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData(v, helper);
            }
        });

        //페이지 갯수 세기
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(distinct date) from tb_timeline",null);
        cursor.moveToFirst();
        page = cursor.getInt(0);
        db.close();

        mViewPager = (ViewPager)view.findViewById(R.id.pager);
        HomeFragment.MyPagerAdapter adapter = new HomeFragment.MyPagerAdapter(getChildFragmentManager(), page);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(page - 1);

        //progressbar로 전환
        button2 = (Button)view.findViewById(R.id.button);
        frag1 = new HomeFragment_p();
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrag(0);
            }
        });

        return view;
    }

    //DB에 데이터 넣기
    private void setData(View v, DBHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        //db.execSQL("delete from tb_timeline");
        db.execSQL("insert into tb_timeline (date, place, category, starttime, endtime) values (?,?,?,?,?)",
                new String[]{"2019/06/02", "한강동아아파트", "sleep", "01:15:16", "06:18:26"});
        db.execSQL("insert into tb_timeline (date, place, category, starttime, endtime) values (?,?,?,?,?)",
                new String[]{"2019/06/02", "경기대학교", "study", "06:18:26", "11:55:12"});
        db.execSQL("insert into tb_timeline (date, place, category, starttime, endtime) values (?,?,?,?,?)",
                new String[]{"2019/06/02", "헬스장", "exercise", "11:55:12", "18:46:33"});
        db.execSQL("insert into tb_timeline (date, place, category, starttime, endtime) values (?,?,?,?,?)",
                new String[]{"2019/06/02", "집", "work", "18:46:33", "19:50:33"});
        db.execSQL("insert into tb_timeline (date, place, category, starttime, endtime) values (?,?,?,?,?)",
                new String[]{"2019/06/02", "허허", "leisure", "19:50:33", "20:57:43"});

        Cursor cursor = db.rawQuery("select distinct date from tb_timeline", null);
        while(cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Cursor cursor2 = db.rawQuery("select * from tb_dailybalance where date=?",  new String[]{date});
            //특정 날짜의 row가 없으면 추가
            if(cursor2.getCount()==0) {
                db.execSQL("insert into tb_dailybalance (date, week, sleep, work, study, exercise, leisure, other) values (?,?,0,0,0,0,0,0)",
                        new String[]{date, getWeek(date)});
            }
        }

        db.close();
    }

    public void setFrag(int n){
        fm = getFragmentManager();
        tran = fm.beginTransaction();
        switch (n){
            case 0:
                tran.replace(R.id.fragment_container, frag1);
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
            Date date = new Date();
            SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy/MM/dd");
            String formatDate = sdfdate.format(date);

            for (int i = 0; i < page; i++) {
                if (position == i) {

                    try {
                        formatDate = subDate(formatDate, (i+1-page));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    frag = TimelineFragment.newInstance(formatDate);
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

    //날짜 빼는 메소드
    public static String subDate(String dt, int d) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        Calendar cal = Calendar.getInstance();
        Date date = format.parse(dt);
        cal.setTime(date);
        cal.add(Calendar.DATE, d);

        return format.format(cal.getTime());
    }

    //날짜로 요일얻기
    public static String getWeek(String date) {
        String day = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd") ;
        Date nDate = null;
        try {
            nDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;
        switch(dayNum){
            case 1:
                day = "일";
                break ;
            case 2:
                day = "월";
                break ;
            case 3:
                day = "화";
                break ;
            case 4:
                day = "수";
                break ;
            case 5:
                day = "목";
                break ;
            case 6:
                day = "금";
                break ;
            case 7:
                day = "토";
                break ;
        }
        return day;
    }
}

