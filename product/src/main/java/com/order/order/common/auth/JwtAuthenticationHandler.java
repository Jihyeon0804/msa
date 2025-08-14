package com.order.order.common.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.order.common.dto.CommonErrorDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

// 401 에러 처리
@Component
@Slf4j
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        log.error(authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);            // header에 상태 코드 세팅 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CommonErrorDTO commonErrorDTO = new CommonErrorDTO(401, "token이 없거나 유효하지 않습니다.");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(commonErrorDTO);


        PrintWriter printWriter = response.getWriter();
        printWriter.write(body);
        printWriter.flush();
    }
}
