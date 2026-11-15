# Liquid Glass Android Kit

CodexBar Android の Liquid Glass デザインを、他の Jetpack Compose アプリでも使える形にしたキットです。

## 新しいアプリを作る

GitHub の **Use this template** からリポジトリを作り、Android Studio で開きます。Android SDK 36 と JDK 17 を用意し、`:app` を Android 8.0 以降の端末で実行すると、画面レイアウト、操作部品、素材の調整例を確認できます。`KitGallery.kt` を自分の画面に置き換え、`app/build.gradle.kts` のアプリ ID を変更して開発を始められます。

## 好きな部品を使う

リリースの Maven ZIP を展開し、[導入手順](README.md#add-one-component-to-an-existing-app)に沿って Gradle 依存関係を追加します。ボタンなら `GlassButton`、タブバーなら `GlassNavigationBar` を呼び出します。ほかにカード、スイッチ、スライダー、セグメント、入力欄、ヘッダーを使えます。ソースを直接編集したい場合は `:designkit` をモジュールとして取り込めます。

`GlassTheme` は元アプリの配色、形状、Inter、ガラス素材の初期値を提供します。`GlassBackdropLayout` に背景を渡すと、その背景を部品が描画に使います。Android 13 以降は屈折、Android 12 はぼかし、Android 8〜11 は色による表示に切り替わります。

[詳しい導入例](docs/integration.md) · [MIT ライセンス](LICENSE) · [著作権と同梱ライセンス](NOTICE.md)
