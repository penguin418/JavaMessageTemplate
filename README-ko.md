[![Maven Central](https://img.shields.io/maven-central/v/io.github.penguin418/message-template.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.penguin418/message-template)

# MessageTemplate

템플릿 메시지를 만들고 처리하기 위한 경량의 Java 라이브러리입니다.
플레이스홀더가 있는 템플릿을 정의하고 런타임에 실제 값으로 효율적으로 대체할 수 있습니다.

템플릿에서 문자열 키로 위치를 예약하고 나중에 그 값들을 대체할 수 있습니다.

`String.format`이나 `StringBuilder`와 비교하여 성능이 크게 느리지 않으므로, 성능이 주요 관심사가 아닌 시나리오(예: 이메일 템플릿)에서 더 적합하며, 가독성과 핵심 로직과의 분리가 더
중요합니다.

`StringBuilder`에서 값을 추가하면서 템플릿을 구축하는 것과 달리, `MessageTemplate`은 나중에 채울 수 있는 플레이스홀더를 미리 정의할 수 있습니다.

## Features

* 플레이스홀더로 템플릿을 구축하기 위한 간단한 API.
* 플레이스홀더에서 기본값 지원.
* 무거운 문자열 연결이나 정규식 대체 없이 효율적인 처리.
* 원본 템플릿 문자열을 가져올 수 있는 기능.
* 사용자 정의 가능한 플레이스홀더 구문.

## Insatllation

Maven이나 Gradle을 사용하여 프로젝트에 `message-template` 라이브러리를 포함할 수 있습니다.

Maven을 사용하는 경우 pom.xml에 다음 의존성을 추가하십시오:

```xml

<dependency>
    <groupId>io.github.penguin418</groupId>
    <artifactId>message-template</artifactId>
    <version>0.2.0</version>
</dependency>
```

Gradle을 사용하는 경우 build.gradle에 다음 의존성을 추가하십시오:

```gradle
implementation 'io.github.penguin418:message-template:0.2.0'
```

Notice

* Java 버전: 이 라이브러리는 Java 17 이상이 필요합니다.

## Usage

### Basic Example | 기본 예제

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

### Handling Default Values | 기본값 처리

플레이스홀더에 기본값이 포함되어 있고 대체 값이 제공되지 않으면, 기본값이 사용됩니다.

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

### Escaping Plaeholder pattern | 플레이스홀더 그대로 사용하기

템플릿에서 플레이스홀더로 처리되지 않고 문자 그대로 `${…}`를 포함하려면, 달러 기호를 백슬래시로 이스케이프하십시오.

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Price: \\${amount}")
        .build();

String message = template.process(Map.of("amount", "100"));
System.out.println(message); // Output: Price: ${amount}
```

### Retrieving the Original Template String | 원본 템플릿 문자열 가져오기

`getTemplate()` 메서드를 사용하여 원본 템플릿 문자열을 가져올 수 있습니다.플레이스홀더는 `${keyword}` 또는 `${keyword:defaultValue}`로 표시됩니다.

```java
MessageTemplate template = MessageTemplate.builder()
        .appendTemplate("Hello, ${name:World}!")
        .build();

String originalTemplate = template.getTemplate();
System.out.println(originalTemplate); // Output: Hello, ${name:World}!
```

### Creating Templates from Strings | 문자열로부터 템플릿 생성

appendTemplate 메서드를 사용하여 템플릿 문자열로부터 직접 템플릿을 생성할 수 있습니다. 

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

MessageTemplate 클래스의 StringBuilder 및 String.format과 비교한 성능 측정치는 다음과 같습니다:

| Number of Parameters | MessageTemplate | StringBuilder | String.format |
|----------------------|-----------------|---------------|---------------|
| 2                    | 318 ms          | 256 ms        | 393 ms        |
| 4                    | 585 ms          | 557 ms        | 729 ms        |
| 7                    | 971 ms          | 780 ms        | 1299 ms       |
| 9                    | 1317 ms         | 1027 ms       | 1537 ms       |

* 성능은 작업을 1,000,000번 반복하여 측정되었습니다 (버전 0.1.0).
* `MessageTemplate` 클래스는 `StringBuilder`보다 느리지만, `String.format`보다 빠릅니다

# Contributing

1. 리포지토리를 포크하세요.
2. 기능 추가나 버그 수정을 위한 새로운 브랜치를 생성하세요. (브랜치 이름에 대한 규칙은 아직 없습니다)
3. 브랜치에 푸시하십시오.
4. 풀 리퀘스트를 제출하십시오.
    - 변경 사항이 기능, 버그 수정 또는 코드 변경을 포함하는 경우, rc 브랜치로 풀 리퀘스트를 제출하세요.
        - rc 브랜치가 없으면 생성하십시오. (rc/x.y.z, x는 브레이킹 체인지, y는 새로운 기능, z는 버그 수정)
    - 변경 사항이 문서 업데이트를 포함하는 경우, master 브랜치로 풀 리퀘스트를 제출하십시오.

# License

이 프로젝트는 MIT 라이선스로 라이선스가 부여되어 있습니다. - [LICENSE](LICENSE) 파일은 GitHub를 통해 생성하였습니다.
