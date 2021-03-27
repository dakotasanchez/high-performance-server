# High performance server example

This is an example of a server that consumes data from clients concurrently and logs the data in a high-performance fashion.
The application can handle up to 5 concurrent clients on port 3000. The client(s) can write
any number of valid data lines (9 digit number with leading 0's if
necessary + a newline character) to the server and optionally send
"terminate" + newline to shut down the server. Invalid data terminates
the connection/thread that encountered it. The numbers are logged in the file
numbers.log and duplicate numbers are discarded. Every 10 seconds a report
is printed to stdout showing deltas for the count of unique and duplicate numbers,
in addition to the total number of unique numbers.

The average observed throughput is about 3-4M numbers per 10-second reporting period. Memory
usage will increase linearly until about 15GB of memory is used. (999,999,999 Integer instances
is the bulk of it). Memory usage could probably be decreased by locking around a primitive array of
size 999,999,999 or something to keep track of duplicates, but I favored readability for this
initial version.

Testing machine: 16GB RAM, 2.3GHz Intel i5, 256GB SSD.

# Building and running

## IntelliJ Idea

Open the project folder in IntelliJ. Assuming IntelliJ is already configured with
your JDK, running the server should be as simple as using the green arrow in the Main
class or using the Run dropdown. Building is done through the Build dropdown. (Increase
java heap memory through Help->Edit custom VM options.)

## Terminal

Navigate to the project folder and run `./gradlew build` to build the project. Run
`./gradlew shadowJar` to create a complete runnable jar. Run
`java -jar build/libs/sanchez-server.jar` to run the server.

# Testing
To run a few (poorly implemented) tests through IntelliJ, they are located in the MainTest
class in the test directory. The most interesting one is probably testThroughput().
Or you can copy one of the client files in the test directory, and run it through a basic
main method somewhere else (to test outside of IntelliJ). Or you can make your own client.

# Assumptions
- Clients are on the local area network
- Clients connect through port 3000 and use TCP/IP
- Server is run on Linux (OSX should work too though)
- Java JDK 11 is installed
- Machine has a couple GB of hard disk space to hold numbers.log