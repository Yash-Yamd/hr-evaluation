package com.example.hrevaluation;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class EvaluationDeleteTests {

    //社員と評価がある設定で削除を依頼すると、評価だけを削除して204を返すかのテスト
    @Test
    void deletesOnlyEvaluationAndReturns204() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        when(employees.existsById(3)).thenReturn(true);
        when(evaluations.existsById(3)).thenReturn(true);

        EvaluationController controller =
                new EvaluationController(employees, evaluations);

        ResponseEntity<Void> response = controller.deleteEvaluation(3);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(evaluations).deleteById(3);
        verify(employees, never()).deleteById(3);
    }

        //社員が存在しない場合、削除せずに404を返すかのテスト
    @Test
    void returns404WhenEmployeeDoesNotExist() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        when(employees.existsById(999)).thenReturn(false);

        EvaluationController controller =
                new EvaluationController(employees, evaluations);

        ResponseEntity<Void> response = controller.deleteEvaluation(999);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(evaluations, never()).deleteById(999);
        verify(employees, never()).deleteById(999);
    }

    //社員がいても評価が未保存の場合、404で止めるかのテスト
    @Test
    void returns404WhenEvaluationDoesNotExist() {
        EmployeeRepository employees = mock(EmployeeRepository.class);
        EvaluationRepository evaluations = mock(EvaluationRepository.class);

        when(employees.existsById(3)).thenReturn(true);
        when(evaluations.existsById(3)).thenReturn(false);

        EvaluationController controller =
                new EvaluationController(employees, evaluations);

        ResponseEntity<Void> response = controller.deleteEvaluation(3);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(evaluations, never()).deleteById(3);
        verify(employees, never()).deleteById(3);
    }
}