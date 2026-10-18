import { spawn } from "node:child_process";
import { createHash } from "node:crypto";
import { closeSync, existsSync, mkdirSync, openSync, readFileSync, unlinkSync, writeFileSync } from "node:fs";
import net from "node:net";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { checkPasswordSettings, initializeDatabase, requestBrowser, supportsNode, waitUntil } from "./launcher-core.mjs";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const runtime = path.join(root, ".launcher");
const lockPath = path.join(runtime, "launcher.json");
const stopPath = path.join(runtime, "stop-request");
const backend = path.join(root, "hr-evaluation-backend");
const frontend = path.join(root, "hr-evaluation-frontend");
const children = [];
let ownsLock = false;
let startedDatabase = false;
let stopRequested = false;
const cancelled = () => stopRequested || existsSync(stopPath);
const compose = (...args) => run("docker", ["compose", ...args]);

function run(command, args, { cwd = root, input, timeoutMs = 120000 } = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { cwd, windowsHide: true, stdio: ["pipe", "pipe", "pipe"] });
    let output = "";
    child.stdout.on("data", (chunk) => { output += chunk.toString(); });
    child.stderr.on("data", (chunk) => { output += chunk.toString(); });
    const timer = setTimeout(() => {
      child.kill();
      reject(new Error(`${command}の返事が時間内に得られませんでした。`));
    }, timeoutMs);
    child.on("error", (error) => { clearTimeout(timer); reject(error); });
    child.on("close", (code) => {
      clearTimeout(timer);
      if (code === 0) resolve(output.trim());
      else reject(new Error(`${command}の処理に失敗しました。\n${output.trim()}`));
    });
    child.stdin.on("error", () => {});
    child.stdin.end(input);
  });
}

function processExists(pid) {
  if (!Number.isInteger(pid) || pid <= 0) return false;
  try { process.kill(pid, 0); return true; } catch { return false; }
}

function portIsBusy(port) {
  return new Promise((resolve) => {
    const socket = net.createConnection({ host: "127.0.0.1", port });
    let settled = false;
    const finish = (busy) => { if (!settled) { settled = true; socket.destroy(); resolve(busy); } };
    socket.once("connect", () => finish(true));
    socket.once("error", () => finish(false));
    socket.setTimeout(1500, () => finish(true));
  });
}

function launchService(command, cwd, logName) {
  const log = openSync(path.join(runtime, logName), "a");
  const child = spawn("cmd.exe", ["/d", "/s", "/c", command], {
    cwd, windowsHide: true, stdio: ["ignore", log, log],
  });
  closeSync(log);
  children.push(child);
  child.on("error", () => { stopRequested = true; });
  return child;
}

async function hasEmployeeList(url) {
  try {
    const response = await fetch(url, { signal: AbortSignal.timeout(3000) });
    const body = await response.json();
    return response.ok && Array.isArray(body) && body.length >= 12;
  } catch { return false; }
}

async function cleanup() {
  for (const child of [...children].reverse()) {
    // 自分が起動して、まだ終了していない処理だけを停止する。
    if (child.pid && child.exitCode === null && child.signalCode === null) {
      await run("taskkill.exe", ["/PID", String(child.pid), "/T", "/F"]).catch((error) => console.error(error.message));
    }
  }
  if (startedDatabase) {
    await compose("stop", "db");
  }
}

async function start() {
  if (process.platform !== "win32") throw new Error("この起動用ファイルはWindows専用です。");
  if (!supportsNode(process.version)) throw new Error("Node.js 20.19以降の20系、または22.12以降を用意してください。");
  mkdirSync(runtime, { recursive: true });
  if (existsSync(lockPath)) {
    const previous = JSON.parse(readFileSync(lockPath, "utf8"));
    if (processExists(previous.pid)) throw new Error("起動用ファイルはすでに動作中です。\n終了する場合は「停止.cmd」を実行してください。");
    unlinkSync(lockPath);
  }
  const lock = openSync(lockPath, "wx");
  writeFileSync(lock, JSON.stringify({ pid: process.pid }));
  closeSync(lock);
  ownsLock = true;
  if (existsSync(stopPath)) unlinkSync(stopPath);

  const envPath = path.join(root, ".env");
  if (!existsSync(envPath)) {
    writeFileSync(envPath, readFileSync(path.join(root, ".env.example")), { flag: "wx" });
    throw new Error(".envのひな型を作成しました。\nREADMEに従って2つのパスワードを設定し、もう一度「起動.cmd」を実行してください。");
  }
  checkPasswordSettings(readFileSync(envPath, "utf8").replace(/^\uFEFF/, ""));

  console.log("[1/6] Java・Docker・npmを確認しています。");
  const java = await run("java", ["-version"]);
  if (!/version\s+"21[.\"]/.test(java)) throw new Error("Java 21を用意してください。\nJAVA_HOMEを設定している場合もJava 21を指定してください。");
  await run("cmd.exe", ["/d", "/s", "/c", "npm --version"]);
  for (const port of [8080, 5173]) {
    if (await portIsBusy(port)) throw new Error(`${port}番が使用中です。\n手動で起動した同じアプリなどを終了してから実行してください。\n使用中のプログラムを自動で停止することはありません。`);
  }
  await compose("version");
  await run("docker", ["info", "--format", "{{.ServerVersion}}"]);

  console.log("[2/6] MySQLを起動しています。");
  const previousDatabase = await compose("ps", "--status", "running", "-q", "db");
  startedDatabase = previousDatabase === "";
  await compose("up", "-d", "db");
  await waitUntil(async () => {
    try {
      await compose("exec", "-T", "db", "sh", "-c", 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --user=root --database="$MYSQL_DATABASE" -e "SELECT 1"');
      return true;
    } catch { return false; }
  }, { timeoutMs: 180000, message: "MySQLの準備が時間内に終わりませんでした。", cancelled });

  console.log("[3/6] 表・部署・社員を準備しています。\n保存済み評価は削除しません。");
  await initializeDatabase(
    (input) => run("docker", ["compose", "exec", "-T", "db", "sh", "-c", 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --default-character-set=utf8mb4 --user=root --database="$MYSQL_DATABASE"'], { input }),
    (name) => readFileSync(path.join(root, name)),
  );
  if (cancelled()) throw new Error("停止の依頼を受け付けました。");

  console.log("[4/6] 画面に必要なライブラリを準備しています。\n初回はダウンロードのため数分かかる場合があります。");
  const lockHash = createHash("sha256").update(readFileSync(path.join(frontend, "package-lock.json"))).digest("hex");
  const dependencyMarker = path.join(runtime, "npm-lock-hash");
  if (!existsSync(path.join(frontend, "node_modules", ".package-lock.json")) ||
      !existsSync(dependencyMarker) || readFileSync(dependencyMarker, "utf8") !== lockHash) {
    await run("cmd.exe", ["/d", "/s", "/c", "npm ci"], { cwd: frontend, timeoutMs: 600000 });
    writeFileSync(dependencyMarker, lockHash);
  }
  if (cancelled()) throw new Error("停止の依頼を受け付けました。");

  console.log("[5/6] 保存・計算のプログラムを起動しています。\n初回はライブラリ取得のため数分かかる場合があります。");
  const backendProcess = launchService(".\\mvnw.cmd spring-boot:run", backend, "backend.log");
  await waitUntil(() => {
    if (backendProcess.exitCode !== null) throw new Error("保存・計算のプログラムが終了しました。\n.launcher/backend.logを確認してください。");
    return hasEmployeeList("http://127.0.0.1:8080/api/employees");
  }, {
    timeoutMs: 600000, message: "保存・計算のプログラムが起動できませんでした。\n.launcher/backend.logを確認してください。",
    cancelled,
  });

  console.log("[6/6] 画面を起動しています。");
  const frontendProcess = launchService("npm run dev -- --host 127.0.0.1 --port 5173 --strictPort", frontend, "frontend.log");
  await waitUntil(() => {
    if (frontendProcess.exitCode !== null) throw new Error("画面のプログラムが終了しました。\n.launcher/frontend.logを確認してください。");
    return hasEmployeeList("http://127.0.0.1:5173/api/employees");
  }, {
    timeoutMs: 60000, message: "画面が起動できませんでした。\n.launcher/frontend.logを確認してください。",
    cancelled,
  });
  const browserOpened = await requestBrowser();
  if (!browserOpened) console.log("ブラウザの自動表示を確認できませんでした。\nアプリは起動したままです。\nChromeで http://127.0.0.1:5173/ を開いてください。");
  console.log("起動しました。\nブラウザが開かない場合：http://127.0.0.1:5173/\nこのウィンドウは開いたままにしてください。\n終了するときは「停止.cmd」を実行するか、このウィンドウでCtrl+Cを押してください。");
  while (!cancelled()) {
    if (children.some((child) => child.exitCode !== null || child.signalCode !== null)) {
      throw new Error("起動したプログラムが終了しました。\n.launcher内のログを確認してください。");
    }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
}

async function stop() {
  if (!existsSync(lockPath)) { console.log("この起動用ファイルで動作中のアプリはありません。\n手動で起動した場合は、そのウィンドウでCtrl+Cを押してください。"); return; }
  const owner = JSON.parse(readFileSync(lockPath, "utf8"));
  if (!processExists(owner.pid)) {
    unlinkSync(lockPath);
    console.log("起動用ファイルはすでに終了しています。\nプログラムが残っている場合はREADMEの手動停止手順を確認してください。");
    return;
  }
  writeFileSync(stopPath, "stop");
  console.log("停止を依頼しました。");
  await waitUntil(() => !existsSync(lockPath), { timeoutMs: 60000, message: "停止がまだ完了していません。\n起動用ファイルのウィンドウを確認してください。" });
  console.log("停止処理が終了しました。\n起動前から動作していたMySQLは、そのまま残します。");
}

if (process.argv[2] === "stop") {
  await stop().catch((error) => { console.error(error.message); process.exitCode = 1; });
} else if (process.argv[2] === "start") {
  for (const signal of ["SIGINT", "SIGTERM"]) process.on(signal, () => { stopRequested = true; });
  try { await start(); }
  catch (error) { console.error(error.message); process.exitCode = 1; }
  finally {
    if (ownsLock) {
      try { await cleanup(); }
      catch (error) { console.error(`停止処理を確認してください。\n${error.message}`); process.exitCode = 1; }
      for (const filename of [stopPath, lockPath]) if (existsSync(filename)) unlinkSync(filename);
    }
  }
} else {
  console.error("「起動.cmd」または「停止.cmd」を実行してください。");
  process.exitCode = 1;
}
