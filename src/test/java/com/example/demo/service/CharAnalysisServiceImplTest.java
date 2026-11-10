package com.example.demo.service;

import com.example.demo.dto.feign.QuoteResponse;
import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.InvalidKalimatException;
import com.example.demo.exception.QuoteUnavailableException;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.rest.QuoteClient;
import com.example.demo.service.impl.CharAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CharAnalysisServiceImplTest {

    private CharAnalysisRepository charAnalysisRepository;
    private QuoteClient quoteClient;
    private CharAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        charAnalysisRepository = Mockito.mock(CharAnalysisRepository.class);
        quoteClient = Mockito.mock(QuoteClient.class);
        service = new CharAnalysisServiceImpl(charAnalysisRepository, quoteClient);
    }

    @Test
    void testHitungDanSimpanPanjangMelebihi255ThrowsInvalidKalimatException() {
        CharAnalysisRequest request = new CharAnalysisRequest();
        request.setKalimat("a".repeat(256));

        assertThrows(InvalidKalimatException.class, () -> service.hitungDanSimpan(request));
    }

    @Test
    void testHitungDanSimpanKosongThrowsInvalidKalimatException() {
        CharAnalysisRequest request = new CharAnalysisRequest();
        request.setKalimat("   ");

        assertThrows(InvalidKalimatException.class, () -> service.hitungDanSimpan(request));
    }

    @Test
    void testHitungDanSimpanDuplicateThrowsDuplicateKalimatException() {
        CharAnalysisRequest request = new CharAnalysisRequest();
        request.setKalimat("gajah mati");

        when(charAnalysisRepository.existsByKalimatIgnoreCase(anyString())).thenReturn(true);

        assertThrows(DuplicateKalimatException.class, () -> service.hitungDanSimpan(request));
    }

    @Test
    void testHitungDanSimpanQuoteAcakSuccess() {
        QuoteResponse mockQuote = new QuoteResponse(1L, "Life is beautiful", "Anonymous");
        when(quoteClient.getQuote()).thenReturn(mockQuote);
        when(charAnalysisRepository.existsByKalimatIgnoreCase(anyString())).thenReturn(false);

        CharAnalysis savedEntity = new CharAnalysis();
        savedEntity.setId(10L);
        savedEntity.setKalimat("Life is beautiful");
        savedEntity.setTotalHuruf(15);
        when(charAnalysisRepository.save(any(CharAnalysis.class))).thenReturn(savedEntity);

        CharAnalysisResponse response = service.hitungDanSimpanQuoteAcak();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Life is beautiful", response.getKalimat());
        assertEquals(15, response.getTotalHuruf());
    }

    @Test
    void testHitungDanSimpanQuoteAcakThrowsQuoteUnavailableException() {
        when(quoteClient.getQuote()).thenThrow(new RuntimeException("Connection timed out"));

        assertThrows(QuoteUnavailableException.class, () -> service.hitungDanSimpanQuoteAcak());
    }
}
