package com.example.hrevaluation;

//1人分の社員情報と、相対評価の計算結果をまとめる
public record RankedEmployee (
        Employee employee,
        int totalScore,
        double standardizedScore,
        int rank,
        String grade
){
}
