import { useEffect, useState } from 'react';
import Modal from './Modal';
import { Field } from './Bits';
import { guestsApi, extrasApi, unitsApi, reservationsApi } from '../api/endpoints';
import { isoDay, shekel } from '../utils';
import { useNotify } from './Notifier';

/**
 * טופס הזמנה חדשה עם הצעת מחיר חיה.
 * preset - ערכים התחלתיים (יחידה/תאריכים) כשמגיעים מתוצאות חיפוש
 */
export default function BookingForm({ preset = {}, onClose, onSaved }) {
  const notify = useNotify();
  const [units, setUnits] = useState([]);
  const [guests, setGuests] = useState([]);
  const [extras, setExtras] = useState([]);
  const [quote, setQuote] = useState(null);
  const [quoteError, setQuoteError] = useState('');
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    unitId: preset.unitId || '',
    guestId: '',
    arrival: preset.arrival || isoDay(7),
    departure: preset.departure || isoDay(9),
    adults: preset.adults || 2,
    children: 0,
    extraIds: [],
    requests: '',
  });

  useEffect(() => {
    Promise.all([unitsApi.list(), guestsApi.list(), extrasApi.list()])
      .then(([u, g, e]) => {
        setUnits(u.filter((x) => x.listed));
        setGuests(g);
        setExtras(e);
      })
      .catch((e) => notify(e.message, 'error'));
  }, [notify]);

  // חישוב מחיר מחדש בכל שינוי רלוונטי
  useEffect(() => {
    if (!form.unitId || !form.guestId) {
      setQuote(null);
      return;
    }
    const timer = setTimeout(() => {
      reservationsApi.quote(payload(form))
        .then((q) => { setQuote(q); setQuoteError(''); })
        .catch((e) => { setQuote(null); setQuoteError(e.message); });
    }, 250);
    return () => clearTimeout(timer);
  }, [form]);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const toggleExtra = (id) =>
    setForm({
      ...form,
      extraIds: form.extraIds.includes(id) ? form.extraIds.filter((x) => x !== id) : [...form.extraIds, id],
    });

  const save = async () => {
    setSaving(true);
    try {
      await reservationsApi.create(payload(form));
      notify('ההזמנה נקלטה וממתינה לאישור');
      onSaved();
    } catch (e) {
      notify(e.message, 'error');
    } finally {
      setSaving(false);
    }
  };

  const unit = units.find((u) => String(u.id) === String(form.unitId));

  return (
    <Modal
      wide
      title="הזמנה חדשה"
      onClose={onClose}
      footer={
        <>
          <button className="btn ghost" onClick={onClose}>ביטול</button>
          <button className="btn primary" disabled={!quote || saving} onClick={save}>
            {saving ? 'שומר...' : `קביעת הזמנה${quote ? ' · ' + shekel(quote.total) : ''}`}
          </button>
        </>
      }
    >
      <div className="booking-grid">
        <div className="form-grid">
          <Field label="יחידה">
            <select value={form.unitId} onChange={set('unitId')}>
              <option value="">בחירת יחידה...</option>
              {units.map((u) => (
                <option key={u.id} value={u.id}>{u.name} · עד {u.capacity} אורחים</option>
              ))}
            </select>
          </Field>
          <Field label="אורח">
            <select value={form.guestId} onChange={set('guestId')}>
              <option value="">בחירת אורח...</option>
              {guests.map((g) => (
                <option key={g.id} value={g.id}>{g.firstName} {g.lastName} · {g.phone}</option>
              ))}
            </select>
          </Field>
          <Field label="הגעה">
            <input type="date" value={form.arrival} min={isoDay(0)} onChange={set('arrival')} />
          </Field>
          <Field label="עזיבה">
            <input type="date" value={form.departure} min={form.arrival} onChange={set('departure')} />
          </Field>
          <Field label="מבוגרים">
            <input type="number" min="1" value={form.adults} onChange={set('adults')} />
          </Field>
          <Field label="ילדים">
            <input type="number" min="0" value={form.children} onChange={set('children')} />
          </Field>

          <div className="span-2">
            <span className="field-label">תוספות</span>
            <div className="chips">
              {extras.map((x) => (
                <button
                  type="button"
                  key={x.id}
                  className={`chip ${form.extraIds.includes(x.id) ? 'chip-on' : ''}`}
                  onClick={() => toggleExtra(x.id)}
                >
                  {x.title} · {shekel(x.price)}{x.chargedPerNight ? ' ללילה' : ''}
                </button>
              ))}
            </div>
          </div>

          <Field label="בקשות מיוחדות">
            <textarea rows="2" value={form.requests} onChange={set('requests')} placeholder="עריסה לתינוק, הגעה מאוחרת..." />
          </Field>
        </div>

        <aside className="receipt">
          <h4>{unit ? unit.name : 'סיכום הזמנה'}</h4>
          {!quote && !quoteError && <p className="muted small">יש לבחור יחידה ואורח כדי לראות מחיר</p>}
          {quoteError && <p className="receipt-error">{quoteError}</p>}
          {quote && (
            <dl>
              <div><dt>{quote.weekdayNights} לילות אמצע שבוע</dt><dd>{shekel(quote.weekdayNights * unit.weekdayRate)}</dd></div>
              <div><dt>{quote.weekendNights} לילות סוף שבוע</dt><dd>{shekel(quote.weekendNights * unit.weekendRate)}</dd></div>
              {quote.discount > 0 && (
                <div className="receipt-save"><dt>הנחת שהייה ארוכה {quote.discountPercent}%</dt><dd>−{shekel(quote.discount)}</dd></div>
              )}
              {quote.extras > 0 && <div><dt>תוספות</dt><dd>{shekel(quote.extras)}</dd></div>}
              <div><dt>דמי ניקיון</dt><dd>{shekel(quote.cleaningFee)}</dd></div>
              <div className="receipt-total"><dt>סה״כ</dt><dd>{shekel(quote.total)}</dd></div>
            </dl>
          )}
        </aside>
      </div>
    </Modal>
  );
}

const payload = (f) => ({
  ...f,
  unitId: Number(f.unitId),
  guestId: Number(f.guestId),
  adults: Number(f.adults),
  children: Number(f.children),
});
