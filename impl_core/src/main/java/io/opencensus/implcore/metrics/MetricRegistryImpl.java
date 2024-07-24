/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.implcore.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.metrics.DerivedDoubleCumulative;
import io.opencensus.metrics.DerivedDoubleGauge;
import io.opencensus.metrics.DerivedLongCumulative;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.DoubleCumulative;
import io.opencensus.metrics.DoubleGauge;
import io.opencensus.metrics.LongCumulative;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.MetricOptions;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final RegisteredMeters registeredMeters;
  private final MetricProducer metricProducer;
  private final Clock clock;

  MetricRegistryImpl(Clock clock) {
    registeredMeters = new RegisteredMeters();
    metricProducer = new MetricProducerForRegistry(registeredMeters, clock);
    this.clock = clock;
  }

  @Override
  public LongGauge addLongGauge(String name, MetricOptions options) {
    LongGaugeImpl longGaugeMetric =
        new LongGaugeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels());
    return (LongGauge) registeredMeters.registerMeter(name, longGaugeMetric);
  }

  @Override
  public DoubleGauge addDoubleGauge(String name, MetricOptions options) {
    DoubleGaugeImpl doubleGaugeMetric =
        new DoubleGaugeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels());
    return (DoubleGauge) registeredMeters.registerMeter(name, doubleGaugeMetric);
  }

  @Override
  public DerivedLongGauge addDerivedLongGauge(String name, MetricOptions options) {
    DerivedLongGaugeImpl derivedLongGauge =
        new DerivedLongGaugeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels());
    return (DerivedLongGauge) registeredMeters.registerMeter(name, derivedLongGauge);
  }

  @Override
  public DerivedDoubleGauge addDerivedDoubleGauge(String name, MetricOptions options) {
    DerivedDoubleGaugeImpl derivedDoubleGauge =
        new DerivedDoubleGaugeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels());
    return (DerivedDoubleGauge) registeredMeters.registerMeter(name, derivedDoubleGauge);
  }

  @Override
  public LongCumulative addLongCumulative(String name, MetricOptions options) {
    LongCumulativeImpl longCumulativeMetric =
        new LongCumulativeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels(),
            clock.now());
    return (LongCumulative) registeredMeters.registerMeter(name, longCumulativeMetric);
  }

  @Override
  public DoubleCumulative addDoubleCumulative(String name, MetricOptions options) {
    DoubleCumulativeImpl longCumulativeMetric =
        new DoubleCumulativeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels(),
            clock.now());
    return (DoubleCumulative) registeredMeters.registerMeter(name, longCumulativeMetric);
  }

  @Override
  public DerivedLongCumulative addDerivedLongCumulative(String name, MetricOptions options) {
    DerivedLongCumulativeImpl derivedLongCumulative =
        new DerivedLongCumulativeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels(),
            clock.now());
    return (DerivedLongCumulative) registeredMeters.registerMeter(name, derivedLongCumulative);
  }

  @Override
  public DerivedDoubleCumulative addDerivedDoubleCumulative(String name, MetricOptions options) {
    DerivedDoubleCumulativeImpl derivedDoubleCumulative =
        new DerivedDoubleCumulativeImpl(
            checkNotNull(name, "name"),
            options.getDescription(),
            options.getUnit(),
            options.getLabelKeys(),
            options.getConstantLabels(),
            clock.now());
    return (DerivedDoubleCumulative) registeredMeters.registerMeter(name, derivedDoubleCumulative);
  }

  private static final class RegisteredMeters {
    private volatile Map<String, Meter> registeredMeters = Collections.emptyMap();

    private Map<String, Meter> getRegisteredMeters() {
      return registeredMeters;
    }

    private synchronized Meter registerMeter(String meterName, Meter meter) {
      Meter existingMeter = registeredMeters.get(meterName);
      if (existingMeter != null) {
        if (!existingMeter.getMetricDescriptor().equals(meter.getMetricDescriptor())) {
          throw new IllegalArgumentException(
              "A different metric with the same name already registered.");
        } else {
          return existingMeter;
        }
      }

      Map<String, Meter> registeredMetersCopy = new LinkedHashMap<String, Meter>(registeredMeters);
      registeredMetersCopy.put(meterName, meter);
      registeredMeters = Collections.unmodifiableMap(registeredMetersCopy);
      return meter;
    }
  }

  private static final class MetricProducerForRegistry extends MetricProducer {
    private final RegisteredMeters registeredMeters;
    private final Clock clock;

    private MetricProducerForRegistry(RegisteredMeters registeredMeters, Clock clock) {
      this.registeredMeters = registeredMeters;
      this.clock = clock;
    }

    @Override
    public Collection<Metric> getMetrics() {
      // Get a snapshot of the current registered meters.
      Map<String, Meter> meters = registeredMeters.getRegisteredMeters();
      if (meters.isEmpty()) {
        return Collections.emptyList();
      }

      List<Metric> metrics = new ArrayList<Metric>(meters.size());
      for (Map.Entry<String, Meter> entry : meters.entrySet()) {
        Metric metric = entry.getValue().getMetric(clock);
        if (metric != null) {
          metrics.add(metric);
        }
      }
      return Collections.unmodifiableCollection(metrics);
    }
  }

  MetricProducer getMetricProducer() {
    return metricProducer;
  }
}
