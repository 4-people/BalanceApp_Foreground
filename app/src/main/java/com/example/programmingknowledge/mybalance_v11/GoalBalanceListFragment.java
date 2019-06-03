package com.example.programmingknowledge.mybalance_v11;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.content.DialogInterface;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.app.AlertDialog;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout;
        import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import lecho.lib.hellocharts.model.Line;


public class GoalBalanceListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_goal_balance_list, container, false);
        LinearLayout layout = (LinearLayout)root.findViewById(R.id.linear);

        //db읽기
        DBHelper helper = new DBHelper(container.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select rest, work, study, exercise, leisure, other, week from tb_goalbalance", null);


        //DB에 데이터가 있으면
        if(cursor.getColumnCount()>0){
            //카드뷰 생성
            while(cursor.moveToNext()) {
                View goalBalanceCardView = inflater.inflate(R.layout.cardview_goalbalance, container, false);

                //요일 표시 텍스트
                TextView week = (TextView)goalBalanceCardView.findViewById(R.id.week);
                week.setText(cursor.getString(6));
                goalBalanceCardView.setId(cursor.getColumnIndex("id"));

                //목표 밸런스 설정창으로 이동(카드뷰 눌렀을때)
                goalBalanceCardView.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        Fragment fragment = new SettingGoalBalanceFragment();

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace( R.id.fragment_container, fragment );
                        fragmentTransaction.commit();
                    }
                });

                //카드뷰 오래 누르면 카드뷰 삭제
                // AlertDialog 빌더를 이용해 종료시 발생시킬 창을 띄운다
                final AlertDialog.Builder alBuilder = new AlertDialog.Builder(container.getContext());
                goalBalanceCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(final View view) {
                        alBuilder.setMessage("삭제하시겠습니까?");

                        // "예" 버튼을 누르면 실행되는 리스너
                        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //카드뷰 삭제
                                LinearLayout linear = (LinearLayout) root.findViewById(R.id.linear);
                                linear.removeView(view);
                                //db삭제 구현하기(아래)
                                DBHelper helper = new DBHelper(getContext());
                                SQLiteDatabase db = helper.getWritableDatabase();
                                TextView week = (TextView)view.findViewById(R.id.week);
                                String weekStr = week.getText().toString();
                                //db.delete("tb_goalbalance","week=?",new String[]{String.valueOf(view.findViewById(R.id.week))});
                                db.execSQL("DELETE FROM tb_goalbalance WHERE week = '"+weekStr+"';");
                            }
                        });
                        // "아니오" 버튼을 누르면 실행되는 리스너
                        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return; // 아무런 작업도 하지 않고 돌아간다
                            }
                        });
                        alBuilder.setTitle("목표 밸런스 삭제");
                        alBuilder.show(); // AlertDialog.Bulider로 만든 AlertDialog를 보여준다.

                        return false;
                    }

                });

                layout.addView(goalBalanceCardView);

                //게이지바 설정
                View rest = (View)goalBalanceCardView.findViewById(R.id.rest);
                rest.setLayoutParams(new LinearLayout.LayoutParams((int)Double.parseDouble(cursor.getString(0))*40, ViewGroup.LayoutParams.MATCH_PARENT));
                View work = (View)goalBalanceCardView.findViewById(R.id.work);
                work.setLayoutParams(new LinearLayout.LayoutParams((int)Double.parseDouble(cursor.getString(1))*40, ViewGroup.LayoutParams.MATCH_PARENT));
                View study = (View)goalBalanceCardView.findViewById(R.id.study);
                study.setLayoutParams(new LinearLayout.LayoutParams((int)Double.parseDouble(cursor.getString(2))*40, ViewGroup.LayoutParams.MATCH_PARENT));
                View exercise = (View)goalBalanceCardView.findViewById(R.id.exercise);
                exercise.setLayoutParams(new LinearLayout.LayoutParams((int)Double.parseDouble(cursor.getString(3))*40, ViewGroup.LayoutParams.MATCH_PARENT));
                View leisure = (View)goalBalanceCardView.findViewById(R.id.leisure);
                leisure.setLayoutParams(new LinearLayout.LayoutParams((int)Double.parseDouble(cursor.getString(4))*40, ViewGroup.LayoutParams.MATCH_PARENT));


                //아랫줄 한줄 저거뭐지
                /*CardView.MarginLayoutParams margin = new CardView.MarginLayoutParams(card.getLayoutParams());
                margin.setMargins(30,30,30,0);
                CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);

                card.setLayoutParams(new LinearLayout.LayoutParams(params));
                card.setLayoutParams(new LinearLayout.LayoutParams(margin));*/

            }
        }

        Cursor cursor2 = db.rawQuery("select week from tb_goalbalance", null);
        ArrayList<String> yo = new ArrayList<>();
        while(cursor2.moveToNext()) {
            String week = cursor2.getString(cursor2.getColumnIndex("week"));
            String[] temp = week.split("\\s");
            for (String s : temp) {
                yo.add(s);
            }
        }
        String temp = "";
        for (String s : yo) {
            temp += s;
        }

        //목표 밸런스 설정창으로 이동(액션버튼 눌렀을때)
        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.AddGoalBalance);

        // 모두 설정되어있으면 버튼 비활성화
        if (temp.contains("월") && temp.contains("화") && temp.contains("수") && temp.contains("목") &&
                temp.contains("금") && temp.contains("토") && temp.contains("일"))
            fab.hide();

        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Fragment fragment = new SettingGoalBalanceFragment();

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace( R.id.fragment_container, fragment );
                fragmentTransaction.commit();
            }
        });

        //카드뷰 오래 누르면 카드뷰 삭제
        /*CardView cardview = root.findViewById(R.id.cardView01);
        // AlertDialog 빌더를 이용해 종료시 발생시킬 창을 띄운다
        final AlertDialog.Builder alBuilder = new AlertDialog.Builder(container.getContext());
        cardview.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(final View view) {
                alBuilder.setMessage("삭제하시겠습니까?");

                // "예" 버튼을 누르면 실행되는 리스너
                alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //LinearLayout relative = (LinearLayout) root.findViewById(R.id.relative);
                        //relative.removeView(view);
                        //db삭제 구현하기(아래)

                    }
                });
                // "아니오" 버튼을 누르면 실행되는 리스너
                alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return; // 아무런 작업도 하지 않고 돌아간다
                    }
                });
                alBuilder.setTitle("목표 밸런스 삭제");
                alBuilder.show(); // AlertDialog.Bulider로 만든 AlertDialog를 보여준다.

                return false;
            }

        });*/

        // Inflate the layout for this fragment
        return root;
    }

}