package com.example.hrevaluation;

import org.springframework.data.jpa.repository.JpaRepository;

//MySQLの「評価（evaluations）」を扱う窓口
public interface EvaluationRepository
    extends JpaRepository<Evaluation, Integer> {
}
