package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CharAnalysisRequest {

    @NotBlank(message = "Kalimat tidak boleh kosong atau hanya berupa spasi")
    @Size(max = 500, message = "Panjang kalimat tidak boleh melebihi 500 karakter")
    private String kalimat;
}
