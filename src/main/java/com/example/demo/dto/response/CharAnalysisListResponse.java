package com.example.demo.dto.response;

import com.example.demo.entity.CharAnalysis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharAnalysisListResponse {
    private Long id;
    private String kalimat;
    private int totalHuruf;

    public static CharAnalysisListResponse from(CharAnalysis data) {
        return new CharAnalysisListResponse(data.getId(), data.getKalimat(), data.getTotalHuruf());
    }
}
