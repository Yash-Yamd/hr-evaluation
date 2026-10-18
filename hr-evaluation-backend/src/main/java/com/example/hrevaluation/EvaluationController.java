package com.example.hrevaluation;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;

//評価の保存依頼の受付
@RestController
public class EvaluationController {

    private final EmployeeRepository employeeRepository; //保存する評価の対象社員が存在するか確認
    private final EvaluationRepository evaluationRepository; //評価を保存・読み取り

    public EvaluationController(
            EmployeeRepository employeeRepository,
            EvaluationRepository evaluationRepository) {
        this.employeeRepository = employeeRepository;
        this.evaluationRepository = evaluationRepository;
    }

    //評価点数を保存用の入れ物へ移す
    private void applyScores(
            Evaluation evaluation,
            EvaluationRequest request) {
        //届いたスキルの点数を取り出す(保存用の入れ物に点数を入れる）
        evaluation.setPracticalSkillScore(request.practicalSkillScore());
        evaluation.setProblemSolvingScore(request.problemSolvingScore());
        evaluation.setLogicalThinkingScore(request.logicalThinkingScore());
        evaluation.setCommunicationScore(request.communicationScore());
        evaluation.setGoalAchievementScore(request.goalAchievementScore());
    }

    //受け取った点数をMySQLに保存する
    @PutMapping("/api/evaluations/{employeeId}")
    public ResponseEntity<EvaluationSaveResponse> saveEvaluation(
            @PathVariable Integer employeeId, //送り先のURLから社員番号を受け取る
            @Valid @RequestBody EvaluationRequest request) { //受け取った5つの点数を、保存前にチェックする

        if (!employeeRepository.existsById(employeeId)) { //指定された社員番号の社員が存在しない場合は
            return ResponseEntity.notFound().build(); //見つかりません（404）という返事を返す
        }

        boolean alreadySaved = //評価があるか調べた結果を記憶する
                evaluationRepository.existsById(employeeId); //対象の社員番号の評価があるか調べる

        //社員番号と5つの点数をまとめてMySQLに保存する処理
        Evaluation evaluation = new Evaluation(); //evaluationというインスタンスを作成
        evaluation.setEmployeeId(employeeId); //誰の評価かを示す社員番号を格納
        applyScores(evaluation, request); //受け取った5つの点数を入れる
        evaluationRepository.save(evaluation); //点数を入れた評価データをMySQLに保存

        //保存した5項目の点数を足して、合計点を求める
        Integer totalScore =
                evaluation.getPracticalSkillScore()
                + evaluation.getProblemSolvingScore()
                + evaluation.getLogicalThinkingScore()
                + evaluation.getCommunicationScore()
                + evaluation.getGoalAchievementScore();

        //初回保存か更新に合わせて、返すメッセージを決める
        String message = "保存しました";

        if (alreadySaved) {
            message = "更新しました";
        }

        //保存した内容を返すための入れ物に詰める
        EvaluationSaveResponse response = new  EvaluationSaveResponse(
                evaluation.getEmployeeId(),
                evaluation.getPracticalSkillScore(),
                evaluation.getProblemSolvingScore(),
                evaluation.getLogicalThinkingScore(),
                evaluation.getCommunicationScore(),
                evaluation.getGoalAchievementScore(),
                totalScore,
                message
        );

        if (alreadySaved) { //社員の評価がすでにあった場合
            return ResponseEntity.ok(response); //200と、保存した内容・合計点・メッセージを返す
        }

        return ResponseEntity.status(201).body(response); //201と、保存した内容・合計点・メッセージを返す
    }

    //取り出した評価を返すための入れ物に移す
    private EvaluationResponse toResponse(Evaluation evaluation) {
        return new EvaluationResponse(
                evaluation.getEmployeeId(),
                evaluation.getPracticalSkillScore(),
                evaluation.getProblemSolvingScore(),
                evaluation.getLogicalThinkingScore(),
                evaluation.getCommunicationScore(),
                evaluation.getGoalAchievementScore()
        );
    }

    //指定した社員の保存済み評価を取り出す
    @GetMapping("/api/evaluations/{employeeId}")
    public ResponseEntity<EvaluationResponse> getEvaluation(
            @PathVariable Integer employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity.notFound().build();
        }

        Evaluation evaluation =
                evaluationRepository.findById(employeeId).orElse(null); //評価がなければデータ無として扱う

        if (evaluation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(evaluation));
    }
    //指定した社員の保存済み評価を削除する
    @DeleteMapping("/api/evaluations/{employeeId}")
    public ResponseEntity<Void> deleteEvaluation(
            @PathVariable Integer employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity.notFound().build();
        }

        if (!evaluationRepository.existsById(employeeId)) {
            return ResponseEntity.notFound().build();
        }

        evaluationRepository.deleteById(employeeId);

        return ResponseEntity.noContent().build();
    }
}
