package config;

import powerbuttonstrategies.Strategy;

public class Config {
    
    private final int intervalSeconds;
    private final Strategy strategy;
    private final String vmmBaseUrl;
    private final String vmmVmsPath;
    private final String vmmHostsPath;
    private final String vmmNodePath;
    private final String vmmPowerButtonPath;

    public Config(int intervalSeconds, Strategy strategy, String vmmBaseUrl, String vmmVmsPath,
                  String vmmHostsPath, String vmmNodePath, String vmmPowerButtonPath) {
        this.intervalSeconds = intervalSeconds;
        this.strategy = strategy;
        this.vmmBaseUrl = vmmBaseUrl;
        this.vmmVmsPath = vmmVmsPath;
        this.vmmHostsPath = vmmHostsPath;
        this.vmmNodePath = vmmNodePath;
        this.vmmPowerButtonPath = vmmPowerButtonPath;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public String getVmmBaseUrl() {
        return vmmBaseUrl;
    }

    public String getVmmVmsPath() {
        return vmmVmsPath;
    }

    public String getVmmHostsPath() {
        return vmmHostsPath;
    }

    public String getVmmNodePath() {
        return vmmNodePath;
    }

    public String getVmmPowerButtonPath() {
        return vmmPowerButtonPath;
    }
    
}
