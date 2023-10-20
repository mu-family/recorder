package com.family.recorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.family.recorder.record.AudioRecorder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_start;
    Button btn_pause;
    Button btn_wavList;
    Button btn_pcmList;

    AudioRecorder audioRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_pause = findViewById(R.id.btn_pause);
        btn_wavList = findViewById(R.id.btn_wavList);
        btn_pcmList = findViewById(R.id.btn_pcmList);

        btn_start.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_wavList.setOnClickListener(this);
        btn_pcmList.setOnClickListener(this);

        btn_pause.setVisibility(View.GONE);
        //初始化
        audioRecorder = AudioRecorder.getInstance();

        checkPermission();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                try {
                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_NO_READY) {
                        //初始化录音文件
                        String fileName = new SimpleDateFormat("MMddhhmmss").format(new Date());
                        audioRecorder.initAudioRecord(fileName, this);
                        audioRecorder.startRecord();

                        btn_start.setText("停止录音");
                        btn_pause.setVisibility(View.VISIBLE);
                    } else {
                        //停止录音
                        audioRecorder.stopRecord();
                        btn_start.setText("开始录音");
                        btn_pause.setText("暂停录音");
                        btn_pause.setVisibility(View.GONE);
                    }
                } catch (IllegalStateException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_pause:
                try {
                    if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
                        //暂停录音
                        audioRecorder.pauseRecord();
                        btn_pause.setText("继续录音");
                        break;
                    } else {
                        audioRecorder.startRecord();
                        btn_pause.setText("暂停录音");
                    }
                } catch (IllegalStateException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_wavList:
                Intent showWavList = new Intent(this, ListActivity.class);
                showWavList.putExtra("type", "wav");
                startActivity(showWavList);
                break;
            case R.id.btn_pcmList:
                Intent showPcmList = new Intent(this, ListActivity.class);
                showPcmList.putExtra("type", "pcm");
                startActivity(showPcmList);
                break;
            default:
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (audioRecorder.getStatus() == AudioRecorder.Status.STATUS_START) {
            audioRecorder.pauseRecord();
            btn_pause.setText("继续录音");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioRecorder.release();
    }

    //*******************************************************************
    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    private static final int PERMISSIONS_RESULT_CODE = 10;                //权限code

    //申请权限
    private void checkPermission() {
        // 检查未授权的权限
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        //申请权限
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[0]), PERMISSIONS_RESULT_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_RESULT_CODE && grantResults.length > 0) {
            // 判断是否获得权限
            for (int i = 0; i < grantResults.length; i++) {
                // 未得到授权的权限
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        Toast.makeText(this, "请手动开启授权进行登录", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        checkPermission();
                    }
                }
            }
        }
    }
    //*******************************************************************
}