import { spawn } from "node:child_process";

// 起動用ファイルで共通に使う、確認と初期登録の処理。
export function supportsNode(version) {
  const [major, minor] = version.replace(/^v/, "").split(".").map(Number);
  return (major === 20 && minor >= 19) || (major === 22 && minor >= 12) || major > 22;
}

export function checkPasswordSettings(text) {
  for (const key of ["DB_PASSWORD", "DB_ROOT_PASSWORD"]) {
    const line = text.split(/\r?\n/).find((candidate) => candidate.startsWith(`${key}=`));
    const value = line?.slice(key.length + 1).trim();
    if (!value || /^replace_with_/i.test(value)) {
      // パスワードの値は画面にもログにも出さない。
      throw new Error(`.envの${key}を自分の環境用のパスワードに設定してください。`);
    }
  }
}

export async function waitUntil(check, { timeoutMs, intervalMs = 1000, message, cancelled = () => false }) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (cancelled()) throw new Error("停止の依頼を受け付けました。");
    if (await check()) return;
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
  throw new Error(message);
}

export async function initializeDatabase(executeSql, readSql) {
  // 順番を守り、途中で失敗したら後続のSQLは実行しない。
  // 既存の評価を削除・初期化するSQLは含めない。
  for (const name of ["schema.sql", "seed-departments.sql", "seed-employees.sql"]) {
    await executeSql(await readSql(name));
  }
}

export function requestBrowser(spawnBrowser = () => spawn("powershell.exe", [
  "-NoProfile", "-NonInteractive", "-Command",
  "Start-Process -FilePath 'http://127.0.0.1:5173/'",
], { windowsHide: true, stdio: "ignore" }), timeoutMs = 10000) {
  // ブラウザから「出力の終了」が来るまで待つと、既存のChrome等で待ち続けることがある。
  // 起動命令自体の終了だけを確認し、失敗や時間切れもアプリの停止理由にしない。
  return new Promise((resolve) => {
    let child;
    let settled = false;
    const finish = (opened) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      child?.unref?.();
      resolve(opened);
    };
    const timer = setTimeout(() => finish(false), timeoutMs);
    try {
      child = spawnBrowser();
      child.once("error", () => finish(false));
      child.once("exit", (code) => finish(code === 0));
    } catch { finish(false); }
  });
}
