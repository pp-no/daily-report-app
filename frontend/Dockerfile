# ---- ビルドステージ ----
FROM node:22-alpine AS build
WORKDIR /app

# パッケージ定義を先にコピーしてnpm installをキャッシュ層にする
# （ソースコードを変えてもこの層は再実行されないため2回目以降のビルドが速くなる）
COPY package.json package-lock.json ./
RUN npm ci

# VITE_API_URL を空にすることでaxiosが相対URL（/api/...）を使うようになる
# nginx が /api/* をbackendコンテナにプロキシするため空文字で問題ない
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL
COPY . .
RUN npm run build

# ---- 配信ステージ ----
# ビルド済みの静的ファイル（dist/）だけをNginxイメージにコピーする
# Node.jsはビルドにしか使わないため最終イメージには含めない
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
