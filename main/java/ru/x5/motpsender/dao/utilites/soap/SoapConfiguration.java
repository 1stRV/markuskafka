package ru.x5.motpsender.dao.utilites.soap;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
@Profile({"dev", "test", "unit"})
public class SoapConfiguration {

    @Value("${sap.pi.default.uri}")
    String sapDefaultUri;

    @Value("${sap.pi.username}")
    String username;

    @Value("${sap.pi.password}")
    String password;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("ru.x5.motpsender.dao.utilites.soap.generated");
        return marshaller;
    }

    @Bean
    public PiDataSignerService piDataSignerService(Jaxb2Marshaller marshaller) {
        PiDataSignerService client = new PiDataSignerService();
        client.setDefaultUri(sapDefaultUri);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setMessageSender(defaultMwMessageSender());
        return client;
    }

    @Bean
    public HttpComponentsMessageSender defaultMwMessageSender() {
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
        messageSender.setCredentials(new UsernamePasswordCredentials(username, password));
        return messageSender;
    }
}
