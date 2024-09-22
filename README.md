[![Maven Central](https://img.shields.io/maven-central/v/io.github.penguin418/message-template.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.penguin418/message-template)

# MessageTemplate

A lightweight Java library for building and processing templated messages.
It allows you to define templates with placeholders and efficiently replace them with actual values at runtime.

You can reserve positions in the template with string keys where the values will be replaced later.

In comparison to `String.format` or `StringBuilder`, the performance is not significantly slower, so it is more suitable
for scenarios where performance is not the primary concern, such as email templates, where readability and separation
from core logic are more important.

Unlike `StringBuilder`, where you add values while building the template, `MessageTemplate` allows you to define
placeholders in advance, which can be filled later.

## Features

* Simple API for building templates with placeholders.
* Support for default values in placeholders.
* Efficient processing without heavy string concatenation or regex replacements.
* Ability to retrieve the original template string.
* Customizable placeholder syntax.

## Installation

You can include the `message-template` library in your project using Maven or Gradle.

Add the following dependency to your pom.xml if you’re using Maven:

```xml

<dependency>
    <groupId>io.github.penguin418</groupId>
    <artifactId>message-template</artifactId>
    <version>0.2.0</version>
</dependency>
```

Add the following dependency to your gradle.build if you’re using Gradle:

```gradle
implementation 'io.github.penguin418:message-template:0.2.0'
```

Notice

* Java version: This library requires Java 17 or higher.
* Please refer to the latest tag on GitHub for the most recent version.

## Usage

### Basic Example

 ```java
MessageTemplate template = MessageTemplate.builder()
        .append("Hello, ")
        .reserve("name", "World")
        .append("!")
        .build();

// Using provided value
String message = template.process(Map.of("name", "Alice"));
System.out.println(message); // Output: Hello, Alice!

// Using default value
message = template.process(Map.of());
System.out.println(message); // Output: Hello, World!
 ```

### Handling Default Values

If a placeholder includes a default value and no replacement is provided, the default value will be used.

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Welcome, ${user:Guest}!")
        .build();

// No replacement provided; default value is used
String message = template.process(Map.of());
System.out.println(message); // Output: Welcome, Guest!

// Replacement provided
message = template.process(Map.of("user", "John"));
System.out.println(message); // Output: Welcome, John!
```

### Escaping Plaeholder pattern

To include literal `${...}` in your template without it being treated as a placeholder, escape the dollar sign with a
backslash.

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Price: \\${amount}")
        .build();

String message = template.process(Map.of("amount", "100"));
System.out.println(message); // Output: Price: ${amount}
```

### Retrieving the Original Template String

You can retrieve the original template string using the getTemplate() method. Placeholders are denoted by
`${keyword}` or `${keyword:defaultValue}`.

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Hello, ${name:World}!")
        .build();

String originalTemplate = template.getTemplate();
System.out.println(originalTemplate); // Output: Hello, ${name:World}!
```

### Creating Templates from Strings

You can create templates directly from a template string using appendTemplate method. 

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Dear ${title:Mr./Ms.} ${lastName},\n")
        .appendTemplate("Thank you for your purchase of ${product}!")
        .build();
String message = template.process(Map.of(
        "lastName", "Smith",
        "product", "Laptop"
));

System.out.println(message);
// Output:
// Dear Mr./Ms. Smith,
// Thank you for your purchase of Laptop!
```

## Performance

The performance measurements for the `MessageTemplate` class compared to `StringBuilder` and `String.format` are as
follows:

| Number of Parameters | MessageTemplate | StringBuilder | String.format |
|----------------------|-----------------|---------------|---------------|
| 2                    | 318 ms          | 256 ms        | 393 ms        |
| 4                    | 585 ms          | 557 ms        | 729 ms        |
| 7                    | 971 ms          | 780 ms        | 1299 ms       |
| 9                    | 1317 ms         | 1027 ms       | 1537 ms       |

* Performance was measured by repeating the operation 1,000,000 times (version 0.1.0).
* The `MessageTemplate` class is slower than `StringBuilder`, but faster than `String.format`.

# Contributing

1. Fork the repository
2. Create a new branch for your feature or bug fix. (no rules for branch naming yet)
3. Push to the branch
4. Submit a pull request
    - If change contains feature, bug fix or any code change, submit a pull request to rc branch.
        - If rc branch is not exist, make one. (rc/x.y.z, x for breaking change, y for new feature, z for bug fix)
    - If change contains documentation update, submit a pull request to master branch.

# License

This project is licensed under the MIT License - [LICENSE](LICENSE) file was generated using GitHub.
