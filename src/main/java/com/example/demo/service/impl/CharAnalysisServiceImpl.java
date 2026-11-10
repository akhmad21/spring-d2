package com.example.demo.service.impl;

import com.example.demo.dto.feign.QuoteResponse;
import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.InvalidKalimatException;
import com.example.demo.exception.QuoteNotFoundException;
import com.example.demo.exception.QuoteUnavailableException;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.rest.QuoteClient;
import com.example.demo.service.CharAnalysisService;
import com.example.demo.util.CharCounter;
import feign.FeignException;
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
    private final QuoteClient quoteClient;

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
    public CharAnalysisResponse simpan(String kalimat, String author) {
        // 1. Validasi input: tidak boleh null, kosong, atau hanya spasi
        if (kalimat == null || kalimat.trim().isEmpty()) {
            throw new InvalidKalimatException("Kalimat tidak boleh kosong atau hanya berupa spasi");
        }

        String trimmedKalimat = kalimat.trim();

        // 2. Validasi panjang maksimal entity (500 karakter)
        if (trimmedKalimat.length() > 500) {
            throw new InvalidKalimatException("Panjang kalimat tidak boleh melebihi 500 karakter");
        }

        // 3. Validasi duplikasi: cek apakah kalimat sudah ada di DB (Case-Insensitive)
        if (charAnalysisRepository.existsByKalimatIgnoreCase(trimmedKalimat)) {
            throw new DuplicateKalimatException("Kalimat '" + trimmedKalimat + "' sudah pernah dianalisis dan tersimpan di database");
        }

        // 4. Proses hitung karakter
        CharCounter counter = new CharCounter(trimmedKalimat);

        // 5. Simpan entity ke database beserta author (jika ada)
        CharAnalysis entity = new CharAnalysis();
        entity.setKalimat(trimmedKalimat);
        entity.setAuthor(author != null ? author.trim() : null);
        entity.setTotalHuruf(counter.getTotalHuruf());
        entity.setCreatedAt(LocalDateTime.now());

        CharAnalysis saved = charAnalysisRepository.save(entity);
        return CharAnalysisResponse.from(saved, counter.toMap());
    }

    @Override
    public CharAnalysisResponse hitungDanSimpan(CharAnalysisRequest request) {
        return simpan(request != null ? request.getKalimat() : null, null);
    }

    @Override
    public CharAnalysisResponse hitungDanSimpanQuoteAcak() {
        QuoteResponse quoteResponse;
        try {
            quoteResponse = quoteClient.getQuote();
        } catch (Exception ex) {
            throw new QuoteUnavailableException("Gagal menghubungi layanan dummyjson: " + ex.getMessage());
        }

        if (quoteResponse == null || quoteResponse.getQuote() == null || quoteResponse.getQuote().isBlank()) {
            throw new QuoteUnavailableException("Layanan quote mengembalikan data kosong");
        }

        return simpan(quoteResponse.getQuote(), quoteResponse.getAuthor());
    }

    @Override
    public CharAnalysisResponse hitungDanSimpanQuoteById(Long id) {
        QuoteResponse quoteResponse;
        try {
            quoteResponse = quoteClient.getQuoteById(id);
        } catch (FeignException.NotFound ex) {
            throw new QuoteNotFoundException(id);
        } catch (Exception ex) {
            throw new QuoteUnavailableException("Gagal menghubungi layanan dummyjson: " + ex.getMessage());
        }

        if (quoteResponse == null || quoteResponse.getQuote() == null || quoteResponse.getQuote().isBlank()) {
            throw new QuoteNotFoundException(id);
        }

        return simpan(quoteResponse.getQuote(), quoteResponse.getAuthor());
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
