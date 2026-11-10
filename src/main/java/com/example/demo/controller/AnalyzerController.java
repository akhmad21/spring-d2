package com.example.demo.controller;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.service.CharAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/api/v1")
public class AnalyzerController {

    private final CharAnalysisService charAnalysisService;

    @GetMapping
    public BaseResponse<List<CharAnalysisListResponse>> cari(@RequestParam(required = false) String kalimat) {
        return BaseResponse.ok(charAnalysisService.cari(kalimat));
    }

    @PostMapping
    public BaseResponse<CharAnalysisResponse> hitungDanSimpan(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @RequestBody CharAnalysisRequest request
    ) {
        String kalimat = request != null ? request.getKalimat() : null;
        log.info("X-Request-Id: {}, kalimat: {}", requestId, kalimat);
        return BaseResponse.ok(charAnalysisService.hitungDanSimpan(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CharAnalysisResponse>> getDetailById(@PathVariable Long id) {
        BaseResponse<CharAnalysisResponse> response = charAnalysisService.getDetailById(id);
        if ("failed".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/minimal")
    public BaseResponse<List<CharAnalysisResponse>> cariMinimalHuruf(@RequestParam(defaultValue = "0") int min) {
        return BaseResponse.ok(charAnalysisService.cariMinimalHuruf(min));
    }

    @GetMapping("/terbaru")
    public BaseResponse<List<CharAnalysisResponse>> cariTerbaru() {
        return BaseResponse.ok(charAnalysisService.cariTerbaru());
    }
}
