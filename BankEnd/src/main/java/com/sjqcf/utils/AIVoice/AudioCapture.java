package com.sjqcf.utils.AIVoice;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AudioCapture {
    private static final int BUFFER_SIZE = 4096;
    private TargetDataLine targetLine;
    private volatile boolean isCapturing = false;
    private Thread captureThread;
    private final BlockingQueue<byte[]> audioQueue;
    private AudioFormat actualFormat;
    
    public AudioCapture() {
        this.audioQueue = new ArrayBlockingQueue<>(100);
    }
    
    public void startCapture() throws LineUnavailableException {
        System.out.println("🔍 开始检测音频设备...");
        printAudioDeviceInfo();
        
        actualFormat = findSupportedFormat();
        
        if (actualFormat == null) {
            System.err.println("❌ 未找到可用的麦克风设备");
            System.err.println("💡 提示: 如果您在Docker容器中运行，需要添加 --device /dev/snd 参数");
            throw new LineUnavailableException("未检测到麦克风设备");
        }
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, actualFormat);
        
        try {
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(actualFormat);
            targetLine.start();
            
            System.out.println("✅ 麦克风设备初始化成功");
            System.out.println("📊 实际采样率: " + actualFormat.getSampleRate() + " Hz");
            System.out.println("📊 位深: " + actualFormat.getSampleSizeInBits() + " bit");
            System.out.println("📊 声道: " + actualFormat.getChannels());
            
            isCapturing = true;
            captureThread = new Thread(this::captureLoop);
            captureThread.setName("AudioCapture");
            captureThread.start();
            
            testMicrophoneLevel();
        } catch (LineUnavailableException e) {
            System.err.println("❌ 无法打开麦克风设备: " + e.getMessage());
            throw e;
        }
    }
    
    private void printAudioDeviceInfo() {
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            System.out.println("📻 可用的音频设备 (" + mixers.length + " 个):");
            
            if (mixers.length == 0) {
                System.err.println("⚠️ 警告: 系统未检测到任何音频设备!");
                System.err.println("   可能原因:");
                System.err.println("   1. 在Docker容器中运行，未映射音频设备");
                System.err.println("   2. Linux服务器没有声卡硬件");
                System.err.println("   3. 音频驱动未正确安装");
                return;
            }
            
            for (Mixer.Info mixerInfo : mixers) {
                System.out.println("  - " + mixerInfo.getName() + " (" + mixerInfo.getDescription() + ")");
                
                try {
                    Mixer mixer = AudioSystem.getMixer(mixerInfo);
                    Line.Info[] targetLines = mixer.getTargetLineInfo();
                    if (targetLines.length > 0) {
                        for (Line.Info lineInfo : targetLines) {
                            System.out.println("    ✅ 支持: " + lineInfo.toString());
                        }
                    } else {
                        System.out.println("    ℹ️ 无输入设备");
                    }
                } catch (Exception e) {
                    System.out.println("    ⚠️ 无法访问: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("获取音频设备信息失败: " + e.getMessage());
        }
    }
    
    private AudioFormat findSupportedFormat() {
        int[] sampleRates = {16000, 22050, 44100, 48000};
        boolean[] signedValues = {true, false};
        boolean[] endianValues = {false, true};
        
        System.out.println("🔎 尝试检测支持的音频格式...");
        
        for (int rate : sampleRates) {
            for (boolean signed : signedValues) {
                for (boolean littleEndian : endianValues) {
                    AudioFormat format = new AudioFormat(
                        rate,
                        16,
                        Config.CHANNELS,
                        signed,
                        littleEndian
                    );
                    
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                    if (AudioSystem.isLineSupported(info)) {
                        System.out.println("✅ 找到支持的格式: " + rate + " Hz, " + 
                            (signed ? "有符号" : "无符号") + ", " + 
                            (littleEndian ? "小端序" : "大端序"));
                        return format;
                    }
                }
            }
        }
        
        System.err.println("⚠️ 标准格式均不被支持，尝试获取系统默认混音器...");
        
        try {
            Mixer defaultMixer = AudioSystem.getMixer(null);
            Line.Info[] lines = defaultMixer.getTargetLineInfo();
            
            if (lines.length > 0) {
                System.out.println("✅ 使用系统默认混音器: " + defaultMixer.getMixerInfo().getName());
                
                for (Line.Info lineInfo : lines) {
                    if (lineInfo instanceof DataLine.Info) {
                        AudioFormat[] formats = ((DataLine.Info) lineInfo).getFormats();
                        if (formats != null && formats.length > 0) {
                            System.out.println("✅ 使用默认格式: " + formats[0].toString());
                            return formats[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取默认混音器失败: " + e.getMessage());
        }
        
        System.err.println("❌ 无法找到任何支持的音频格式");
        return null;
    }
    
    private void captureLoop() {
        byte[] buffer = new byte[Config.AUDIO_CHUNK_SIZE];
        
        while (isCapturing) {
            int bytesRead = targetLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                byte[] audioData = new byte[bytesRead];
                System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                try {
                    audioQueue.put(audioData);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    public byte[] readAudioData() throws InterruptedException {
        return audioQueue.poll();
    }
    
    public void stopCapture() {
        isCapturing = false;
        if (captureThread != null) {
            captureThread.interrupt();
        }
        if (targetLine != null) {
            targetLine.stop();
            targetLine.close();
        }
    }
    
    public boolean isCapturing() {
        return isCapturing;
    }
    
    public AudioFormat getActualFormat() {
        return actualFormat;
    }
    
    private void testMicrophoneLevel() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                
                byte[] buffer = new byte[Config.AUDIO_CHUNK_SIZE];
                int bytesRead = targetLine.read(buffer, 0, buffer.length);
                
                if (bytesRead > 0) {
                    int sum = 0;
                    for (int i = 0; i < bytesRead; i += 2) {
                        if (i + 1 < bytesRead) {
                            short sample = (short) ((buffer[i] & 0xFF) | (buffer[i + 1] << 8));
                            sum += Math.abs(sample);
                        }
                    }
                    double average = (double) sum / (bytesRead / 2);
                    
                    System.out.println("🎤 麦克风音量测试 - 平均振幅: " + String.format("%.2f", average));
                    if (average < 50) {
                        System.err.println("⚠️ 警告: 麦克风音量过低! 请检查麦克风设置");
                    } else if (average < 200) {
                        System.out.println("ℹ️ 提示: 麦克风音量偏低,但可以正常工作");
                    } else {
                        System.out.println("✅ 麦克风音量正常");
                    }
                } else {
                    System.err.println("⚠️ 警告: 未能读取到音频数据");
                }
            } catch (Exception e) {
                System.err.println("麦克风测试失败: " + e.getMessage());
            }
        }).start();
    }
    
    public static byte[] readWavFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("音频文件不存在: " + filePath);
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileData = new byte[(int) file.length()];
            fis.read(fileData);
            
            if (filePath.toLowerCase().endsWith(".wav") && fileData.length > Config.WAV_HEADER_SIZE) {
                byte[] audioData = new byte[fileData.length - Config.WAV_HEADER_SIZE];
                System.arraycopy(fileData, Config.WAV_HEADER_SIZE, audioData, 0, audioData.length);
                return audioData;
            }
            
            return fileData;
        }
    }
    
    public static short[] bytesToInt16Samples(byte[] data) {
        short[] samples = new short[data.length / 2];
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        return samples;
    }
    
    public static byte[] int16SamplesToBytes(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(samples);
        return bytes;
    }
}