import config.Config;
import httpClient.HttpClient;
import models.ClusterState;
import powerbuttonstrategies.*;
import vmm.VmmClient;

public class PowerButtonPresser {
    
    private static Config config = new Config(
            60, Strategy.JUST_IN_TIME, "http://0.0.0.0:34372/vmmanager/", "vms/", "nodes/",
            "node/", "powerButton/");
    
    private static VmmClient vmmClient = new VmmClient(
            new HttpClient(), config.getVmmBaseUrl(), config.getVmmVmsPath(), config.getVmmHostsPath(), 
            config.getVmmNodePath(), config.getVmmPowerButtonPath());
    
    private static PowerButtonStrategy powerButtonStrategy = getStrategy();
    
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        while (true) {
            powerButtonStrategy.applyStrategy(new ClusterState(vmmClient.getVms(), vmmClient.getHosts()));
            try {
                Thread.sleep(config.getIntervalSeconds()*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static PowerButtonStrategy getStrategy() { // Turn into factory?
        switch (config.getStrategy()) {
            case ALL_SERVERS_ON:
                return new AllServersOnStrategy(vmmClient);
            case JUST_IN_TIME:
                return new JustInTimeStrategy(vmmClient);
            case N_BACKUP_HOSTS:
                return new NBackupHostsStrategy(vmmClient);
            case PATTERN_RECOGNITION:
                return new PatternRecognitionStrategy(vmmClient);
            default:
                return null; // Throw exception here
        }
    }
    
}
