package com.axwave.ak.programmingtask.client.Config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class Config {
    private static final Logger log = Logger.getLogger(Config.class);

    private static class ConfigHolder {
        private static final Configuration INSTANCE = loadConfig();

        private static Configuration loadConfig() {
            Configurations configs = new Configurations();
            try {
                return configs.properties(new File("application.properties"));
            } catch (ConfigurationException e) {
                String message = "Loading configuration error.";
                log.error(message, e);
                throw new Error(message, e);
            }
        }
    }

    public static String getServerUrl() {
        return ConfigHolder.INSTANCE.getString("server.url", "http://127.0.0.1:8080/saveSample");
    }

    public static short getMagicNumber() {
        return ConfigHolder.INSTANCE.getShort("magic.number", (short) 0x1234);
    }

    public static float getSampleRate() {
        return ConfigHolder.INSTANCE.getFloat("sample.rate", 16000);
    }

    public static int getSampleSizeInBits() {
        return ConfigHolder.INSTANCE.getInt("sample.size.in.bits", 8);
    }

    public static int getChannels() {
        return ConfigHolder.INSTANCE.getInt("channels", 2);
    }

    public static int getCaptureRateSeconds() {
        return ConfigHolder.INSTANCE.getInt("capture.rate.seconds", 1);
    }

    public static int getSampleSendSeconds() {
        return ConfigHolder.INSTANCE.getInt("sample.send.seconds", 4);
    }

    public static int getSendRateSeconds() {
        return ConfigHolder.INSTANCE.getInt("send.rate.seconds", 2);
    }

    public static int getExecutorCorePoolSize(){
        return ConfigHolder.INSTANCE.getInt("executor.core.pool.size", 1);
    }

    public static int getExecutorMaximumPoolSize(){
        return ConfigHolder.INSTANCE.getInt("executor.maximum.pool.size", 5);
    }

    public static int getExecutorKeepAliveSeconds(){
        return ConfigHolder.INSTANCE.getInt("executor.keep.alive", 60);
    }

    public static TimeUnit getExecutorKeepAliveTimeUnit(){
        return TimeUnit.valueOf(ConfigHolder.INSTANCE.getString("executor.keep.alive.time.unit", "SECONDS"));
    }
}
