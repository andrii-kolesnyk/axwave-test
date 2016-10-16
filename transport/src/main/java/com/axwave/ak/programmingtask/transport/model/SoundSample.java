package com.axwave.ak.programmingtask.transport.model;

import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import lombok.*;

import java.io.Serializable;

@ToString(callSuper = true, exclude = "sample")
@NoArgsConstructor
public class SoundSample extends Metadata implements Serializable {
    @Getter
    @Setter
    private byte[] sample;

    private short formatId;

    public void setFormat(SoundFormat format) {
        this.formatId = format.getId();
    }

    public SoundFormat getFormat() {
        return SoundFormat.getSoundFormatById(formatId);
    }
}
