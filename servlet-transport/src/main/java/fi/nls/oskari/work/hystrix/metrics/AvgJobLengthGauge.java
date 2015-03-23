package fi.nls.oskari.work.hystrix.metrics;

import com.codahale.metrics.Gauge;

/**
 * Created by SMAKINEN on 19.3.2015.
 */
public class AvgJobLengthGauge implements Gauge<Long> {

    private TimingGauge gauge;

    public AvgJobLengthGauge(TimingGauge param) {
        gauge = param;
    }
    public Long getValue() {
        return gauge.getAvgRuntime();
    }
}
