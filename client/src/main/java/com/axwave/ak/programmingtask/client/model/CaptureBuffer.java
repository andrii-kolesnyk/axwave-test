package com.axwave.ak.programmingtask.client.model;

import lombok.Getter;
import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Buffer class for storing audio sample's data red from AudioInputStream and timestamps of samples
 */
@Getter
@SuppressWarnings("unused")
public class CaptureBuffer {
    private final CircularFifoQueue<Byte> dataQueue;
    private final CircularFifoQueue<Long> timeStampQueue;

    public CaptureBuffer(int maximumCaptureBytes, int maximumSampleCount) {
        dataQueue = new CircularFifoQueue<>(maximumCaptureBytes);
        timeStampQueue = new CircularFifoQueue<>(maximumSampleCount);
    }
}
