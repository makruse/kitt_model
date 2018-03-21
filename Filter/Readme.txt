Due to unnessecary Units, Brackets and high precision mathematical Characters(± XE-XX)
the default output lifeCycling.csv is not easily usable in for Example R.
Therefore the FilterForR Program filters all irritating Characters out,
in addition it also calcs down the steps to Days(since the file is written on a daily
basis anyway, Header gets also renamed) and replaces the complex UUID to a simple, 
more readable long value, and at last replaces the Tab-separators with comma separators.

As a nice side effect the file size gets reduced by about 45%, for a 200mb file
the process takes about 48 Seconds, could probably be faster since i used the
most easy implemententation, not nessecarily the fastest.

Therefore i also don't know what happens when the steps exceed about 
68,09 years(max positive integer) or when you run another output file through it,
it will most likely crash, even though everythings with steps as first header value
should work fine(or an int that will be interpreted as seconds and calced down).
The last value will always be interpreted as an ID an simplified. If you want to 
modify something, the source files are included as well.

To run the program just start it on the command line with:
java -jar FilterForR.jar "full path to file in quotes"

the -jar part is probably optional