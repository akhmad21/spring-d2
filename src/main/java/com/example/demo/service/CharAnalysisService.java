package com.example.demo.service;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;

import java.util.List;

public interface CharAnalysisService {
    List<CharAnalysisListResponse> cari(String kata);
    CharAnalysisResponse hitungDanSimpan(CharAnalysisRequest request);
    BaseResponse<CharAnalysisResponse> getDetailById(Long id);
    List<CharAnalysisResponse> cariMinimalHuruf(int min);
    List<CharAnalysisResponse> cariTerbaru();
}
