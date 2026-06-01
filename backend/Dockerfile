# ---- ビルドステージ ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# ビルド設定ファイルを先にコピーして依存ライブラリのダウンロードをキャッシュ層にする
# （ソースコードを変えてもこの層は再実行されないため2回目以降のビルドが速くなる）
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# ソースコードをコピーしてJARをビルド（-x test はDB接続が必要なテストをスキップ）
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ---- 実行ステージ ----
# JDKではなくJRE（実行専用）の軽量イメージを使いイメージサイズを削減する
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
