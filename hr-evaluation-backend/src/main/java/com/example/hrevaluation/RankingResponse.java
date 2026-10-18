package com.example.hrevaluation;

//画面へ返す1人分の相対評価結果
public record RankingResponse(
        Integer employeeId,
        String departmentName,
        String lastName,
        String firstName,
        int totalScore,
        double standardizedScore,
        int overallRank,
        String relativeGrade
) {
}