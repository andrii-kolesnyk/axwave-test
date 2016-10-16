package com.axwave.ak.programmingtask.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class RecordSample {
    private byte[] sample;
    private long timestamp;
}
