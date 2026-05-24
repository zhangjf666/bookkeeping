# Theme and Language Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add user-level theme and language configuration that syncs across PC and H5, with system-follow defaults.

**Architecture:** A central `useUserConfig` composable manages loading/applying/saving config. Backend stores config in `userConfig` table. SQL migration handles historical users.

**Tech Stack:** Vue 3, Pinia, Element Plus, Vant, vue-i18n, Java/Spring Boot, MySQL

---

## File Structure

### New Files
- `bk-pc/src/composables/useUserConfig.ts` — Core composable for config load/apply/save
- Modify: `bk-server/sql/v1.0.2.sql` — Append theme/language migration SQL

### Modified Files
- `bk-server/src/main/resources/user_default_config.json` — Add default theme/language for new users
- `bk-pc/src/locales/zh-CN.yaml` — Add PC translations
- `bk-pc/src/locales/en.yaml` — Add PC translations
- `bk-pc/src/locales/mobile/zh-CN.yaml` — Add mobile translations
- `bk-pc/src/locales/mobile/en.yaml` — Add mobile translations
- `bk-pc/src/layout/hooks/useTranslationLang.ts` — Add `translationSystem` and backend save
- `bk-pc/src/views/login/index.vue` — Default to system language/light theme, load config after login
- `bk-pc/src/views/register/index.vue` — Load config after register+login
- `bk-pc/src/views/settings/common/index.vue` — Add theme/language settings
- `bk-pc/src/layout/components/lay-navbar/index.vue` — Add "system" to language dropdown
- `bk-pc/src/layout/components/lay-setting/index.vue` — Save theme to backend on change
- `bk-pc/src/views/mobile/user/CommonSettings.vue` — Add theme/language settings

---

## Task Dependency Graph

```
Task 1 (Backend defaults)
  → Task 2 (useUserConfig composable)
    → Task 3 (Translations)
      → Task 4 (useTranslationLang)
        → Task 5 (Login page)
        → Task 6 (Navbar)
        → Task 7 (PC settings)
        → Task 8 (Setting panel)
        → Task 9 (H5 settings)
Task 10 (SQL migration) — independent
```

---

## Task 1: Backend Default Config

**Files:**
- Modify: `bk-server/src/main/resources/user_default_config.json`

- [ ] **Step 1: Add default configurations**

In the `userConfig` array, add after the existing items:

```json
{
  "name": "theme",
  "value": "system",
  "description": "主题设置(system:跟随系统,light:浅色,dark:深色)"
},
{
  "name": "language",
  "value": "system",
  "description": "语言设置(system:跟随系统,zh-CN:简体中文,en:英文)"
}
```

- [ ] **Step 2: Commit**

```bash
git add bk-server/src/main/resources/user_default_config.json
git commit -m "feat: add theme and language to default user config"
```

---

## Task 2: useUserConfig Composable

**Files:**
- Create: `bk-pc/src/composables/useUserConfig.ts`

- [ ] **Step 1: Create the composable**

```typescript
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

  /** Get system language */
  const getSystemLanguage = (): string => {
    const lang = navigator.language.toLowerCase();
    return lang.startsWith("zh") ? "zh" : "en";
  };

  /** Get actual language value (system → detect OS) */
  const getActualLanguage = (language: string): string => {
    return language === "system" ? getSystemLanguage() : language;
  };

  /** Apply theme */
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

  /** Apply language */
  const applyLanguage = (language: string): void => {
    const actualLang = getActualLanguage(language);
    locale.value = actualLang;
    storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, {
      locale: actualLang
    });
  };

  /** Load user config from backend */
  const loadUserConfig = async (userId: number): Promise<void> => {
    const result = await getUserConfigList(userId);
    configList.value = result || [];

    const themeConfig = configList.value.find(c => c.name === "theme");
    const languageConfig = configList.value.find(c => c.name === "language");

    if (themeConfig) {
      applyTheme(themeConfig.value);
    }
    if (languageConfig) {
      applyLanguage(languageConfig.value);
    }
  };

  /** Save config to backend and apply */
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

    if (name === "theme") {
      applyTheme(value);
    } else if (name === "language") {
      applyLanguage(value);
    }
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
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/composables/useUserConfig.ts
git commit -m "feat: add useUserConfig composable"
```

---

## Task 3: Translations

**Files:**
- Modify: `bk-pc/src/locales/zh-CN.yaml`
- Modify: `bk-pc/src/locales/en.yaml`
- Modify: `bk-pc/src/locales/mobile/zh-CN.yaml`
- Modify: `bk-pc/src/locales/mobile/en.yaml`

- [ ] **Step 1: Add to zh-CN.yaml under existing `commonConfig` section**

```yaml
commonConfig:
  pureTheme: "主题"
  pureThemeSystem: "跟随系统"
  pureThemeLight: "浅色"
  pureThemeDark: "深色"
  pureLanguage: "语言"
  pureLanguageSystem: "跟随系统"
  pureLanguageZh: "简体中文"
  pureLanguageEn: "English"
```

- [ ] **Step 2: Add to en.yaml under existing `commonConfig` section**

```yaml
commonConfig:
  pureTheme: "Theme"
  pureThemeSystem: "System"
  pureThemeLight: "Light"
  pureThemeDark: "Dark"
  pureLanguage: "Language"
  pureLanguageSystem: "System"
  pureLanguageZh: "简体中文"
  pureLanguageEn: "English"
```

- [ ] **Step 3: Add to mobile/zh-CN.yaml under existing `mobile.commonSettings` section**

```yaml
mobile:
  commonSettings:
    theme: "主题"
    themeSystem: "跟随系统"
    themeLight: "浅色"
    themeDark: "深色"
    language: "语言"
    languageSystem: "跟随系统"
    languageZh: "简体中文"
    languageEn: "English"
```

- [ ] **Step 4: Add to mobile/en.yaml under existing `mobile.commonSettings` section**

```yaml
mobile:
  commonSettings:
    theme: "Theme"
    themeSystem: "System"
    themeLight: "Light"
    themeDark: "Dark"
    language: "Language"
    languageSystem: "System"
    languageZh: "简体中文"
    languageEn: "English"
```

- [ ] **Step 5: Commit**

```bash
git add bk-pc/src/locales/
git commit -m "feat: add theme and language translations"
```

---

## Task 4: useTranslationLang Hook

**Files:**
- Modify: `bk-pc/src/layout/hooks/useTranslationLang.ts`

- [ ] **Step 1: Add imports and modify functions**

Replace the file content with:

```typescript
import { useNav } from "./useNav";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { ref, watch, onBeforeMount, type Ref } from "vue";
import { useUserConfig } from "@/composables/useUserConfig";
import { useUserStoreHook } from "@/store/modules/user";

export function useTranslationLang(ref?: Ref) {
  const { $storage, changeTitle, handleResize } = useNav();
  const { locale, t } = useI18n();
  const route = useRoute();
  const { getSystemLanguage, saveConfig } = useUserConfig();
  const userStore = useUserStoreHook();

  /** Track selected language option: system/zh/en */
  const languageSetting = ref("system");

  function translationCh() {
    languageSetting.value = "zh";
    $storage.locale = { locale: "zh" };
    locale.value = "zh";
    ref && handleResize(ref.value);
    if (userStore.id) {
      saveConfig("language", "zh");
    }
  }

  function translationEn() {
    languageSetting.value = "en";
    $storage.locale = { locale: "en" };
    locale.value = "en";
    ref && handleResize(ref.value);
    if (userStore.id) {
      saveConfig("language", "en");
    }
  }

  function translationSystem() {
    languageSetting.value = "system";
    const systemLang = getSystemLanguage();
    $storage.locale = { locale: systemLang };
    locale.value = systemLang;
    ref && handleResize(ref.value);
    if (userStore.id) {
      saveConfig("language", "system");
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
```

- [ ] **Step 2: Commit**

```bash
git add bk-pc/src/layout/hooks/useTranslationLang.ts
git commit -m "feat: add translationSystem and auto-save to backend"
```

---

## Task 5: Login Page

**Files:**
- Modify: `bk-pc/src/views/login/index.vue`

- [ ] **Step 1: Add imports**

Add to existing imports:
```typescript
import { useUserConfig } from "@/composables/useUserConfig";
import { storageLocal } from "@pureadmin/utils";
import { responsiveStorageNameSpace } from "@/config";
```

- [ ] **Step 2: Setup composable and language option tracking**

After existing setup code, add:
```typescript
const { loadUserConfig, getSystemLanguage, applyTheme } = useUserConfig();
const languageOption = ref("system");

const selectLanguage = (option: string) => {
  languageOption.value = option;
  if (option === "system") {
    locale.value = getSystemLanguage();
  } else {
    locale.value = option;
  }
  $storage.locale = { locale: locale.value };
};
```

- [ ] **Step 3: Set defaults on mount**

Replace the `onMounted` hook:
```typescript
onMounted(() => {
  loadCaptcha();
  // Default language: follow system
  const systemLang = getSystemLanguage();
  locale.value = systemLang;
  storageLocal().setItem(`${responsiveStorageNameSpace()}locale`, {
    locale: systemLang
  });
  languageOption.value = "system";
  // Default theme: light
  applyTheme("light");
});
```

- [ ] **Step 4: Add "system" option to language dropdown**

Replace the `el-dropdown-menu` content with:
```vue
<el-dropdown-menu class="translation">
  <el-dropdown-item
    :class="[
      'dark:text-white!',
      languageOption === 'system' ? 'selected' : ''
    ]"
    @click="selectLanguage('system')"
  >
    <span v-show="languageOption === 'system'" class="check-system">
      <IconifyIconOffline :icon="Check" />
    </span>
    {{ t("commonConfig.pureLanguageSystem") }}
  </el-dropdown-item>
  <el-dropdown-item
    :style="getDropdownItemStyle(locale, 'zh')"
    :class="['dark:text-white!', getDropdownItemClass(locale, 'zh')]"
    @click="selectLanguage('zh')"
  >
    <IconifyIconOffline
      v-show="locale === 'zh'"
      class="check-zh"
      :icon="Check"
    />
    {{ t("commonConfig.pureLanguageZh") }}
  </el-dropdown-item>
  <el-dropdown-item
    :style="getDropdownItemStyle(locale, 'en')"
    :class="['dark:text-white!', getDropdownItemClass(locale, 'en')]"
    @click="selectLanguage('en')"
  >
    <span v-show="locale === 'en'" class="check-en">
      <IconifyIconOffline :icon="Check" />
    </span>
    {{ t("commonConfig.pureLanguageEn") }}
  </el-dropdown-item>
</el-dropdown-menu>
```

- [ ] **Step 5: Load user config after login success**

In the `onLogin` success handler, after `router.push()`:
```typescript
await loadUserConfig(useUserStoreHook().id);
```

- [ ] **Step 6: Commit**

```bash
git add bk-pc/src/views/login/index.vue
git commit -m "feat: login page defaults to system language and light theme"
```

---

## Task 6: Navbar Language Switch

**Files:**
- Modify: `bk-pc/src/layout/components/lay-navbar/index.vue`

- [ ] **Step 1: Update destructured imports**

Change from:
```typescript
const { t, locale, translationCh, translationEn } = useTranslationLang();
```

To:
```typescript
const { t, locale, languageSetting, translationCh, translationEn, translationSystem } =
  useTranslationLang();
```

- [ ] **Step 2: Add "system" option to language dropdown**

Add before the existing zh option:
```vue
<el-dropdown-item
  :class="[
    'dark:text-white!',
    languageSetting === 'system' ? 'selected' : ''
  ]"
  @click="translationSystem"
>
  <span v-show="languageSetting === 'system'" class="check-system">
    <IconifyIconOffline :icon="Check" />
  </span>
  {{ t("commonConfig.pureLanguageSystem") }}
</el-dropdown-item>
```

- [ ] **Step 3: Update existing options to use new translation functions**

Replace the zh option content:
```vue
<el-dropdown-item
  :style="getDropdownItemStyle(locale, 'zh')"
  :class="['dark:text-white!', getDropdownItemClass(locale, 'zh')]"
  @click="translationCh"
>
  <IconifyIconOffline
    v-show="locale === 'zh'"
    class="check-zh"
    :icon="Check"
  />
  {{ t("commonConfig.pureLanguageZh") }}
</el-dropdown-item>
```

Replace the en option content:
```vue
<el-dropdown-item
  :style="getDropdownItemStyle(locale, 'en')"
  :class="['dark:text-white!', getDropdownItemClass(locale, 'en')]"
  @click="translationEn"
>
  <span v-show="locale === 'en'" class="check-en">
    <IconifyIconOffline :icon="Check" />
  </span>
  {{ t("commonConfig.pureLanguageEn") }}
</el-dropdown-item>
```

- [ ] **Step 4: Commit**

```bash
git add bk-pc/src/layout/components/lay-navbar/index.vue
git commit -m "feat: add system language option to navbar"
```

---

## Task 7: PC Common Settings Page

**Files:**
- Modify: `bk-pc/src/views/settings/common/index.vue`

- [ ] **Step 1: Add import**

```typescript
import { useUserConfig } from "@/composables/useUserConfig";
```

- [ ] **Step 2: Add state and setup**

After existing setup code:
```typescript
const { loadUserConfig, saveConfig } = useUserConfig();

const theme = ref("system");
const language = ref("system");

const themeOptions = [
  { value: "system", label: t("commonConfig.pureThemeSystem") },
  { value: "light", label: t("commonConfig.pureThemeLight") },
  { value: "dark", label: t("commonConfig.pureThemeDark") }
];

const languageOptions = [
  { value: "system", label: t("commonConfig.pureLanguageSystem") },
  { value: "zh", label: t("commonConfig.pureLanguageZh") },
  { value: "en", label: t("commonConfig.pureLanguageEn") }
];
```

- [ ] **Step 3: Update loadData**

Add to the end of `loadData`, before `finally`:
```typescript
const themeConfig = configList.value.find(c => c.name === "theme");
if (themeConfig) {
  theme.value = themeConfig.value;
}
const languageConfig = configList.value.find(c => c.name === "language");
if (languageConfig) {
  language.value = languageConfig.value;
}
```

- [ ] **Step 4: Add handlers**

```typescript
const handleThemeChange = async (value: string) => {
  await saveConfig("theme", value);
};

const handleLanguageChange = async (value: string) => {
  await saveConfig("language", value);
};
```

- [ ] **Step 5: Add UI elements**

Add inside the first config section (after the credit card switch, before `el-divider`):
```vue
<div class="config-item">
  <span class="config-label">{{ t("commonConfig.pureTheme") }}</span>
  <el-radio-group v-model="theme" @change="handleThemeChange">
    <el-radio-button
      v-for="opt in themeOptions"
      :key="opt.value"
      :value="opt.value"
    >
      {{ opt.label }}
    </el-radio-button>
  </el-radio-group>
</div>

<div class="config-item">
  <span class="config-label">{{ t("commonConfig.pureLanguage") }}</span>
  <el-radio-group v-model="language" @change="handleLanguageChange">
    <el-radio-button
      v-for="opt in languageOptions"
      :key="opt.value"
      :value="opt.value"
    >
      {{ opt.label }}
    </el-radio-button>
  </el-radio-group>
</div>
```

- [ ] **Step 6: Commit**

```bash
git add bk-pc/src/views/settings/common/index.vue
git commit -m "feat: add theme and language settings to PC common config"
```

---

## Task 8: Setting Panel Theme Save

**Files:**
- Modify: `bk-pc/src/layout/components/lay-setting/index.vue`

- [ ] **Step 1: Add import and setup**

Add to existing imports:
```typescript
import { useUserConfig } from "@/composables/useUserConfig";
import { useUserStoreHook } from "@/store/modules/user";
```

Add to setup:
```typescript
const { saveConfig } = useUserConfig();
const userStore = useUserStoreHook();
```

- [ ] **Step 2: Save theme on Segmented change**

In the `Segmented` component's `@change` handler for overall style, add backend save:

Find this existing code:
```vue
@change="
  theme => {
    theme.index === 1 && theme.index !== 2
      ? (dataTheme = true)
      : (dataTheme = false);
    overallStyle = theme.option.theme;
    dataThemeChange(theme.option.theme);
    theme.index === 2 && watchSystemThemeChange();
  }
"
```

Replace with:
```vue
@change="
  theme => {
    theme.index === 1 && theme.index !== 2
      ? (dataTheme = true)
      : (dataTheme = false);
    overallStyle = theme.option.theme;
    dataThemeChange(theme.option.theme);
    theme.index === 2 && watchSystemThemeChange();
    if (userStore.id) {
      saveConfig('theme', theme.option.theme);
    }
  }
"
```

- [ ] **Step 3: Commit**

```bash
git add bk-pc/src/layout/components/lay-setting/index.vue
git commit -m "feat: save theme to backend on setting panel change"
```

---

## Task 9: H5 Common Settings Page

**Files:**
- Modify: `bk-pc/src/views/mobile/user/CommonSettings.vue`

- [ ] **Step 1: Add imports**

Add to existing imports:
```typescript
import { useUserConfig } from "@/composables/useUserConfig";
```

- [ ] **Step 2: Add state and setup**

After existing setup:
```typescript
const { loadUserConfig, saveConfig } = useUserConfig();

const theme = ref("system");
const language = ref("system");

const showThemePicker = ref(false);
const showLanguagePicker = ref(false);

const themeOptions = computed(() => [
  { value: "system", label: t("mobile.commonSettings.themeSystem") },
  { value: "light", label: t("mobile.commonSettings.themeLight") },
  { value: "dark", label: t("mobile.commonSettings.themeDark") }
]);

const languageOptions = computed(() => [
  { value: "system", label: t("mobile.commonSettings.languageSystem") },
  { value: "zh", label: t("mobile.commonSettings.languageZh") },
  { value: "en", label: t("mobile.commonSettings.languageEn") }
]);

const themeText = computed(() => {
  return themeOptions.value.find(item => item.value === theme.value)?.label || "";
});

const languageText = computed(() => {
  return languageOptions.value.find(item => item.value === language.value)?.label || "";
});
```

- [ ] **Step 3: Update loadConfigs**

Add to the end of `loadConfigs`, before `finally`:
```typescript
const themeConfig = getConfigByName("theme");
if (themeConfig) {
  theme.value = themeConfig.value;
}
const languageConfig = getConfigByName("language");
if (languageConfig) {
  language.value = languageConfig.value;
}
```

- [ ] **Step 4: Add handlers**

```typescript
const handleThemeSelect = async (value: string) => {
  showThemePicker.value = false;
  if (value === theme.value) return;
  await saveConfig("theme", value);
  // Reload to get updated config
  await loadConfigs();
};

const handleLanguageSelect = async (value: string) => {
  showLanguagePicker.value = false;
  if (value === language.value) return;
  await saveConfig("language", value);
  // Reload to get updated config
  await loadConfigs();
};
```

- [ ] **Step 5: Add UI elements**

Add inside the first `van-cell-group` (常用设置), after the credit card cell:
```vue
<van-cell
  :title="t('mobile.commonSettings.theme')"
  :value="themeText"
  is-link
  @click="showThemePicker = true"
/>
<van-cell
  :title="t('mobile.commonSettings.language')"
  :value="languageText"
  is-link
  @click="showLanguagePicker = true"
/>
```

Add after the last `van-dialog`:
```vue
<!-- Theme picker -->
<van-action-sheet
  v-model:show="showThemePicker"
  :title="t('mobile.commonSettings.theme')"
>
  <div class="picker-list">
    <van-cell
      v-for="item in themeOptions"
      :key="item.value"
      :title="item.label"
      clickable
      @click="handleThemeSelect(item.value)"
    >
      <template #right-icon>
        <van-icon
          v-if="theme === item.value"
          name="success"
          color="#d83d34"
        />
      </template>
    </van-cell>
  </div>
</van-action-sheet>

<!-- Language picker -->
<van-action-sheet
  v-model:show="showLanguagePicker"
  :title="t('mobile.commonSettings.language')"
>
  <div class="picker-list">
    <van-cell
      v-for="item in languageOptions"
      :key="item.value"
      :title="item.label"
      clickable
      @click="handleLanguageSelect(item.value)"
    >
      <template #right-icon>
        <van-icon
          v-if="language === item.value"
          name="success"
          color="#d83d34"
        />
      </template>
    </van-cell>
  </div>
</van-action-sheet>
```

- [ ] **Step 6: Commit**

```bash
git add bk-pc/src/views/mobile/user/CommonSettings.vue
git commit -m "feat: add theme and language settings to H5 common settings"
```

---

## Task 10: Register Page Load Config

**Files:**
- Modify: `bk-pc/src/views/register/index.vue`

- [ ] **Step 1: Add import**

```typescript
import { useUserConfig } from "@/composables/useUserConfig";
```

- [ ] **Step 2: Add setup**

```typescript
const { loadUserConfig } = useUserConfig();
```

- [ ] **Step 3: Load config after register+login**

In the `onRegister` success handler, after `router.push()`:
```typescript
await loadUserConfig(useUserStoreHook().id);
```

- [ ] **Step 4: Commit**

```bash
git add bk-pc/src/views/register/index.vue
git commit -m "feat: load user config after registration"
```

---

## Task 11: SQL Migration for Historical Users

**Files:**
- Modify: `bk-server/sql/v1.0.2.sql`

- [ ] **Step 1: Append migration SQL to v1.0.2.sql**

Append to the end of the file:

```sql

-- 为历史用户补齐 theme 和 language 默认配置
INSERT INTO user_config (user_id, name, value, description, enable)
SELECT u.id, 'theme', 'system', '主题设置(system:跟随系统,light:浅色,dark:深色)', '1'
FROM bookkeeping_user u
WHERE NOT EXISTS (
    SELECT 1 FROM user_config uc WHERE uc.user_id = u.id AND uc.name = 'theme'
);

INSERT INTO user_config (user_id, name, value, description, enable)
SELECT u.id, 'language', 'system', '语言设置(system:跟随系统,zh-CN:简体中文,en:英文)', '1'
FROM bookkeeping_user u
WHERE NOT EXISTS (
    SELECT 1 FROM user_config uc WHERE uc.user_id = u.id AND uc.name = 'language'
);
```

- [ ] **Step 2: Commit**

```bash
git add bk-server/sql/v1.0.2.sql
git commit -m "feat: add SQL migration for historical user theme/language config"
```

---

## Self-Review

### Spec Coverage Check

| Spec Requirement | Implementing Task |
|-----------------|-------------------|
| Register: add theme/language config | Task 1 (backend JSON) |
| Login page: default system language, light theme | Task 5 |
| Login page: add "system" language option | Task 5 |
| After login: load and apply user config | Task 2, Task 5 |
| PC settings: add theme/language config | Task 7 |
| Navbar: add "system" language option | Task 6 |
| Setting panel: save theme on change | Task 8 |
| H5 settings: add theme/language config | Task 9 |
| Historical users: SQL migration | Task 11 |

**All requirements covered.**

### Placeholder Scan

- No "TBD", "TODO", "implement later"
- No vague descriptions
- All code blocks contain actual implementation code
- All file paths are exact

### Type Consistency

- `saveConfig(name: string, value: string)` used consistently across all tasks
- `useUserConfig()` return values match between definition and usage
- Translation keys (`commonConfig.*`, `mobile.commonSettings.*`) consistent across translations and components

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-05-23-theme-language-config.md`.**

**Two execution options:**

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
