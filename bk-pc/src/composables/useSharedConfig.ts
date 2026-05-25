import { ref } from "vue";

/** 模块级共享语言设置偏好（system/zh/en），用于导航栏和设置页面双向同步 */
export const sharedLanguageSetting = ref<string>("system");

/** 模块级共享主题风格（light/dark/system），用于设置面板和设置页面双向同步 */
export const sharedOverallStyle = ref<string>("light");

/** 模块级共享深色模式开关 */
export const sharedDataTheme = ref<boolean>(false);
