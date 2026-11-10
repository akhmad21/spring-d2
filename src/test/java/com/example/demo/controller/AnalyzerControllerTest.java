package com.example.demo.controller;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.dto.response.BaseResponse;
import com.example.demo.dto.response.CharAnalysisListResponse;
import com.example.demo.dto.response.CharAnalysisResponse;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.InvalidKalimatException;
import com.example.demo.exception.QuoteNotFoundException;
import com.example.demo.exception.QuoteUnavailableException;
import com.example.demo.service.CharAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalyzerControllerTest {

    private MockMvc mockMvc;
    private CharAnalysisService charAnalysisService;

    @BeforeEach
    void setUp() {
        charAnalysisService = Mockito.mock(CharAnalysisService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalyzerController(charAnalysisService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetListOnlyReturnsIdKalimatTotalHuruf() throws Exception {
        CharAnalysisListResponse item = new CharAnalysisListResponse(1L, "gajah mati", 9);
        Mockito.when(charAnalysisService.cari(null)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].kalimat").value("gajah mati"))
                .andExpect(jsonPath("$.data[0].totalHuruf").value(9))
                .andExpect(jsonPath("$.data[0].frekuensi").doesNotExist());
    }

    @Test
    void testHitungDanSimpan() throws Exception {
        CharAnalysisResponse mockResponse = new CharAnalysisResponse(
                1L,
                "gajah mati meninggalkan gading",
                27,
                Map.of('a', 6, 'g', 5)
        );
        Mockito.when(charAnalysisService.hitungDanSimpan(any(CharAnalysisRequest.class))).thenReturn(mockResponse);

        String jsonPayload = "{\"kalimat\": \"gajah mati meninggalkan gading\"}";

        mockMvc.perform(post("/api/v1")
                        .header("X-Request-Id", "req-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.kalimat").value("gajah mati meninggalkan gading"))
                .andExpect(jsonPath("$.data.totalHuruf").value(27));
    }

    @Test
    void testHitungDanSimpanDuplicateThrows409() throws Exception {
        Mockito.when(charAnalysisService.hitungDanSimpan(any(CharAnalysisRequest.class)))
                .thenThrow(new DuplicateKalimatException("Kalimat 'gajah mati' sudah pernah dianalisis dan tersimpan di database"));

        String jsonPayload = "{\"kalimat\": \"gajah mati\"}";

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Kalimat 'gajah mati' sudah pernah dianalisis dan tersimpan di database"));
    }

    @Test
    void testHitungDanSimpanInvalidThrows400() throws Exception {
        Mockito.when(charAnalysisService.hitungDanSimpan(any(CharAnalysisRequest.class)))
                .thenThrow(new InvalidKalimatException("Kalimat tidak boleh kosong atau hanya berupa spasi"));

        String jsonPayload = "{\"kalimat\": \"   \"}";

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Kalimat tidak boleh kosong atau hanya berupa spasi"));
    }

    @Test
    void testHitungDanSimpanPanjangMelebihi500Throws400() throws Exception {
        String kalimatPanjang = "a".repeat(501);
        String jsonPayload = "{\"kalimat\": \"" + kalimatPanjang + "\"}";

        mockMvc.perform(post("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Panjang kalimat tidak boleh melebihi 500 karakter"));
    }

    @Test
    void testHitungDanSimpanQuoteAcakSuccess() throws Exception {
        CharAnalysisResponse mockResponse = new CharAnalysisResponse(
                5L,
                "Life is beautiful",
                "Anonymous",
                15,
                Map.of('e', 2, 'i', 3)
        );
        Mockito.when(charAnalysisService.hitungDanSimpanQuoteAcak()).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/from-quote")
                        .header("X-Request-Id", "feign-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.kalimat").value("Life is beautiful"))
                .andExpect(jsonPath("$.data.author").value("Anonymous"))
                .andExpect(jsonPath("$.data.totalHuruf").value(15));
    }

    @Test
    void testHitungDanSimpanQuoteByIdSuccess() throws Exception {
        CharAnalysisResponse mockResponse = new CharAnalysisResponse(
                7L,
                "Your heart is the size of an ocean.",
                "Rumi",
                28,
                Map.of('a', 3, 'o', 3)
        );
        Mockito.when(charAnalysisService.hitungDanSimpanQuoteById(eq(1L))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/from-quote/1")
                        .header("X-Request-Id", "quote-id-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.kalimat").value("Your heart is the size of an ocean."))
                .andExpect(jsonPath("$.data.author").value("Rumi"))
                .andExpect(jsonPath("$.data.totalHuruf").value(28));
    }

    @Test
    void testHitungDanSimpanQuoteByIdNotFoundThrows404() throws Exception {
        Mockito.when(charAnalysisService.hitungDanSimpanQuoteById(eq(99999L)))
                .thenThrow(new QuoteNotFoundException(99999L));

        mockMvc.perform(post("/api/v1/from-quote/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Quote dengan id 99999 tidak ditemukan di DummyJSON"));
    }

    @Test
    void testHitungDanSimpanQuoteAcakExternalErrorThrows503() throws Exception {
        Mockito.when(charAnalysisService.hitungDanSimpanQuoteAcak())
                .thenThrow(new QuoteUnavailableException("Gagal menghubungi layanan dummyjson"));

        mockMvc.perform(post("/api/v1/from-quote"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Layanan quote eksternal sedang tidak tersedia: Gagal menghubungi layanan dummyjson"));
    }

    @Test
    void testDetailByIdFound() throws Exception {
        CharAnalysisResponse mockResponse = new CharAnalysisResponse(
                1L,
                "gajah mati",
                9,
                Map.of('a', 3)
        );
        Mockito.when(charAnalysisService.getDetailById(eq(1L))).thenReturn(BaseResponse.ok(mockResponse));

        mockMvc.perform(get("/api/v1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.kalimat").value("gajah mati"));
    }

    @Test
    void testDetailByIdNotFoundReturns404() throws Exception {
        Mockito.when(charAnalysisService.getDetailById(eq(99L)))
                .thenReturn(BaseResponse.error("Data dengan id 99 tidak ditemukan"));

        mockMvc.perform(get("/api/v1/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.message").value("Data dengan id 99 tidak ditemukan"));
    }

    @Test
    void testCariMinimalHuruf() throws Exception {
        CharAnalysisResponse item = new CharAnalysisResponse(1L, "kalimat panjang", 14, Map.of('a', 4));
        Mockito.when(charAnalysisService.cariMinimalHuruf(10)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/minimal?min=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].kalimat").value("kalimat panjang"));
    }

    @Test
    void testCariTerbaru() throws Exception {
        CharAnalysisResponse item = new CharAnalysisResponse(2L, "kalimat baru", 11, Map.of('a', 3));
        Mockito.when(charAnalysisService.cariTerbaru()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/terbaru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(2));
    }
}
