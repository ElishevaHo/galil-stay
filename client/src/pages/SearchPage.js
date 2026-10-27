import { useState } from 'react';
import { unitsApi } from '../api/endpoints';
import { Empty, ErrorBox, Field, Loading, PageHead, Stars } from '../components/Bits';
import BookingForm from '../components/BookingForm';
import { isoDay, shekel, UNIT_KINDS } from '../utils';

export default function SearchPage({ go }) {
  const [query, setQuery] = useState({ arrival: isoDay(7), departure: isoDay(9), guests: 2 });
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [booking, setBooking] = useState(null);

  const search = async (e) => {
    e?.preventDefault();
    setLoading(true);
    setError('');
    try {
      setResults(await unitsApi.available(query.arrival, query.departure, query.guests));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const set = (key) => (e) => setQuery({ ...query, [key]: e.target.value });

  return (
    <>
      <PageHead title="חיפוש יחידה פנויה" sub="בחירת תאריכים ומספר אורחים - והמערכת תציג מה פנוי ובכמה" />

      <form className="searchbar" onSubmit={search}>
        <Field label="הגעה">
          <input type="date" value={query.arrival} min={isoDay(0)} onChange={set('arrival')} />
        </Field>
        <Field label="עזיבה">
          <input type="date" value={query.departure} min={query.arrival} onChange={set('departure')} />
        </Field>
        <Field label="אורחים">
          <input type="number" min="1" max="20" value={query.guests} onChange={set('guests')} />
        </Field>
        <button className="btn primary">חיפוש</button>
      </form>

      {loading && <Loading />}
      {error && <ErrorBox message={error} />}
      {results && !loading && results.length === 0 && (
        <Empty text="אין יחידות פנויות לתאריכים האלה. כדאי לנסות תאריכים אחרים." />
      )}

      <div className="results">
        {results?.map(({ unit, quote }) => (
          <article key={unit.id} className="result">
            <div className={`result-art art-${unit.kind.toLowerCase()}`}>
              <span>{UNIT_KINDS[unit.kind].mark}</span>
            </div>
            <div className="result-body">
              <div className="result-top">
                <h3>{unit.name}</h3>
                <Stars value={unit.averageStars} />
              </div>
              <p className="muted small">{UNIT_KINDS[unit.kind].label} · {unit.region} · עד {unit.capacity} אורחים · {unit.bedrooms} חדרי שינה</p>
              <p className="result-desc">{unit.description}</p>
              <div className="features">
                {unit.privatePool && <span>🏊 בריכה פרטית</span>}
                {unit.jacuzzi && <span>♨ ג׳קוזי</span>}
                {unit.petsAllowed && <span>🐕 ידידותי לכלבים</span>}
              </div>
            </div>
            <div className="result-price">
              <span className="muted small">{quote.nights} לילות{quote.discountPercent ? ` · ${quote.discountPercent}% הנחה` : ''}</span>
              <strong>{shekel(quote.total)}</strong>
              <span className="muted small">כולל ניקיון</span>
              <button
                className="btn primary"
                onClick={() => setBooking({ unitId: unit.id, arrival: query.arrival, departure: query.departure, adults: Number(query.guests) })}
              >
                להזמנה
              </button>
            </div>
          </article>
        ))}
      </div>

      {booking && (
        <BookingForm
          preset={booking}
          onClose={() => setBooking(null)}
          onSaved={() => { setBooking(null); go('reservations'); }}
        />
      )}
    </>
  );
}
