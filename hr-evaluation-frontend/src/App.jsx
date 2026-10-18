import "./App.css";
import { useEffect, useState } from "react";

function App() {
  //社員を選択肢に表示する→社員一覧を持つ場所を作る
  const [employees, setEmployees] = useState([]);
  const [employeesError, setEmployeesError] = useState("");

  //選んだ社員番号を持つ場所
  const [employeeId, setEmployeeId] = useState("");

  //選んだ5項目の点数を持つ場所
  const [practicalSkillScore, setPracticalSkillScore] = useState("");
  const [problemSolvingScore, setProblemSolvingScore] = useState("");
  const [logicalThinkingScore, setLogicalThinkingScore] = useState("");
  const [communicationScore, setCommunicationScore] = useState("");
  const [goalAchievementScore, setGoalAchievementScore] = useState("");

  //評価を読み込み中か、保存中かを覚える
  const [isLoadingEvaluation, setIsLoadingEvaluation] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  //削除中かどうかと、削除に失敗した案内を覚える
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  //評価の取得失敗と、保存結果のメッセージを持つ場所
  const [evaluationError, setEvaluationError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");

  //相対評価の結果・計算中の状態・失敗の案内を覚える
  const [rankings, setRankings] = useState([]);
  const [isCalculating, setIsCalculating] = useState(false);
  const [rankingError, setRankingError] = useState("");

  //評価を保存済みの社員番号と、確認失敗の案内を覚える
  const [savedEmployeeIds, setSavedEmployeeIds] = useState([]);
  const [saveStatusError, setSaveStatusError] = useState("");

  //最初の保存状況を確認している間はtrueにする
  const [isCheckingSaveStatus, setIsCheckingSaveStatus] = useState(true);

  //今、入力画面と結果画面のどちらを表示しているか覚える
  const [page, setPage] = useState("entry");

  //選んだ5項目を数字にして合計する
  const totalScore =
    Number(practicalSkillScore) +
    Number(problemSolvingScore) +
    Number(logicalThinkingScore) +
    Number(communicationScore) +
    Number(goalAchievementScore);

  //社員がいて、全員の評価が保存済か確認する
  const allSaved =
    employees.length > 0 &&
    employees.every((employee) =>
      savedEmployeeIds.includes(employee.employeeId),
    );

  //選んだ社員の保存済み評価を削除してよいか確認する
  const canDelete =
    employeeId !== "" &&
    savedEmployeeIds.includes(Number(employeeId)) &&
    !isCheckingSaveStatus &&
    !isLoadingEvaluation &&
    !isSaving &&
    !isDeleting &&
    !isCalculating &&
    evaluationError === "";

  //社員と5項目が全て選ばれているか確認する
  const canSave =
    employeeId !== "" &&
    practicalSkillScore !== "" &&
    problemSolvingScore !== "" &&
    logicalThinkingScore !== "" &&
    communicationScore !== "" &&
    goalAchievementScore !== "" &&
    !isLoadingEvaluation &&
    !isSaving &&
    !isDeleting &&
    !isCalculating &&
    evaluationError === "";

  //是認保存済みで、確認失敗や処理中の状態ではないか確認する
  const canCalculate =
    allSaved &&
    saveStatusError === "" &&
    employeesError === "" &&
    !isSaving &&
    !isDeleting &&
    !isCalculating &&
    !isLoadingEvaluation;

  //社員ごとの評価を確認し、保存済の社員番号を渡す
  async function loadSavedEmployeeIds(employeeList) {
    const savedIds = [];

    for (const employee of employeeList) {
      const response = await fetch(`/api/evaluations/${employee.employeeId}`);

      if (response.status === 404) {
        continue;
      }

      if (!response.ok) {
        throw new Error("評価の保存状況を確認できませんでした");
      }

      savedIds.push(employee.employeeId);
    }

    return savedIds;
  }

  //保存済み評価から計算した相対評価の結果を取得する
  async function handleCalculate() {
    if (!canCalculate) {
      return;
    }

    setIsCalculating(true);
    setRankingError("");

    try {
      const response = await fetch("/api/rankings");
      const data = await response.json();

      if (!response.ok) {
        setRankingError(data.message || "相対評価を実行できませんでした。");
        return;
      }

      setRankings(data);
      setPage("results");
    } catch (error) {
      console.error("相対評価の取得に失敗しました", error);
      setRankingError(
        "相対評価の結果を取得できませんでした。もう一度お試しください。",
      );
    } finally {
      setIsCalculating(false);
    }
  }

  //削除できる状態か確認し、実行前に確認画面を出す
  async function handleDelete() {
    if (!canDelete) {
      return;
    }

    const confirmed = window.confirm(
      "選択中の社員の保存済み評価を削除しますか？",
    );

    if (!confirmed) {
      return;
    }

    //削除を依頼して、成功後の画面を更新する
    setIsDeleting(true);
    setDeleteError("");
    setSaveMessage("");

    try {
      const response = await fetch(`/api/evaluations/${employeeId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error("評価を削除できませんでした");
      }

      setPracticalSkillScore("");
      setProblemSolvingScore("");
      setLogicalThinkingScore("");
      setCommunicationScore("");
      setGoalAchievementScore("");

      setSavedEmployeeIds((previous) =>
        previous.filter((id) => id !== Number(employeeId)),
      );

      setRankings([]);
      setRankingError("");
      setSaveMessage("削除しました");
    } catch (error) {
      console.error("評価の削除に失敗しました", error);
      setDeleteError("評価を削除できませんでした。社員を選び直してください。");
    } finally {
      setIsDeleting(false);
    }
  }

  //保存して良い状態なら、送る5項目の点数をまとめる
  async function handleSave() {
    if (!canSave) {
      return;
    }

    const requestBody = {
      practicalSkillScore: Number(practicalSkillScore),
      problemSolvingScore: Number(problemSolvingScore),
      logicalThinkingScore: Number(logicalThinkingScore),
      communicationScore: Number(communicationScore),
      goalAchievementScore: Number(goalAchievementScore),
    };

    setIsSaving(true);
    setSaveMessage("");
    setDeleteError("");

    try {
      const response = await fetch(`/api/evaluations/${employeeId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error("評価を保存できませんでした");
      }

      const result = await response.json();
      setSaveMessage(result.message);
      setSavedEmployeeIds((previous) => [
        ...new Set([...previous, Number(employeeId)]),
      ]);
    } catch (error) {
      console.error("評価の保存に失敗しました", error);
      setSaveMessage("評価を保存できませんでした。もう一度お試しください。");
    } finally {
      setIsSaving(false);
    }
  }

  //社員を切り替えたら社員番号を覚えて点数欄を空にする
  function handleEmployeeChange(event) {
    setEmployeeId(event.target.value);
    setIsLoadingEvaluation(event.target.value !== "");
    setEvaluationError("");
    setSaveMessage("");
    setDeleteError("");
    setPracticalSkillScore("");
    setProblemSolvingScore("");
    setLogicalThinkingScore("");
    setCommunicationScore("");
    setGoalAchievementScore("");
  }

  //社員一覧を読み込み、保存済み評価を確認する
  useEffect(() => {
    let active = true;

    fetch("/api/employees")
      .then((response) => {
        if (!response.ok) {
          throw new Error("社員一覧を取得できませんでした");
        }
        return response.json();
      })
      .then(async (data) => {
        if (!active) {
          return;
        }

        setEmployees(data);

        try {
          const savedIds = await loadSavedEmployeeIds(data);

          if (!active) {
            return;
          }

          setSavedEmployeeIds((previous) => [
            ...new Set([...previous, ...savedIds]),
          ]);
          setSaveStatusError("");
          setIsCheckingSaveStatus(false);
        } catch (error) {
          if (!active) {
            return;
          }

          console.error("保存状況の確認に失敗しました", error);
          setSaveStatusError(
            "評価の保存状況を確認できませんでした。画面を再読み込みしてください。",
          );
        }
      })
      .catch((error) => {
        if (!active) {
          return;
        }

        console.error("社員一覧の取得に失敗しました", error);
        setEmployeesError(
          "社員一覧を読み込めませんでした。保存・読み込みプログラムが起動しているか確認し、画面を再読み込みしてください。",
        );
      });

    return () => {
      active = false;
    };
  }, []);

  //社員番号が変わったら、保存済み評価を取り出す
  useEffect(() => {
    if (employeeId === "") {
      return;
    }

    const controller = new AbortController();

    fetch(`/api/evaluations/${employeeId}`, {
      signal: controller.signal,
    })
      .then((response) => {
        if (response.status === 404) {
          return null;
        }
        if (!response.ok) {
          throw new Error("評価を取得できませんでした");
        }
        return response.json();
      })
      .then((evaluation) => {
        if (evaluation === null || controller.signal.aborted) {
          return;
        }

        setPracticalSkillScore(String(evaluation.practicalSkillScore));
        setProblemSolvingScore(String(evaluation.problemSolvingScore));
        setLogicalThinkingScore(String(evaluation.logicalThinkingScore));
        setCommunicationScore(String(evaluation.communicationScore));
        setGoalAchievementScore(String(evaluation.goalAchievementScore));
      })
      .catch((error) => {
        if (error.name !== "AbortError" && !controller.signal.aborted) {
          console.error("評価の取得に失敗しました", error);
          setEvaluationError(
            "評価を読み込めませんでした。社員を選び直してください。",
          );
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoadingEvaluation(false);
        }
      });

    return () => controller.abort();
  }, [employeeId]);

  if (page === "results") {
    return (
      <main>
        <h1>評価一覧・結果画面</h1>
        <div className="actions results-toolbar">
          <p className="period">評価期間：2026年度上期</p>

          <button
            type="button"
            className="return-button"
            onClick={() => setPage("entry")}
          >
            ← 評価入力に戻る
          </button>
        </div>

        <p className="results-description">
          同じ評価期間の全従業員を、標準化点が高い順に一覧表示
        </p>

        <table className="results-table">
          <thead>
            <tr>
              <th>社員</th>
              <th>部署</th>
              <th>合計点</th>
              <th>標準化点</th>
              <th>全社順位</th>
              <th>ランク</th>
            </tr>
          </thead>

          <tbody>
            {rankings.map((ranking) => (
              <tr key={ranking.employeeId}>
                <td>
                  {ranking.lastName} {ranking.firstName}
                </td>
                <td>{ranking.departmentName}</td>
                <td>{ranking.totalScore}</td>
                <td>{ranking.standardizedScore.toFixed(2)}</td>
                <td>{ranking.overallRank}</td>
                <td>{ranking.relativeGrade}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <p className="results-rule">
          表示ルール：部署ごとに合計点を標準化し、全従業員を比較する。
          全社順位とA～Fランクはアプリが算出する。
        </p>
      </main>
    );
  }

  return (
    <main>
      <h1>評価入力画面</h1>
      <p className="period">評価期間：2026年度上期</p>
      <div className="field">
        <label htmlFor="employee">評価する社員</label>
        <select
          id="employee"
          value={employeeId}
          onChange={handleEmployeeChange}
          disabled={isSaving || isDeleting || isCalculating}
        >
          <option value="">社員を選択してください</option>
          {/* 社員を選択肢に表示する→取り込んだ社員を並べる */}
          {employees.map((employee) => (
            <option key={employee.employeeId} value={employee.employeeId}>
              {employee.departmentName} {employee.lastName} {employee.firstName}
            </option>
          ))}
        </select>
      </div>
      <h2>評価項目（それぞれ1～5で選択）</h2>
      <div className="field">
        <label htmlFor="practicalSkillScore">実務スキル</label>
        <select
          id="practicalSkillScore"
          value={practicalSkillScore}
          onChange={(event) => setPracticalSkillScore(event.target.value)}
          disabled={
            isLoadingEvaluation || isSaving || isDeleting || isCalculating
          }
        >
          <option value="">1～5</option>
          <option value="1">1</option>
          <option value="2">2</option>
          <option value="3">3</option>
          <option value="4">4</option>
          <option value="5">5</option>
        </select>
      </div>
      <div className="field">
        <label htmlFor="problemSolvingScore">課題解決力</label>
        <select
          id="problemSolvingScore"
          value={problemSolvingScore}
          onChange={(event) => setProblemSolvingScore(event.target.value)}
          disabled={
            isLoadingEvaluation || isSaving || isDeleting || isCalculating
          }
        >
          <option value="">1～5</option>
          <option value="1">1</option>
          <option value="2">2</option>
          <option value="3">3</option>
          <option value="4">4</option>
          <option value="5">5</option>
        </select>
      </div>
      <div className="field">
        <label htmlFor="logicalThinkingScore">論理思考力</label>
        <select
          id="logicalThinkingScore"
          value={logicalThinkingScore}
          onChange={(event) => setLogicalThinkingScore(event.target.value)}
          disabled={
            isLoadingEvaluation || isSaving || isDeleting || isCalculating
          }
        >
          <option value="">1〜5</option>
          <option value="1">1</option>
          <option value="2">2</option>
          <option value="3">3</option>
          <option value="4">4</option>
          <option value="5">5</option>
        </select>
      </div>
      <div className="field">
        <label htmlFor="communicationScore">コミュニケーション力</label>
        <select
          id="communicationScore"
          value={communicationScore}
          onChange={(event) => setCommunicationScore(event.target.value)}
          disabled={
            isLoadingEvaluation || isSaving || isDeleting || isCalculating
          }
        >
          <option value="">1〜5</option>
          <option value="1">1</option>
          <option value="2">2</option>
          <option value="3">3</option>
          <option value="4">4</option>
          <option value="5">5</option>
        </select>
      </div>
      <div className="field">
        <label htmlFor="goalAchievementScore">目標達成力</label>
        <select
          id="goalAchievementScore"
          value={goalAchievementScore}
          onChange={(event) => setGoalAchievementScore(event.target.value)}
          disabled={
            isLoadingEvaluation || isSaving || isDeleting || isCalculating
          }
        >
          <option value="">1〜5</option>
          <option value="1">1</option>
          <option value="2">2</option>
          <option value="3">3</option>
          <option value="4">4</option>
          <option value="5">5</option>
        </select>
      </div>
      <p>合計点：{totalScore} / 25点</p>
      {/* 評価保存ボタン */}
      <div className="actions">
        <button
          type="button"
          className="save-button"
          onClick={handleSave}
          disabled={!canSave}
        >
          {isSaving ? "保存中です…" : "評価を保存"}
        </button>
        {/* 評価削除ボタン */}
        <button
          type="button"
          className="delete-button"
          onClick={handleDelete}
          disabled={!canDelete}
        >
          {isDeleting ? "削除中です…" : "評価を削除"}
        </button>
        {/* 相対評価実行ボタン */}
        <button
          type="button"
          className="calculate-button"
          onClick={handleCalculate}
          disabled={!canCalculate}
        >
          {isCalculating
            ? "結果を取得中です…"
            : "相対評価を実行（部署別の標準化）→"}
        </button>
      </div>
      <p className="role-note">
        ※相対評価については課題用の仮配置。
        <br />
        {"　"}本来、部署責任者には表示せず、人事担当者だけが実行する。
      </p>
      {isLoadingEvaluation && <p role="status">評価を読み込み中です</p>}
      {employeesError && <p role="alert">{employeesError}</p>}
      {evaluationError && <p role="alert">{evaluationError}</p>}
      {deleteError && <p role="alert">{deleteError}</p>}
      {saveMessage && <p role="status">{saveMessage}</p>}
      {isCalculating && <p role="status">相対評価の結果を取得中です…</p>}
      {rankingError && <p role="alert">{rankingError}</p>}
      {saveStatusError && <p role="alert">{saveStatusError}</p>}
    </main>
  );
}
export default App;
