import { useState, useEffect } from 'react';

/**
 * 画面幅がブレークポイント未満かどうかを返すカスタムフック
 * ウィンドウのリサイズに追従してリアルタイムに値が変わる
 * @param breakpoint 判定基準となる幅（px）。デフォルト768px
 * @returns モバイルサイズ（breakpoint未満）なら true
 */
const useIsMobile = (breakpoint = 768): boolean => {
  // 初期値はSSRを考慮せず、即座にwindow幅で判定する
  const [isMobile, setIsMobile] = useState(() => window.innerWidth < breakpoint);

  useEffect(() => {
    // ウィンドウサイズが変わるたびに再判定する
    const handler = () => setIsMobile(window.innerWidth < breakpoint);
    window.addEventListener('resize', handler);
    // コンポーネントのアンマウント時にイベントリスナーを削除してメモリリークを防ぐ
    return () => window.removeEventListener('resize', handler);
  }, [breakpoint]);

  return isMobile;
};

export default useIsMobile;
