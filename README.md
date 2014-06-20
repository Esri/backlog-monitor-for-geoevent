backlog-monitor-for-geoevent
============================

This is a JavaFX application that connects to the local GeoEvent server through JMX, and displays the current message backlog in all of the message buffers.


In order to build this application, you need to have Java 1.7, JavaFX, and Maven installed.  The built application runs on a Java 1.7 VM and can interact with GEP versions 10.2.0 - 10.2.2.

To build the application:
* open a command prompt in the directory containing the pom.xml file.
* make sure JavaFX is on the classpath with this command "mvn com.zenjava:javafx-maven-plugin:2.0:fix-classpath"   (for details on this, see http://zenjava.com/javafx/maven/fix-classpath.html)
* next build the application like this "mvn clean jfx:jar" which creates a jar file in target/jfx/app.
* Now you can run the executable jar file with this command "mvn jfx:run" or "java -jar <jarfile-in-target>"



