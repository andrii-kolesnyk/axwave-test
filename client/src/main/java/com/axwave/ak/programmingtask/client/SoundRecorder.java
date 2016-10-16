package com.axwave.ak.programmingtask.client;

import com.axwave.ak.programmingtask.client.config.Config;
import com.axwave.ak.programmingtask.client.exception.LineNotSupportedError;
import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import com.axwave.ak.programmingtask.client.model.CaptureBuffer;
import com.axwave.ak.programmingtask.client.model.RecordSample;
import com.axwave.ak.programmingtask.transport.model.Metadata;
import com.axwave.ak.programmingtask.transport.model.SoundSample;
import com.axwave.ak.programmingtask.transport.service.AudioService;
import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianRuntimeException;
import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundRecorder {
    private static final float SAMPLE_RATE = Config.getSampleRate();
    private static final int SAMPLE_SIZE_IN_BITS = Config.getSampleSizeInBits();
    private static final int CHANNELS = Config.getChannels();

    private static final int CAPTURE_RATE_SECONDS = Config.getCaptureRateSeconds();

    private static final int SAMPLE_SEND_SECONDS = Config.getSampleSendSeconds();
    private static final int SEND_RATE_SECONDS = Config.getSendRateSeconds();

    private static final int EXECUTOR_CORE_POOL_SIZE = Config.getExecutorCorePoolSize();
    private static final int EXECUTOR_MAXIMUM_POOL_SIZE = Config.getExecutorMaximumPoolSize();
    private static final int EXECUTOR_KEEP_ALIVE = Config.getExecutorKeepAliveSeconds();
    private static final TimeUnit EXECUTOR_KEEP_ALIVE_TIME_UNIT = Config.getExecutorKeepAliveTimeUnit();

    private static final int BYTES_PER_SECOND = SAMPLE_SIZE_IN_BITS / 8 * CHANNELS * (int) SAMPLE_RATE;

    private static final int CAPTURE_BUFFER_DATA_QUEUE_SIZE = BYTES_PER_SECOND * SAMPLE_SEND_SECONDS;
    private static final int CAPTURE_BUFFER_TIMESTAMP_QUEUE_SIZE = SAMPLE_SEND_SECONDS;

    private static final short MAGIC_NUMBER = Config.getMagicNumber();

    private static final Logger log = Logger.getLogger(SoundRecorder.class);

    private final ConcurrentLinkedQueue<RecordSample> taskQueue = new ConcurrentLinkedQueue<>();
    private final CaptureBuffer captureBuffer;

    private final Timer sendTaskTimer;

    private TargetDataLine line;
    private final AudioService service;
    private final AtomicBoolean continueCapture;
    private AudioFormat audioFormat;

    private final ThreadPoolExecutor sendTaskExecutor;

    public SoundRecorder(AudioService service) {
        this.service = service;
        this.sendTaskTimer = new Timer();
        this.captureBuffer = new CaptureBuffer(CAPTURE_BUFFER_DATA_QUEUE_SIZE, CAPTURE_BUFFER_TIMESTAMP_QUEUE_SIZE);
        this.continueCapture = new AtomicBoolean(true);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        this.sendTaskExecutor = new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE, EXECUTOR_MAXIMUM_POOL_SIZE,
                EXECUTOR_KEEP_ALIVE, EXECUTOR_KEEP_ALIVE_TIME_UNIT, workQueue);
    }

    public void start() {
        scheduleSendTask();

        captureRecordSample();
    }

    private void scheduleSendTask() {
        TimerTask sendTask = getSendTask();
        sendTaskTimer.schedule(sendTask, SAMPLE_SEND_SECONDS * 1000, SEND_RATE_SECONDS * 1000);
    }

    private TimerTask getSendTask() {
        return new TimerTask() {
            @Override
            public void run() {
                //wait for recordSample
                while (taskQueue.size() == 0) {
                    Thread.yield();
                }

                RecordSample recordSample = taskQueue.poll();

                //synchronize on object created in captureRecordSample() method
                synchronized (recordSample) {
                    SoundSample soundSample = getSoundSample(recordSample.getSample(), recordSample.getTimestamp());

                    sendTaskExecutor.execute(() -> sendSample(soundSample));
                }
            }
        };
    }

    private void captureRecordSample() {
        AudioInputStream input = this.startCapture();

        byte[] buffer = new byte[BYTES_PER_SECOND];

        int numberOfReads = 0;

        while (this.continueCapture.get()) {
            try {
                captureTimestamp();
                captureOneRateToQueue(input, buffer);
                numberOfReads++;

                if (numberOfReads >= SAMPLE_SEND_SECONDS && numberOfReads % SEND_RATE_SECONDS == 0) {
                    int sampleBufferLength = captureBuffer.getDataQueue().size() - 1;
                    byte[] sampleBuffer = new byte[sampleBufferLength];
                    for (int i = 0; i < sampleBufferLength; i++) {
                        sampleBuffer[i] = captureBuffer.getDataQueue().get(i);
                    }
                    taskQueue.add(new RecordSample(sampleBuffer, getTimestamp()));

                    //reset read number to avoid overflow
                    numberOfReads = SAMPLE_SEND_SECONDS;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AudioInputStream startCapture() {
        try {
            audioFormat = defineAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                throw new LineNotSupportedError();
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            log.debug("Start capturing...");

            return new AudioInputStream(line);
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            throw new Error(ex);
        }
    }

    private void finishCapture() {
        this.continueCapture.set(false);
        if (Objects.nonNull(line)) {
            line.stop();
            line.close();
        }
        log.debug("Finished");
    }

    private AudioFormat defineAudioFormat() {
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, signed, bigEndian);
    }

    private void captureOneRateToQueue(AudioInputStream input, byte[] buffer) throws IOException {
        int length = input.read(buffer, 0, BYTES_PER_SECOND * CAPTURE_RATE_SECONDS);
        log.debug("read bytes " + length);
        for (int i = 0; i < buffer.length; i++) {
            captureBuffer.getDataQueue().add(buffer[i]);
        }
    }

    private void captureTimestamp() {
        log.debug("Sample timestamp " + OffsetDateTime.now().toString());
        captureBuffer.getTimeStampQueue().add(System.currentTimeMillis());
    }

    private SoundSample getSoundSample(byte[] sample, long timestamp) {
        SoundSample soundSample = new SoundSample();
        soundSample.setMagicNumber(MAGIC_NUMBER);
        soundSample.setFormat(SoundFormat.getSoundFormatForEncodingName(audioFormat.getEncoding().toString()));
        soundSample.setSample(sample);
        soundSample.setTimestamp(timestamp);
        return soundSample;
    }

    private Long getTimestamp() {
        return captureBuffer.getTimeStampQueue().peek();
    }

    private void sendSample(SoundSample sample) {
        log.debug(sample.toString());
        try {
            Metadata echoResponse = service.saveSoundSample(sample);
            log.debug(echoResponse);
        } catch (HessianRuntimeException | HessianException e) {
            log.debug("Communication error occurred, stopping capturing.", e);
            finishCapture();
            sendTaskExecutor.shutdown();
            sendTaskTimer.cancel();
        }
    }
}