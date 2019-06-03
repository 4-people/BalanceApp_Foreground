package com.example.programmingknowledge.mybalance_v11;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.qap.ctimelineview.TimelineRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimelineFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    TextView dateNow;

    //Create Timeline Rows List
    private ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();
    ArrayAdapter<TimelineRow> myAdapter;

    public static TimelineFragment newInstance(String param1) {
        TimelineFragment fragment = new TimelineFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_home,container,false);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        final DBHelper helper = new DBHelper(container.getContext());

        //위에 날짜 띄우기
        dateNow = (TextView)view.findViewById(R.id.nowDate);
        String date = mParam1.substring(5);
        if (date.equals(new SimpleDateFormat("MM/dd").format(new Date())))
            dateNow.setText("오늘");
        else
            dateNow.setText(date);


        //db 데이터 불러오기
        putData(view, helper);

        // Create the Timeline Adapter
        myAdapter = new TimelineViewAdapter(getActivity(), 0, timelineRowsList,
                //if true, list will be sorted by date
                false);
        // Get the ListView and Bind it with the Timeline Adapter
        ListView myListView = (ListView) view.findViewById(R.id.timeline_listView);
        myListView.setAdapter(myAdapter);

        //길게 눌렀을때 수정/삭제
        String[] s = { "휴식", "일", "공부", "운동", "여가", "기타"};
        final ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, s);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final AlertDialog.Builder alBuilder = new AlertDialog.Builder(container.getContext());
        AdapterView.OnItemLongClickListener adapterListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                final TimelineRow row = timelineRowsList.get(position);
                final SimpleDateFormat transFormat = new SimpleDateFormat("HH:mm:ss");
                final EditText place = new EditText(getActivity());
                final TextView sPlace = new TextView(getActivity());
                final TextView sCategory = new TextView(getActivity());
                final Spinner category = new Spinner(getActivity());

                LinearLayout.LayoutParams params2 = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params2.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params2.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                category.setLayoutParams(params2);
                category.setAdapter(adp);
                place.setSingleLine(true);
                place.setText(row.getDescription());
                place.setHint("수정할 장소를 입력해주세요");
                sPlace.setText("장소");
                sCategory.setText("카테고리");

                LinearLayout container = new LinearLayout(getContext());
                LinearLayout.LayoutParams params = new  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                place.setLayoutParams(params);
                sCategory.setLayoutParams(params);
                sPlace.setLayoutParams(params);
                container.setOrientation(LinearLayout.VERTICAL);
                container.addView(sPlace);
                container.addView(place);
                container.addView(sCategory);
                container.addView(category);


                // "취소" 버튼을 누르면 실행되는 리스너
                alBuilder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return; // 아무런 작업도 하지 않고 돌아간다
                    }
                });

                // "삭제" 버튼을 누르면 실행되는 리스너
                alBuilder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timelineRowsList.remove(position);
                        String starttime = transFormat.format(row.getDate());
                        DBHelper helper = new DBHelper(getContext());
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("DELETE FROM tb_timeline WHERE starttime = \"" + starttime + "\"");
                        db.close();
                        myAdapter.notifyDataSetChanged();
                    }
                });

                // "수정" 버튼을 누르면 실행되는 리스너
                alBuilder.setNegativeButton("수정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sPlace = place.getText().toString();
                        String starttime = transFormat.format(row.getDate());
                        String sCategory = category.getSelectedItem().toString();
                        switch (sCategory) {
                            case "휴식":
                                sCategory = "rest";
                                break;
                            case "일":
                                sCategory = "work";
                                break;
                            case "공부":
                                sCategory = "study";
                                break;
                            case "운동":
                                sCategory = "exercise";
                                break;
                            case "여가":
                                sCategory = "leisure";
                                break;
                            default:
                                sCategory = "other";
                        }

                        DBHelper helper = new DBHelper(getContext());
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("UPDATE tb_timeline SET place=\"" + sPlace + "\", category="
                                + "\"" + sCategory + "\" WHERE starttime = \"" + starttime + "\"");
                        db.close();
                        putData(view, helper);
                        myAdapter.notifyDataSetChanged();
                    }
                });

                alBuilder.setMessage("장소/카테고리 수정");
                alBuilder.setTitle("타임라인 수정/삭제");
                alBuilder.setView(container);
                alBuilder.show(); // AlertDialog.Bulider로 만든 AlertDialog를 보여준다.

                return false;
            }
        };
        myListView.setOnItemLongClickListener(adapterListener);

        return view;
    }

    //timeline 리스트 만들기
    private TimelineRow createTimelineRow(int id, String place, String category, String starttime) {
        //카테고리 이미지 분류
        int categoryNum;
        int color;
        String cg;
        switch (category) {
            case "rest":
                categoryNum = 0;
                color = getResources().getColor(R.color.rest);
                cg = "휴식";
                break;
            case "work":
                categoryNum = 1;
                color = getResources().getColor(R.color.work);
                cg = "일";
                break;
            case "study":
                categoryNum = 2;
                color = getResources().getColor(R.color.study);
                cg = "공부";
                break;
            case "exercise":
                categoryNum = 3;
                color = getResources().getColor(R.color.exercise);
                cg = "운동";
                break;
            case "leisure":
                categoryNum = 4;
                color = getResources().getColor(R.color.leisure);
                cg = "여가";
                break;
            default:
                categoryNum = 5;
                color = getResources().getColor(R.color.others);
                cg = "기타";
        }

        //날짜 String -> Date
        SimpleDateFormat sdfformat = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try {
            date = sdfformat.parse(starttime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create new timeline row (pass your Id)
        TimelineRow myRow = new TimelineRow(id);

        // 날짜설정
        myRow.setDate(date);
        // 카테고리 설정
        myRow.setTitle(cg);
        // 장소 설정
        myRow.setDescription(place);
        // 이미지 설정
        myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.category_0 + categoryNum));
        // To set row Below Line Color (optional)
        myRow.setBellowLineColor(color);
        // To set row Below Line Size in dp (optional)
        myRow.setBellowLineSize(6);
        // To set row Image Size in dp (optional)
        myRow.setImageSize(30);
        // To set background color of the row image (optional)
        myRow.setBackgroundColor(color);
        // To set the Background Size of the row image in dp (optional)
        myRow.setBackgroundSize(60);
        // To set row Date text color (optional)
        myRow.setDateColor(Color.argb(255, 0, 0, 0));
        // To set row Title text color (optional)
        myRow.setTitleColor(Color.argb(255, 0, 0, 0));
        // To set row Description text color (optional)
        myRow.setDescriptionColor(Color.argb(255, 0, 0, 0));

        return myRow;
    }

    //DB에서 데이터 불러오기
    private void putData(View v, DBHelper helper) {
        timelineRowsList.clear();
        String date = mParam1;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_timeline where date=?",
                new String[]{date});
        int i = 0;

        if(cursor.getCount()==0) return;

        while(cursor.moveToNext()) {
            String place = cursor.getString(cursor.getColumnIndex("place"));
            String category = cursor.getString(cursor.getColumnIndex("category"));
            String starttime = cursor.getString(cursor.getColumnIndex("starttime"));

            timelineRowsList.add(createTimelineRow(i, place, category, starttime));
            i++;
        }


        db.close();
    }

}
