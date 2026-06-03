package com.dailyreport.backend.api.dto;

/**
 * 認証レスポンスDTO
 *
 * 【record】Java 16+ の機能。不変のデータクラスを簡潔に定義できる。
 * コンストラクタ・getter・equals/hashCode/toString が自動生成される。
 * DTOのような「データを運ぶだけ」のクラスに適している。
 *
 * ログイン・登録成功時にフロントエンドへ返す JWT トークンを格納する。
 * フロントエンドはこのトークンを localStorage に保存し、以降のリクエストに使用する。
 */
public record AuthResponse(String token) {}
