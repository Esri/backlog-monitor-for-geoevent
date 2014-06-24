# backlog-monitor-for-geoevent

ArcGIS 10.2.x GeoEvent Processor utility to monitor the number of messages waiting to be processed by GeoEvnet Services and Outputs.
This application is a JavaFX application that connects to the local GeoEvent Processor through JMX, and displays current message backlog counts of message buffers.


![App](backlog-for-geoevent.png?raw=true)

## Instructions

Building the source code:

1. Open a command prompt in the directory containing the pom.xml file.
2. Make sure JavaFX is on the classpath with this command "mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath"   (for details on this, see http://zenjava.com/javafx/maven/fix-classpath.html)
3. Build the application - "mvn clean jfx:jar" which creates a jar file in target/jfx/app.
4. Now you can run the executable jar file by going to the folder where the jar file is, and running "java -jar <jarfile>"

## Requirements

* ArcGIS 10.2.x GeoEvent Processor for Server.
* Java JDK 1.7 or greater.
* JavaFX.
* Maven.

## Usage

The lines on the display represent the number of messages in each backlog.  Over time, the backlog should not be growing.  There might be temporary
"spikes" in the graph when a burst of messages come in, and the server processes the messages.  However, if one of the lines is consistently growing,
then the server is not likely to recover, and it will eventually run out of resources.

Following is a healthy server example:
(backlog-for-geoevent.png?raw=true)

Following is an unhealthy server example:
(unhealthy.png?raw=true)

## Resources

* [ArcGIS GeoEvent Processor for Server Resource Center](http://pro.arcgis.com/share/geoevent-processor/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)
* [twitter@esri](http://twitter.com/esri)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2013 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt?raw=true) file.

[](ArcGIS, GeoEvent, Processor)
[](Esri Tags: ArcGIS GeoEvent Processor for Server)
[](Esri Language: Java)