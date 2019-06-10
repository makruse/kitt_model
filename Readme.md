Simulation developed in zmt-bremen's department of Theoretical Ecology & Modelling. Building is done with Gradle and zmt-build. For the complete documentation on zmt-build visit the [zmt-build project page](https://gitlab.leibniz-zmt.de/ecomod/zmt-build).

# IDE Support
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

## How to Run a Distribution
1. run-launcher kitt -ea "nameOfExported".xml
2. change entries in exported.xml that should vary from default,
	for variation in a value just add lines with the variated values e.g. for seed add <long>XX</long><long>YY</long> and so on...
3. ./run-Kitt-batch-dry exported.xml //if exported.xml has another location than your current directory, use full path
4. start with ./run-Kitt-separate-jvms "allOutput Directories"
	example: ./run-Kitt-separate-jvms kitt_output_batch_exported_00000/run_0000* 
	or in case of more than 10 runs: ./run-Kitt-separate-jvms kitt_output_batch_exported_00000/run_000* 
	
5. the actual runtime needs to be changed in "run-Kitt-separate-jvms" in the value UNTIL_TIME, given as seconds
	
### ---------Important Notes: ---------------
Depending on which OS you generated the files, they will have different line endings,
something slurm can't work with(at least not with windows line endings), so you need to convert
the script files(all the run stuff) and possibly the .xml file to UNIX Line endings, for that i
recommend this little tool:
Dos2Unix: https://sourceforge.net/projects/dos2unix/
it does exactly what you need, however it only fixes the line endings, but if you want to change
the default map, than you need to fix the path to the map as well, so switch "\" with "/" if necessary.
If you get in step 4 an Error with just exit code 1, it's probably one or both of the above things(because slurm can't find/read the generated params.xml)

##Lib
here you can put local libraries, currently there is only a modified zmt-core, which does no number formatting