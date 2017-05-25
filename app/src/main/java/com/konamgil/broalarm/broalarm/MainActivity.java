package com.konamgil.broalarm.broalarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

public class MainActivity extends AppCompatActivity {

    private Context context = MainActivity.this;
    private TextView tvSetTime;
    private int setHour = 0;
    private int setMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //리시버 등록
        registerReceiver();
        reStartTimerOneMinuteRegisterReceiver();

        init(); // 위젯 이닛
    }

    /**
     * init 위젯초기화 및 리스너 연결
     */
    private void init(){
        //스타트 버튼
        Button btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(mOnClickListener);

        //타임피커
        TimePicker mTimePicker = (TimePicker) findViewById(R.id.mTimePicker);
        mTimePicker.setOnTimeChangedListener(mOnTimeChangedListener);

        //타임표시
        tvSetTime = (TextView)findViewById(R.id.tvSetTime);
    }

    //버튼 리스너
    Button.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int setTime = getSettingHour();
            int setMinute = getSettingMinute();
            Calendar CalTime = makeSetLongTime(setTime,setMinute);

            setAlarm(context,CalTime);
            Toast.makeText(context,"알람이 설정되었습니다",Toast.LENGTH_SHORT).show();
        }
    };

    //mOnTimeChangedListener : 타임피커 체인지 리스너
    TimePicker.OnTimeChangedListener mOnTimeChangedListener = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            tvSetTime.setText(String.valueOf(hourOfDay) + "시  " + String.valueOf(minute) +"분");
            setHour = hourOfDay;
            setMinute = minute;
        }



    };

    //타임피커에서 세팅된 시간 가져오기
    public int getSettingHour(){
        return setHour;
    }
    //타임피커에서 세팅된 분 가져오기
    private int getSettingMinute(){
        return setMinute;
    }

    /**
     * 설정된 시간 구하기
     * @param hour
     * @param minute
     * @return
     */
    private Calendar makeSetLongTime(int hour, int minute){
        Calendar calNow = Calendar.getInstance();   // 현재 시간을 위한 Calendar 객체를 구한다.
        Calendar calSet = (Calendar)calNow.clone();   // 바로 위에서 구한 객체를 복제 한다.

        calSet.set(Calendar.HOUR_OF_DAY, hour);   // 타임피커에서 받아온 시간으로 시간 설정
        calSet.set(Calendar.MINUTE, minute);        // 타임피커에서 받아온 시간으로 분 설정
        calSet.set(Calendar.SECOND, 0);               // 초는 '0'으로 설정
        calSet.set(Calendar.MILLISECOND, 0);       // 밀리 초도  '0' 으로 설정

        if(calSet.compareTo(calNow) <= 0){            // 설정한 시간과 현재 시간 비교
            // 만약 설정한 시간이 현재 시간보다 이전이면
            calSet.add(Calendar.DATE, 1);  // 설정 시간에 하루를 더한다.
        }
        return calSet;
    }

    /**
     * alarm 설정하는 메서드
     * @param context
     * @param time
     */
    public void setAlarm(Context context, Calendar time){

        //intent 구성
        Intent sendIntent = new Intent("com.konamgil.broalarm.broalarm.Start");

        PendingIntent sender = PendingIntent.getBroadcast(context, 0, sendIntent, 0);

        //알람매니저, time.getTimeInMillis() <-- 타임피커로 지정해준 시간
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                   //버전이 21이상(마시멜로 이상)
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time.getTimeInMillis(),sender),sender);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {       //버전이 19~20(키캣~ 롤리팝)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), sender);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), sender); //계속 0초 ~ 3초 오차범위가 생김
        }

            //test용
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000,sender);
    }

    //브로드캐스트 리시버 등록
    public void registerReceiver(){
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showNotify(context,0);
                Toast.makeText(context,"알람이 실행되었습니다",Toast.LENGTH_SHORT).show();
            }
        },new IntentFilter("com.konamgil.broalarm.broalarm.Start"));
    }

    //다시 버튼 눌럿을 때 실행될 브로드 캐스트 리시버 설정
    public void reStartTimerOneMinuteRegisterReceiver(){
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showNotify(context, 1);
            }
        }, new IntentFilter("com.konamgil.broalarm.broalarm.reStart"));
    }


    /**
     * 상단 노티피케이션 출력 부분
     * @param context
     */
    public void showNotify(final Context context, int code){

        //커스텀 노티피케이션내 확인 버튼 : ResultActivity로 이동
        Intent intent = new Intent(this, ResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent content = PendingIntent.getActivity(this,0,intent,0);
        //

        //커스텀 노티피케이션내 다시 버튼 : 1분뒤에 다시 시작
        Intent sender = new Intent("com.konamgil.broalarm.broalarm.reStart");
        PendingIntent reSender = PendingIntent.getBroadcast(this, 0, sender, 0);
        //

        //커스텀 노티피케이션
        RemoteViews customView = new RemoteViews(context.getPackageName(), R.layout.customnotiview);//커스텀 레이아웃
        customView.setTextViewText(R.id.tvTitle, "알람입니다" );
        customView.setTextViewText(R.id.contents,"버튼을 클릭하세요");

        //확인버튼
        customView.setOnClickPendingIntent(R.id.btnOk,content);

        //다시버튼
        customView.setOnClickPendingIntent(R.id.btnReNoti,reSender);


        //노티피케이션 설정
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.smile)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLights(0xff00ff00, 500, 500)
                .setContent(customView)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                ;

        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(code == 0){ // 첫 브로드 캐스트 시작 부분
            nm.notify(1234, builder.build());
        }
        else if(code == 1) { //다시 시작 버튼을 눌럿을 경우
            Thread mThread = new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * 60); //1분
                        nm.notify(1234, builder.build());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Toast.makeText(context, "1분뒤에 다시 알람을 울리겠습니다", Toast.LENGTH_SHORT).show();
            nm.cancel(1234);
            mThread.start();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}


