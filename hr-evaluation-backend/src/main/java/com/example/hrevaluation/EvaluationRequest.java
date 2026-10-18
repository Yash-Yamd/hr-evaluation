package com.example.hrevaluation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

//画面から送られる5つの点数を受け取る入れ物
public record EvaluationRequest( //画面からアプリへ受け取る
         @NotNull
         @Min(1)
         @Max(5)
        Integer practicalSkillScore,

        @NotNull
        @Min(1)
        @Max(5)
        Integer problemSolvingScore,

        @NotNull
        @Min(1)
        @Max(5)
        Integer logicalThinkingScore,

        @NotNull
        @Min(1)
        @Max(5)
        Integer communicationScore,

        @NotNull
        @Min(1)
        @Max(5)
        Integer goalAchievementScore
) {
}
