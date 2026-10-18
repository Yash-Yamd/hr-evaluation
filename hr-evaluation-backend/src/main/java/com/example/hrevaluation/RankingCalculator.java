package com.example.hrevaluation;

//部署の平均点・標準偏差・標準化を計算する
public class RankingCalculator {

    //部署の合計点を全部足して、人数で割る
    public static double calculateAverage(double[] scores) {
        if (scores.length == 0) {
            throw new IllegalArgumentException("点数がないため平均点を算出できません");
        }

        double sum = 0;

        for (double score : scores) {
            sum += score;
        }

        return sum / scores.length;
    }

    //平均との差を二乗して、全部足す
    public static double calculateSquaredDifferenceSum(double[] scores) {
        double average = calculateAverage(scores);
        double sum = 0;

        for (double score : scores) {
            double difference = score - average;
            sum += difference * difference;
        }

        return sum;
    }

    //平均との差の二乗の合計を人数で割り、平方根を求める
    public static double calculateStandardDeviation(double[] scores) {
        double squaredDifferenceSum = calculateSquaredDifferenceSum(scores);
        double variance = squaredDifferenceSum / scores.length;

        return Math.sqrt(variance);
    }

    //社員の合計点から部署の平均を引き、標準偏差で割る
    public static double calculateStandardizedScore(double score, double[] scores) {
        double average = calculateAverage(scores);
        double standardDeviation = calculateStandardDeviation(scores);

        if (standardDeviation == 0) {
            throw new IllegalArgumentException("点数のばらつきがないため標準化できません");
        }

        return (score - average) / standardDeviation;
    }

    //保存済み評価から5項目を取り出して足す
    public static int calculateTotalScore(Evaluation evaluation) {
        return evaluation.getPracticalSkillScore()
                + evaluation.getProblemSolvingScore()
                + evaluation.getLogicalThinkingScore()
                + evaluation.getCommunicationScore()
                + evaluation.getGoalAchievementScore();
    }
    //自分より高い標準化点の人数に1を足して、全社順位を求める
    public static int calculateRank(double score, double[] allScores) {
        int rank = 1;

        for (double otherScore : allScores) {
            double difference = otherScore - score;

            if (difference >= 0.0000000001) {
                rank++;
            }
        }

        return rank;
    }

    //全社順位を、評価対象人数に対する割合にする
    public static double calculateRankPercentage(int rank, int employeeCount) {
        if (employeeCount < 1 || rank < 1 || rank > employeeCount) {
            throw new IllegalArgumentException("順位または評価対象人数が正しくありません");
        }

        return(double) rank / employeeCount * 100;
    }

    //順位の割合からランクを決める。ただし1位は必ずAとする
    public static String calculateGrade(int rank, int employeeCount) {
        double percentage = calculateRankPercentage(rank, employeeCount);

        if (rank == 1) {
            return "A";
        }
        if (percentage <= 10) {
            return "A";
        }
        if (percentage <= 30) {
            return "B";
        }
        if (percentage <= 50) {
            return "C";
        }
        if (percentage <= 70) {
            return "D";
        }
        if (percentage <= 90) {
            return "E";
        }

        return "F";
    }
}
