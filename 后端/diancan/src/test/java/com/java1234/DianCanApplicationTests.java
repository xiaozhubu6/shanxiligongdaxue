package com.java1234;

import com.java1234.util.MD5Util;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DianCanApplicationTests {

    /**
     * 测试方法：用于验证Spring上下文是否能够正常加载
     * 这是一个JUnit测试用例，通常用于检查Spring应用程序配置是否正确
     * 如果测试通过，表示Spring上下文成功加载，所有Bean都能正确初始化
     */
    @Test  // 标记这是一个测试方法，由JUnit测试框架执行
    void contextLoads() {
    }

    @Test
    void generateMD5() {
        String password = "admin";
        String encryptedPassword = MD5Util.MD5Encode(password, "UTF-8");
        System.out.println("原始密码: " + password);
        System.out.println("MD5加密后: " + encryptedPassword);
    }
}
