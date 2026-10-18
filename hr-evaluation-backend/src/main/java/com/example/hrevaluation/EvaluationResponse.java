package com.example.hrevaluation;

//保存済み評価を取り出した時に返す情報の入れ物
public record EvaluationResponse(
        Integer employeeId,
        Integer practicalSkillScore,
        Integer problemSolvingScore,
        Integer logicalThinkingScore,
        Integer communicationScore,
        Integer goalAchievementScore
) {
}
