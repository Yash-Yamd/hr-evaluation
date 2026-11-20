# Windowsでの起動と操作

[アプリの概要に戻る](../README.md)

以降のコマンドは、特記のない限りリポジトリ直下で実行します。

起動方法は、次の2通りを用意しています。\
どちらも同じアプリを起動します。

- **方法A：起動用ファイルを使う** ― コマンドを一つずつ入力せずに準備・起動したい場合。\
- **方法B：Windows PowerShellでコマンドを実行する** ― 準備・起動の処理を順番に確認しながら進めたい場合。

まず「共通の事前準備」を行い、その後はいずれか一方の方法で起動してください。\
2つの方法を同時に実行する必要はありません。\
この案内は、ZIPを展開するかGitから取得して、ファイルをPC内に保存した状態を前提にしています。

### 共通の事前準備

#### 必要なもの

- Java 21
- Node.jsとnpm（Node.js 20.19以降の20系、または22.12以降）
- DockerとDocker Composeが使える環境
- PC版Chrome

Mavenは同梱の `mvnw.cmd` から実行します。\
別途Mavenをインストールする手順は不要です。\
初回実行時には必要なライブラリを取得します。\
初回のライブラリ・MySQLイメージの取得には、インターネット接続と数分以上の待ち時間が必要になる場合があります。\
Docker Desktopを起動し、Dockerが利用できる状態にしてください。\
起動用ファイルは、これらのソフト自体をインストールするものではありません。

#### パスワードの設定

`compose.yaml`と同じフォルダの`.env.example`をコピーして、`.env`という名前で、リポジトリ直下に保存します。\
`.env.txt`にならないよう、Windowsのエクスプローラーでファイル名の拡張子を表示して確認してください。\
ファイル内の`replace_with_...`の2つの値を、それぞれ自分の環境用のパスワードへ置き換えます。\
`DB_PASSWORD`と`DB_ROOT_PASSWORD`の名前は変更しません。\
前者は一般ユーザー用、後者は管理者用のパスワードです。\
MySQLとJavaの設定として共通に読み込むため、半角英数字を組み合わせた十分に長い値を指定し、値を引用符で囲まないでください。\
実際のパスワードを提出文やGitHubに載せる必要はありません。

- 一般ユーザー名は `hr_user`、データベース名は `hr_evaluation` です。\
- Spring Bootは `.env` の `DB_PASSWORD` を読み込みます。\
- 初期設定済みの環境では、MySQLに設定済みのパスワードを使います。\
  `.env` だけを書き換えても、既存のMySQLのパスワードは変更されません。\
- `.env` は `.gitignore` でGit管理対象から除外しています。\
  実際のパスワードはREADMEやGitに記載しません。

### 方法A：起動用ファイルを使う

1. 共通の事前準備を済ませ、`起動.cmd`をダブルクリックします。\
2. 表示される進捗に従って待ちます。\
   必要なソフトの確認、MySQLの起動、表・部署・社員の準備、画面用ライブラリの準備、バックエンドとフロントエンドの起動を順番に行います。\
3. 起動が完了すると既定のブラウザに`http://127.0.0.1:5173/`を開きます。\
   Chrome以外のブラウザが開いた場合は、このURLをChromeで開いてください。\
   自動で開かない場合もアプリは起動を続けるので、URLを手動で開けます。\
4. 使用中は起動用ファイルのウィンドウを開いたままにします。\
5. 終了する場合は`停止.cmd`をダブルクリックします。\
   起動用ファイルのウィンドウで`Ctrl + C`を押しても停止処理を依頼できます。

`.env`がない場合は、起動用ファイルがひな型を作成して案内を出します。\
パスワードの設定後、もう一度`起動.cmd`を実行してください。\
初回の評価は0件であり、画面から全12人の評価を保存すると相対評価を実行できます。

既存の部署・社員の初期登録は同じ番号の値を上書きせず、保存済み評価も削除しません。\
初期準備SQLは既存の表の構造を変更するものではありません。\
8080番または5173番が使用中の場合は、使用中のプログラムを勝手に終了せず、案内を出して停止します。\
手動で同じアプリを起動している場合は先に終了してください。

停止用ファイルは、この起動用ファイルから動かしたバックエンド・フロントエンドを停止します。\
MySQLも今回新たに起動した場合だけ停止し、起動前から動作していたMySQLはそのまま残します。\
データを保存しているDockerの領域を削除する処理はありません。

起動に失敗した場合は、表示された案内を確認してください。\
バックエンド・フロントエンドの詳細は`.launcher/backend.log`と`.launcher/frontend.log`に残ります。\
`.launcher`はGit管理対象外です。\
手順を一つずつ確認したい場合は、次の方法Bを使用できます。

### 方法B：Windows PowerShellでコマンドを実行する

起動用ファイルを使わず、次の手順を順番に実行する方法です。\
初めて使う環境は「初回のデータベース準備」から、準備済みの環境は「アプリの起動」から進めてください。

#### 初回のデータベース準備

初回構築用の表・部署・社員の準備手順です。\
Windows PowerShellで、リポジトリ直下の`compose.yaml`があるフォルダから実行します。\
先に前項の`.env`を設定してください。

1. Dockerを利用できる状態にし、MySQLを起動します。

```powershell
docker compose up -d db
docker compose ps
```

初めて使う保存領域では、`compose.yaml`の設定によりデータベース`hr_evaluation`と一般ユーザー`hr_user`が用意されます。\
表と初期データは次の手順で手動登録します。\
起動直後に接続を拒否された場合は、MySQLの準備が終わってからやり直してください。

2. 3つのSQLファイルをMySQLのコンテナ内へコピーします。

```powershell
docker cp .\schema.sql hr-evaluation-db-1:/tmp/hr-evaluation-schema.sql
docker cp .\seed-departments.sql hr-evaluation-db-1:/tmp/hr-evaluation-seed-departments.sql
docker cp .\seed-employees.sql hr-evaluation-db-1:/tmp/hr-evaluation-seed-employees.sql
```

3. 表→部署→社員の順番に実行します。\
   途中で`ERROR`が出た場合は、そこで止めて内容を確認します。\
   パスワードはコンテナ内の環境変数から渡すため、コマンドへ直接記入しません。

```powershell
docker exec hr-evaluation-db-1 sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --default-character-set=utf8mb4 --user=root --database=hr_evaluation < /tmp/hr-evaluation-schema.sql'
docker exec hr-evaluation-db-1 sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --default-character-set=utf8mb4 --user=root --database=hr_evaluation < /tmp/hr-evaluation-seed-departments.sql'
docker exec hr-evaluation-db-1 sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --default-character-set=utf8mb4 --user=root --database=hr_evaluation < /tmp/hr-evaluation-seed-employees.sql'
```

ここでの`root`は初回準備用です。\
通常のアプリ接続には、既存の設定どおり`hr_user`を使います。\
`utf8mb4`は日本語を扱うための文字コード指定です。

4. 作成結果を確認します。

```powershell
'SHOW TABLES; SELECT (SELECT COUNT(*) FROM departments) AS departments_count, (SELECT COUNT(*) FROM employees) AS employees_count, (SELECT COUNT(*) FROM evaluations) AS evaluations_count;' | docker exec -i hr-evaluation-db-1 sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql --user=root --database=hr_evaluation'
```

初回の空の環境では、表が`departments`・`employees`・`evaluations`の3つ、部署3件・社員12件・評価0件になります。\
評価点は最初から登録せず、画面で入力・保存します。\
全員分の保存が終わるまで相対評価は実行できません。\
準備後は、次節のバックエンド・フロントエンドの起動へ進みます。

既存の表は`IF NOT EXISTS`で作成を省略します。\
既存の表の構造を更新する機能はありません。\
部署・社員の初期登録も同じ番号の既存データを上書きしません。\
保存済み評価がある環境では、評価件数は0件とは限りません。

初回準備SQLの検証結果は[テストと確認記録](testing.md#初回準備sqlの確認範囲)を参照してください。

#### アプリの起動

Windows PowerShellで、3つのターミナルを使います。\
各ターミナルの最初の作業場所は、リポジトリ直下の`compose.yaml`があるフォルダとします。

##### 1. MySQLを起動する

Dockerが利用できる状態にして、実行します。

```powershell
docker compose up -d
docker compose ps
```

`db` が起動していることを確認します。\
MySQLの起動直後は接続できるまで少し待つ場合があります。

##### 2. 保存・計算のプログラムを起動する

別のターミナルで実行します。

```powershell
cd .\hr-evaluation-backend
.\mvnw.cmd spring-boot:run
```

`Started HrEvaluationBackendApplication` が表示されたら起動完了です。\
このターミナルは開いたままにします。

##### 3. 画面を起動する

さらに別のターミナルで実行します。\
`npm ci` は初回、または必要なライブラリを揃え直す場合に実行します。

```powershell
cd .\hr-evaluation-frontend
npm ci
npm run dev -- --host 127.0.0.1
```

このターミナルも開いたままにし、Chromeで [評価入力画面](http://127.0.0.1:5173/) を開きます。\
5173が使用中の場合は、ターミナルに表示されたURLを確認します。

#### PowerShellで起動した場合の終了方法

1. 画面を起動したターミナルで `Ctrl + C` を押します。\
2. Spring Bootを起動したターミナルで `Ctrl + C` を押します。\
3. リポジトリ直下の`compose.yaml`があるフォルダのターミナルで次を実行します。

```powershell
docker compose stop
```

## 操作方法

1. 「評価する社員」から社員を選びます。\
2. 保存済みの評価があれば、5項目の点数が読み込まれます。\
   未保存の場合は未選択になります。\
3. 5項目をそれぞれ1～5で選択し、合計点を確認します。\
4. 「評価を保存」を押します。\
   初回は「保存しました」、更新時は「更新しました」と表示されます。\
5. 全員分を保存したら、「相対評価を実行（部署別の標準化）→」を押します。\
6. 結果画面で、社員・部署・合計点・標準化点・全社順位・ランクを確認します。\
7. 「← 評価入力に戻る」で入力画面へ戻れます。

社員と5項目が選ばれ、評価の読み込み中・保存中・削除中・相対評価の取得中ではなく、評価の読み込みに失敗していない場合に保存できます。\
全員の保存状況を確認でき、処理中ではない場合に相対評価を実行できます。

保存済み評価を削除する場合は、社員を選んで「評価を削除」を押し、確認画面でOKを選びます。\
取消時は削除しません。\
成功すると社員選択を保ったまま5項目が未選択・合計0点となり、「削除しました」と表示します。\
全員分を保存し直すまで相対評価は実行できません。\
失敗時は画面の点数を維持して案内し、社員を選び直して保存状態を確認します。\
最初の保存状況の確認中や、読み込み・保存・削除・計算の途中には削除できません。

**相対評価に使うのは保存済みの点数です。**\
点数を変更した場合は、先に「評価を保存」を押してから相対評価を実行してください。

## よくある起動時の確認

- **画面が開かない**：Viteのターミナルが起動したままか、表示されたURLが正しいか確認します。\
- **社員一覧や評価を読み込めない**：MySQLとSpring Bootの起動状態を確認します。\
  停止していたものを起動したあと、画面を再読み込みします。\
- **相対評価を実行できない**：全員の評価が保存済みか、保存状況の確認に失敗していないか確認します。\
- **MySQLへ接続できない**：`docker compose ps`、開発用PCの3311番、既存データベースと `.env` のパスワードの一致を確認します。
