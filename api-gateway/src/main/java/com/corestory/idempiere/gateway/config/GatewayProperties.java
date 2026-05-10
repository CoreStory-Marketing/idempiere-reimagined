package com.corestory.idempiere.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "idempiere")
public class GatewayProperties {

    private Jwt jwt = new Jwt();
    private DemoUser demoUser = new DemoUser();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public DemoUser getDemoUser() { return demoUser; }
    public void setDemoUser(DemoUser demoUser) { this.demoUser = demoUser; }

    public static class Jwt {
        private String signingKey;
        private String issuer;
        private long ttlMinutes = 60;

        public String getSigningKey() { return signingKey; }
        public void setSigningKey(String signingKey) { this.signingKey = signingKey; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public long getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(long ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }

    public static class DemoUser {
        private String username = "admin";
        private String password = "admin";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
