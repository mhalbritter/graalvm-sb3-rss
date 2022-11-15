package com.example.web;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.graalvm.nativeimage.VMRuntime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Instant;

/**
 * @author Moritz Halbritter
 */
@RestController
class Controller {
    @GetMapping("/")
    String foo() {
        return "foo";
    }

    @PostMapping("/gc")
    String gc() {
        long freeMemoryBefore = Runtime.getRuntime().freeMemory();
        long maxMemoryBefore = Runtime.getRuntime().maxMemory();
        long totalMemoryBefore = Runtime.getRuntime().totalMemory();
        System.gc();
        long freeMemoryAfter = Runtime.getRuntime().freeMemory();
        long maxMemoryAfter = Runtime.getRuntime().maxMemory();
        long totalMemoryAfter = Runtime.getRuntime().totalMemory();

        return "spring-native-web Before:\n" +
                "Free: " + freeMemoryBefore + '\n' +
                "Used: " + (totalMemoryBefore - freeMemoryBefore) + '\n' +
                "Max: " + maxMemoryBefore + '\n' +
                "Total: " + totalMemoryBefore + '\n' +
                '\n' +
                "After:\n" +
                "Free: " + freeMemoryAfter + '\n' +
                "Used: " + (totalMemoryAfter - freeMemoryAfter) + '\n' +
                "Max: " + maxMemoryAfter + '\n' +
                "Total: " + totalMemoryAfter + '\n';
    }

    @PostMapping("/heapdump")
    void heapdump() throws IOException {
        try {
            VMRuntime.dumpHeap("spring-native-web-native-image-%s.hprof".formatted(Instant.now()), true);
        } catch (UnsupportedOperationException e) {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                    server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap("spring-native-web-jvm-%s.hprof".formatted(Instant.now()), true);
        }
    }
}
