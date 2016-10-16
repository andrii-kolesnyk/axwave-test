package com.axwave.ak.programmingtask.server.service;

import com.axwave.ak.programmingtask.transport.format.SoundFormat;
import com.axwave.ak.programmingtask.transport.model.Metadata;
import com.axwave.ak.programmingtask.transport.model.SoundSample;
import com.axwave.ak.programmingtask.transport.service.AudioService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class AudioServiceImpl implements AudioService {
    private static final Logger log = Logger.getLogger(AudioServiceImpl.class);

    private final String safeFolderPath;

    public AudioServiceImpl(@Value("#{${save.folder.path} ?: systemProperties['user.dir']}") String saveFolderPath) {
        this.safeFolderPath = saveFolderPath;
    }

    public Metadata saveSoundSample(SoundSample sample) {
        log.debug("received sound sample: " + sample);

        saveSoundSampleToFolder(sample);

        Metadata echoResponse = sample;
        return echoResponse;
    }

    private void saveSoundSampleToFolder(SoundSample sample) {
        File saveFolder = getSaveFolderForSoundFormat(sample.getFormat());

        String filePath = saveFolder.getPath() + "/" + sample.getTimestamp() + sample.getFormat().getFileExtension();
        File file = new File(filePath);
        log.debug("write sample to file: " + file.getPath());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(sample.getSample());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            log.error("Error occurred during writing sound sample to file " + file.getPath(), e);
        }
    }

    private File getSaveFolderForSoundFormat(SoundFormat soundFormat) {
        File saveFolder = new File(safeFolderPath + "/" + soundFormat.getEncodingName());

        if (!saveFolder.exists()) {
            saveFolder.mkdir();
        }
        return saveFolder;
    }
}
