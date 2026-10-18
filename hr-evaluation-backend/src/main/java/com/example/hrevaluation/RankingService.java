package com.example.hrevaluation;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

//社員と評価を取り出し、相対評価の計算を進める
@Service
public class RankingService {

    private final EmployeeRepository employeeRepository;
    private final EvaluationRepository evaluationRepository;

    public RankingService(
            EmployeeRepository employeeRepository,
            EvaluationRepository evaluationRepository) {
        this.employeeRepository = employeeRepository;
        this.evaluationRepository = evaluationRepository;
    }

    //保存済評価を、社員番号で取り出せる形にまとめる
    private Map<Integer, Evaluation> loadEvaluationsByEmployeeId() {
        Map<Integer, Evaluation> evaluationsByEmployeeId = new HashMap<>();

        for (Evaluation evaluation : evaluationRepository.findAll()) {
            evaluationsByEmployeeId.put(evaluation.getEmployeeId(), evaluation);
        }

        return evaluationsByEmployeeId;
    }

    //社員の合計点を部署ごとにまとめる
    public Map<Integer, List<Double>> collectDepartmentScores() {
        Map<Integer, Evaluation> evaluations = loadEvaluationsByEmployeeId();
        List<Employee> employees = employeeRepository.findAll();
        checkMissingEvaluations(employees, evaluations);
        Map<Integer, List<Double>> departmentScores = new HashMap<>();

        for (Employee employee : employees) {
            Evaluation evaluation = evaluations.get(employee.getEmployeeId());

            if (employee.getDepartment() == null) {
                throw new IllegalStateException("社員の所属部署が設定されていません");
            }

            Integer departmentId = employee.getDepartment().getDepartmentId();
            double totalScore = RankingCalculator.calculateTotalScore(evaluation);

            departmentScores
                    .computeIfAbsent(departmentId, key -> new ArrayList<>())
                    .add(totalScore);
        }

        return departmentScores;
    }

    //部署の点数一覧を、計算処理が受け取れる形にする
    private double[] toScoreArray(List<Double> scoreList) {
        double[] scores = new double[scoreList.size()];

        for (int i =0; i <scoreList.size(); i++) {
            scores[i] = scoreList.get(i);
        }

        return scores;
    }

    //所属部署の点数を使い、社員ごとの標準化点を求める
    public Map<Integer, Double> calculateStandardizedScores() {
        Map<Integer, List<Double>> departmentScores = collectDepartmentScores();
        Map<Integer, Evaluation> evaluations = loadEvaluationsByEmployeeId();
        Map<Integer, Double> standardizedScores = new HashMap<>();

        for (Employee employee : employeeRepository.findAll()) {
            Integer employeeId = employee.getEmployeeId();
            Integer departmentId = employee.getDepartment().getDepartmentId();

            double totalScore =
                    RankingCalculator.calculateTotalScore(evaluations.get(employeeId));
            List<Double> scoreList = departmentScores.get(departmentId);
            String departmentName = employee.getDepartment().getDepartmentName();

            if (scoreList.size() < 2) {
                throw new IllegalArgumentException(
                        departmentName + "は社員が2人未満のため計算できません");
            }

            double[] scores = toScoreArray(scoreList);

            if(RankingCalculator.calculateStandardDeviation(scores) == 0) {
                throw new IllegalArgumentException(
                        departmentName + "は全員の合計点が同じため計算できません");
            }

            double standardizedScore =
                    RankingCalculator.calculateStandardizedScore(totalScore, scores);

            standardizedScores.put(employeeId, standardizedScore);
        }

        return standardizedScores;
    }

    //全員の合計点・標準化点・順位・ランクをまとめる
    public List<RankedEmployee> calculateRankings() {
        Map<Integer, Double> standardizedScores = calculateStandardizedScores();
        Map<Integer, Evaluation> evaluations = loadEvaluationsByEmployeeId();

        double[] allScores =
                toScoreArray(new ArrayList<>(standardizedScores.values()));
        int employeeCount = standardizedScores.size();

        List<RankedEmployee> rankings = new ArrayList<>();

        for (Employee employee : employeeRepository.findAll()) {
            Integer employeeId = employee.getEmployeeId();
            int totalScore =
                    RankingCalculator.calculateTotalScore(evaluations.get(employeeId));
            double standardizedScore = standardizedScores.get(employeeId);
            int rank = RankingCalculator.calculateRank(standardizedScore,allScores);
            String grade = RankingCalculator.calculateGrade(rank, employeeCount);

            rankings.add(new RankedEmployee(
                    employee, totalScore, standardizedScore, rank, grade));
        }

        rankings.sort(
                Comparator.comparingDouble(RankedEmployee::standardizedScore)
                        .reversed()
        );

        return rankings;
    }

    //評価が未保存の社員名を全員分まとめ、計算を止める
    private void checkMissingEvaluations(
            List<Employee> employees,
            Map<Integer, Evaluation> evaluations) {
        List<String> missingNames = new ArrayList<>();

        for (Employee employee : employees) {
            if (!evaluations.containsKey(employee.getEmployeeId())) {
                missingNames.add(
                        employee.getLastName() + " " + employee.getFirstName());
            }
        }

        if (!missingNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "評価が未保存です：" + String.join("、", missingNames));
        }
    }
}
