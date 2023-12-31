package org.techtown.lottoworld;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class SelfInputActivity extends AppCompatActivity {

    // Todo : local DB 에서 latest Round 를 가져온 후
    //  Self Input 에서 쓰게끔
    //  spinner에서 회차를 받아와야됨
    //  저장하기 누르면 db로 넘겨줘야 돼.
    //  reset 누르면 다 리셋.

    // Variable for Spinner UI
    int selected_Round;
    Button resetInputButton,myTicketListButton,saveNumbersButton;
    Spinner roundSpinner;
    ArrayList arrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;

    // Variable for Button Events
    Button[] inputButtons = new Button[47];
    boolean[] isButtonClicked = new boolean[47];
    int buttonActiveCnt = 0;
    TextView choosenBall1,choosenBall2,choosenBall3,choosenBall4,choosenBall5,choosenBall6;
    LinkedList<Integer> choosenBallList = new LinkedList<Integer>();

    // 임시변수
    int global_i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inputButtons = new Button[47];
        isButtonClicked = new boolean[47];
        choosenBallList = new LinkedList<Integer>();
        buttonActiveCnt = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_input);
        choosenBallList.clear();
        // View Matching
        resetInputButton = findViewById(R.id.resetInput);
        saveNumbersButton = findViewById(R.id.SaveTicketButton);
        myTicketListButton = findViewById(R.id.myTicketListButton);
        roundSpinner = findViewById(R.id.roundSpinner);
        choosenBall1 = findViewById(R.id.choosenBall1);
        choosenBall2 = findViewById(R.id.choosenBall2);
        choosenBall3 = findViewById(R.id.choosenBall3);
        choosenBall4 = findViewById(R.id.choosenBall4);
        choosenBall5 = findViewById(R.id.choosenBall5);
        choosenBall6 = findViewById(R.id.choosenBall6);


        adapterSetting();
        buttonSetting();
        resetInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                Toast.makeText(getApplicationContext(), "번호를 처음부터 다시 입력합니다", Toast.LENGTH_SHORT).show();
            }
        });
        saveNumbersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선택된 번호쿼리가 6개로 가득 차야함
                if(buttonActiveCnt == 6) {
                    int[] temp = new int[7];
                    for(int t=0; t<6; t++){
                        temp[t] = (int)choosenBallList.get(t);
                    }
                    NumberQuery nq = new NumberQuery(selected_Round,"2022",temp);
                    saveSelfInput(nq);
                } else{
                    Toast.makeText(getApplicationContext(), "번호 6개를 모두 선택해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });
        myTicketListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    // Function for Setting Spinner Adapter
    public void adapterSetting(){
        LatestRound round = null;
        {
            try {
                round = new LatestRound();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int latestRound = round.getRound() + 1;
        for(int i=latestRound;i>=1;i--){
            arrayList.add(Integer.toString(i));
        }
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item,
                arrayList);
        roundSpinner.setAdapter(arrayAdapter);
        roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_Round =  Integer.valueOf((String)parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Function for Setting Button Event
    public void buttonSetting(){
        // View Matching for 45 buttons
        int tempResId;
        for(int i=1;i<=45;i++){
            isButtonClicked[i] = false;
            tempResId = getResources()
                    .getIdentifier("button"+Integer.toString(i),"id",getPackageName());
            inputButtons[i] = findViewById(tempResId);
        }

        // global_i = button Index
        for(global_i=1;global_i<=45;global_i++) {
            inputButtons[global_i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // View ID를 받아옴
                    int tempId = v.getId();
                    String tempName = getResources().getResourceEntryName(tempId);
                    int tempIndex =  Integer.parseInt(tempName.substring(6));
                    // Button Activated
                    if(!isButtonClicked[tempIndex] && buttonActiveCnt<6){
                        isButtonClicked[tempIndex] = true;
                        ++buttonActiveCnt;
                        choosenBallList.add(tempIndex);
                        v.setBackgroundResource(R.drawable.selfinputbuttonclicked);
                        displayPressed();
                    }// Button Disabled
                    else if(isButtonClicked[tempIndex]) {
                        isButtonClicked[tempIndex] = false;
                        --buttonActiveCnt;
                        choosenBallList.remove(choosenBallList.indexOf(tempIndex));
                        v.setBackgroundResource(R.drawable.selfinputbutton);
                        displayPressed();
                    }
                }
            });
        }
    }

    // 밑에 공 6개 띄우는 애
    public void displayPressed(){
        int i;
        int tmpId;
        String choosenBall = "choosenBall";
        // View ID를 받아옴
        Collections.sort(choosenBallList);

        Log.d("loging",Integer.toString(buttonActiveCnt));

        for(i=0;i<buttonActiveCnt;i++){
            String choosenBallfull = "";
            choosenBallfull = choosenBall + Integer.toString(i+1);
            tmpId = getResources()
                    .getIdentifier(choosenBallfull,"id",getPackageName());
            TextView tmpView = findViewById(tmpId);
            tmpView.setText(Integer.toString(choosenBallList.get(i)));
        }
        for(;i<6;i++){
            String choosenBallfull = "";
            choosenBallfull = choosenBall + Integer.toString(i+1);
            tmpId = getResources()
                    .getIdentifier(choosenBallfull,"id",getPackageName());
            TextView tmpView = findViewById(tmpId);
            tmpView.setText("");
        }
    }

    // 리셋 버튼
    public void resetButtons(){
        buttonActiveCnt = 0;
        choosenBallList.clear();
        for(int z=1;z<=45;z++) {
            isButtonClicked[z] = false;
            inputButtons[z].setBackgroundResource(R.drawable.selfinputbutton);
        }
        displayPressed();
    }

    // 저장하기 버튼 누르면..
    public void saveSelfInput(NumberQuery nq){
        try {
            Log.d("saveSelfInput","You pushed button");
            LatestRound round = null;
            {
                try {
                    round = new LatestRound();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            DataAdapter si_DbAdapter = new DataAdapter(getApplicationContext());
            si_DbAdapter.open();
            int rank;
            if(nq.round == round.getRound() + 1){
                rank = -1;
                Toast.makeText(getApplicationContext(),"최신회차 저장 rank = -1",Toast.LENGTH_LONG).show();
            }
            else{
                rank = si_DbAdapter.getRank(nq.round, nq.nums);
            }
            Log.d("in that function",Integer.toString(rank));
            si_DbAdapter.insertPurchasedNum(rank,nq);
            si_DbAdapter.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}