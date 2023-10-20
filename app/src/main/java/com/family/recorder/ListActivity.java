package com.family.recorder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.family.recorder.adapter.FileListAdapter;
import com.family.recorder.record.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HXL on 16/8/11.
 */
public class ListActivity extends Activity {

    ListView listView;
    List<File> list = new ArrayList<>();
    FileListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = findViewById(R.id.listView);
        if("pcm".equals(getIntent().getStringExtra("type"))){
            list= FileUtils.getPcmFiles(this);
        }else{
            list=FileUtils.getWavFiles(this);
        }

        adapter = new FileListAdapter(this, list);
        listView.setAdapter(adapter);
    }
}