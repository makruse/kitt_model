Simulation developed in zmt-bremen's department of Theoretical Ecology & Modelling. Building is done with gradle. For more information about gradle visit https://gradle.org/.

## Building
You can build a distribution by running the following in the project root folder:
```shell
./gradlew installDist
```
    
Build files are then stored in the build subdirectory.
    
To generate .project and .classpath files for developing in the Eclipse IDE enter:
```shell
./gradlew eclipse
```

Other build tasks can be listed with:
```shell
./gradlew tasks
```