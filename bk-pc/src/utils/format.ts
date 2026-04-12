export const formatAmount = (amount: number | undefined): string => {
  if (amount === undefined || amount === null) return "0.00";
  return new Intl.NumberFormat("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
};
