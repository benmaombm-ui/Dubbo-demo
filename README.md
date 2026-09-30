# Dubbo Demo — Spring Boot + Dubbo 3 + Nacos Microservice Example

A microservice quickstart project based on **Spring Boot 2.7 + Apache Dubbo 3.2 + Nacos**, designed to help you quickly understand the complete workflow of Dubbo RPC calls. The project demonstrates **dual-protocol** support — both the classic **Dubbo** protocol and the next-generation **Triple** (gRPC-compatible) protocol — so you can compare them side by side.

The project consists of three modules:

| Module | Description |
|------|---|
| `dubbo-api` | Service interface definition (`GreetingService`) |
| `dubbo-provider` | Service provider — implements the interface, exposes it over **both Dubbo (port 20880) and Triple (port 50051)** protocols, and registers with Nacos |
| `dubbo-consumer` | Service consumer — provides two REST endpoints (`/greeting/dubbo` and `/greeting/triple`) that invoke the Provider via the corresponding protocol |

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

To specify a Nacos namespace:

```bash
java -jar dubbo-provider/target/dubbo-provider-1.0.0-SNAPSHOT.jar \
  --dubbo.registry.address="nacos://your-nacos-ip:8848?namespace=<your-namespace-id>"
```

> **Note:** By default, the `public` namespace is used and no extra parameter is needed. If you need to register the service under a specific namespace, use the command above. The `namespace` value must be the namespace **ID** (not the display name) as shown in the Nacos console.

The Provider listens on the following ports by default:
- HTTP port: `8081`
- Dubbo protocol port: `20880`
- Triple protocol port: `50051`

### Step 2: Start the Consumer

```bash
java -jar dubbo-consumer/target/dubbo-consumer-1.0.0-SNAPSHOT.jar \
  --dubbo.registry.address=nacos://your-nacos-ip:8848
```

To specify a Nacos namespace:

```bash
java -jar dubbo-consumer/target/dubbo-consumer-1.0.0-SNAPSHOT.jar \
  --dubbo.registry.address="nacos://your-nacos-ip:8848?namespace=<your-namespace-id>"
```

> **Note:** By default, the `public` namespace is used and no extra parameter is needed. If you need to connect to a specific namespace, use the command above. The `namespace` value must be the namespace **ID** (not the display name) as shown in the Nacos console. Make sure the Consumer uses the **same namespace** as the Provider, otherwise service discovery will fail.

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

The Consumer exposes two REST endpoints, one for each protocol:

```bash
# Verify via Dubbo protocol
curl http://localhost:8082/greeting/dubbo?name=Dubbo

# Verify via Triple protocol
curl http://localhost:8082/greeting/triple?name=Triple
```

Expected responses:

```
Hello, Dubbo! This response is from Dubbo Provider.
Hello, Triple! This response is from Dubbo Provider.
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
- **`GreetingServiceImpl.java`**: The concrete implementation of the interface. Annotated with `@DubboService(protocol = {"dubbo", "triple"})` to expose the service over **both** Dubbo and Triple protocols simultaneously.
- **`GreetingController.java`**: A Spring MVC controller with **two** injected service proxies:
  - `@DubboReference(protocol = "dubbo")` — routes calls through the Dubbo protocol (endpoint `/greeting/dubbo`)
  - `@DubboReference(protocol = "tri")` — routes calls through the Triple protocol (endpoint `/greeting/triple`)
  > **Note:** In Dubbo 3.x the SPI name for the Triple protocol is `tri`, not `triple`.
- **`ProviderApplication.java` / `ConsumerApplication.java`**: Spring Boot application entry points, with `@EnableDubbo` to enable Dubbo functionality.

---

## 6. Request Flow

The Consumer exposes two REST endpoints. Depending on which endpoint the user hits, the call is routed through a different protocol:

```
  User (curl / browser)
        │
        ├─ HTTP GET /greeting/dubbo?name=Dubbo
        │                                 ├─ HTTP GET /greeting/triple?name=Triple
        ▼                                 ▼
┌──────────────────────────────────────────────────────────┐
│  dubbo-consumer (port 8082)                              │
│                                                          │
│  GreetingController                                      │
│  ├─ /greeting/dubbo   → @DubboReference(protocol="dubbo")│
│  └─ /greeting/triple  → @DubboReference(protocol="tri")  │
└────────────┬──────────────────────────┬──────────────────┘
             │                          │
             │ Dubbo protocol           │ Triple protocol
             │ (port 20880)             │ (port 50051)
             ▼                          ▼
┌──────────────────────────────────────────────────────────┐
│  dubbo-provider (port 8081)                              │
│                                                          │
│  GreetingServiceImpl                                     │
│  └─ @DubboService(protocol={"dubbo","triple"})           │
│     sayHello(name)                                       │
│       → returns "Hello, {name}! ..."                     │
└────────────────────────┬─────────────────────────────────┘
                         │  Register / Discover
                         ▼
┌──────────────────────────────────────────────────────────┐
│  Nacos Registry (port 8848)                              │
│                                                          │
│  ┌─ Provider registers on startup (both protocols)       │
│  └─ Consumer subscribes on startup                       │
└──────────────────────────────────────────────────────────┘
```

**Step-by-Step Breakdown:**

1. **User sends an HTTP request** — The user sends either `GET /greeting/dubbo?name=Dubbo` or `GET /greeting/triple?name=Triple` to the Consumer (port `8082`) via a browser or `curl`.
2. **Consumer receives the request** — `GreetingController` has two handler methods, each backed by a different `@DubboReference` proxy:
   - `/greeting/dubbo` uses `@DubboReference(protocol = "dubbo")` → calls via the Dubbo protocol.
   - `/greeting/triple` uses `@DubboReference(protocol = "tri")` → calls via the Triple protocol.
3. **Consumer discovers the Provider via Nacos** — The Dubbo framework queries the Nacos registry to obtain the list of Provider instances for `GreetingService` and selects one based on the load balancing strategy.
4. **Consumer initiates the RPC call** — Depending on the chosen protocol:
   - **Dubbo protocol**: persistent TCP connection to the Provider's port `20880`.
   - **Triple protocol**: HTTP/2 (gRPC-compatible) connection to the Provider's port `50051`.
5. **Provider processes the request and returns the result** — `GreetingServiceImpl` (annotated with `@DubboService(protocol = {"dubbo", "triple"})`) executes `sayHello(name)` and returns `"Hello, {name}! This response is from Dubbo Provider."`.
6. **Result is returned to the user** — The Provider sends the result back through the same protocol, and the Consumer returns it as the HTTP response body.

---

## 7. Dependencies & Protocol Notes

- **Dubbo 3.x built-in Triple support**: Apache Dubbo 3.x ships with the Triple protocol out of the box — no additional Dubbo modules are required.
- **`protobuf-java` dependency**: The Triple protocol relies on Protocol Buffers for serialization. Both `dubbo-provider` and `dubbo-consumer` include `com.google.protobuf:protobuf-java:3.25.5` as a dependency.
- **Protocol SPI naming**: In Dubbo 3.x configuration, the Triple protocol's SPI name is **`tri`**, not `triple`. Use `tri` in `@DubboReference(protocol = "tri")` and in YAML configuration (`name: tri`).
- **Protocol configuration in `application.yml`**: The Provider declares both protocols under `dubbo.protocols`:
  ```yaml
  dubbo:
    protocols:
      dubbo:
        name: dubbo
        port: 20880
      triple:
        name: tri
        port: 50051
  ```

---
