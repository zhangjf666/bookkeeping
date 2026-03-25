import { ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";

export const REGEXP_PWD =
  /^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)]|[()])+$)(?!^.*[\u4E00-\u9FA5].*$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){8,18}$/;

export const ruleFormRef = ref<FormInstance>();

export function createLoginRules(t: (key: string) => string): FormRules {
  return {
    username: [
      {
        required: true,
        message: () => t("login.pureUsernameReg"),
        trigger: "blur"
      }
    ],
    password: [
      {
        required: true,
        message: () => t("login.purePassWordReg"),
        trigger: "blur"
      }
    ],
    captcha: [
      {
        required: true,
        message: () => t("login.pureCaptchaReg"),
        trigger: "blur"
      }
    ]
  };
}

export function createRegisterRules(t: (key: string) => string): FormRules {
  return {
    username: [
      {
        required: true,
        message: () => t("login.pureUsernameReg"),
        trigger: "blur"
      },
      {
        min: 3,
        max: 20,
        message: () => t("login.pureUsernameLengthReg"),
        trigger: "blur"
      }
    ],
    password: [
      {
        required: true,
        message: () => t("login.purePassWordReg"),
        trigger: "blur"
      },
      {
        min: 6,
        max: 20,
        message: () => t("login.purePasswordLengthReg"),
        trigger: "blur"
      }
    ],
    repeatPassword: [
      {
        required: true,
        message: () => t("login.pureRepeatPasswordReg"),
        trigger: "blur"
      },
      {
        validator: (rule, value, callback) => {
          const form = ruleFormRef?.value?.model || {};
          if (value !== form.password) {
            callback(new Error(t("login.purePasswordNotMatch")));
          } else {
            callback();
          }
        },
        trigger: "blur"
      }
    ],
    captcha: [
      {
        required: true,
        message: () => t("login.pureCaptchaReg"),
        trigger: "blur"
      }
    ]
  };
}
