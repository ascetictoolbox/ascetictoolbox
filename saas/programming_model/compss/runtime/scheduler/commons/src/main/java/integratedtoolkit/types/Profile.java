package integratedtoolkit.types;

public class Profile {

    private long executions;
    protected long sample;
    protected long startTime;
    protected long minTime;
    protected long averageTime;
    protected long maxTime;

    public Profile() {
        this.executions = 0;
        this.minTime = Long.MAX_VALUE;
        this.averageTime = 100;
        this.maxTime = Long.MIN_VALUE;
        this.sample = 0;
        startTime = System.currentTimeMillis();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void end() {
        ++executions;
        averageTime = System.currentTimeMillis() - startTime;
        minTime = averageTime;
        maxTime = averageTime;
    }

    public long getExecutionCount() {
        return executions;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getMinExecutionTime() {
        return minTime;
    }

    public long getAverageExecutionTime() {
        return averageTime;
    }

    public long getMaxExecutionTime() {
        return maxTime;
    }

    public void accumulate(Profile profile) {
        long totalExecutions = executions + profile.executions;
        if (totalExecutions > 0) {
            minTime = Math.min(minTime, profile.minTime);
            averageTime = (profile.executions * profile.averageTime + executions * averageTime) / totalExecutions;
            maxTime = Math.max(maxTime, profile.maxTime);
            executions = totalExecutions;
            sample += profile.sample;
        } else {
            long totalSamples = sample + profile.sample;
            if (totalSamples > 0) {
                minTime = Math.min(minTime, profile.minTime);
                averageTime = (profile.sample * profile.averageTime + sample * averageTime) / totalSamples;
                maxTime = Math.max(maxTime, profile.maxTime);
                sample = totalSamples;
            }
        }
    }

    public String toString() {
        return "[Profile executions=" + executions + " minTime" + minTime + " avgTime" + averageTime + " maxTime" + maxTime + "]";
    }

}
