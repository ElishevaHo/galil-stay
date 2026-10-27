import { useState } from 'react';
import { reservationsApi, unitsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, PageHead } from '../components/Bits';
import { isoDay, shortDate } from '../utils';

const DAYS = 14;

const dayIndex = (iso, startIso) =>
  Math.round((Date.parse(iso) - Date.parse(startIso)) / 86400000);

/** לוח תפוסה: שורה לכל יחידה, עמודה לכל יום, פס לכל הזמנה */
export default function Occupancy() {
  const [start, setStart] = useState(isoDay(-2));
  const end = isoDay(DAYS, new Date(start + 'T00:00:00'));

  const units = useLoad(() => unitsApi.list());
  const bookings = useLoad(() => reservationsApi.timeline(start, end), [start, end]);

  const shift = (n) => setStart(isoDay(n, new Date(start + 'T00:00:00')));
  const days = Array.from({ length: DAYS }, (_, i) => isoDay(i, new Date(start + 'T00:00:00')));
  const today = isoDay(0);

  if (units.loading && !units.data) return <Loading />;
  if (units.error || bookings.error) return <ErrorBox message={units.error || bookings.error} onRetry={bookings.reload} />;

  return (
    <>
      <PageHead title="לוח תפוסה" sub={`${shortDate(start)} – ${shortDate(days[DAYS - 1])}`}>
        <button className="btn ghost" onClick={() => shift(-7)}>→ שבוע קודם</button>
        <button className="btn ghost" onClick={() => setStart(isoDay(-2))}>היום</button>
        <button className="btn ghost" onClick={() => shift(7)}>שבוע הבא ←</button>
      </PageHead>

      <div className="board-scroll">
        <div className="board" style={{ '--days': DAYS }}>
          <div className="board-row board-header">
            <div className="board-label" />
            {days.map((d) => {
              const date = new Date(d + 'T00:00:00');
              const weekend = date.getDay() === 4 || date.getDay() === 5;
              return (
                <div key={d} className={`board-day ${d === today ? 'is-today' : ''} ${weekend ? 'is-weekend' : ''}`}>
                  <small>{date.toLocaleDateString('he-IL', { weekday: 'narrow' })}</small>
                  {date.getDate()}
                </div>
              );
            })}
          </div>

          {units.data.map((u) => (
            <div key={u.id} className="board-row">
              <div className="board-label">
                <strong>{u.name}</strong>
                {!u.listed && <small className="muted">מוסתרת</small>}
              </div>
              {days.map((d, i) => (
                <div
                  key={d}
                  className={`board-cell ${d === today ? 'is-today' : ''}`}
                  style={{ gridColumn: i + 2, gridRow: 1 }}
                />
              ))}
              {(bookings.data || [])
                .filter((r) => r.unitId === u.id)
                .map((r) => {
                  const from = Math.max(dayIndex(r.arrival, start), 0);
                  const to = Math.min(dayIndex(r.departure, start), DAYS);
                  if (to <= from) return null;
                  return (
                    <div
                      key={r.id}
                      className={`board-bar bar-${r.status.toLowerCase()}`}
                      style={{ gridColumn: `${from + 2} / ${to + 2}`, gridRow: 1 }}
                      title={`${r.guestName} · ${shortDate(r.arrival)}–${shortDate(r.departure)}`}
                    >
                      {r.guestName}
                    </div>
                  );
                })}
            </div>
          ))}
        </div>
      </div>

      <div className="legend">
        <span><i className="bar-confirmed" /> מאושרת</span>
        <span><i className="bar-pending" /> ממתינה</span>
        <span><i className="bar-completed" /> הסתיימה</span>
        <span><i className="is-weekend-key" /> סוף שבוע</span>
      </div>
    </>
  );
}
