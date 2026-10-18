package com.example.hrevaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankingCalculatorTests {

    //標準化点が同展だった場合の順位テスト（同点なら1位・1位・3位になるか）
    @Test
    void givesSameRankToTiesAndSkipsNextRank() {
        double[] scores = {1.4, 1.4, 0.6};

        assertEquals(1, RankingCalculator.calculateRank(scores[0], scores));
        assertEquals(1, RankingCalculator.calculateRank(scores[1], scores));
        assertEquals(3, RankingCalculator.calculateRank(scores[2], scores));
    }

    //標準化点の小さな端数（0.0000000001未満）は同点として扱うかテスト
    @Test
    void treatsTinyDifferenceAsTie() {
        double[] scores = {1.4, 1.40000000001, 0.6};

        assertEquals(1, RankingCalculator.calculateRank(scores[0], scores));
        assertEquals(1, RankingCalculator.calculateRank(scores[1], scores));
        assertEquals(3, RankingCalculator.calculateRank(scores[2], scores));
    }

    //差が大きければ順位を分けるかテスト（0.000000001は同点とする範囲を超える）
    @Test
    void givesDifferentRanksWhenDifferenceIsLargeEnough() {
        double[] scores = {1.4, 1.400000001, 0.6};

        assertEquals(2, RankingCalculator.calculateRank(scores[0], scores));
        assertEquals(1, RankingCalculator.calculateRank(scores[1], scores));
        assertEquals(3, RankingCalculator.calculateRank(scores[2], scores));
    }

    //差が境目ちょうどの場合に順位が分かれるかテスト
    @Test
    void separatesRanksAtExactToleranceBoundary() {
        double[] scores = {0.0, 0.0000000001, -0.6};

        assertEquals(2, RankingCalculator.calculateRank(scores[0], scores));
        assertEquals(1, RankingCalculator.calculateRank(scores[1], scores));
        assertEquals(3, RankingCalculator.calculateRank(scores[2], scores));
    }

    //A～Fランクの境目が正しく動作するかのテスト
    @Test
    void assignsGradesAtPercentageBoundaries() {
        assertEquals("A", RankingCalculator.calculateGrade(10, 100));
        assertEquals("B", RankingCalculator.calculateGrade(11, 100));

        assertEquals("B", RankingCalculator.calculateGrade(30, 100));
        assertEquals("C", RankingCalculator.calculateGrade(31, 100));

        assertEquals("C", RankingCalculator.calculateGrade(50, 100));
        assertEquals("D", RankingCalculator.calculateGrade(51, 100));

        assertEquals("D", RankingCalculator.calculateGrade(70, 100));
        assertEquals("E", RankingCalculator.calculateGrade(71, 100));

        assertEquals("E", RankingCalculator.calculateGrade(90, 100));
        assertEquals("F", RankingCalculator.calculateGrade(91, 100));
    }

    //1位は必ずにAになるかテスト
    @Test
    void givesFirstPlaceGradeAEvenInSmallGroup() {
        assertEquals("A", RankingCalculator.calculateGrade(1, 3));
        assertEquals("D", RankingCalculator.calculateGrade(2, 3));
        assertEquals("F", RankingCalculator.calculateGrade(3, 3));
    }
}