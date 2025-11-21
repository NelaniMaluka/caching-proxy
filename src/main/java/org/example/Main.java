package org.example;

import org.example.model.ApiResponse;
import org.example.service.ApiService;
import org.example.service.ApiServiceImpl;
import org.example.service.ApiServiceProxy;

import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        ApiService realApiService = new ApiServiceImpl();
        ApiServiceProxy proxy = new ApiServiceProxy(realApiService);

        // Test the proxy
        CompletableFuture<ApiResponse> r1 = proxy.getDataAsync("https://catfact.ninja/fact");
        CompletableFuture<ApiResponse> r2 = proxy.getDataAsync("https://catfact.ninja/fact");
        CompletableFuture<ApiResponse> r3 = proxy.getDataAsync("https://httpbin.org/image/png");
        CompletableFuture<ApiResponse> r4 = proxy.getDataAsync("https://httpbin.org/image/png");
        CompletableFuture<ApiResponse> r5 = proxy
                .getDataAsync("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
        CompletableFuture<ApiResponse> r6 = proxy
                .getDataAsync("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");

        // Wait for the futures to complete and print the results
        printResult("Cat Fact JSON", r1.join());
        printResult("Cat Fact JSON", r2.join());
        printResult("PNG Image", r3.join());
        printResult("PNG Image", r4.join());
        printResult("PDF File", r5.join());
        printResult("PDF File", r6.join());
    }

    private static void printResult(String label, ApiResponse response) {
        System.out.println("\n==== " + label + " ====");

        if (response.isText()) {
            System.out.println("Type: TEXT");
            System.out.println("Content:\n" + response.getText());
        } else if (response.isImage()) {
            System.out.println("Type: IMAGE");
            System.out.println("Image width: " + response.getImage().getWidth());
            System.out.println("Image height: " + response.getImage().getHeight());
        } else if (response.isBinary()) {
            System.out.println("Type: BINARY");
            System.out.println("Binary size: " + response.getBinary().length + " bytes");
        } else {
            System.out.println("Unknown response type.");
        }
    }

}