package com.example.demo.service;

import com.example.demo.dto.request.CharAnalysisRequest;
import com.example.demo.exception.DuplicateKalimatException;
import com.example.demo.exception.InvalidKalimatException;
import com.example.demo.repository.CharAnalysisRepository;
import com.example.demo.service.impl.CharAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CharAnalysisServiceImplTest {

    private CharAnalysisRepository charAnalysisRepository;
    private CharAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        charAnalysisRepository = Mockito.mock(CharAnalysisRepository.class);
        service = new CharAnalysisServiceImpl(charAnalysisRepository);
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
}
