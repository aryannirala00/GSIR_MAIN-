package com.gsir.federation.dto;

public class RegisterSiteRequest {
    private String name;
    private String organization;
    private String apiUrl;
    private String capabilities;
    private String policies;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getCapabilities() { return capabilities; }
    public void setCapabilities(String capabilities) { this.capabilities = capabilities; }

    public String getPolicies() { return policies; }
    public void setPolicies(String policies) { this.policies = policies; }
}

