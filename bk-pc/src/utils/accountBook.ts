export const ACCOUNT_BOOK_ICON_MAP: Record<string, string> = {
  book: "📔",
  travel: "✈️",
  food: "🍜",
  shopping: "🛒",
  medical: "🏥",
  education: "📚",
  money: "💰",
  chart: "📊",
  home: "🏠",
  car: "🚗",
  entertainment: "🎬",
  gift: "🎁"
};

export const getAccountBookIcon = (image: string): string => {
  return ACCOUNT_BOOK_ICON_MAP[image] || "📔";
};
