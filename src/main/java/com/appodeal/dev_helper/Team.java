package com.appodeal.dev_helper;

public class Team {
    private final String projectKey;
    private final String component;

    public Team(String projectKey, String component) {
        this.projectKey = projectKey;
        this.component = component;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getComponent() {
        return component;
    }
}


