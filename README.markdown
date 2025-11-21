# Cacheing Proxy API Service 🚀

A **high-performance Java-based caching proxy** that fetches, caches, and serves API responses — supporting **text, images, and binary files** (PDFs, ZIPs, etc.). Built with modern **Java 23** features, **HTTP Client**, **Failsafe** for resilient retries, and a custom **LRU cache**.

Perfect for reducing API load, speeding up repeated requests, and handling unreliable third-party services gracefully.

---

## Features

| Feature                        | Description                                                                 |
|-------------------------------|-----------------------------------------------------------------------------|
| **Smart LRU Caching**         | Evicts least recently used entries with optional per-entry expiration       |
| **Multi-Type Support**        | Handles JSON/Text, Images (PNG/JPG/GIF), and Binary files (PDF, ZIP, etc.)  |
| **Automatic Content Detection**| Uses `Content-Type` header to classify responses accurately                |
| **Resilient Retries**         | Failsafe-powered retry with exponential backoff (up to 3 attempts)         |
| **Rate Limiting**             | Configurable per-URL rate limiting to prevent throttling                    |
| **Asynchronous Fetching**     | Non-blocking requests using `CompletableFuture` and thread pool             |
| **Safe URL Handling**         | Blocks malformed or potentially dangerous URLs                             |
| **Comprehensive Logging**     | Powered by Log4j2 — logs cache hits/misses, retries, and errors             |

---

## Supported Response Types

The proxy returns a unified `ApiResponse` object with easy access:

```java
response.isText()     → boolean
response.getText()    → String

response.isImage()    → boolean
response.getImage()   → BufferedImage

response.isBinary()   → boolean
response.getBinary()  → byte[]
```

---

## Usage Example

```java
public class Main {
    public static void main(String[] args) {
        ApiService realApiService = new ApiServiceImpl();
        ApiServiceProxy proxy = new ApiServiceProxy(realApiService);

        CompletableFuture<ApiResponse> r1 = proxy.getDataAsync("https://catfact.ninja/fact");
        CompletableFuture<ApiResponse> r2 = proxy.getDataAsync("https://httpbin.org/image/png");
        CompletableFuture<ApiResponse> r3 = proxy.getDataAsync("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");

        printResult("Cat Fact JSON", r1.join());
        printResult("PNG Image", r2.join());
        printResult("PDF File", r3.join());
    }

    private static void printResult(String label, ApiResponse response) {
        System.out.println("\n==== " + label + " ====");
        if (response.isText()) {
            System.out.println("Type: TEXT");
            System.out.println("Content:\n" + response.getText());
        } else if (response.isImage()) {
            System.out.println("Type: IMAGE");
            System.out.println("Width: " + response.getImage().getWidth() + "px");
            System.out.println("Height: " + response.getImage().getHeight() + "px");
        } else if (response.isBinary()) {
            System.out.println("Type: BINARY");
            System.out.println("Size: " + response.getBinary().length + " bytes");
        }
    }
}
```

### Example Outputs

**JSON/Text Response**
```
==== Cat Fact JSON ====
Type: TEXT
Content:
{"fact":"Cats sleep 70% of their lives.","length":34}
```

**Image Response**
```
==== PNG Image ====
Type: IMAGE
Width: 100px
Height: 100px
```

**Binary/PDF Response**
```
==== PDF File ====
Type: BINARY
Size: 13200 bytes
```

---

## Installation & Running

```bash
git clone https://github.com/yourusername/caching-proxy-api.git
cd caching-proxy-api

# Compile (with modules if using Java 23+)
javac --release 23 -d out src/main/java/module-info.java src/main/java/org/example/**/*.java

# Run
java --class-path out org.example.Main
```

> Requires **Java 23+**

---

## Dependencies

| Dependency         | Purpose                              |
|--------------------|---------------------------------------|
| Java 23            | Core language & HTTP Client           |
| Failsafe 3.3.1     | Retry policies & circuit breaking     |
| Log4j2 2.20.0      | Structured logging                    |
| Lombok 1.18.32     | Reduces boilerplate (optional)        |

---

## Project Structure

```
src/main/java/org/example/
├─ Main.java
├─ service/
│   ├─ ApiService.java
│   ├─ ApiServiceImpl.java
│   └─ ApiServiceProxy.java
└─ model/
    ├─ ApiResponse.java
    └─ CacheEntry.java
```

---

## Key Concepts

- **LRU Cache**: Custom implementation with automatic eviction
- **Failsafe RetryPolicy**: Handles transient network failures gracefully
- **Async Execution**: Uses virtual threads (Java 23+) or fixed thread pool
- **Content-Type Detection**: Robust classification of all major MIME types
- **Safety First**: Blocks suspicious or malformed URLs

---

## Future Enhancements

- Disk-based persistent cache
- Streaming support for large files
- Prometheus metrics endpoint (cache hit ratio, latency)
- Configurable cache size & TTL via properties
- HTTP/2 & HTTP/3 support

---

## License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.