package com.example.hrevaluation;

//評価を保存した後に返す情報の入れ物
public record EvaluationSaveResponse(
        Integer employeeId,
        Integer practicalSkillScore,
        Integer problemSolvingScore,
        Integer logicalThinkingScore,
        Integer communicationScore,
        Integer goalAchievementScore,
        Integer totalScore,
        String message
){
}
