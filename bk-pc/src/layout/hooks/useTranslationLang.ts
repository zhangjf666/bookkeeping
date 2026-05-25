import { useNav } from "./useNav";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { watch, onBeforeMount, type Ref } from "vue";
import { useUserConfig } from "@/composables/useUserConfig";
import { useUserStoreHook } from "@/store/modules/user";
import { sharedLanguageSetting } from "@/composables/useSharedConfig";

export function useTranslationLang(targetRef?: Ref) {
  const { $storage, changeTitle, handleResize } = useNav();
  const { locale, t } = useI18n();
  const route = useRoute();
  const { getSystemLanguage, saveConfig } = useUserConfig();
  const userStore = useUserStoreHook();

  /** Track selected language option: system/zh/en */
  const languageSetting = sharedLanguageSetting;

  async function translationCh() {
    languageSetting.value = "zh";
    $storage.locale = { locale: "zh" };
    locale.value = "zh";
    targetRef && handleResize(targetRef.value);
    if (userStore.id) {
      try {
        await saveConfig("language", "zh");
      } catch (error) {
        console.error("Failed to save language config:", error);
      }
    }
  }

  async function translationEn() {
    languageSetting.value = "en";
    $storage.locale = { locale: "en" };
    locale.value = "en";
    targetRef && handleResize(targetRef.value);
    if (userStore.id) {
      try {
        await saveConfig("language", "en");
      } catch (error) {
        console.error("Failed to save language config:", error);
      }
    }
  }

  async function translationSystem() {
    languageSetting.value = "system";
    const systemLang = getSystemLanguage();
    $storage.locale = { locale: systemLang };
    locale.value = systemLang;
    targetRef && handleResize(targetRef.value);
    if (userStore.id) {
      try {
        await saveConfig("language", "system");
      } catch (error) {
        console.error("Failed to save language config:", error);
      }
    }
  }

  watch(
    () => locale.value,
    () => {
      changeTitle(route.meta);
    }
  );

  onBeforeMount(() => {
    locale.value = $storage.locale?.locale ?? "zh";
  });

  return {
    t,
    route,
    locale,
    languageSetting,
    translationCh,
    translationEn,
    translationSystem
  };
}
