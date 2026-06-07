export const formatAmount = (amount: number | undefined): string => {
  if (amount === undefined || amount === null) return "0.00";
  return new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
};

/**
 * 格式化数字（千分位）
 */
export const formatNumber = (value: number | undefined): string => {
  if (value === undefined || value === null) return "0.00";
  return new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
};
