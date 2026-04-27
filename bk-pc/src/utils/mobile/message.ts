import { showNotify, closeNotify } from "vant";
import { showLoadingToast, closeToast } from "vant";

/** 提示配置 */
const NOTIFY_DURATION = 3000; // 默认展示时间 3 秒

/** 成功提示（绿色） */
export const showSuccess = (message: string) => {
  showNotify({
    type: "success",
    message,
    color: "#fff",
    background: "#07c160",
    duration: NOTIFY_DURATION
  });
};

/** 错误提示（红色） */
export const showError = (message: string) => {
  showNotify({
    type: "danger",
    message,
    color: "#fff",
    background: "#ee0a24",
    duration: NOTIFY_DURATION
  });
};

/** 警告提示（橙色） */
export const showWarning = (message: string) => {
  showNotify({
    type: "warning",
    message,
    color: "#fff",
    background: "#ff976a",
    duration: NOTIFY_DURATION
  });
};

/** 信息提示（蓝色） */
export const showInfo = (message: string) => {
  showNotify({
    type: "primary",
    message,
    color: "#fff",
    background: "#1989fa",
    duration: NOTIFY_DURATION
  });
};

/** 加载提示 */
export const showLoading = (message: string = "加载中...") => {
  showLoadingToast({
    message,
    forbidClick: true,
    duration: 0 // 不自动关闭
  });
};

/** 关闭加载提示 */
export const hideLoading = () => {
  closeToast();
};

/** 关闭通知提示 */
export const hideNotify = () => {
  closeNotify();
};
