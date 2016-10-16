package com.axwave.ak.programmingtask.transport.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Metadata implements Serializable {
    protected short magicNumber;
    protected long timestamp;
}
