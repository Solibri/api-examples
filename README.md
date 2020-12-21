# api-examples
[Solibri Developer Platform](https://solibri.github.io/Developer-Platform/) APIs examples

## Licensing

The examples themselves are licensed with the relaxed MIT license. The intention is that anybody can take the examples, use, modify and build on top of them freely, also for commercial purposes.

Note that while we license the examples with a relaxed license, the dependencies the examples use, might have different licenses, so check the possible restrictions they might have separately.

## Versioning

The repository will host only one version of the examples, but old states can be found from the Git tags.
The API is supposed to be developed in a way that any example will work with the version it was created for and with any newer versions keeping the API fully backwards compatible.

## How to use

The examples are build using Maven build system. Building them should be possible on any major operating system.
The build the examples JDK 11 and Maven installation are needed. We hope to integrate Maven Wrapper to the examples at once that is integrated to the Maven core, removing the need for manual Maven installations. Gradle or any other Java build system can also be used to build the examples, but we do not provide the build configurations for those systems.
