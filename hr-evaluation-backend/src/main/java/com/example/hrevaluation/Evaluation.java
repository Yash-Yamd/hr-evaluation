package com.example.hrevaluation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//「誰の評価を保存するか」を持つ入れ物
@Entity //このクラスを出たーベースに保存するデータとして扱わせる目印
@Table(name = "evaluations") //対応する表はevaluation
public class Evaluation {

    @Id //この項目で保存あれた1件を区別する
    @Column(name = "employee_id") //対応する表の項目名は”employee_id”
    private Integer employeeId; //社員番号を持つ場所

    //5つの評価点を持つ場所
    @Column(name = "practical_skill_score")
    private Integer practicalSkillScore;

    @Column(name = "problem_solving_score")
    private Integer problemSolvingScore;

    @Column(name = "logical_thinking_score")
    private Integer logicalThinkingScore;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "goal_achievement_score")
    private Integer goalAchievementScore;

    //評価データが持つ値を取り出す処理（ゲッター）
    public Integer getEmployeeId(){
        return employeeId; //社員番号
    }

    public Integer getPracticalSkillScore(){
        return practicalSkillScore; //実務スキル
    }

    public Integer getProblemSolvingScore(){
        return problemSolvingScore; //課題解決力
    }
    public Integer getLogicalThinkingScore(){
        return logicalThinkingScore; //論理思考力
    }
    public Integer getCommunicationScore(){
        return communicationScore; //コミュニケーション力
    }
    public Integer getGoalAchievementScore(){
        return goalAchievementScore; //目標達成力
    }

    //社員番号、評価点を入れる処理（セッター）
    public void setEmployeeId(Integer employeeId){
        this.employeeId = employeeId;
    }
    public void setPracticalSkillScore(Integer practicalSkillScore){
        this.practicalSkillScore = practicalSkillScore;
    }

    public void setProblemSolvingScore(Integer problemSolvingScore){
        this.problemSolvingScore = problemSolvingScore;
    }
    public void setLogicalThinkingScore(Integer logicalThinkingScore){
        this.logicalThinkingScore = logicalThinkingScore;
    }

    public void setCommunicationScore(Integer communicationScore) {
        this.communicationScore = communicationScore;
    }

    public void setGoalAchievementScore(Integer goalAchievementScore) {
        this.goalAchievementScore = goalAchievementScore;
    }
}
