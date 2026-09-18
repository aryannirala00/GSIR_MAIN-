package com.gsir.auth;

import com.gsir.auth.dto.AuthResponse;
import com.gsir.auth.dto.RefreshRequest;
import com.gsir.auth.dto.RegisterRequest;
import com.gsir.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceConcurrencyTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testConcurrentRefresh() throws InterruptedException {
        AuthResponse authRes = authService.register(new RegisterRequest("concurrent@example.com", "Password123"));
        String refreshToken = authRes.refreshToken();
        
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> newRefreshTokens = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    AuthResponse res = authService.refresh(new RefreshRequest(refreshToken));
                    successCount.incrementAndGet();
                    newRefreshTokens.add(res.refreshToken());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        
        // Because of pessimistic locking, exactly ONE thread should succeed
        assertEquals(1, successCount.get());
        assertEquals(numThreads - 1, failureCount.get());
        assertEquals(1, newRefreshTokens.size());
    }
}
