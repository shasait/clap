clap
====

[![Maven Central](https://img.shields.io/maven-central/v/de.hasait/clap.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.hasait%22%20AND%20a%3A%22clap%22)

Command Line Arguments Parser for Java

* Licensed under the Apache License, Version 2.0
* Supports *NIX style options
* Short options: -v
* Long options: --verbose
* Options with zero arguments (flags): -v
* Options with exact number of arguments: -p22 --port=22 --port 22 --sum=1;6
* Options with unlimited number of arguments
* Decisions/Alternatives (e.g. to support different commands)
* Keywords
* Nameless option
* Generified for type safety
* Custom type converters
* Imperative style (e.g. addOption, addFlag, ...)
* Annotations (@Option, @HelpCategory, ...)
* Annotations can be mixed with imperative style
* Mapping of arguments to nested data structures
* I18N for usage and error messages
* Customizable usage and help categories

```
<dependency>
    <groupId>de.hasait</groupId>
    <artifactId>clap</artifactId>
    <version>1.3.0</version>
</dependency>
```

## Examples

### Basic usage

```java
public class BasicCLI {
    public static void main(String[] args) {
        CLAP clap = new CLAP();
        CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "Increase verbosity level");
        CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "Print help", true);

        CLAPResult result = clap.parse(args);

        if (result.contains(helpOption)) {
            clap.printUsageAndHelp(System.out);
            return;
        }

        int verbosityLevel = result.getCount(verboseOption);

        // TODO do something useful
    }
}
```

### Decisions/Alternatives

Example with two exclusive sets of options: One set for acting as client and another one for acting as server.

```java
public class ClientServerCLI { 
    public static void main(String[] args) {
        // Usage: [-v|--verbose] [-h|--help] { {-H|--host <host>} {-p|--port <port>} | {-l|--listen <port>} }

        CLAP clap = new CLAP();
        CLAPValue<Boolean> verboseOption = clap.addFlag('v', "verbose", false, "Increase verbosity level");
        CLAPValue<Boolean> helpOption = clap.addFlag('h', "help", false, "Print help", true);
        CLAPNode decision = clap.addDecision();
        CLAPNode clientBranch = decision.addNodeList();
        clientBranch.setHelpCategory(2000, "Client");
        CLAPValue<String> clientHost = clientBranch.addOption1(String.class, 'H', "host", true, "The host to connect to", "host");
        CLAPValue<Integer> clientPort = clientBranch.addOption1(Integer.class, 'p', "port", true, "The port to connect to", "port");
        
        CLAPNode serverBranch = decision.addNodeList();
        serverBranch.setHelpCategory(2001, "Server");
        CLAPValue<Integer> serverPort = serverBranch.addOption1(Integer.class, 'l', "listen", true, "The port to listen on", "port");
        
        CLAPResult result;
        try {
            result = clap.parse(args);
        } catch (CLAPException e) {
            clap.printUsageAndHelp(System.out);
            throw e;
        }
        
        if (result.contains(helpOption)) {
            clap.printUsageAndHelp(System.out);
            return;
        }
        
        int verbosityLevel = result.getCount(verboseOption);
        System.out.println("verbosityLevel=" + verbosityLevel);
        
        if (result.contains(clientBranch)) {
            System.out.println("Connecting to " + result.getValue(clientHost) + ":" + result.getValue(clientPort) + "...");
            // TODO client
        } else if (result.contains(serverBranch)) {
            System.out.println("Listening on " + result.getValue(serverPort) + "...");
            // TODO server
        }
        
    }
}
```

### More examples

Please have a look at the various [tests](https://github.com/shasait/clap/tree/master/src/test/java/de/hasait/clap). You will also find
examples for annotation based parsing.
