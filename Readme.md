Simulation developed in zmt-bremen's department of Theoretical Ecology & Modelling. Building is done with Gradle and zmt-build. For the complete documentation on zmt-build visit the [zmt-build project page](https://gitlab.leibniz-zmt.de/ecomod/zmt-build).

## IDE Support    
To generate .project and .classpath files for developing in the Eclipse IDE enter:
```shell
./gradlew eclipse
```
This will also generate a launch configuration to run the simulation via GUI.

Other build tasks can be listed with:
```shell
./gradlew tasks
```

## Eclipse Support for Gradle
Gradle is supported by Eclipse via Buildship:
https://github.com/eclipse/buildship/

## Distribution
You can build a distribution by running the following in the project root folder:
```shell
./gradlew installDist
```

Build files are then stored in the build subdirectory.