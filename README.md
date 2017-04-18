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
    <version>1.2.0</version>
</dependency>
```

## Examples

Please have a look at the various [tests](https://github.com/shasait/clap/tree/master/src/test/java/de/hasait/clap).
