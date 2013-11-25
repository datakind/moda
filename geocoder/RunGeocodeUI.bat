REM If OutOfMemoryError error is encountered while running the batch script..
REM ..try increasing the memory allocated (-Xmx1024m) to a larger amount..
REM ..such as -Xmx2048m 

java -Xmx1024m -classpath .;./lib/jcsv-1.4.0.jar Geocoder.GeocodeUI
