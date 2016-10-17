package com.axwave.ak.programmingtask.client;

import com.axwave.ak.programmingtask.client.config.Config;
import com.axwave.ak.programmingtask.client.exception.LineNotSupportedError;
import com.axwave.ak.programmingtask.client.model.CaptureBuffer;
import com.axwave.ak.programmingtask.client.model.RecordSample;
import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import lombok.Getter;
import org.apache.log4j.Logger;

import javax.sound.sampled.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SampleRecorder {
    private static final Logger log = Logger.getLogger(SampleRecorder.class);

    private static final float SAMPLE_RATE = Config.getSampleRate();
    private static final int SAMPLE_SIZE_IN_BITS = Config.getSampleSizeInBits();
    private static final int CHANNELS = Config.getChannels();

    private static final int CAPTURE_RATE_SECONDS = Config.getCaptureRateSeconds();

    static final int SAMPLE_SEND_SECONDS = Config.getSampleSendSeconds();
    static final int SEND_RATE_SECONDS = Config.getSendRateSeconds();

    private static final int BYTES_PER_SECOND = SAMPLE_SIZE_IN_BITS / 8 * CHANNELS * (int) SAMPLE_RATE;

    private static final int CAPTURE_BUFFER_DATA_QUEUE_SIZE = BYTES_PER_SECOND * SAMPLE_SEND_SECONDS;
    private static final int CAPTURE_BUFFER_TIMESTAMP_QUEUE_SIZE = SAMPLE_SEND_SECONDS;

    @Getter
    private final ConcurrentLinkedQueue<RecordSample> taskQueue = new ConcurrentLinkedQueue<>();
    private final CaptureBuffer captureBuffer;

    private TargetDataLine line;
    private final AtomicBoolean continueCapture;
    private AudioFormat audioFormat;


    public SampleRecorder() {
        this.captureBuffer = new CaptureBuffer(CAPTURE_BUFFER_DATA_QUEUE_SIZE, CAPTURE_BUFFER_TIMESTAMP_QUEUE_SIZE);
        this.continueCapture = new AtomicBoolean(true);
    }

    //todo separate sound recording and sending samples
    /**
     * Starting capturing audio samples
     */
    public void start() {
        captureRecordSample();
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
                    taskQueue.add(new RecordSample(sampleBuffer, getTimestamp(),
                            SoundFormat.getSoundFormatForEncodingName(audioFormat.getEncoding().toString())));

                    //reset read number to avoid overflow
                    numberOfReads = SAMPLE_SEND_SECONDS;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Long getTimestamp() {
        return captureBuffer.getTimeStampQueue().peek();
    }

    private AudioInputStream startCapture() {
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

    void finishCapture() {
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
}