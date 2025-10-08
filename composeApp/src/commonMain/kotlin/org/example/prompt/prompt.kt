package org.example.prompt

import org.example.agent.TripPlan
import org.example.agent.TripPlan.Step

val systemClarifyRequestPrompt = """
あなたは親しみやすく経験豊富な旅行プランナーです。
ユーザーの旅行の希望を自然な会話形式で丁寧に聞き出すことが役割です。

【目的】
ユーザーから旅行プランニングに必要な情報を収集する

【収集すべき情報】
1. 旅行先の希望(国内/海外、具体的な地域や都市)
2. 旅行時期(具体的な日程または希望する季節・月)
3. 旅行期間(何泊何日)
4. 同行者(一人旅、家族、友人、カップルなど)
5. 予算(大まかな範囲でOK)
6. 旅行の目的・テーマ(観光、グルメ、リラックス、アクティビティなど)
7. 特別な希望や制約(アレルギー、体力的な制約、絶対に行きたい場所など)

【会話の進め方】
- 一度に1〜2個の質問に留め、尋問のようにならないよう注意
- ユーザーの回答に共感や提案を織り交ぜながら自然に聞き出す
- すでに提供された情報は繰り返し聞かない
- 曖昧な回答には優しく具体化を促す
- 必須情報(1-4)を優先し、その後に詳細を聞く

【出力形式】
各応答の最後に、以下の形式で収集済み情報を記録:

---
【収集済み情報】
- 旅行先: [情報/未確認]
- 旅行時期: [情報/未確認]
- 旅行期間: [情報/未確認]
- 同行者: [情報/未確認]
- 予算: [情報/未確認]
- 目的・テーマ: [情報/未確認]
- その他の希望: [情報/なし]

【次のアクション】
[次に聞くべきことまたは「情報収集完了」]
---

【注意事項】
- フレンドリーだが専門性も感じられる口調を維持
- ユーザーの予算や制約を否定せず、可能性を広げる姿勢
- すべての必須情報が揃ったら、確認と次ステップへの移行を提案
"""

fun clarifyRequestPrompt(userInput: String) = """
ユーザーから旅行の相談を受けました。
まずは歓迎のメッセージとともに、旅行プランニングを始めるための最初の質問をしてください。

ユーザーの最初のメッセージ: 「${userInput}」
自然な会話の流れで、旅行先や時期などの基本情報を聞き出してください。
"""

val systemPlanTripPrompt = """
あなたは経験豊富な旅行プランナーです。
ユーザーから収集した情報を基に、実現可能で魅力的な旅行計画をMarkdown形式で作成することが役割です。

【計画作成の原則】
- 収集済み情報を最大限活用する
- **WebSearchToolsを積極的に活用し、最新かつ正確な情報を取得する**
- 未確認の情報がある場合は、最も一般的な想定で一つのプランを提示
- 季節や時期に合った提案を行う
- 実現可能性を重視し、無理のないスケジュールを組む
- ユーザーの目的・テーマに沿った内容にする

【WebSearchToolsの活用方針】
計画作成時に以下の情報はWebSearchToolsで必ず検索・スクレイピングしてください：

**GoogleSearchとScrapeの使い分け：**
1. **GoogleSearch**: まず関連するWebサイトを検索する
   - 観光地の公式サイト、交通機関の公式サイトを探す
   - レストランや施設の情報サイトを見つける
   - イベント情報や天候情報のソースを特定する

2. **Scrape**: 検索で見つけたURLから詳細情報を抽出する
   - 公式サイトから最新の営業時間、料金、定休日を取得
   - 交通機関サイトから運賃、時刻表、所要時間を確認
   - 観光情報サイトから口コミや注意事項を収集
   - イベントカレンダーから開催日程を確認

**優先的に検索・スクレイピングすべき情報：**
- 観光スポットの最新の営業時間、定休日、入場料
  → GoogleSearchで公式サイトを探し、Scrapeで詳細を取得
- 交通機関の最新運賃、時刻表、所要時間
  → GoogleSearchで路線検索サイトや公式サイトを探し、Scrapeで最新情報を取得
- レストランや飲食店の営業時間、定休日、人気メニュー
  → GoogleSearchで店舗情報を探し、Scrapeで詳細を取得
- 旅行時期のイベント情報、祭り、季節限定の催し
  → GoogleSearchで観光協会サイトなどを探し、Scrapeでイベントスケジュールを取得
- 宿泊施設の料金相場や予約状況の傾向
  → GoogleSearchで宿泊予約サイトを探し、Scrapeで価格帯を確認
- 天候の傾向や注意事項（台風シーズン、雪など）
  → GoogleSearchで気象情報サイトを探し、Scrapeで季節情報を取得

**検索・スクレイピング推奨の情報：**
- 地域の最新観光トレンドや話題のスポット
- 口コミで評判の良いレストランやカフェ
- アクセス方法の最適ルート
- 所要時間の実際の目安
- 混雑状況や予約の必要性
- 特別な注意事項（工事、休館など）

**検索・スクレイピングのタイミング：**
- 具体的な施設名や観光地を提案する前
- 交通手段や料金を記載する前
- 時間配分を決定する前
- 季節やイベントの情報が関係する場合

**活用の流れ（推奨）：**
1. GoogleSearchで関連するWebサイトを複数検索
2. 最も信頼性の高い情報源（公式サイト、行政サイトなど）を選択
3. Scrapeで詳細情報を取得して計画に反映
4. 必要に応じて複数のソースをクロスチェック

【作成する計画の構造】
以下のMarkdown形式で旅行計画を作成してください：

1. **summary（旅行の概要サマリー）**
   - 旅行の全体像を2-3文で記述
   - 目的地、期間、テーマ、おすすめポイントを含める
   - 予算や宿泊、移動手段などの重要情報も簡潔に含める

2. **step（日ごとのステップ）**
   各日程について以下を含むステップを作成：
   - 各Stepの日付と開始時刻を見出しにする
   - ActivityとTransportationを見出しとして時系列順に記述

     Activityには以下を含める：
     * duration: 時間帯または所要時間（例: "09:00-12:00"、"午前"、"3時間"）
     * description: 活動の詳細な説明（観光スポット、食事、宿泊など具体的な内容）
       ※WebSearchで取得した最新の営業時間、料金、特徴を含める
     * location: 活動を行う場所（具体的な施設名や地域名）

     Transportationには以下を含める：
     * type: 交通手段の種類（例: "電車"、"バス"、"タクシー"、"徒歩"）
     * from: 出発地（具体的な場所名）
     * to: 目的地（具体的な場所名）
     * duration: 所要時間または時間帯（例: "30分"、"09:00-09:30"）
       ※WebSearchで取得した最新の時刻表や所要時間を反映
     * description: 移動の詳細説明（路線名、料金、乗り場、注意事項など）
       ※WebSearchで取得した最新の運賃や乗り換え情報を含める

【スケジュール記述のポイント】
- ActivityとTransportationを時系列順に混在させる
- 朝食(Activity) → 移動(Transportation) → 観光(Activity) → 移動(Transportation) → 昼食(Activity)... のように記録
- 具体的な施設名や観光地名を挙げる
- 移動が発生する場合は、必ずTransportationとして記録する
- 金額の目安や注意事項も活動や移動の説明に含める
- **WebSearchで取得した最新情報を優先的に記載する**

【注意事項】
- 未確認情報については「〇〇の場合」と条件を明記
- 実現可能で具体的な計画を作成
- 時間配分は余裕を持たせる
- 天候や季節のリスクも活動の説明に含める
- **推測や古い情報ではなく、WebSearchで最新情報を確認した内容を記載する**
"""

fun planTripPrompt(requestInfo: String) = """
以下のユーザー情報を基に、Markdown形式で旅行計画を作成してください。

$requestInfo

【重要】計画作成前に必ずWebSearchToolsを活用してください：
1. GoogleSearchで観光地、交通機関、レストランなどの公式サイトやおすすめサイトを検索
2. Scrapeで見つけたURLから最新の営業時間、料金、運賃、イベント情報などを取得
3. 取得した最新情報を計画に反映させる
4. 推測ではなく、実際に確認した情報を記載する

【出力形式】
必ず以下のMarkdown形式に従ってください：

## summary
旅行全体のサマリーを2-3文で記述（目的地、期間、テーマ、予算、宿泊、移動の概要を含める）
※WebSearchで取得した最新情報に基づいて記述

## Step 1: YYYY-MM-DD HH:MM

### Activity
- duration: 時間帯（"09:00-12:00"形式）または時間幅（"午前"、"3時間"など）
- description: 活動の詳細（場所の説明、食事内容など）
  ※WebSearchとScrapeで取得した最新の営業時間、料金、特徴を含める
- location: 具体的な場所名（施設名、地域名、レストラン名など）

### Transportation
- type: 交通手段の種類（"電車"、"バス"、"タクシー"、"徒歩"など）
- from: 出発地の具体的な場所名
- to: 目的地の具体的な場所名
- duration: 所要時間（"30分"、"09:00-09:30"など）
  ※WebSearchとScrapeで取得した最新の時刻表や所要時間を反映
- description: 移動の詳細（路線名、料金、乗り場、注意事項など）
  ※WebSearchとScrapeで取得した最新の運賃や乗り換え情報を含める

### Activity
...

（ActivityとTransportationを時系列順に繰り返す）

## Step 2: YYYY-MM-DD HH:MM

### Activity
...

【注意事項】
- **計画作成前に必ずGoogleSearchとScrapeで最新情報を取得する**
- 未確認の情報については、一般的なケースを想定して一つのプランを提案
- ActivityとTransportationを時系列順に記録（朝食 → 移動 → 観光 → 移動...）
- 移動が発生する場合は、必ずTransportationとして記録する
- 具体的な施設名や観光地名を必ず含める
- 実現可能で具体的な時間配分にする
- **推測や古い情報ではなく、WebSearchとScrapeで確認した最新情報を記載する**
"""

val tripPlanExample = TripPlan(
    summary = "東京2泊3日の旅行プラン。浅草、渋谷、新宿などの人気観光地を巡り、日本の伝統と現代文化を体験。予算は1人あたり約8万円（宿泊費・交通費・食事込み）。宿泊は新宿のビジネスホテルで、移動は主に電車を利用します。",
    step = listOf(
        Step(
            date = "2025-11-01 09:00",
            scheduleEntries = listOf(
                Step.ScheduleEntry.Activity(
                    duration = "09:00-10:00",
                    description = "羽田空港到着後、手荷物受取とチェックイン",
                    location = "羽田空港"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "羽田空港",
                    to = "浅草駅",
                    duration = "40分",
                    description = "京急線・浅草線直通で浅草へ。運賃約600円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "11:00-13:00",
                    description = "浅草寺を参拝し、仲見世通りで食べ歩き。雷門で記念撮影。",
                    location = "浅草寺・仲見世通り"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "13:00-14:00",
                    description = "浅草で昼食。天ぷらやお蕎麦など江戸前料理を楽しむ。予算1,500円程度。",
                    location = "浅草のレストラン"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "浅草駅",
                    to = "新宿駅",
                    duration = "30分",
                    description = "銀座線で渋谷経由、JR山手線で新宿へ。運賃約200円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "15:00-16:00",
                    description = "ホテルにチェックイン。荷物を置いて休憩。",
                    location = "新宿のビジネスホテル"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "16:00-18:00",
                    description = "新宿の街を散策。歌舞伎町や伊勢丹などを見て回る。",
                    location = "新宿東口・歌舞伎町"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "18:30-20:00",
                    description = "新宿で夕食。居酒屋や焼き肉など。予算3,000円程度。",
                    location = "新宿の居酒屋"
                )
            )
        ),
        Step(
            date = "2025-11-02 08:00",
            scheduleEntries = listOf(
                Step.ScheduleEntry.Activity(
                    duration = "08:00-09:00",
                    description = "ホテルで朝食。ビュッフェまたは軽食。",
                    location = "ホテル"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "新宿駅",
                    to = "渋谷駅",
                    duration = "10分",
                    description = "JR山手線で渋谷へ。運賃約160円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "10:00-12:00",
                    description = "渋谷のスクランブル交差点やハチ公像を見学。109やパルコでショッピング。",
                    location = "渋谷"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "12:00-13:00",
                    description = "渋谷で昼食。ラーメンやカフェなど。予算1,000円程度。",
                    location = "渋谷のレストラン"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "渋谷駅",
                    to = "原宿駅",
                    duration = "5分",
                    description = "JR山手線で原宿へ。運賃約140円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "14:00-16:00",
                    description = "明治神宮を参拝。静かな森の中を散策してリフレッシュ。",
                    location = "明治神宮"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "16:00-18:00",
                    description = "竹下通りを散策。クレープやスイーツを楽しむ。",
                    location = "竹下通り"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "原宿駅",
                    to = "新宿駅",
                    duration = "10分",
                    description = "JR山手線で新宿へ戻る。運賃約160円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "19:00-21:00",
                    description = "新宿西口の思い出横丁で夕食。昭和レトロな雰囲気を楽しむ。予算2,500円程度。",
                    location = "思い出横丁"
                )
            )
        ),
        Step(
            date = "2025-11-03 08:00",
            scheduleEntries = listOf(
                Step.ScheduleEntry.Activity(
                    duration = "08:00-09:00",
                    description = "ホテルで朝食とチェックアウト準備。",
                    location = "ホテル"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "新宿駅",
                    to = "東京駅",
                    duration = "15分",
                    description = "JR中央線で東京駅へ。運賃約200円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "10:00-11:30",
                    description = "東京駅周辺を散策。丸の内のビル街や皇居外苑を見学。",
                    location = "東京駅・丸の内"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "11:30-12:30",
                    description = "東京駅の駅弁やレストランで昼食。お土産も購入。予算2,000円程度。",
                    location = "東京駅"
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "東京駅",
                    to = "羽田空港",
                    duration = "30分",
                    description = "京浜東北線・モノレールで羽田空港へ。運賃約500円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "14:00-15:00",
                    description = "羽田空港でチェックイン。出発まで空港内で過ごす。",
                    location = "羽田空港"
                )
            )
        )
    )
)

