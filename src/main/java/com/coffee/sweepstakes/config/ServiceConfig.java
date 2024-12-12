package com.coffee.sweepstakes.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class ServiceConfig {

    @Value("${admin.user.username}")
    private String userName;

    @Value("${admin.user.password}")
    private String password;

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.user}")
    private String sftpUserName;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Value("${sftp.directory}")
    private String sftpDirectory;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${sendgrid.key}")
    private String sendgridKey;

    @Value("${sendgrid.from.email}")
    private String sendgridFromEmail;

}
