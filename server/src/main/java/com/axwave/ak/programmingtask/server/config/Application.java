package com.axwave.ak.programmingtask.server.config;

import com.axwave.ak.programmingtask.transport.service.AudioService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.remoting.caucho.HessianServiceExporter;

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = {"com.axwave.ak.programmingtask"})
@PropertySources({
        @PropertySource(value = {"classpath:application.properties"})})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "/saveSample")
    public HessianServiceExporter saveSample(AudioService audioService) {
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setService(audioService);
        exporter.setServiceInterface(AudioService.class);
        return exporter;
    }
}
