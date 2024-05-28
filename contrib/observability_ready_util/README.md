# OpenCensus Observability Ready Util for Java

[![Build Status][travis-image]][travis-url]
[![Windows Build Status][appveyor-image]][appveyor-url]

The *OpenCensus Observability Ready Util for Java* allows users to use OpenCensus easily.

It provides a wrapper that
* Enables [Basic RPC views](https://github.com/census-instrumentation/opencensus-java/blob/2a17c8482ffb04540ea4ac0a5f746ad8d536c996/contrib/grpc_metrics/src/main/java/io/opencensus/contrib/grpc/metrics/RpcViews.java#L219)
* Sets Probabilistic sampling rate to `0.0001`
* Creates and Registers OCAgent Exporter to collect traces and metrics

## Quickstart

### Add the dependencies to your project

For Maven add to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-contrib-observability-ready-util</artifactId>
    <version>0.25.0</version>
  </dependency>
</dependencies>
```

For Gradle add to your dependencies:
```groovy
compile 'io.opencensus:opencensus-contrib-observability-ready-util:0.25.0'
```

### Enable OpenCensus:

```java
import io.opencensus.contrib.observability.ready.util.BasicSetup;

public class YourClass {
  public static void main(String[] args) {
    // It is recommended to call this method before doing any RPC call to avoid missing stats.
    BasicSetup.enableOpenCensus("with-service-name");
  }
}
```

> If Agent is not yet up and running, Exporter will just retry connection.

### And deploy OpenCensus Agent:

It will require you to deploy the [OpenCensus-Agent](https://github.com/census-instrumentation/opencensus-service#opencensus-agent) in order to export and examine the stats and traces.
The OpenCensus Agent exporter aka “ocagent-exporter” enables your applications to send the
observability that they’ve collected using OpenCensus to the OpenCensus Agent.


[travis-image]: https://travis-ci.org/census-instrumentation/opencensus-java.svg?branch=master
[travis-url]: https://travis-ci.org/census-instrumentation/opencensus-java
[appveyor-image]: https://ci.appveyor.com/api/projects/status/hxthmpkxar4jq4be/branch/master?svg=true
[appveyor-url]: https://ci.appveyor.com/project/opencensusjavateam/opencensus-java/branch/master
