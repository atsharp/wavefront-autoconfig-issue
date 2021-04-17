# wavefront-autoconfig-issue
Demonstrate an issue using Spring Sleuth and Wavefront's Spring starter with a custom autoconfiguration

This example imports Spring, Spring Sleuth, and wavefront-spring-boot-starter

The application loads an autoconfiguration class which includes a dependency on `Tracer` which is auto-configured by Sleuth.

The application fails to create a sampler (a noop sampler is created which effectively means no results are emitted). The reason being that WavefrontAutoConfiguration is pushed AFTER SleuthAutoConfiguration despite appropriate AutoConfigureAfter/AutoConfigureBefore annotations
