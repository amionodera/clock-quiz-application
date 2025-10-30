package main;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;
import java.util.Scanner;

// 時間の加減算クイズアプリ
public class TimeQuizApp {

    // クイズの出題モード（足し算／引き算／ランダム）を定義
    enum Mode { ADD, SUBTRACT, RANDOM }

    // ユーザー入力用スキャナ（標準入力）
    private static final Scanner scanner = new Scanner(System.in);

    // ランダム値生成用インスタンス
    private static final Random random = new Random();

    // 連続正解数（スコア管理）
    private static int streak = 0;

    // 現在の出題モード（起動時に選択）
    private static Mode mode;

    // アプリのエントリーポイント（起動処理）
    public static void main(String[] args) {
        System.out.println("🕒 時間クイズへようこそ！");

        // モードの選択（足し算／引き算／ランダム）
        selectMode();

        // クイズループの開始
        runQuiz();
    }

    // ユーザーに出題モードを選ばせる処理
    private static void selectMode() {
        System.out.println("モードを選んでください：");
        System.out.println("1. 足し算だけ");
        System.out.println("2. 引き算だけ");
        System.out.println("3. ランダム");
        System.out.print("番号を入力：");

        int choice = scanner.nextInt();

        // 入力値に応じてモードを設定（不正な値はランダム扱い）
        switch (choice) {
            case 1: mode = Mode.ADD; break;
            case 2: mode = Mode.SUBTRACT; break;
            default: mode = Mode.RANDOM; break;
        }
    }

    // クイズのメインループ処理
    private static void runQuiz() {
        boolean continuePlaying = true;

        while (continuePlaying) {
            // 問題を1問生成
            Question q = generateQuestion();

            // 問題文の表示（例：3時15分 の 1時間45分後 は何時何分？）
            System.out.printf("\n🕒 問題：%d時%d分 の %d時間%d分%s は何時何分？\n",
                    q.baseTime.getHour(), q.baseTime.getMinute(),
                    q.duration.toHoursPart(), q.duration.toMinutesPart(),
                    q.isAddition ? "後" : "前");

            // ユーザーの答えを取得（時・分）
            LocalTime userAnswer = askUserInput();

            // 答えの正誤判定
            boolean correct = checkAnswer(userAnswer, q.correctAnswer);

            // 判定結果に応じてメッセージ表示とスコア更新
            if (correct) {
                System.out.println("✅ 正解！おめでとう！");
                streak++; // 連続正解数を加算
            } else {
                System.out.printf("❌ 不正解。正解は「%d時%d分」でした。\n",
                        q.correctAnswer.getHour(), q.correctAnswer.getMinute());
                streak = 0; // 連続正解数をリセット
            }

            // 現在の連続正解数を表示
            System.out.printf("🌟 連続正解数：%d問\n", streak);

            // 次の問題に進むか確認
            System.out.print("\n次の問題に進みますか？（y/n）：");
            String next = scanner.next();
            continuePlaying = next.equalsIgnoreCase("y");
        }

        // 終了メッセージ
        System.out.println("🦊 遊んでくれてありがとう！");
    }

    // クイズ1問分の情報を保持する内部クラス
    private static class Question {
        LocalTime baseTime;       // 問題の基準時刻
        Duration duration;        // 加減する時間（1〜3時間＋0〜59分）
        boolean isAddition;       // 足し算か引き算か
        LocalTime correctAnswer;  // 正解の時刻（計算済み）

        // コンストラクタ：問題の構成要素を初期化
        Question(LocalTime baseTime, Duration duration, boolean isAddition, LocalTime correctAnswer) {
            this.baseTime = baseTime;
            this.duration = duration;
            this.isAddition = isAddition;
            this.correctAnswer = correctAnswer;
        }
    }

    // 問題をランダムに生成する処理
    private static Question generateQuestion() {
        // ランダムな基準時刻（0〜23時、0〜59分）を生成
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        LocalTime baseTime = LocalTime.of(hour, minute);

        // ランダムな加減時間（1〜3時間、0〜59分）を生成
        int addHours = random.nextInt(3) + 1;
        int addMinutes = random.nextInt(60);
        Duration duration = Duration.ofHours(addHours).plusMinutes(addMinutes);

        // モードに応じて加算か減算かを決定
        boolean isAddition = switch (mode) {
            case ADD -> true;
            case SUBTRACT -> false;
            case RANDOM -> random.nextBoolean();
        };

        // 正解の時刻を計算（加算または減算）
        LocalTime correctAnswer = isAddition
                ? baseTime.plus(duration)
                : baseTime.minus(duration);

        // 問題オブジェクトを生成して返却
        return new Question(baseTime, duration, isAddition, correctAnswer);
    }

    // ユーザーから時刻（時・分）を入力してもらう処理
    private static LocalTime askUserInput() {
        System.out.print("あなたの答えを入力してください：\n時：");
        int hour = scanner.nextInt();
        System.out.print("分：");
        int minute = scanner.nextInt();

        // 入力値が範囲外でもエラーにならないよう、24時間制・60分制に丸めて時刻を生成
        return LocalTime.of(hour % 24, minute % 60);
    }

    // ユーザーの答えと正解を比較して、正誤を判定する処理
    private static boolean checkAnswer(LocalTime user, LocalTime correct) {
        // 時と分が一致していれば正解とする（秒やナノ秒は無視）
        return user.getHour() == correct.getHour() && user.getMinute() == correct.getMinute();
    }
}
