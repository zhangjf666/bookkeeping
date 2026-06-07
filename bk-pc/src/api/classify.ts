import { http } from "@/utils/http";
import type {
  Classify,
  ClassifyForm,
  ClassifyPageResult,
  ClassifyType
} from "@/types/classify";

const enableToBackend = (enable: "YES" | "NO"): string => {
  return enable;
};

const backendEnableToClassify = (enable: string): "YES" | "NO" => {
  if (enable === "YES" || enable === "1") return "YES";
  if (enable === "NO" || enable === "0") return "NO";
  return "NO";
};

const convertClassifyFromBackend = (item: any): Classify => {
  return {
    ...item,
    type: item.type as ClassifyType,
    image: item.image,
    pid: item.pid,
    enable: backendEnableToClassify(item.enable)
  };
};

const convertClassifyToBackend = (data: ClassifyForm): any => {
  return {
    ...data,
    type: data.type,
    image: data.image,
    enable: enableToBackend(data.enable)
  };
};

export const getClassifyList = (
  userId: number,
  params?: { name?: string; type?: ClassifyType }
) => {
  return http
    .request<Classify[]>("get", "/classify", {
      params: {
        userId,
        ...(params?.name && { name: params.name }),
        ...(params?.type && { type: params.type })
      }
    })
    .then(result => {
      if (Array.isArray(result)) {
        return result.map(convertClassifyFromBackend);
      }
      return [];
    });
};

export const getClassifyPage = (
  userId: number,
  params?: {
    name?: string;
    type?: ClassifyType;
    pageNo?: number;
    pageSize?: number;
  }
) => {
  return http
    .request<ClassifyPageResult>("get", "/classify", {
      params: {
        userId,
        ...(params?.name && { name: params.name }),
        ...(params?.type && { type: params.type }),
        ...(params?.pageNo && { pageNo: params.pageNo }),
        ...(params?.pageSize && { pageSize: params.pageSize })
      }
    })
    .then(result => {
      if (Array.isArray(result)) {
        return {
          list: result.map(convertClassifyFromBackend),
          totalCount: result.length
        };
      }
      return {
        list: (result.list || result.record || []).map(
          convertClassifyFromBackend
        ),
        totalCount: result.totalCount || result.total || 0
      };
    });
};

export const createClassify = (data: ClassifyForm) => {
  return http.request<number>("post", "/classify", {
    data: convertClassifyToBackend(data)
  });
};

export const updateClassify = (data: ClassifyForm) => {
  return http.request<void>("put", "/classify", {
    data: convertClassifyToBackend(data)
  });
};

export const deleteClassify = (ids: number[]) => {
  return http.request<void>("delete", "/classify", { data: ids });
};
