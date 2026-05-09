import { ref, type Ref } from "vue";

interface InfiniteScrollOptions {
  pageSize?: number;
}

/**
 * 无限滚动加载 Composable
 * 只负责滚动加载的状态管理和触发，具体加载逻辑由调用方实现
 */
export function useInfiniteScroll(options?: InfiniteScrollOptions) {
  const pageSize = options?.pageSize || 20;

  const loading = ref(false);
  const finished = ref(false);
  const pageNo = ref(1);
  const error: Ref<string | null> = ref(null);

  /**
   * 加载更多
   * @param fetchFn 实际的加载函数，由调用方实现
   */
  const loadMore = async (
    fetchFn: (pageNo: number, pageSize: number) => Promise<boolean>
  ) => {
    // 注意：不检查 loading，因为 van-list 通过 v-model:loading 已经管理了
    if (finished.value) return;

    loading.value = true;
    error.value = null;

    try {
      const hasMore = await fetchFn(pageNo.value, pageSize);
      finished.value = !hasMore;

      // 如果还有更多数据，页码增加
      if (hasMore) {
        pageNo.value++;
      }
    } catch (e: any) {
      error.value = e.message || "加载失败";
      finished.value = true; // 出错时停止加载
    } finally {
      loading.value = false;
    }
  };

  /**
   * van-list 的 @load 回调
   * @param fetchFn 实际的加载函数
   */
  const onLoadMore = (
    fetchFn: (pageNo: number, pageSize: number) => Promise<boolean>
  ) => {
    if (finished.value) return;
    loadMore(fetchFn);
  };

  /**
   * 重置状态（用于刷新或切换筛选条件）
   */
  const reset = () => {
    pageNo.value = 1;
    finished.value = false;
    loading.value = false;
    error.value = null;
  };

  /**
   * 重置并加载
   */
  const resetAndLoad = async (
    fetchFn: (pageNo: number, pageSize: number) => Promise<boolean>
  ) => {
    reset();
    await loadMore(fetchFn);
  };

  return {
    loading,
    finished,
    pageNo,
    error,
    loadMore,
    onLoadMore,
    reset,
    resetAndLoad
  };
}
