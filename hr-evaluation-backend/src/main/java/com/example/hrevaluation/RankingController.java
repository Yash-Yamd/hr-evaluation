package com.example.hrevaluation;

import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//相対評価の結果を取得する依頼の受付
@RestController
public class RankingController {

    private static final Logger logger =
            LoggerFactory.getLogger(RankingController.class);

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    //依頼を受けて全員分を計算し、結果一覧を返す
    @GetMapping("/api/rankings")
    public ResponseEntity<?> getRankings() {
        try {
            List<RankedEmployee> results = rankingService.calculateRankings();
            List<RankingResponse> responses = new ArrayList<>();

            for (RankedEmployee result : results) {
                responses.add(toResponse(result));
            }

            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", error.getMessage()));
        } catch (Exception error) {
            logger.error("相対評価の計算中に予期しないエラーが発生しました", error);

            return ResponseEntity.internalServerError().body(
                    Map.of("message",
                            "相対評価の計算に失敗しました。もう一度お試しください。"));
        }
    }

    //計算用の結果を、画面へ返す8項目に移す
    private RankingResponse toResponse(RankedEmployee result){
        Employee employee = result.employee();

        return new RankingResponse(
                employee.getEmployeeId(),
                employee.getDepartment().getDepartmentName(),
                employee.getLastName(),
                employee.getFirstName(),
                result.totalScore(),
                result.standardizedScore(),
                result.rank(),
                result.grade()
        );
    }
}
