package com.axwave.ak.programmingtask.transport.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Metadata implements Serializable {
    private static final long serialVersionUID = 1345063487181242288L;

    protected short magicNumber;
    protected long timestamp;
}
