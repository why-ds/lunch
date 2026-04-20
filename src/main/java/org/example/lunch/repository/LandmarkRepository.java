package org.example.lunch.repository;

import org.example.lunch.entity.Landmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandmarkRepository extends JpaRepository<Landmark, Integer> {
    List<Landmark> findByUseYnOrderBySortOrd(String useYn);
    boolean existsByLandmarkCd(String landmarkCd);
    @Query("SELECT DISTINCT l.sidoNm FROM Landmark l WHERE l.useYn = 'Y' AND l.sidoNm IS NOT NULL ORDER BY l.sidoNm")
    List<String> findDistinctSidoNms();

    @Query("SELECT DISTINCT l.gugunCd, l.gugunNm FROM Landmark l WHERE l.useYn = 'Y' AND l.sidoNm = :sidoNm AND l.gugunNm IS NOT NULL ORDER BY l.gugunNm")
    List<Object[]> findDistinctGugunsBySidoNm(@Param("sidoNm") String sidoNm);

    List<Landmark> findByGugunCdAndUseYnOrderBySortOrd(String gugunCd, String useYn);
}