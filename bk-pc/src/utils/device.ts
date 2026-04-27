/**
 * 设备检测工具
 * 用于判断当前访问设备类型，实现 PC/移动端自动适配
 */

/**
 * 检测是否为移动设备
 * 通过 User-Agent 判断
 */
export const isMobile = (): boolean => {
  if (typeof navigator === "undefined") return false;

  const ua = navigator.userAgent;
  const mobileRegex =
    /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile|mobile/i;

  return mobileRegex.test(ua);
};

/**
 * 检测是否为 PC 设备
 */
export const isPC = (): boolean => {
  return !isMobile();
};

/**
 * 检测屏幕宽度是否为移动端尺寸
 * 用于响应式调整
 */
export const isMobileScreen = (): boolean => {
  if (typeof window === "undefined") return false;
  return window.innerWidth < 768;
};

/**
 * 获取设备类型
 */
export const getDeviceType = (): "mobile" | "pc" => {
  return isMobile() ? "mobile" : "pc";
};

/**
 * 检测是否为 iOS 设备
 */
export const isIOS = (): boolean => {
  if (typeof navigator === "undefined") return false;
  return /iPhone|iPad|iPod/i.test(navigator.userAgent);
};

/**
 * 检测是否为 Android 设备
 */
export const isAndroid = (): boolean => {
  if (typeof navigator === "undefined") return false;
  return /Android/i.test(navigator.userAgent);
};

/**
 * 检测是否为微信浏览器
 */
export const isWechat = (): boolean => {
  if (typeof navigator === "undefined") return false;
  return /MicroMessenger/i.test(navigator.userAgent);
};

/**
 * 获取安全区域顶部高度（适配 iPhone 刘海屏）
 */
export const getSafeAreaTop = (): number => {
  if (typeof window === "undefined") return 0;
  // CSS env variable support check
  const css = window.CSS;
  if (
    css &&
    css.supports &&
    css.supports("padding-top: env(safe-area-inset-top)")
  ) {
    return 44;
  }
  return 20;
};

/**
 * 获取安全区域底部高度（适配 iPhone 底部横条）
 */
export const getSafeAreaBottom = (): number => {
  if (typeof window === "undefined") return 0;
  // CSS env variable support check
  const css = window.CSS;
  if (
    css &&
    css.supports &&
    css.supports("padding-bottom: env(safe-area-inset-bottom)")
  ) {
    return 34;
  }
  return 0;
};
