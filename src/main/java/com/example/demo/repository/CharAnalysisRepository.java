package com.example.demo.repository;

import com.example.demo.entity.CharAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharAnalysisRepository extends JpaRepository<CharAnalysis, Long> {

    List<CharAnalysis> findByKalimatContainingIgnoreCase(String kata);

    // Tugas 5: Query JPQL (menggunakan nama Class Entity 'CharAnalysis' dan field Java 'totalHuruf')
    @Query("SELECT c FROM CharAnalysis c WHERE c.totalHuruf >= :min")
    List<CharAnalysis> findByTotalHurufMinimal(@Param("min") int min);

    // Tugas 6: Query Native SQL (menggunakan nama Tabel DB 'mst_char_analysis' dan kolom 'created_at')
    @Query(value = "SELECT * FROM mst_char_analysis ORDER BY created_at DESC LIMIT 5", nativeQuery = true)
    List<CharAnalysis> findTop5Terbaru();
}
