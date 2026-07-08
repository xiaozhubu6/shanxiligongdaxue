package com.zhuqilong.back.controller;

import com.zhuqilong.back.common.ApiResponse;
import com.zhuqilong.back.entity.Account;
import com.zhuqilong.back.entity.Child;
import com.zhuqilong.back.entity.Elder;
import com.zhuqilong.back.mapper.AccountMapper;
import com.zhuqilong.back.mapper.ChildMapper;
import com.zhuqilong.back.mapper.ElderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录认证相关接口")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private ElderMapper elderMapper;
    
    @Autowired
    private ChildMapper childMapper;
    
    @Autowired
    private AccountMapper accountMapper;

    @PostMapping("/login")
    @Operation(summary = "统一登录接口")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            String userType = request.getUserType();

            Map<String, Object> result = new HashMap<>();

            if ("elder".equals(userType)) {
                // 老人登录：用户名格式 elder+ID，密码固定为123456
                if (!username.startsWith("elder")) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("老人用户名格式错误"));
                }
                
                try {
                    Long elderId = Long.parseLong(username.substring(5));
                    Elder elder = elderMapper.selectById(elderId);
                    
                    if (elder == null) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
                    }
                    
                    // 简化密码验证：固定密码123456
                    if (!"123456".equals(password)) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("密码错误"));
                    }
                    
                    // 获取账户信息
                    Account account = accountMapper.selectByElderId(elderId);
                    
                    result.put("id", elder.getId());
                    result.put("username", username);
                    result.put("name", elder.getName());
                    result.put("userType", "elder");
                    result.put("age", elder.getAge());
                    result.put("gender", elder.getGender());
                    result.put("balance", account != null ? account.getBalance() : 0);
                    result.put("token", "elder_token_" + elderId + "_" + System.currentTimeMillis());
                    
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("用户名格式错误"));
                }
                
            } else if ("child".equals(userType)) {
                // 子女登录：用户名格式 child+ID，密码固定为123456
                if (!username.startsWith("child")) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("子女用户名格式错误"));
                }
                
                try {
                    Long childId = Long.parseLong(username.substring(5));
                    Child child = childMapper.selectById(childId);
                    
                    if (child == null) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
                    }
                    
                    // 简化密码验证：固定密码123456
                    if (!"123456".equals(password)) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("密码错误"));
                    }
                    
                    result.put("id", child.getId());
                    result.put("username", username);
                    result.put("name", child.getName());
                    result.put("userType", "child");
                    result.put("phone", child.getPhone());
                    result.put("token", "child_token_" + childId + "_" + System.currentTimeMillis());
                    
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("用户名格式错误"));
                }
                
            } else if ("admin".equals(userType)) {
                // 管理员登录：固定用户名admin，密码admin123
                if (!"admin".equals(username) || !"admin123".equals(password)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
                }
                
                result.put("id", 1L);
                result.put("username", "admin");
                result.put("name", "系统管理员");
                result.put("userType", "admin");
                result.put("token", "admin_token_" + System.currentTimeMillis());
                
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户类型错误"));
            }

            return ResponseEntity.ok(ApiResponse.success("登录成功", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "登出接口")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            // 简化处理，实际项目中可以清除token缓存
            return ResponseEntity.ok(ApiResponse.success("登出成功", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("登出失败: " + e.getMessage()));
        }
    }

    // 登录请求DTO
    public static class LoginRequest {
        private String username;
        private String password;
        private String userType;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }
    }
}
