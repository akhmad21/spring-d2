package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "mst_char_analysis")
@Data
public class CharAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String kalimat;

    @Column(name = "total_huruf")
    private int totalHuruf;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
