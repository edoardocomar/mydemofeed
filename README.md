# mydemofeed

## prerequisites
* Gradle (tested with Gradle 3.4.1)
* JVM (tested with JDK 1.8.0_131 on macOS)

## build, installation, execution
1. `gradle build`
1. `gradle installDist`

1. `cd build/install`
1. `bin/mydemofeed config/demofeed.properties`

## Notes
 The 'user' entity is really just a subscription.
 
 The REST API implemented can be described via this 
 [swagger.yaml](docs/swagger.yaml) document

## Enhancements 
* handling generic binary data for the article content
