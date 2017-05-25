package com.konamgil.broalarm.broalarm;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by konamgil on 2017-05-25.
 */

public class ResultActivity extends AppCompatActivity {

    private TextView tvTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        init();
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(1234);
    }

    /**
     * 위젯 초기화
     */
    public void init(){
        tvTime = (TextView)findViewById(R.id.tvTime);
        tvTime.setText("알림이 수신되었습니다");

    }
}
