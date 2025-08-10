package me.critiq.backend;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class CritiQBackendApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCode() throws Exception {
        var session = new MockHttpSession();

        mockMvc.perform(
                get("/user/code")
                        .param("email", "karigenb2572@gmail.com")
                        .session(session)
        ).andExpect(status().isOk());

        var code = String.valueOf(session.getAttribute("code"));
        assert StringUtils.hasText(code);
        log.info("session中的验证码为: {}", code);
    }

}
