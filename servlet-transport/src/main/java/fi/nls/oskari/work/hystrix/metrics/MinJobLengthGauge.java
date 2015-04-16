package fi.nls.oskari.work.hystrix.metrics;

import com.codahale.metrics.Gauge;

/**
 * Created by SMAKINEN on 19.3.2015.
 */
public class MinJobLengthGauge implements Gauge<Long> {

    private TimingGauge gauge;

    public MinJobLengthGauge(TimingGauge param) {
        gauge = param;
    }
    public Long getValue() {
        return gauge.getMinJobLength();
    }
}
