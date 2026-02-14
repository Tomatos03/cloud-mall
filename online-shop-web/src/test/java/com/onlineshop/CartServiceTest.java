package com.onlineshop;

import cn.hutool.core.lang.UUID;
import com.onlineshop.framework.models.auth.service.ITokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/2/10
 */
@SpringBootTest
public class CartServiceTest {
    private static final int TOKEN_COUNT = 100;
    @Autowired
    private ITokenService tokenService;

    @Test
    public void generateTokensToCsv() throws IOException {
        try (FileWriter writer = new FileWriter("tokens.csv")) {
            writer.write("token\n");
            for (int i = 0; i < TOKEN_COUNT; i++) {
                String uuid = UUID.fastUUID()
                                  .toString(true);

                writer.write(uuid + "\n");
            }
        }
    }
}
