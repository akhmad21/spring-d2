package com.example.demo.service.impl;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.service.CharAnalysisService;
import com.example.demo.util.CharCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CharAnalysisServiceImpl implements CharAnalysisService {

    private final CharAnalysisRepository charAnalysisRepository;

    @Override
    public List<CharAnalysisListResponse> cari(String kata) {
        List<CharAnalysis> semua;
        if (kata == null || kata.isBlank()) {
            semua = charAnalysisRepository.findAll();
        } else {
            semua = charAnalysisRepository.findByKalimatContainingIgnoreCase(kata);
        }

        List<CharAnalysisListResponse> hasil = new ArrayList<>();
        for (CharAnalysis data : semua) {
            hasil.add(CharAnalysisListResponse.from(data));
        }
        return hasil;
    }

    @Override
    public CharAnalysisResponse hitungDanSimpan(CharAnalysisRequest request) {
        String kalimat = request != null && request.getKalimat() != null ? request.getKalimat() : "";
        CharCounter counter = new CharCounter(kalimat);

        CharAnalysis entity = new CharAnalysis();
        entity.setKalimat(kalimat);
        entity.setTotalHuruf(counter.getTotalHuruf());
        entity.setCreatedAt(LocalDateTime.now());

        CharAnalysis saved = charAnalysisRepository.save(entity);
        return CharAnalysisResponse.from(saved, counter.toMap());
    }

    @Override
    public BaseResponse<CharAnalysisResponse> getDetailById(Long id) {
        return charAnalysisRepository.findById(id)
                .map(data -> BaseResponse.ok(toResponse(data)))
                .orElseGet(() -> BaseResponse.error("Data dengan id " + id + " tidak ditemukan"));
    }

    @Override
    public List<CharAnalysisResponse> cariMinimalHuruf(int min) {
        List<CharAnalysis> list = charAnalysisRepository.findByTotalHurufMinimal(min);
        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (CharAnalysis data : list) {
            hasil.add(toResponse(data));
        }
        return hasil;
    }

    @Override
    public List<CharAnalysisResponse> cariTerbaru() {
        List<CharAnalysis> list = charAnalysisRepository.findTop5Terbaru();
        List<CharAnalysisResponse> hasil = new ArrayList<>();
        for (CharAnalysis data : list) {
            hasil.add(toResponse(data));
        }
        return hasil;
    }

    private CharAnalysisResponse toResponse(CharAnalysis data) {
        CharCounter counter = new CharCounter(data.getKalimat());
        return CharAnalysisResponse.from(data, counter.toMap());
    }
}
