package com.axwave.ak.programmingtask.transport.service;


import com.axwave.ak.programmingtask.transport.model.Metadata;
import com.axwave.ak.programmingtask.transport.model.SoundSample;

public interface AudioService {
    Metadata saveSoundSample(SoundSample sample);
}
