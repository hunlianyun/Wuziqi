package com.yangqi.wuziqi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button bt_reset;
    private Button bt_retract;
    private ChessView chessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initListener();
    }

    private void initListener() {
        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chessView.resetChessBoard();
            }
        });
        bt_retract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chessView.retract();
            }
        });
    }

    private void initUI() {

        bt_reset = (Button) findViewById(R.id.bt_reset);
        bt_retract = (Button) findViewById(R.id.bt_retract);
        chessView = (ChessView) findViewById(R.id.chessView);
    }
}
