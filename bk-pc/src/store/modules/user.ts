import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays,
  storageLocal
} from "../utils";
import { login, logout, getUserInfo } from "@/api/auth";
import type { LoginParams, LoginResult } from "@/types/auth";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";

export const useUserStore = defineStore("pure-user", {
  state: (): userType => ({
    id: storageLocal().getItem<DataInfo<number>>(userKey)?.id ?? 0,
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    isRemembered: false,
    loginDay: 7
  }),
  actions: {
    SET_ID(id: number) {
      this.id = id;
    },
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    SET_USERNAME(username: string) {
      this.username = username;
    },
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    async loginByUsername(data: LoginParams) {
      return new Promise<LoginResult>((resolve, reject) => {
        login(data)
          .then(result => {
            const { token, user } = result;
            const tokenData: DataInfo<Date> = {
              accessToken: token,
              refreshToken: token,
              expires: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
              id: user.id,
              avatar: user.avatar,
              username: user.username,
              nickname: user.nickName,
              roles: [],
              permissions: []
            };
            setToken(tokenData);
            this.SET_ID(user.id);
            this.SET_AVATAR(user.avatar);
            this.SET_USERNAME(user.username);
            this.SET_NICKNAME(user.nickName);
            resolve(result);
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    async getUserInfo() {
      return new Promise((resolve, reject) => {
        getUserInfo()
          .then(result => {
            const { user, permission } = result;
            this.SET_ID(user.id);
            this.SET_ROLES(["user"]);
            this.SET_PERMS(permission);
            this.SET_AVATAR(user.avatar);
            this.SET_USERNAME(user.username);
            this.SET_NICKNAME(user.nickName);
            resolve(result);
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    logOut() {
      logout()
        .then(() => {
          this.id = 0;
          this.username = "";
          this.roles = [];
          this.permissions = [];
          removeToken();
          useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
          resetRouter();
          router.push("/login");
        })
        .catch(() => {
          this.id = 0;
          this.username = "";
          this.roles = [];
          this.permissions = [];
          removeToken();
          useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
          resetRouter();
          router.push("/login");
        });
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
