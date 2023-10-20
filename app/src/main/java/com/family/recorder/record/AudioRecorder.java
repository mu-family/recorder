package com.family.recorder.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于实现录音   暂停录音
 */
public class AudioRecorder {

    @SuppressLint("StaticFieldLeak")
    public static AudioRecorder audioRecorder;
    private Context context;                                                    //上下文
    private int bufferSizeInBytes;                                              //构建最小音频录制缓冲区

    private AudioRecord audioRecord;                                            //录音对象
    private Status status = Status.STATUS_NO_READY;                             //录音状态

    //文件名
    private String fileName;

    //录音文件
    private final List<String> filesName = new ArrayList<>();

    private AudioRecorder() {}

    //单例模式
    public static AudioRecorder getInstance() {
        if (audioRecorder == null) {
            audioRecorder = new AudioRecorder();
        }
        return audioRecorder;
    }

    //初始化录音数据
    @SuppressLint("MissingPermission")
    public void initAudioRecord(String fileName, Context context) {

        int audioSource = MediaRecorder.AudioSource.MIC;        //音频数据来源，麦克风
        int sampleRateInHz = 44100;                             //采样率
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;      //声道，立体声
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       //数据编码方式

        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        this.fileName = fileName;
        this.context = context;
        status = Status.STATUS_READY;
    }

    //开始录音
    public void startRecord() {
        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        audioRecord.startRecording();

        new Thread(this::writeDataToFile).start();
    }

    //暂停录音
    public void pauseRecord() {
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("没有在录音");
        } else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    //停止录音
    public void stopRecord() {
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            audioRecord.stop();
            status = Status.STATUS_STOP;
            release();
        }
    }

    //释放资源
    public void release() {
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                List<String> filePaths = new ArrayList<>();
                for (String fileName : filesName) {
                    filePaths.add(FileUtils.getPcmFileAbsolutePath(fileName, context));
                }
                //清除
                filesName.clear();
                //将多个pcm文件转化为wav文件
                mergePCMFilesToWAVFile(filePaths);
            }else {
                //这里由于只要录音过filesName.size都会大于0,没录音时fileName为null
                //会报空指针 NullPointerException
                //将单个pcm文件转化为wav文件
                //makePCMFileToWAVFile();
            }
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    //将音频信息写入文件
    private void writeDataToFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audioData = new byte[bufferSizeInBytes];

        FileOutputStream fos;
        int readSize;
        try {
            String currentFileName = fileName;
            if (status == Status.STATUS_PAUSE) {
                //假如是暂停录音 将文件名后面加个数字,防止重名文件内容被覆盖
                currentFileName += filesName.size();
            }
            filesName.add(currentFileName);
            File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName, context));
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
        //将录音状态设置成正在录音状态
        status = Status.STATUS_START;
        while (status == Status.STATUS_START) {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                try {
                    fos.write(audioData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos.close();// 关闭写入流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将pcm合并成wav
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        new Thread(() -> {
            if (PcmToWav.mergePCMFilesToWAVFile(filePaths, FileUtils.getWavFileAbsolutePath(fileName, context))) {
                //操作成功
                Log.e("TAG", "***转化成功");
            } else {
                //操作失败
                Log.e("TAG", "***转化失败");
                throw new IllegalStateException("mergePCMFilesToWAVFile fail");
            }
            fileName = null;
        }).start();
    }

    //将单个pcm文件转化为wav文件
    private void makePCMFileToWAVFile() {
        new Thread(() -> {
            if (PcmToWav.makePCMFileToWAVFile(FileUtils.getPcmFileAbsolutePath(fileName, context), FileUtils.getWavFileAbsolutePath(fileName, context), true)) {
                //操作成功
                Log.e("TAG", "---转化成功");
            } else {
                //操作失败
                Log.e("TAG", "---转化失败");
                throw new IllegalStateException("makePCMFileToWAVFile fail");
            }
            fileName = null;
        }).start();
    }

    //获取录音对象的状态
    public Status getStatus() {
        return status;
    }

    //获取本次录音文件的个数
    public int getPcmFilesCount() {
        return filesName.size();
    }

    //录音对象的状态
    public  enum Status {
        STATUS_NO_READY,            //未开始
        STATUS_READY,               //预备
        STATUS_START,               //录音
        STATUS_PAUSE,               //暂停
        STATUS_STOP                 //停止
    }
}