package com.example.demo.service;

import com.example.demo.dto.feign.QuoteResponse;
import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.entity.CharAnalysis;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.InvalidKalimatException;
import com.example.demo.exception.QuoteNotFoundException;
import com.example.demo.exception.QuoteUnavailableException;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.rest.QuoteClient;
import com.example.demo.service.impl.CharAnalysisServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

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
    void testHitungDanSimpanPanjangMelebihi500ThrowsInvalidKalimatException() {
        CharAnalysisRequest request = new CharAnalysisRequest();
        request.setKalimat("a".repeat(501));

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
    void testSimpanWithAuthorSuccess() {
        when(charAnalysisRepository.existsByKalimatIgnoreCase(anyString())).thenReturn(false);

        CharAnalysis savedEntity = new CharAnalysis();
        savedEntity.setId(7L);
        savedEntity.setKalimat("Your heart is the size of an ocean.");
        savedEntity.setAuthor("Rumi");
        savedEntity.setTotalHuruf(28);
        when(charAnalysisRepository.save(any(CharAnalysis.class))).thenReturn(savedEntity);

        CharAnalysisResponse response = service.simpan("Your heart is the size of an ocean.", "Rumi");

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("Your heart is the size of an ocean.", response.getKalimat());
        assertEquals("Rumi", response.getAuthor());
        assertEquals(28, response.getTotalHuruf());
    }

    @Test
    void testHitungDanSimpanQuoteAcakSuccess() {
        QuoteResponse mockQuote = new QuoteResponse(1L, "Life is beautiful", "Anonymous");
        when(quoteClient.getQuote()).thenReturn(mockQuote);
        when(charAnalysisRepository.existsByKalimatIgnoreCase(anyString())).thenReturn(false);

        CharAnalysis savedEntity = new CharAnalysis();
        savedEntity.setId(10L);
        savedEntity.setKalimat("Life is beautiful");
        savedEntity.setAuthor("Anonymous");
        savedEntity.setTotalHuruf(15);
        when(charAnalysisRepository.save(any(CharAnalysis.class))).thenReturn(savedEntity);

        CharAnalysisResponse response = service.hitungDanSimpanQuoteAcak();

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Life is beautiful", response.getKalimat());
        assertEquals("Anonymous", response.getAuthor());
        assertEquals(15, response.getTotalHuruf());
    }

    @Test
    void testHitungDanSimpanQuoteAcakThrowsQuoteUnavailableException() {
        when(quoteClient.getQuote()).thenThrow(new RuntimeException("Connection timed out"));

        assertThrows(QuoteUnavailableException.class, () -> service.hitungDanSimpanQuoteAcak());
    }

    @Test
    void testHitungDanSimpanQuoteByIdSuccess() {
        QuoteResponse mockQuote = new QuoteResponse(1L, "Your heart is an ocean", "Rumi");
        when(quoteClient.getQuoteById(1L)).thenReturn(mockQuote);
        when(charAnalysisRepository.existsByKalimatIgnoreCase(anyString())).thenReturn(false);

        CharAnalysis savedEntity = new CharAnalysis();
        savedEntity.setId(1L);
        savedEntity.setKalimat("Your heart is an ocean");
        savedEntity.setAuthor("Rumi");
        savedEntity.setTotalHuruf(19);
        when(charAnalysisRepository.save(any(CharAnalysis.class))).thenReturn(savedEntity);

        CharAnalysisResponse response = service.hitungDanSimpanQuoteById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Your heart is an ocean", response.getKalimat());
        assertEquals("Rumi", response.getAuthor());
    }

    @Test
    void testHitungDanSimpanQuoteByIdThrowsQuoteNotFoundException() {
        Request request = Request.create(Request.HttpMethod.GET, "/quotes/999", Collections.emptyMap(), null, new RequestTemplate());
        when(quoteClient.getQuoteById(999L)).thenThrow(new FeignException.NotFound("Not Found", request, null, Collections.emptyMap()));

        assertThrows(QuoteNotFoundException.class, () -> service.hitungDanSimpanQuoteById(999L));
    }
}
