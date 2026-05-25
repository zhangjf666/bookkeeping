import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useDataThemeChange } from "@/layout/hooks/useDataThemeChange";
import { getUserConfigList, updateUserConfig } from "@/api/userConfig";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameSpace } from "@/config";
import { useUserStoreHook } from "@/store/modules/user";
import { sharedLanguageSetting } from "@/composables/useSharedConfig";
import type { UserConfig } from "@/types/userConfig";

// Module-level shared config list so all composable instances share the same data
const sharedConfigList = ref<UserConfig[]>([]);

export function useUserConfig() {
  const { locale } = useI18n();
  const { dataThemeChange, dataTheme, overallStyle } = useDataThemeChange();
  const configList = sharedConfigList;

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
    sharedLanguageSetting.value = language;
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
    let config = configList.value.find(c => c.name === name);

    // Auto-load configs from API if not found locally and user is logged in
    if (!config) {
      const userStore = useUserStoreHook();
      if (userStore.id) {
        try {
          const result = await getUserConfigList(userStore.id);
          configList.value = result || [];
          config = configList.value.find(c => c.name === name);
        } catch (error) {
          console.error("Failed to auto-load user config:", error);
        }
      }
    }

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
