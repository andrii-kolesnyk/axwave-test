package com.axwave.ak.programmingtask.client.model;

import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Buffer class for transfer sample of some period to {@link com.axwave.ak.programmingtask.client.SampleSender}
 */
@Getter
@ToString
@AllArgsConstructor
@SuppressWarnings("unused")
@SuppressFBWarnings("EI_EXPOSE_REP")
public class RecordSample {
    private byte[] sample;
    private long timestamp;
    private SoundFormat format;
}
