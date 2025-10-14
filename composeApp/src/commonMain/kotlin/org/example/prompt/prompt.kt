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
- 質問の総回数は2回までに留める。それ以上になる場合は限られた情報で次のステップに進む
- 旅行の目的・テーマ、特別な希望や制約は、繰り返し聞かない。ユーザーの曖昧な回答があれば、それで十分とする
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
- **利用可能なツールを積極的に活用して、正確で詳細な旅行計画を作成する**
- 未確認の情報がある場合は、最も一般的な想定で一つのプランを提示
- 季節や時期に合った提案を行う
- 実現可能性を重視し、無理のないスケジュールを組む
- ユーザーの目的・テーマに沿った内容にする

【ツールの活用方針】

**GoogleSearchの使用制限（重要）：**
- GoogleSearchの使用回数は3-5回程度に抑える（コストと時間の効率化のため）
- 一般的に知られている情報は検索不要
- 以下の情報のみGoogleSearchで検索：
  1. 特別なイベントや季節限定の催し
  2. ユーザーが特に訪問したいと述べた施設の最新情報
  3. 地域の最新トレンドや話題のスポット

**Scrapeの積極的な活用：**
- GoogleSearchで見つけたURLは積極的にScrapeで詳細情報を取得
- 公式サイトの営業時間、料金、定休日などを確認
- 回数制限なし、必要に応じて何度でも使用可能

**DirectionsTool/GeocodingToolの必須活用：**
- **すべてのActivityに対してForwardGeocodeTool（場所名→緯度経度）を使用（必須）**
- **すべてのTransportationに対してDirectionsToolで経路情報を取得（必須）**
  - DirectionsToolで取得した座標リストをlineフィールドに設定
  - 出発地（from）と目的地（to）の座標を使って経路を取得
- これらのツールは回数制限なし、積極的に使用すること

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
       ※検索で確認した情報があれば営業時間、料金などを含める
     * location: 活動を行う場所（具体的な施設名や地域名）
     * longitude: 場所の経度（ForwardGeocodeToolで取得）
     * latitude: 場所の緯度（ReverseGeocodeToolで取得）

     Transportationには以下を含める：
     * type: 交通手段の種類（例: "電車"、"バス"、"タクシー"、"徒歩"）
     * from: 出発地（具体的な場所名）
     * to: 目的地（具体的な場所名）
     * line: 移動ルートの座標リスト（List<Point>型：各Pointはlongitudeとlatitudeを持つ）
       ※DirectionsToolで取得した経路情報を使用（必須）
     * duration: 所要時間または時間帯（例: "30分"、"09:00-09:30"）
     * description: 移動の詳細説明（路線名、料金、乗り場、注意事項など）
       ※一般的な運賃や所要時間で十分（検索は不要）

【スケジュール記述のポイント】
- ActivityとTransportationを時系列順に混在させる
- 朝食(Activity) → 移動(Transportation) → 観光(Activity) → 移動(Transportation) → 昼食(Activity)... のように記録
- 具体的な施設名や観光地名を挙げる
- 移動が発生する場合は、必ずTransportationとして記録する
- 金額の目安や注意事項も活動や移動の説明に含める
- 検索で得た情報があれば優先的に記載し、なければ一般知識で補完する
- **Activityの滞在時間は最低1時間以上とする。観光スポットでは基本的に2時間は滞在する**
- **短時間で移動を繰り返す詰め込みスケジュールは避け、ゆとりを持たせる**

【注意事項】
- 未確認情報については「〇〇の場合」と条件を明記
- 実現可能で具体的な計画を作成
- 時間配分は余裕を持たせる
- **1日のActivityの数は5個程度に抑え、ゆとりのあるスケジュールにする**
- 天候や季節のリスクも活動の説明に含める
- 情報の正確性と計画作成の効率性のバランスを取る
"""

fun planTripPrompt(requestInfo: String) = """
以下のユーザー情報を基に、Markdown形式で旅行計画を作成してください。

$requestInfo

【重要】ツールの活用方針：
1. **GoogleSearch**: 3-5回程度に抑える（優先度の高い情報のみ）
   - 特別なイベントや季節限定情報など、確認が必要な重要情報のみ検索
   - 一般的な観光地や交通機関の基本情報は検索不要
2. **Scrape**: GoogleSearchで見つけたURLを積極的にScrapeで詳細情報を取得（回数制限なし）
3. **ForwardGeocodeTool**: すべてのActivityで場所名から緯度経度を取得（必須）
4. **DirectionsTool**: すべてのTransportationで経路情報を取得（必須）
   - 出発地と目的地の座標を使って経路を取得し、lineフィールドに設定

【出力形式】
必ず以下のMarkdown形式に従ってください：

## summary
旅行全体のサマリーを2-3文で記述（目的地、期間、テーマ、予算、宿泊、移動の概要を含める）

## Step 1: YYYY-MM-DD HH:MM

### Activity
- duration: 時間帯（"09:00-12:00"形式）または時間幅（"午前"、"3時間"など）
- description: 活動の詳細（場所の説明、食事内容など）
  ※検索で確認した情報があれば含める
- location: 具体的な場所名（施設名、地域名、レストラン名など）
- longitude: 場所の経度（ForwardGeocodeToolで取得）
- latitude: 場所の緯度（ForwardGeocodeToolで取得）

### Transportation
- type: 交通手段の種類（"電車"、"バス"、"タクシー"、"徒歩"など）
- from: 出発地の具体的な場所名
- to: 目的地の具体的な場所名
- line: 移動ルートの座標リスト（List<Point>型：各Pointはlongitudeとlatitudeを持つ）
  ※DirectionsToolで取得した経路情報を使用（必須）
- duration: 所要時間（"30分"、"09:00-09:30"など）
- description: 移動の詳細（路線名、料金、乗り場、注意事項など）

### Activity
...

（ActivityとTransportationを時系列順に繰り返す）

## Step 2: YYYY-MM-DD HH:MM

### Activity
...

【注意事項】
- 検索は優先度の高い情報のみ（3-5回程度）に絞る
- 未確認の情報については、一般的なケースを想定して一つのプランを提案
- ActivityとTransportationを時系列順に記録（朝食 → 移動 → 観光 → 移動...）
- 移動が発生する場合は、必ずTransportationとして記録し、DirectionsToolで経路情報（line）を取得する
- 具体的な施設名や観光地名を必ず含める
- 実現可能で具体的な時間配分にする
- **Activityの滞在時間は最低1時間以上とする。観光スポットでは基本的に2時間は滞在する**
- **1日のActivityの数は5個程度に抑え、ゆとりのあるスケジュールにする**
- 情報の正確性と効率性のバランスを取る
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
                    location = "羽田空港",
                    longitude = 139.7798,
                    latitude = 35.5494
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "羽田空港",
                    to = "浅草駅",
                    lineId = "1",
                    duration = "40分",
                    description = "京急線・浅草線直通で浅草へ。運賃約600円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "11:00-13:00",
                    description = "浅草寺を参拝し、仲見世通りで食べ歩き。雷門で記念撮影。",
                    location = "浅草寺・仲見世通り",
                    longitude = 139.7967,
                    latitude = 35.7148
                ),
                Step.ScheduleEntry.Activity(
                    duration = "13:00-14:00",
                    description = "浅草で昼食。天ぷらやお蕎麦など江戸前料理を楽しむ。予算1,500円程度。",
                    location = "浅草のレストラン",
                    longitude = 139.7950,
                    latitude = 35.7115
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "浅草駅",
                    to = "新宿駅",
                    lineId = "2",
                    duration = "30分",
                    description = "銀座線で渋谷経由、JR山手線で新宿へ。運賃約200円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "15:00-16:00",
                    description = "ホテルにチェックイン。荷物を置いて休憩。",
                    location = "新宿のビジネスホテル",
                    longitude = 139.7034,
                    latitude = 35.6938
                ),
                Step.ScheduleEntry.Activity(
                    duration = "16:00-18:00",
                    description = "新宿の街を散策。歌舞伎町や伊勢丹などを見て回る。",
                    location = "新宿東口・歌舞伎町",
                    longitude = 139.7050,
                    latitude = 35.6950
                ),
                Step.ScheduleEntry.Activity(
                    duration = "18:30-20:00",
                    description = "新宿で夕食。居酒屋や焼き肉など。予算3,000円程度。",
                    location = "新宿の居酒屋",
                    longitude = 139.7020,
                    latitude = 35.6935
                )
            )
        ),
        Step(
            date = "2025-11-02 08:00",
            scheduleEntries = listOf(
                Step.ScheduleEntry.Activity(
                    duration = "08:00-09:00",
                    description = "ホテルで朝食。ビュッフェまたは軽食。",
                    location = "ホテル",
                    longitude = 139.7034,
                    latitude = 35.6938
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "新宿駅",
                    to = "渋谷駅",
                    lineId = "3",
                    duration = "10分",
                    description = "JR山手線で渋谷へ。運賃約160円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "10:00-12:00",
                    description = "渋谷のスクランブル交差点やハチ公像を見学。109やパルコでショッピング。",
                    location = "渋谷",
                    longitude = 139.7040,
                    latitude = 35.6617
                ),
                Step.ScheduleEntry.Activity(
                    duration = "12:00-13:00",
                    description = "渋谷で昼食。ラーメンやカフェなど。予算1,000円程度。",
                    location = "渋谷のレストラン",
                    longitude = 139.7004,
                    latitude = 35.6595
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "渋谷駅",
                    to = "原宿駅",
                    lineId = "4",
                    duration = "5分",
                    description = "JR山手線で原宿へ。運賃約140円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "14:00-16:00",
                    description = "明治神宮を参拝。静かな森の中を散策してリフレッシュ。",
                    location = "明治神宮",
                    longitude = 139.6993,
                    latitude = 35.6764
                ),
                Step.ScheduleEntry.Activity(
                    duration = "16:00-18:00",
                    description = "竹下通りを散策。クレープやスイーツを楽しむ。",
                    location = "竹下通り",
                    longitude = 139.7028,
                    latitude = 35.6705
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "原宿駅",
                    to = "新宿駅",
                    lineId = "5",
                    duration = "10分",
                    description = "JR山手線で新宿へ戻る。運賃約160円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "19:00-21:00",
                    description = "新宿西口の思い出横丁で夕食。昭和レトロな雰囲気を楽しむ。予算2,500円程度。",
                    location = "思い出横丁",
                    longitude = 139.6995,
                    latitude = 35.6910
                )
            )
        ),
        Step(
            date = "2025-11-03 08:00",
            scheduleEntries = listOf(
                Step.ScheduleEntry.Activity(
                    duration = "08:00-09:00",
                    description = "ホテルで朝食とチェックアウト準備。",
                    location = "ホテル",
                    longitude = 139.7034,
                    latitude = 35.6938
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "新宿駅",
                    to = "東京駅",
                    lineId = "6",
                    duration = "15分",
                    description = "JR中央線で東京駅へ。運賃約200円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "10:00-11:30",
                    description = "東京駅周辺を散策。丸の内のビル街や皇居外苑を見学。",
                    location = "東京駅・丸の内",
                    longitude = 139.7671,
                    latitude = 35.6812
                ),
                Step.ScheduleEntry.Activity(
                    duration = "11:30-12:30",
                    description = "東京駅の駅弁やレストランで昼食。お土産も購入。予算2,000円程度。",
                    location = "東京駅",
                    longitude = 139.7671,
                    latitude = 35.6812
                ),
                Step.ScheduleEntry.Transportation(
                    type = "電車",
                    from = "東京駅",
                    to = "羽田空港",
                    lineId = "7",
                    duration = "30分",
                    description = "京浜東北線・モノレールで羽田空港へ。運賃約500円。"
                ),
                Step.ScheduleEntry.Activity(
                    duration = "14:00-15:00",
                    description = "羽田空港でチェックイン。出発まで空港内で過ごす。",
                    location = "羽田空港",
                    longitude = 139.7798,
                    latitude = 35.5494
                )
            )
        )
    )
)

