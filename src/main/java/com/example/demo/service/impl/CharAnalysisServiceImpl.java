package com.example.demo.service.impl;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.InvalidKalimatException;
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
        // 1. Validasi input: tidak boleh null, kosong, atau hanya spasi
        if (request == null || request.getKalimat() == null || request.getKalimat().trim().isEmpty()) {
            throw new InvalidKalimatException("Kalimat tidak boleh kosong atau hanya berupa spasi");
        }

        String kalimat = request.getKalimat().trim();

        // 2. Validasi panjang maksimal entity (255 karakter)
        if (kalimat.length() > 255) {
            throw new InvalidKalimatException("Panjang kalimat tidak boleh melebihi 255 karakter");
        }

        // 3. Validasi duplikasi: cek apakah kalimat sudah ada di DB (Case-Insensitive)
        if (charAnalysisRepository.existsByKalimatIgnoreCase(kalimat)) {
            throw new DuplicateKalimatException("Kalimat '" + kalimat + "' sudah pernah dianalisis dan tersimpan di database");
        }

        // 3. Proses hitung karakter
        CharCounter counter = new CharCounter(kalimat);

        // 4. Simpan entity ke database
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