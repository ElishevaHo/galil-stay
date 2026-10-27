import { useMemo, useState } from 'react';
import { reservationsApi, reviewsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { Empty, ErrorBox, Field, Loading, PageHead, StatusTag } from '../components/Bits';
import Modal, { Confirm } from '../components/Modal';
import BookingForm from '../components/BookingForm';
import { useNotify } from '../components/Notifier';
import { shekel, shortDate, STATUS } from '../utils';

const FILTERS = [['', 'הכל'], ...Object.entries(STATUS).map(([k, v]) => [k, v.label])];

export default function ReservationsPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useLoad(() => reservationsApi.list());
  const [filter, setFilter] = useState('');
  const [text, setText] = useState('');
  const [creating, setCreating] = useState(false);
  const [cancelling, setCancelling] = useState(null);
  const [reviewing, setReviewing] = useState(null);

  const rows = useMemo(() => {
    const q = text.trim();
    return (data || [])
      .filter((r) => !filter || r.status === filter)
      .filter((r) => !q || r.guestName.includes(q) || r.unitName.includes(q) || String(r.id) === q);
  }, [data, filter, text]);

  const act = async (fn, okText) => {
    try {
      await fn();
      notify(okText);
      reload();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  const askCancel = async (r) => {
    try {
      const { refund } = await reservationsApi.refundPreview(r.id);
      setCancelling({ ...r, refundPreview: refund });
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  return (
    <>
      <PageHead title="הזמנות" sub={data ? `${data.length} הזמנות במערכת` : ''}>
        <button className="btn primary" onClick={() => setCreating(true)}>+ הזמנה חדשה</button>
      </PageHead>

      <div className="toolbar">
        <div className="segmented">
          {FILTERS.map(([key, label]) => (
            <button key={key} className={filter === key ? 'on' : ''} onClick={() => setFilter(key)}>{label}</button>
          ))}
        </div>
        <input className="search" placeholder="חיפוש לפי אורח, יחידה או מספר..." value={text} onChange={(e) => setText(e.target.value)} />
      </div>

      {loading && !data && <Loading />}
      {error && <ErrorBox message={error} onRetry={reload} />}
      {data && rows.length === 0 && <Empty text="לא נמצאו הזמנות" />}

      {rows.length > 0 && (
        <div className="table-card">
          <table>
            <thead>
              <tr>
                <th>#</th><th>אורח</th><th>יחידה</th><th>תאריכים</th><th>הרכב</th><th>סכום</th><th>סטטוס</th><th />
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id}>
                  <td className="muted">{r.id}</td>
                  <td>
                    <strong>{r.guestName}</strong>
                    {r.extraTitles.length > 0 && <div className="muted small">{r.extraTitles.join(' · ')}</div>}
                  </td>
                  <td>{r.unitName}</td>
                  <td className="nowrap">{shortDate(r.arrival)} – {shortDate(r.departure)}<div className="muted small">{r.nights} לילות</div></td>
                  <td>{r.adults}+{r.children}</td>
                  <td className="nowrap">
                    {shekel(r.amount)}
                    {r.status === 'CANCELLED' && <div className="muted small">הוחזר {shekel(r.refund)}</div>}
                  </td>
                  <td><StatusTag status={r.status} /></td>
                  <td className="row-actions">
                    {r.status === 'PENDING' && (
                      <button className="btn primary sm" onClick={() => act(() => reservationsApi.confirm(r.id), 'ההזמנה אושרה')}>אישור</button>
                    )}
                    {r.status === 'CONFIRMED' && (
                      <button className="btn ghost sm" onClick={() => act(() => reservationsApi.complete(r.id), 'השהייה נסגרה')}>סגירת שהייה</button>
                    )}
                    {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                      <button className="btn text-danger sm" onClick={() => askCancel(r)}>ביטול</button>
                    )}
                    {r.status === 'COMPLETED' && !r.reviewed && (
                      <button className="btn ghost sm" onClick={() => setReviewing(r)}>★ חוות דעת</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {creating && <BookingForm onClose={() => setCreating(false)} onSaved={() => { setCreating(false); reload(); }} />}

      {cancelling && (
        <Confirm
          danger
          title={`ביטול הזמנה #${cancelling.id}`}
          okLabel="ביטול ההזמנה"
          text={`לפי מדיניות הביטול, ${cancelling.guestName} יקבל/תקבל החזר של ${shekel(cancelling.refundPreview)} מתוך ${shekel(cancelling.amount)}.`}
          onClose={() => setCancelling(null)}
          onOk={() => { const id = cancelling.id; setCancelling(null); act(() => reservationsApi.cancel(id), 'ההזמנה בוטלה'); }}
        />
      )}

      {reviewing && (
        <ReviewForm
          reservation={reviewing}
          onClose={() => setReviewing(null)}
          onSaved={() => { setReviewing(null); notify('חוות הדעת נשמרה'); reload(); }}
        />
      )}
    </>
  );
}

function ReviewForm({ reservation, onClose, onSaved }) {
  const notify = useNotify();
  const [stars, setStars] = useState(5);
  const [text, setText] = useState('');

  const save = async () => {
    try {
      await reviewsApi.create({ reservationId: reservation.id, stars, text });
      onSaved();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  return (
    <Modal
      title={`חוות דעת · ${reservation.unitName}`}
      onClose={onClose}
      footer={<><button className="btn ghost" onClick={onClose}>ביטול</button><button className="btn primary" onClick={save}>שמירה</button></>}
    >
      <p className="muted">{reservation.guestName} · {shortDate(reservation.arrival)} – {shortDate(reservation.departure)}</p>
      <div className="star-picker" role="radiogroup" aria-label="דירוג">
        {[1, 2, 3, 4, 5].map((n) => (
          <button key={n} type="button" className={n <= stars ? 'lit' : ''} onClick={() => setStars(n)} aria-label={`${n} כוכבים`}>★</button>
        ))}
      </div>
      <Field label="מה האורחים כתבו">
        <textarea rows="4" value={text} onChange={(e) => setText(e.target.value)} />
      </Field>
    </Modal>
  );
}
