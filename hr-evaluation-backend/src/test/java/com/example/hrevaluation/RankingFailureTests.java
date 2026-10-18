package com.example.hrevaluation;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingFailureTests {

    @Test
    void reportsAllMissingEmployees() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        Employee first = mock(Employee.class);
        when(first.getEmployeeId()).thenReturn(1);
        when(first.getLastName()).thenReturn("青木");
        when(first.getFirstName()).thenReturn("彩");

        Employee second = mock(Employee.class);
        when(second.getEmployeeId()).thenReturn(2);
        when(second.getLastName()).thenReturn("石田");
        when(second.getFirstName()).thenReturn("健");

        when(employees.findAll()).thenReturn(List.of(first, second));
        when(evaluations.findAll()).thenReturn(List.of());

        RankingService service = new RankingService(employees, evaluations);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.collectDepartmentScores());

        assertEquals("評価が未保存です：青木 彩、石田 健", error.getMessage());
    }

    @Test
    void rejectsDepartmentWithOneEmployee() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        Department department = mock(Department.class);
        when(department.getDepartmentId()).thenReturn(1);
        when(department.getDepartmentName()).thenReturn("営業部");

        Employee employee = mock(Employee.class);
        when(employee.getEmployeeId()).thenReturn(1);
        when(employee.getDepartment()).thenReturn(department);

        when(employees.findAll()).thenReturn(List.of(employee));
        when(evaluations.findAll()).thenReturn(List.of(createEvaluation(1, 3)));

        RankingService service = new RankingService(employees, evaluations);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateStandardizedScores());

        assertEquals("営業部は社員が2人未満のため計算できません", error.getMessage());
    }

    @Test
    void rejectsDepartmentWithEqualTotals() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        Department department = mock(Department.class);
        when(department.getDepartmentId()).thenReturn(1);
        when(department.getDepartmentName()).thenReturn("営業部");

        Employee first = mock(Employee.class);
        when(first.getEmployeeId()).thenReturn(1);
        when(first.getDepartment()).thenReturn(department);

        Employee second = mock(Employee.class);
        when(second.getEmployeeId()).thenReturn(2);
        when(second.getDepartment()).thenReturn(department);

        Evaluation firstEvaluation = createEvaluation(1, 3);
        Evaluation secondEvaluation = createEvaluation(2, 3);
        secondEvaluation.setPracticalSkillScore(2);
        secondEvaluation.setCommunicationScore(4);

        when(employees.findAll()).thenReturn(List.of(first, second));
        when(evaluations.findAll()).thenReturn(List.of(firstEvaluation, secondEvaluation));

        RankingService service = new RankingService(employees, evaluations);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateStandardizedScores());

        assertEquals("営業部は全員の合計点が同じため計算できません", error.getMessage());
    }

    //確認用に、5項目が同じ点数の評価を作る
    private Evaluation createEvaluation(int employeeId, int score) {
        Evaluation evaluation = new Evaluation();
        evaluation.setEmployeeId(employeeId);
        evaluation.setPracticalSkillScore(score);
        evaluation.setProblemSolvingScore(score);
        evaluation.setLogicalThinkingScore(score);
        evaluation.setCommunicationScore(score);
        evaluation.setGoalAchievementScore(score);
        return evaluation;
    }
}
