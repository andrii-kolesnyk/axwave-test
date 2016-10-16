package com.axwave.ak.programmingtask.transport.format;

public enum SoundFormat {
    PCM_SIGNED((short) 1, "PCM_SIGNED", ".pcm"),
    PCM_UNSIGNED((short) 2, "PCM_UNSIGNED", ".pcm"),
    PCM_FLOAT((short) 3, "PCM_FLOAT", ".pcm"),
    ULAW((short) 4, "ULAW", ".ulaw"),
    ALAW((short) 5, "ALAW", ".alaw"),

    UNDEFINED((short) -1, "undefined", ".undefined");

    private short id;
    private String encodingName;
    private String encodingExtension;

    SoundFormat(short id, String encodingName, String encodingExtension) {
        this.id = id;
        this.encodingName = encodingName;
        this.encodingExtension = encodingExtension;
    }

    public short getId() {
        return id;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public static SoundFormat getSoundFormatById(short id) {
        SoundFormat result = SoundFormat.UNDEFINED;
        for (SoundFormat soundFormat : SoundFormat.values()) {
            if (soundFormat.getId() == id) {
                result = soundFormat;
                break;
            }
        }
        return result;
    }

    public static SoundFormat getSoundFormatForEncodingName(String encodingName){
        SoundFormat result = SoundFormat.UNDEFINED;
        for(SoundFormat format : SoundFormat.values()){
            if(format.getEncodingName().equalsIgnoreCase(encodingName)){
                result = format;
                break;
            }
        }
        return result;
    }

    public String getFileExtension() {
        return encodingExtension;
    }
}
