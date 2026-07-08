package com.java1234.interceptor;

import com.java1234.entity.R;
import com.java1234.util.JwtUtils;
import com.java1234.util.StringUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 鉴权拦截器

 */
public class SysInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        System.out.println(path);
        
        // 允许公告接口无需登录访问
        if (path.equals("/announcement/list")) {
            return true;
        }
        
        if(handler instanceof HandlerMethod){
            String token = request.getHeader("Authorization");
            System.out.println("token:"+token);
            if(StringUtil.isEmpty(token)){
                System.out.println("token为空！");
                throw new RuntimeException("请先登录");
            }else{
                Claims claims = JwtUtils.validateJWT(token).getClaims();
                // 管理员 admin开头
                if(path.startsWith("/admin")){
                    if(claims==null||!claims.getSubject().equals("admin")||!claims.getId().equals("-1")){
                        throw new RuntimeException("无权限访问");
                    }else{
                        System.out.println("验证成功");
                        return true;
                    }
                }else{ // 普通用户 鉴权
                    if(claims==null){
                        throw new RuntimeException("登录已过期，请重新登录");
                    }else{
                        System.out.println("验证成功");
                        return true;
                    }
                }
            }
        }else{
            return true;
        }
    }
}