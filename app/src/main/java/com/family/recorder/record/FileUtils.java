package com.family.recorder.record;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理录音文件的类
 */
public class FileUtils {

    //原始文件(不能播放)
    private final static String AUDIO_PCM_BASE_PATH = "/pcm/";
    //可播放的高质量音频文件
    private final static String AUDIO_WAV_BASE_PATH = "/wav/";

    public static String getPcmFileAbsolutePath(String fileName, Context context) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("fileName isEmpty");
        }

        String mAudioRawPath;
        if (!fileName.endsWith(".pcm")) {
            fileName = fileName + ".pcm";
        }
        String fileBasePath = context.getCacheDir() + AUDIO_PCM_BASE_PATH;
        File file = new File(fileBasePath);
        //创建目录
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioRawPath = fileBasePath + fileName;

        return mAudioRawPath;
    }

    public static String getWavFileAbsolutePath(String fileName, Context context) {
        if (fileName == null) {
            throw new NullPointerException("fileName can't be null");
        }

        String mAudioWavPath;
        if (!fileName.endsWith(".wav")) {
            fileName = fileName + ".wav";
        }
        String fileBasePath = context.getCacheDir() + AUDIO_WAV_BASE_PATH;
        File file = new File(fileBasePath);
        //创建目录
        if (!file.exists()) {
            file.mkdirs();
        }
        mAudioWavPath = fileBasePath + fileName;

        return mAudioWavPath;
    }

    //获取全部pcm文件列表
    public static List<File> getPcmFiles(Context context) {
        List<File> list = new ArrayList<>();
        String fileBasePath = context.getCacheDir() + AUDIO_PCM_BASE_PATH;

        File rootFile = new File(fileBasePath);
        if (rootFile.exists()) {
            File[] files = rootFile.listFiles();
            assert files != null;
            Collections.addAll(list, files);
        }
        return list;
    }

    //获取全部wav文件列表
    public static List<File> getWavFiles(Context context) {
        List<File> list = new ArrayList<>();
        String fileBasePath = context.getCacheDir() + AUDIO_WAV_BASE_PATH;

        File rootFile = new File(fileBasePath);
        if (rootFile.exists()) {
            File[] files = rootFile.listFiles();
            assert files != null;
            Collections.addAll(list, files);
        }
        return list;
    }
}