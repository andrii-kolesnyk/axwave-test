package com.axwave.ak.programmingtask.transport.model;

import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true, exclude = "sample")
@NoArgsConstructor
@SuppressWarnings("unused")
@SuppressFBWarnings("EI_EXPOSE_REP")
public class SoundSample extends Metadata {
    private static final long serialVersionUID = 4353278496632602706L;

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
