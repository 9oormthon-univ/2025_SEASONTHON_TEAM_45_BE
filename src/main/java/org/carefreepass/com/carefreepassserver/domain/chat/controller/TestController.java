package org.carefreepass.com.carefreepassserver.domain.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/health")
    public ApiResponseTemplate<String> healthCheck() {
        return ApiResponseTemplate.ok()
                .code("TEST_2001")
                .message("서버가 정상 작동 중입니다.")
                .body("OK");
    }
    
    @PostMapping("/echo")
    public ApiResponseTemplate<String> echo(@RequestBody String message) {
        log.info("Echo test: {}", message);
        return ApiResponseTemplate.ok()
                .code("TEST_2002")
                .message("메시지 수신 완료")
                .body("Echo: " + message);
    }
}