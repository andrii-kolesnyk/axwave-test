package com.axwave.ak.programmingtask.client;

import com.axwave.ak.programmingtask.client.config.Config;
import com.axwave.ak.programmingtask.transport.service.AudioService;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;

public class Application {
    private static final String SERVER_URL = Config.getServerUrl();

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        HessianProxyFactory hessianProxyFactory = new HessianProxyFactory();
        final AudioService service = (AudioService) hessianProxyFactory.create(AudioService.class, SERVER_URL);

        SampleRecorder sampleRecorder = new SampleRecorder();
        SampleSender sender = new SampleSender(service, sampleRecorder);

        sender.scheduleSendTask();
        sampleRecorder.start();
    }
}
