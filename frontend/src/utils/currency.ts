/**
 * Formats a number as Indian Rupee (INR).
 * Ensures exactly 2 decimal places and uses Indian numbering system (e.g. ₹1,00,000.00).
 * Safely handles null or undefined values.
 */
export const formatINR = (amount: number | null | undefined): string => {
  if (amount == null) return '-';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};
