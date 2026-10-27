// פונקציות עזר לתצוגה

export const shekel = (n) =>
  '₪' + Number(n || 0).toLocaleString('he-IL', { maximumFractionDigits: 0 });

export const shortDate = (iso) => {
  if (!iso) return '';
  const [y, m, d] = iso.slice(0, 10).split('-');
  return `${d}.${m}.${y.slice(2)}`;
};

export const weekday = (iso) =>
  new Date(iso + 'T00:00:00').toLocaleDateString('he-IL', { weekday: 'short' });

/** תאריך בפורמט YYYY-MM-DD, בהזזה של מספר ימים מהיום */
export const isoDay = (offset = 0, from = new Date()) => {
  const d = new Date(from);
  d.setDate(d.getDate() + offset);
  const pad = (x) => String(x).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

export const UNIT_KINDS = {
  CABIN: { label: 'בקתה', mark: '🌲' },
  VILLA: { label: 'וילה', mark: '🏡' },
  SUITE: { label: 'סוויטה', mark: '🛁' },
};

export const STATUS = {
  PENDING: { label: 'ממתינה', tone: 'amber' },
  CONFIRMED: { label: 'מאושרת', tone: 'green' },
  COMPLETED: { label: 'הסתיימה', tone: 'slate' },
  CANCELLED: { label: 'בוטלה', tone: 'red' },
};

export const MONTHS = ['ינו׳', 'פבר׳', 'מרץ', 'אפר׳', 'מאי', 'יוני', 'יולי', 'אוג׳', 'ספט׳', 'אוק׳', 'נוב׳', 'דצמ׳'];
