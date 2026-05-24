import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { getUserConfigList, updateUserConfig } from "@/api/userConfig";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameSpace } from "@/config";
import type { UserConfig } from "@/types/userConfig";

export function useUserConfig() {
  const { locale } = useI18n();
  const { dataThemeChange, dataTheme, overallStyle } = useDataThemeChange();
  const configList = ref<UserConfig[]>([]);

  const getSystemLanguage = (): string => {
    const lang = navigator.language.toLowerCase();
    return lang.startsWith("zh") ? "zh" : "en";
  };

  const getActualLanguage = (language: string): string => {
    return language === "system" ? getSystemLanguage() : language;
  };

  const applyTheme = (theme: string): void => {
    overallStyle.value = theme;
    if (theme === "system") {
      dataTheme.value = window.matchMedia("(prefers-color-scheme: dark)").matches;
    } else if (theme === "dark") {
      dataTheme.value = true;
    } else {
      dataTheme.value = false;
    }
    dataThemeChange(theme);
  };

  const applyLanguage = (language: string): void => {
    const actualLang = getActualLanguage(language);
    locale.value = actualLang;
    storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, {
      locale: actualLang
    });
  };

  const loadUserConfig = async (userId: number): Promise<void> => {
    try {
      const result = await getUserConfigList(userId);
      configList.value = result || [];
      const themeConfig = configList.value.find(c => c.name === "theme");
      const languageConfig = configList.value.find(c => c.name === "language");
      if (themeConfig) applyTheme(themeConfig.value);
      if (languageConfig) applyLanguage(languageConfig.value);
    } catch (error) {
      console.error("Failed to load user config:", error);
    }
  };

  const saveConfig = async (name: string, value: string): Promise<void> => {
    const config = configList.value.find(c => c.name === name);
    if (!config) return;
    await updateUserConfig({
      id: config.id,
      userId: config.userId,
      name: config.name,
      value,
      description: config.description,
      enable: config.enable
    });
    config.value = value;
    if (name === "theme") applyTheme(value);
    else if (name === "language") applyLanguage(value);
  };

  return {
    configList,
    loadUserConfig,
    applyTheme,
    applyLanguage,
    saveConfig,
    getSystemLanguage,
    getActualLanguage
  };
}
