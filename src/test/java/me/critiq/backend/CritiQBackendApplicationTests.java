package me.critiq.backend;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.util.IdUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class CritiQBackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdUtil idUtil;

    private ExecutorService executor = Executors.newFixedThreadPool(500);

    @Test
    void testGetCode() throws Exception {
        var session = new MockHttpSession();

        mockMvc.perform(
                get("/user/code")
                        .param("email", "karigenb2572@gmail.com")
                        .session(session)
        ).andExpect(status().isOk());

        var code = session.getAttribute("code").toString();
        assert StringUtils.hasText(code);
        log.info("session中的验证码为: {}", code);
    }

    @Test
    void testIdUtil() throws InterruptedException {
        int threadCount = 300;
        var latch = new CountDownLatch(threadCount);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                var id = idUtil.nextId("order");
                log.info("id = {}", id);
            }
            latch.countDown();
        };

        var begin = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(task);
        }
        latch.await();
        var end = System.currentTimeMillis();

        log.info("time = {}", Duration.ofMillis(end - begin));

    }

}
