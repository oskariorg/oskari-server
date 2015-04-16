package fi.nls.oskari.work.hystrix.metrics;

import com.codahale.metrics.Gauge;

/**
 * Created by SMAKINEN on 19.3.2015.
 */
public class MaxJobLengthGauge implements Gauge<Long> {

    private TimingGauge gauge;

    public MaxJobLengthGauge(TimingGauge param) {
        gauge = param;
    }

    public Long getValue() {
        return gauge.getMaxJobLength();
    }
}
