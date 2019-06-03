package com.example.programmingknowledge.mybalance_v11;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;


public class SettingGoalBalanceFragment extends Fragment implements View.OnClickListener {
    TextView sleepCountTv, workCountTv, studyCountTv, exerciseCountTv, leisureCountTv, othersCountTv;
    double sleepCount, workCount, studyCount, exerciseCount, leisureCount, others;
    Button sleepMinus, workMinus, studyMinus, exerciseMinus, leisureMinus;
    Button sleepPlus, workPlus, studyPlus, exercisePlus, leisurePlus;
    View sleepView, workView, studyView, exerciseView, leisureView;
    CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    ArrayList<CheckBox> checkBoxArrayList = new ArrayList<CheckBox>();
    String checkedWeek = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final DBHelper helper = new DBHelper(container.getContext());

        View root = inflater.inflate(R.layout.fragment_setting_goal_balance, container, false);

        InitializeView(root, helper);

        sleepMinus.setOnClickListener(this);
        sleepPlus.setOnClickListener(this);
        workMinus.setOnClickListener(this);
        workPlus.setOnClickListener(this);
        studyMinus.setOnClickListener(this);
        studyPlus.setOnClickListener(this);
        exerciseMinus.setOnClickListener(this);
        exercisePlus.setOnClickListener(this);
        leisureMinus.setOnClickListener(this);
        leisurePlus.setOnClickListener(this);

        //밸런스 확인
        TextView tv = (TextView) root.findViewById(R.id.view);
        tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setBalance(v, helper);
            }
        });


        // Inflate the layout for this fragment
        return root;
    }

    //plus or minus
    @Override
    public void onClick(View v) {
        MyOnClick(v);

        //버튼 누르고있으면 반복 실행되게 하는 repeat메소드
        this.repeat(sleepMinus);
        this.repeat(sleepPlus);
        this.repeat(workMinus);
        this.repeat(workPlus);
        this.repeat(studyMinus);
        this.repeat(studyPlus);
        this.repeat(exerciseMinus);
        this.repeat(exercisePlus);
        this.repeat(leisureMinus);
        this.repeat(leisurePlus);
    }


    public void InitializeView(View root, DBHelper helper) {
        sleepCountTv = (TextView) root.findViewById(R.id.sleepCount);
        sleepCount = parseInt(sleepCountTv.getText().toString());
        sleepMinus = (Button) root.findViewById(R.id.sleepMinus);
        sleepPlus = (Button) root.findViewById(R.id.sleepPlus);
        sleepView = (View) root.findViewById(R.id.sleep);

        workCountTv = (TextView) root.findViewById(R.id.workCount);
        workCount = parseInt(workCountTv.getText().toString());
        workMinus = (Button) root.findViewById(R.id.workMinus);
        workPlus = (Button) root.findViewById(R.id.workPlus);
        workView = (View) root.findViewById(R.id.work);

        studyCountTv = (TextView) root.findViewById(R.id.studyCount);
        studyCount = parseInt(studyCountTv.getText().toString());
        studyMinus = (Button) root.findViewById(R.id.studyMinus);
        studyPlus = (Button) root.findViewById(R.id.studyPlus);
        studyView = (View) root.findViewById(R.id.study);

        exerciseCountTv = (TextView) root.findViewById(R.id.exerciseCount);
        exerciseCount = parseInt(exerciseCountTv.getText().toString());
        exerciseMinus = (Button) root.findViewById(R.id.exerciseMinus);
        exercisePlus = (Button) root.findViewById(R.id.exercisePlus);
        exerciseView = (View) root.findViewById(R.id.exercise);

        leisureCountTv = (TextView) root.findViewById(R.id.leisureCount);
        leisureCount = parseInt(leisureCountTv.getText().toString());
        leisureMinus = (Button) root.findViewById(R.id.leisureMinus);
        leisurePlus = (Button) root.findViewById(R.id.leisurePlus);
        leisureView = (View) root.findViewById(R.id.leisure);

        othersCountTv = (TextView) root.findViewById(R.id.othersCount);

        checkMon = (CheckBox) root.findViewById(R.id.CheckMon);
        checkTue = (CheckBox) root.findViewById(R.id.CheckTue);
        checkWed = (CheckBox) root.findViewById(R.id.CheckWed);
        checkThu = (CheckBox) root.findViewById(R.id.CheckThu);
        checkFri = (CheckBox) root.findViewById(R.id.CheckFri);
        checkSat = (CheckBox) root.findViewById(R.id.CheckSat);
        checkSun = (CheckBox) root.findViewById(R.id.CheckSun);


        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select week from tb_goalbalance", null);

        while(cursor.moveToNext()) {
            String week = cursor.getString(cursor.getColumnIndex("week"));
            String[] yo = week.split("\\s");
            for (String s : yo) {
                switch (s) {
                    case "월":
                        checkMon.setEnabled(false);
                        break;
                    case "화":
                        checkTue.setEnabled(false);
                        break;
                    case "수":
                        checkWed.setEnabled(false);
                        break;
                    case "목":
                        checkThu.setEnabled(false);
                        break;
                    case "금":
                        checkFri.setEnabled(false);
                        break;
                    case "토":
                        checkSat.setEnabled(false);
                        break;
                    case "일":
                        checkSun.setEnabled(false);
                        break;
                }
            }
        }

        checkBoxArrayList.add(checkMon);
        checkBoxArrayList.add(checkTue);
        checkBoxArrayList.add(checkWed);
        checkBoxArrayList.add(checkThu);
        checkBoxArrayList.add(checkFri);
        checkBoxArrayList.add(checkSat);
        checkBoxArrayList.add(checkSun);
    }


    public void MyOnClick(View view) {
        others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
        switch (view.getId()) {
            case R.id.sleepMinus:
                if (sleepCount == 0) break;
                sleepCount -= 0.5;
                sleepCountTv.setText("" + sleepCount);
                sleepView.getLayoutParams().width = (int) (sleepCount * 40);
                sleepView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
            case R.id.sleepPlus:
                if (others == 0) break;
                sleepCount += 0.5;
                sleepCountTv.setText("" + sleepCount);
                sleepView.getLayoutParams().width = (int) (sleepCount * 40);
                sleepView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;

            case R.id.workMinus:
                if (workCount == 0) break;
                workCount -= 0.5;
                workCountTv.setText("" + workCount);
                workView.getLayoutParams().width = (int) (workCount * 40);
                workView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
            case R.id.workPlus:
                if (others == 0) break;
                workCount += 0.5;
                workCountTv.setText("" + workCount);
                workView.getLayoutParams().width = (int) (workCount * 40);
                workView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;

            case R.id.studyMinus:
                if (studyCount == 0) break;
                studyCount -= 0.5;
                studyCountTv.setText("" + studyCount);
                studyView.getLayoutParams().width = (int) (studyCount * 40);
                studyView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
            case R.id.studyPlus:
                if (others == 0) break;
                studyCount += 0.5;
                studyCountTv.setText("" + studyCount);
                studyView.getLayoutParams().width = (int) (studyCount * 40);
                workView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;

            case R.id.exerciseMinus:
                if (exerciseCount == 0) break;
                exerciseCount -= 0.5;
                exerciseCountTv.setText("" + exerciseCount);
                exerciseView.getLayoutParams().width = (int) (exerciseCount * 40);
                exerciseView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
            case R.id.exercisePlus:
                if (others == 0) break;
                exerciseCount += 0.5;
                exerciseCountTv.setText("" + exerciseCount);
                exerciseView.getLayoutParams().width = (int) (exerciseCount * 40);
                workView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;

            case R.id.leisureMinus:
                if (leisureCount == 0) break;
                leisureCount -= 0.5;
                leisureCountTv.setText("" + leisureCount);
                leisureView.getLayoutParams().width = (int) (leisureCount * 40);
                leisureView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
            case R.id.leisurePlus:
                if (others == 0) break;
                leisureCount += 0.5;
                leisureCountTv.setText("" + leisureCount);
                leisureView.getLayoutParams().width = (int) (leisureCount * 40);
                workView.requestLayout();
                others = 24 - (sleepCount + workCount + studyCount + exerciseCount + leisureCount);
                othersCountTv.setText("" + others);
                break;
        }
    }


    public void setBalance(View v, DBHelper helper) {

        //LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View outerview = inflater.inflate(R.layout.content_balance_list, null);

        //Intent outerIntent = new Intent(this, BalanceListActivity.class);

        Fragment fragment = new GoalBalanceListFragment();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();


        //db 추가하기
        SQLiteDatabase db = helper.getWritableDatabase();
        for (int i = 0; i < checkBoxArrayList.size(); i++) {
            if (checkBoxArrayList.get(i).isChecked()) {
                checkedWeek += checkBoxArrayList.get(i).getText();
                checkedWeek += " ";
            }
        }
        db.execSQL("insert into tb_goalbalance (sleep, work, study, exercise, leisure, other, week) values (" +
                sleepCount + "," + workCount + "," + studyCount + "," + exerciseCount + "," + leisureCount + "," + others + ",'" + checkedWeek + "')");
        db.close();

        //onBackPressed();

//        CardView cardView = new CardView(outerIntent.this);
//      cardView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        cardView.setCardBackgroundColor(383838);

//        outerview.setContent
//        Intent intent=new Intent(MainActivity.this, SettingActivity.class);
//        startActivity(intent);

    }

    public void repeat(final Button button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 300);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    MyOnClick(button);
                    mHandler.postDelayed(this, 100);
                }
            };

        });
    }

}
