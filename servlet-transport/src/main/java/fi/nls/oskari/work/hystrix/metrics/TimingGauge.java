package fi.nls.oskari.work.hystrix.metrics;

/**
 * Created by SMAKINEN on 19.3.2015.
 */
public class TimingGauge {

    protected long maxJobLength = 0;
    protected long minJobLength = Long.MAX_VALUE;
    protected long jobCount = 0;
    protected long avgRuntime = 0;

    public void setupTimingStatistics(long runTimeMS) {
        jobCount++;
        if(runTimeMS > maxJobLength) {
            maxJobLength = runTimeMS;
        }
        if(runTimeMS < minJobLength) {
            minJobLength = runTimeMS;
        }
        if(avgRuntime == 0) {
            avgRuntime = runTimeMS;
        }
        else {
            avgRuntime = ((avgRuntime * (jobCount -1)) + runTimeMS) / jobCount;
        }
    }

    public long getMaxJobLength() {
        return maxJobLength;
    }

    public long getMinJobLength() {
        return minJobLength;
    }

    public long getJobCount() {
        return jobCount;
    }

    public long getAvgRuntime() {
        return avgRuntime;
    }
}
