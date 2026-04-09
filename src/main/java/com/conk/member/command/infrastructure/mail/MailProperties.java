package com.conk.member.command.infrastructure.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String fromName;
    private String loginUrl;
    private String serviceName;
    private String setupBaseUrl;

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getSetupBaseUrl() { return setupBaseUrl; }
    public void setSetupBaseUrl(String setupBaseUrl) { this.setupBaseUrl = setupBaseUrl; }
}
