# wavefront-autoconfig-issue
Demonstrate an issue using Spring's Wavefront config with autoconfiguration

This example imports Spring Sleuth, and wavefront-spring-boot-starter

The application simply adds an autoconfiguration class which includes a dependency on `Tracer` which is auto-configured by Sleuth.

The application fails to create a sampler. The reason being that WavefrontAutoConfiguration is pushed AFTER SleuthAutoConfiguration
