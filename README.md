# Dubbo Demo — Spring Boot + Dubbo 3 + Nacos Microservice Example

A microservice quickstart project based on **Spring Boot 2.7 + Apache Dubbo 3.2 + Nacos**, designed to help you quickly understand the complete workflow of Dubbo RPC calls.

The project consists of three modules:

| Module | Description                                                                               |
|------|-------------------------------------------------------------------------------------------|
| `dubbo-api` | Service interface definition (`GreetingService`)                                          |
| `dubbo-provider` | Service provider — implements the interface and registers with Nacos                      |
| `dubbo-consumer` | Service consumer — invokes the Provider via Dubbo RPC and exposes a REST endpoint to test |

---

## 1. Get the source code

https://github.com/benmaombm-ui/Dubbo-demo

---

## 2. How to Build

Run the following command from the project root directory:

```bash
mvn clean package -DskipTests
```

After a successful build, the fat jars are located in each module's `target` directory:

```
dubbo-provider/target/dubbo-provider-1.0.0-SNAPSHOT.jar
dubbo-consumer/target/dubbo-consumer-1.0.0-SNAPSHOT.jar
```

---

## 3. How to Run

### Step 1: Start the Provider

```bash
java -jar dubbo-provider/target/dubbo-provider-1.0.0-SNAPSHOT.jar \
  --dubbo.registry.address=nacos://your-nacos-ip:8848
```

The Provider listens on the following ports by default:
- HTTP port: `8081`
- Dubbo protocol port: `20880`

### Step 2: Start the Consumer

```bash
java -jar dubbo-consumer/target/dubbo-consumer-1.0.0-SNAPSHOT.jar \
  --dubbo.registry.address=nacos://your-nacos-ip:8848
```

The Consumer listens on HTTP port `8082` by default.

---

## 4. How to Verify

### Verify Provider Startup

The following log message indicates a successful startup:

```
Started ProviderApplication in x.xxx seconds
```

You should also see log entries showing that the Dubbo service has been registered with Nacos.

### Verify Consumer Startup

The following log message indicates a successful startup:

```
Started ConsumerApplication in x.xxx seconds
```

### Verify End-to-End Call

```bash
curl http://localhost:8082/greeting?name=Dubbo
```

Expected response:

```
Hello, Dubbo! This response is from Dubbo Provider.
```

## 5. Project Structure

```
Dubbo-demo/
├── pom.xml                          # Parent POM — manages dependency versions and modules
├── dubbo-api/                       # Interface definition module
│   ├── pom.xml
│   └── src/main/java/com/example/dubbo/api/
│       └── GreetingService.java     # Dubbo service interface
├── dubbo-provider/                  # Service provider module
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/dubbo/provider/
│       │   ├── ProviderApplication.java    # Application entry point
│       │   └── GreetingServiceImpl.java    # Interface implementation
│       └── resources/
│           └── application.yml             # Configuration (port, Nacos address, etc.)
├── dubbo-consumer/                  # Service consumer module
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/dubbo/consumer/
│       │   ├── ConsumerApplication.java    # Application entry point
│       │   └── GreetingController.java     # REST endpoint — calls the remote service
│       └── resources/
│           └── application.yml             # Configuration (port, Nacos address, etc.)
└── README.md
```

**Key Files:**

- **`GreetingService.java`**: Defines the `sayHello(String name)` interface. Both the Provider and Consumer modules depend on this module.
- **`GreetingServiceImpl.java`**: The concrete implementation of the interface, registered as a Dubbo service via the `@DubboService` annotation.
- **`GreetingController.java`**: A Spring MVC controller that injects the remote service proxy via `@DubboReference` and exposes the `/greeting` REST endpoint.
- **`ProviderApplication.java` / `ConsumerApplication.java`**: Spring Boot application entry points, with `@EnableDubbo` to enable Dubbo functionality.

---

## 6. Request Flow

When a user visits `http://localhost:8082/greeting?name=Dubbo`, the complete call flow is illustrated below:

```
  User (curl / browser)
        │
        │  HTTP GET /greeting?name=Dubbo
        ▼
┌───────────────────────────────────────────┐
│  dubbo-consumer (port 8082)               │
│                                           │
│  GreetingController                       │
│  ├─ @GetMapping("/greeting")              │
│  └─ @DubboReference → GreetingService     │
└─────────────────────┬─────────────────────┘
                      │
                      │  Dubbo RPC call (port 20880)
                      │  Provider address resolved via Nacos
                      ▼
┌───────────────────────────────────────────┐
│  dubbo-provider (port 8081)               │
│                                           │
│  GreetingServiceImpl                      │
│  └─ @DubboService                         │
│     sayHello("Dubbo")                     │
│       → returns "Hello, Dubbo! ..."       │
└─────────────────────┬─────────────────────┘
                      │  Register / Discover
                      ▼
┌───────────────────────────────────────────┐
│  Nacos Registry (port 8848)               │
│                                           │
│  ┌─ Provider registers on startup         │
│  └─ Consumer subscribes on startup        │
└───────────────────────────────────────────┘
```

**Step-by-Step Breakdown:**

1. **User sends an HTTP request** — The user sends `GET http://localhost:8082/greeting?name=Dubbo` to the Consumer's REST endpoint via a browser or `curl`.
2. **Consumer receives the request** — The `@GetMapping("/greeting")` method in `GreetingController` handles the request and calls `greetingService.sayHello("Dubbo")` through the remote service proxy injected by `@DubboReference`.
3. **Consumer discovers the Provider via Nacos** — Under the hood, the Dubbo framework queries the Nacos registry to obtain the list of Provider instances for `GreetingService` (e.g., `192.168.x.x:20880`) and selects one based on the load balancing strategy.
4. **Consumer initiates a Dubbo RPC call** — The Consumer sends a remote procedure call to the selected Provider instance over the Dubbo protocol (persistent TCP connection, default port `20880`).
5. **Provider processes the request and returns the result** — The `GreetingServiceImpl` on the Provider side (annotated with `@DubboService`) executes the `sayHello("Dubbo")` method and produces the return value `"Hello, Dubbo! This response is from Dubbo Provider."`.
6. **Result is returned to the user** — The Provider sends the result back to the Consumer via the Dubbo protocol, and the Consumer returns it as the HTTP response body to the user.

---
