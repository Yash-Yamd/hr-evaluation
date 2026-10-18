import assert from "node:assert/strict";
import test from "node:test";
import { EventEmitter } from "node:events";
import { checkPasswordSettings, initializeDatabase, requestBrowser, supportsNode, waitUntil } from "./launcher-core.mjs";

test("Viteが要求するNode.jsの境目を判定する", () => {
  for (const version of ["20.19.0", "22.12.0", "24.20.0"]) assert.equal(supportsNode(version), true);
  for (const version of ["18.20.0", "20.18.9", "21.7.0", "22.11.9"]) assert.equal(supportsNode(version), false);
});

test("未設定・ひな型のパスワードを拒否し、エラーに設定値を含めない", () => {
  assert.throws(() => checkPasswordSettings("DB_PASSWORD=replace_with_your_database_password\nDB_ROOT_PASSWORD=test-only-value"), /DB_PASSWORD/);
  assert.throws(() => checkPasswordSettings("DB_PASSWORD=test-only-value\nDB_ROOT_PASSWORD="), (error) =>
    error.message.includes("DB_ROOT_PASSWORD") && !error.message.includes("test-only-value"));
  assert.doesNotThrow(() => checkPasswordSettings("DB_PASSWORD=test-only-one\nDB_ROOT_PASSWORD=test-only-two\n"));
});

test("表作成に失敗した場合は部署・社員の登録へ進まない", async () => {
  const readNames = [];
  await assert.rejects(initializeDatabase(
    async () => { throw new Error("表の作成に失敗"); },
    async (name) => { readNames.push(name); return "確認用SQL"; },
  ), /表の作成に失敗/);
  assert.deepEqual(readNames, ["schema.sql"]);
});

test("起動待ちの途中で停止依頼があれば、後続の確認を実行しない", async () => {
  let checks = 0;
  await assert.rejects(waitUntil(async () => { checks++; return false; }, {
    timeoutMs: 1000, intervalMs: 1, cancelled: () => true, message: "時間切れ",
  }), /停止の依頼/);
  assert.equal(checks, 0);
});

test("時間内に準備ができなかった場合を成功扱いしない", async () => {
  await assert.rejects(waitUntil(async () => false, {
    timeoutMs: 0, message: "起動できませんでした",
  }), /起動できませんでした/);
});

test("ブラウザの出力が閉じなくても、起動命令が終了すれば待機を終える", async () => {
  const shell = new EventEmitter();
  const opened = requestBrowser(() => shell, 1000);
  // closeは発生させない。従来の出力終了待ちでは止まっていた条件。
  shell.emit("exit", 0);
  assert.equal(await opened, true);
});

test("ブラウザの起動失敗は例外にせず、手動表示へ切り替えられる結果を返す", async () => {
  const shell = new EventEmitter();
  const opened = requestBrowser(() => shell, 1000);
  shell.emit("error", new Error("ブラウザを開けない"));
  assert.equal(await opened, false);
});

test("ブラウザから返事がない場合も、時間切れで待機を終える", async () => {
  assert.equal(await requestBrowser(() => new EventEmitter(), 5), false);
});
