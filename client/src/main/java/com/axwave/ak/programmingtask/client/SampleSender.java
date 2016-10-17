package com.axwave.ak.programmingtask.client;

import com.axwave.ak.programmingtask.client.config.Config;
import com.axwave.ak.programmingtask.client.model.RecordSample;
import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import com.axwave.ak.programmingtask.transport.model.Metadata;
import com.axwave.ak.programmingtask.transport.model.SoundSample;
import com.axwave.ak.programmingtask.transport.service.AudioService;
import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianRuntimeException;
import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class SampleSender {
    private static final Logger log = Logger.getLogger(SampleSender.class);

    private static final int EXECUTOR_CORE_POOL_SIZE = Config.getExecutorCorePoolSize();
    private static final int EXECUTOR_MAXIMUM_POOL_SIZE = Config.getExecutorMaximumPoolSize();
    private static final int EXECUTOR_KEEP_ALIVE = Config.getExecutorKeepAliveSeconds();
    private static final TimeUnit EXECUTOR_KEEP_ALIVE_TIME_UNIT = Config.getExecutorKeepAliveTimeUnit();

    private static final short MAGIC_NUMBER = Config.getMagicNumber();

    private final Timer sendTaskTimer;

    private final AudioService service;
    private final ThreadPoolExecutor sendTaskExecutor;
    private final SampleRecorder sampleRecorder;

    public SampleSender(AudioService service, SampleRecorder sampleRecorder){
        this.service = service;
        this.sampleRecorder = sampleRecorder;

        this.sendTaskTimer = new Timer();

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        this.sendTaskExecutor = new ThreadPoolExecutor(EXECUTOR_CORE_POOL_SIZE, EXECUTOR_MAXIMUM_POOL_SIZE,
                EXECUTOR_KEEP_ALIVE, EXECUTOR_KEEP_ALIVE_TIME_UNIT, workQueue);
    }

    public void scheduleSendTask() {
        TimerTask sendTask = getSendTask();
        sendTaskTimer.schedule(sendTask, SampleRecorder.SAMPLE_SEND_SECONDS * 1000L,
                SampleRecorder.SEND_RATE_SECONDS * 1000L);
    }

    /**
     * Create task to poll recordSample from {@link SampleRecorder#taskQueue} and send it to server using {@link #sendTaskExecutor}
     * @return new TimerTask
     */
    private TimerTask getSendTask() {
        return new TimerTask() {
            @Override
            public void run() {
                ConcurrentLinkedQueue<RecordSample> taskQueue = sampleRecorder.getTaskQueue();
                //wait for recordSample
                while (taskQueue.size() == 0) {
                    Thread.yield();
                }

                RecordSample recordSample = taskQueue.poll();

                //synchronize on object created in captureRecordSample() method
                synchronized (recordSample) {
                    SoundSample soundSample = getSoundSample(recordSample.getSample(), recordSample.getTimestamp(),
                            recordSample.getFormat());

                    sendTaskExecutor.execute(() -> sendSample(soundSample));
                }
            }
        };
    }

    private SoundSample getSoundSample(byte[] sample, long timestamp, SoundFormat format) {
        SoundSample soundSample = new SoundSample();
        soundSample.setMagicNumber(MAGIC_NUMBER);
        soundSample.setFormat(format);
        soundSample.setSample(sample);
        soundSample.setTimestamp(timestamp);
        return soundSample;
    }

    private void sendSample(SoundSample sample) {
        log.debug("Sending sample " + sample.toString());
        try {
            Metadata echoResponse = service.saveSoundSample(sample);
            log.debug("Received echo response " + echoResponse);
        } catch (HessianRuntimeException | HessianException e) {
            log.debug("Communication error occurred, stopping capturing.", e);
            sampleRecorder.finishCapture();
            sendTaskExecutor.shutdown();
            sendTaskTimer.cancel();
        }
    }
}