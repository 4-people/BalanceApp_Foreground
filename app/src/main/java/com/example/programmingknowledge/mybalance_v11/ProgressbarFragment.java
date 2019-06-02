package com.example.programmingknowledge.mybalance_v11;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.programmingknowledge.mybalance_v11.HomeFragment.getWeek;

public class ProgressbarFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    FragmentManager fm;
    FragmentTransaction tran;
    HomeFragment frag2;

    View view;
    ImageView imageView;
    TextView dateNow;

    TextView textView8, textView9, textView10, textView11, textView12;

    public static ProgressbarFragment newInstance(String param1) {
        ProgressbarFragment fragment = new ProgressbarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, Bundle savedInstanceState) {
        final DBHelper helper = new DBHelper(container.getContext());
        view = inflater.inflate(R.layout.fragment_progressbar, container, false);

        //tb_dailybalance 설정
        setProgressbar(view, helper);

        //위에 날짜 띄우기
        dateNow = (TextView)view.findViewById(R.id.today);
        String date = mParam1.substring(5);
        if (date.equals(new SimpleDateFormat("MM/dd").format(new Date())))
            dateNow.setText("오늘");
        else
            dateNow.setText(date);


        final RoundCornerProgressBar progress1 = (RoundCornerProgressBar) view.findViewById(R.id.progressBar1);
        progress1.setProgressColor(Color.parseColor("#A481D6"));
        progress1.setProgressBackgroundColor(getResources().getColor(R.color.sleep));
        progress1.setMax(100);
        //progress1.setProgress(75);

        int progressColor1 = progress1.getProgressColor();
        int backgroundColor1 = progress1.getProgressBackgroundColor();

        final RoundCornerProgressBar progress2 = (RoundCornerProgressBar) view.findViewById(R.id.progressBar2);
        progress2.setProgressColor(Color.parseColor("#E384BD"));
        progress2.setProgressBackgroundColor(getResources().getColor(R.color.work));
        progress2.setMax(100);


        int progressColor2 = progress2.getProgressColor();
        int backgroundColor2 = progress2.getProgressBackgroundColor();

        final RoundCornerProgressBar progress3 = (RoundCornerProgressBar) view.findViewById(R.id.progressBar3);
        progress3.setProgressColor(Color.parseColor("#F7889E"));
        progress3.setProgressBackgroundColor(getResources().getColor(R.color.study));
        progress3.setMax(100);


        int progressColor3 = progress3.getProgressColor();
        int backgroundColor3 = progress3.getProgressBackgroundColor();


        final RoundCornerProgressBar progress4 = (RoundCornerProgressBar) view.findViewById(R.id.progressBar4);
        progress4.setProgressColor(Color.parseColor("#FFA582"));
        progress4.setProgressBackgroundColor(getResources().getColor(R.color.exercise));
        progress4.setMax(100);


        int progressColor4 = progress4.getProgressColor();
        int backgroundColor4 = progress4.getProgressBackgroundColor();


        final RoundCornerProgressBar progress5 = (RoundCornerProgressBar) view.findViewById(R.id.progressBar5);
        progress5.setProgressColor(Color.parseColor("#FEE687"));
        progress5.setProgressBackgroundColor(getResources().getColor(R.color.leisure));
        progress5.setMax(100);


        int progressColor5 = progress5.getProgressColor();
        int backgroundColor5 = progress5.getProgressBackgroundColor();

        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor2 = db.rawQuery("select * from tb_dailybalance where date=? ", new String[]{mParam1});
        String week = getWeek(mParam1);
        Cursor cursor1 = db.rawQuery("select * from tb_goalbalance where week Like" + " \"%" + week + "%\"", null);

        float[] goal = new float[5];
        float[] measured = new float[5];
        float[] result = new float[5];

        if (cursor1.getCount() == 0) {
            showMessage("경고", "모든 요일의 밸런스를 설정해주세요!");
        }
        while (cursor1.moveToNext()) {
            goal[0] = cursor1.getFloat(cursor1.getColumnIndex("sleep"));
            goal[1] = cursor1.getFloat(cursor1.getColumnIndex("work"));
            goal[2] = cursor1.getFloat(cursor1.getColumnIndex("study"));
            goal[3] = cursor1.getFloat(cursor1.getColumnIndex("exercise"));
            goal[4] = cursor1.getFloat(cursor1.getColumnIndex("leisure"));

            //나누는값이 0 이 안되게
            for (int i = 0; i < 4; i++) {
                if (goal[i] == 0) {
                    goal[i] = 1;
                }
            }
        }

        while (cursor2.moveToNext()) {
            measured[0] = cursor2.getFloat(cursor2.getColumnIndex("sleep"));
            measured[1] = cursor2.getFloat(cursor2.getColumnIndex("work"));
            measured[2] = cursor2.getFloat(cursor2.getColumnIndex("study"));
            measured[3] = cursor2.getFloat(cursor2.getColumnIndex("exercise"));
            measured[4] = cursor2.getFloat(cursor2.getColumnIndex("leisure"));
        }

        db.close();

        result[0] = measured[0] / goal[0] * 100;
        result[1] = measured[1] / goal[1] * 100;
        result[2] = measured[2] / goal[2] * 100;
        result[3] = measured[3] / goal[3] * 100;
        result[4] = measured[4] / goal[4] * 100;

        //추천활동 정하기
        float min = 100;
        float max = 100;
        int recommend_min = 100;
        int recommend_max=0;
        //recommend는 0~4 를 통해 카테고리 알려줌

        for(int i=0;i<result.length;i++) {
            if(max<result[i]) {
                //max의 값보다 array[i]이 크면 max = array[i]
                max = result[i];
                recommend_max = i;
            }

            if(min>result[i]) {
                //min의 값보다 array[i]이 작으면 min = array[i]
                min = result[i];
                recommend_min = i;
            }


            //추천활동이 카테고리별 과잉(너무 많이 함)에 대한 결과일 경우 +5를 해준다.(setRecommend로 넘기기 쉽게)
            if(max-100 > 100-min)
                recommend_min = recommend_max+5;



        }
        setRecommend(recommend_min);


        textView8 = (TextView) view.findViewById(R.id.textView8);
        textView8.setText("" + Math.round(result[0]) + "%");
        textView9 = (TextView) view.findViewById(R.id.textView9);
        textView9.setText("" + Math.round(result[1]) + "%");
        textView10 = (TextView) view.findViewById(R.id.textView10);
        textView10.setText("" + Math.round(result[2]) + "%");
        textView11 = (TextView) view.findViewById(R.id.textView11);
        textView11.setText("" + Math.round(result[3]) + "%");
        textView12 = (TextView) view.findViewById(R.id.textView12);
        textView12.setText("" + Math.round(result[4]) + "%");

        progress1.setProgress(result[0]);
        progress2.setProgress(result[1]);
        progress3.setProgress(result[2]);
        progress4.setProgress(result[3]);
        progress5.setProgress(result[4]);

        return view;
    }

    private void setRecommend(int recommend) {
        imageView = (ImageView)view.findViewById(R.id.imageView);
        TextView textView2 = (TextView)view.findViewById(R.id.recommend);

        switch (recommend){
            case 0:
                //수면 부족
                textView2.setText("수면이 부족한");
                imageView.setImageResource(R.drawable.sleep_l);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=숙면+도움&profileId="));
                        startActivity(intent);
                    }
                });
                break;

            case 1:
                //일 부족
                textView2.setText("업무가 부족한");
                imageView.setImageResource(R.drawable.work_l);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=업무+효율&profileId="));
                        startActivity(intent);
                    }
                });

                break;
            case 2:
                //공부 부족
                textView2.setText("공부가 부족한");
                imageView.setImageResource(R.drawable.study_l);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=공부+집중&profileId="));
                        startActivity(intent);
                    }
                });

                break;
            case 3:
                //운동 부족
                textView2.setText("운동이 부족한");
                imageView.setImageResource(R.drawable.exercise_l);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=운동+장점&profileId="));
                        startActivity(intent);
                    }
                });

                break;
            case 4:
                //여가 부족
                textView2.setText("여가가 부족한");
                imageView.setImageResource(R.drawable.leisure_l);


                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=휴식+도움&profileId="));
                        startActivity(intent);
                    }
                });
                break;

            case 5:
                //잠 많음
                textView2.setText("수면이 많은");
                imageView.setImageResource(R.drawable.sleep_m);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=잠+중독&profileId="));
                        startActivity(intent);
                    }
                });

                break;
            case 6:
                //업무 많음
                textView2.setText("업무가 많은");
                imageView.setImageResource(R.drawable.work_m);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=업무+도움&profileId="));
                        startActivity(intent);
                    }
                });
                break;
            case 7:
                //공부 많음
                textView2.setText("공부가 많은");
                imageView.setImageResource(R.drawable.study_m);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=공부+효율적&profileId="));
                        startActivity(intent);
                    }
                });
                break;
            case 8:
                //운동 많음
                textView2.setText("운동이 많은");
                imageView.setImageResource(R.drawable.exercise_m);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=운동+중독&profileId="));
                        startActivity(intent);
                    }
                });
                break;

            case 9:
                //여가 많음
                textView2.setText("여가가 많은");
                imageView.setImageResource(R.drawable.leisure_m);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent (Intent.ACTION_VIEW, Uri.parse("https://brunch.co.kr/search?q=계획적+생활&profileId="));
                        startActivity(intent);
                    }
                });
                break;
        }
    }


    public void setFrag(int n) {
        fm = getFragmentManager();
        tran = fm.beginTransaction();
        switch (n) {
            case 1:
                tran.replace(R.id.fragment_container, frag2);
                tran.commit();
                break;
            //main_frame자리에 현재 frame이름
        }
    }

    /*
    public void setDailybalance(View v, DBHelper helper) {

        //db 추가하기
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-14','토',0,1,2,4,8,0,'잠 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-15','일',10,1,2,4,8,0,'운동 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-16','월',8,8,3,2,2,1,'균형')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-17','화',6,6,6,6,0,0,'여가 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-18','수',0,0,0,0,0,24,'잠 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-19','목',1,4,1,4,1,13,'잠 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-20','금',2,1,2,2,8,9,'운동 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-21','토',4,4,4,4,4,4,'운동 부족')");
        db.execSQL("insert into tb_dailybalance (date,week,sleep, work, study, exercise, leisure, other, recommend) values ('2019-05-22','일',10,3,3,3,3,2,'일 부족')");
        db.close();
    }*/

    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    //tb_dailybalance 설정
    private void setProgressbar(View v, DBHelper helper) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        double sleep = 0, work = 0, study = 0, exercise = 0, leisure = 0, other = 0;
        String week = getWeek(mParam1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_dailybalance where date=?", new String[]{mParam1});
        if(cursor.getCount()==0) {
            db.execSQL("insert into tb_dailybalance (date, week, sleep, work, study, exercise, leisure, other) values (?,?,0,0,0,0,0,0)",
                    new String[]{mParam1, week});
        }
        Cursor cursor2 = db.rawQuery("select * from tb_timeline where date=?", new String[]{mParam1});
        while(cursor2.moveToNext()) {
            String category = cursor2.getString(cursor2.getColumnIndex("category"));
            String starttime =  cursor2.getString(cursor2.getColumnIndex("starttime"));
            String endtime = cursor2.getString(cursor2.getColumnIndex("endtime"));
            if (endtime == null) {
                try {
                    if (format.parse(starttime).getTime() > format.parse(format.format(new Date())).getTime())
                        endtime = "24:00:00";
                    else
                        endtime = format.format(new Date());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            double playtime = getTime(starttime, endtime);
            switch (category) {
                case "sleep":
                    sleep += playtime;
                    break ;
                case "work":
                    work += playtime;
                    break ;
                case "study":
                    study += playtime;
                    break ;
                case "exercise":
                    exercise += playtime;
                    break ;
                case "leisure":
                    leisure += playtime;
                    break ;
                default:
                    other += playtime;
            }
        }
        String sql = "update tb_dailybalance set sleep=" + sleep + ", work=" + work + ", study=" + study
                + ", exercise=" + exercise + ", leisure=" + leisure + ", other=" + other + " where date=\"" + mParam1 + "\"";
        db.execSQL(sql);
        db.close();
    }


    // 활동시간 계산
    private double getTime(String starttime, String endtime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date start = null;
        Date end = null;

        try {
            start = format.parse(starttime);
            end = format.parse(endtime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double time = (end.getTime() - start.getTime()) / 1000;
        time = Math.round(time/3600*100)/100.0;

        return time;
    }


}
