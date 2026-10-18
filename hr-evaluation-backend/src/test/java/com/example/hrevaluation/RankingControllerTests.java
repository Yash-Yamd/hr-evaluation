package com.example.hrevaluation;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingControllerTests {

    @Test
    void returns400WhenCalculationIsNotPossible() {
        RankingService service = mock(RankingService.class);
        when(service.calculateRankings()).thenThrow(
                new IllegalArgumentException("評価が未保存です：青木 彩"));

        RankingController controller = new RankingController(service);
        ResponseEntity<?> response = controller.getRankings();

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                Map.of("message", "評価が未保存です：青木 彩"),
                response.getBody());
    }
    @Test
    void returns500WhenUnexpectedFailureOccurs() {
        RankingService service = mock(RankingService.class);
        when(service.calculateRankings()).thenThrow(
                new IllegalStateException("確認用の予期しない失敗"));

        RankingController controller = new RankingController(service);
        ResponseEntity<?> response = controller.getRankings();

        assertEquals(500, response.getStatusCode().value());
        assertEquals(
                Map.of("message",
                        "相対評価の計算に失敗しました。もう一度お試しください。"),
                response.getBody());
    }
}