import { useMemo, useState } from 'react';
import { guestsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { Empty, ErrorBox, Field, Loading, PageHead, StatusTag } from '../components/Bits';
import Modal, { Confirm } from '../components/Modal';
import { useNotify } from '../components/Notifier';
import { shekel, shortDate } from '../utils';

const BLANK = { firstName: '', lastName: '', email: '', phone: '', city: '' };

export default function GuestsPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useLoad(() => guestsApi.list());
  const [text, setText] = useState('');
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const [viewing, setViewing] = useState(null);

  const rows = useMemo(() => {
    const q = text.trim().toLowerCase();
    if (!q) return data || [];
    return (data || []).filter((g) =>
      `${g.firstName} ${g.lastName} ${g.email} ${g.phone} ${g.city}`.toLowerCase().includes(q));
  }, [data, text]);

  const remove = async () => {
    const id = removing.id;
    setRemoving(null);
    try {
      await guestsApi.remove(id);
      notify('האורח נמחק');
      reload();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  return (
    <>
      <PageHead title="אורחים" sub={data ? `${data.length} אורחים רשומים` : ''}>
        <button className="btn primary" onClick={() => setEditing(BLANK)}>+ אורח חדש</button>
      </PageHead>

      <div className="toolbar">
        <input className="search" placeholder="חיפוש לפי שם, טלפון, עיר..." value={text} onChange={(e) => setText(e.target.value)} />
      </div>

      {loading && !data && <Loading />}
      {error && <ErrorBox message={error} onRetry={reload} />}
      {data && rows.length === 0 && <Empty text="לא נמצאו אורחים" />}

      {rows.length > 0 && (
        <div className="table-card">
          <table>
            <thead>
              <tr><th>שם</th><th>טלפון</th><th>אימייל</th><th>עיר</th><th>שהיות</th><th>סה״כ</th><th /></tr>
            </thead>
            <tbody>
              {rows.map((g) => (
                <tr key={g.id}>
                  <td>
                    <button className="link strong" onClick={() => setViewing(g)}>{g.firstName} {g.lastName}</button>
                    {g.stays >= 3 && <span className="tag tag-green mini">אורח קבוע</span>}
                  </td>
                  <td className="nowrap" dir="ltr">{g.phone}</td>
                  <td className="muted">{g.email}</td>
                  <td>{g.city}</td>
                  <td>{g.stays}</td>
                  <td>{shekel(g.totalSpent)}</td>
                  <td className="row-actions">
                    <button className="btn ghost sm" onClick={() => setEditing(g)}>עריכה</button>
                    <button className="btn text-danger sm" onClick={() => setRemoving(g)}>מחיקה</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editing && <GuestForm initial={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reload(); }} />}
      {viewing && <GuestHistory guest={viewing} onClose={() => setViewing(null)} />}
      {removing && (
        <Confirm danger title="מחיקת אורח" okLabel="מחיקה"
          text={`למחוק את ${removing.firstName} ${removing.lastName}?`}
          onClose={() => setRemoving(null)} onOk={remove} />
      )}
    </>
  );
}

function GuestForm({ initial, onClose, onSaved }) {
  const notify = useNotify();
  const [g, setG] = useState(initial);
  const [errors, setErrors] = useState({});
  const set = (key) => (e) => setG({ ...g, [key]: e.target.value });

  const save = async () => {
    // בדיקה בסיסית בצד הלקוח, השרת בודק שוב
    const bad = {};
    if (!g.firstName.trim()) bad.firstName = 'שדה חובה';
    if (!g.lastName.trim()) bad.lastName = 'שדה חובה';
    if (!/^\S+@\S+\.\S+$/.test(g.email)) bad.email = 'אימייל לא תקין';
    setErrors(bad);
    if (Object.keys(bad).length) return;

    try {
      if (g.id) await guestsApi.update(g.id, g);
      else await guestsApi.create(g);
      notify(g.id ? 'הפרטים עודכנו' : 'האורח נוסף');
      onSaved();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  return (
    <Modal
      title={g.id ? 'עריכת אורח' : 'אורח חדש'}
      onClose={onClose}
      footer={<><button className="btn ghost" onClick={onClose}>ביטול</button><button className="btn primary" onClick={save}>שמירה</button></>}
    >
      <div className="form-grid">
        <Field label="שם פרטי" error={errors.firstName}><input value={g.firstName} onChange={set('firstName')} /></Field>
        <Field label="שם משפחה" error={errors.lastName}><input value={g.lastName} onChange={set('lastName')} /></Field>
        <Field label="אימייל" error={errors.email}><input dir="ltr" value={g.email} onChange={set('email')} /></Field>
        <Field label="טלפון" hint="לדוגמה 052-1234567"><input dir="ltr" value={g.phone || ''} onChange={set('phone')} /></Field>
        <Field label="עיר"><input value={g.city || ''} onChange={set('city')} /></Field>
      </div>
    </Modal>
  );
}

function GuestHistory({ guest, onClose }) {
  const { data, loading } = useLoad(() => guestsApi.history(guest.id), [guest.id]);
  return (
    <Modal title={`${guest.firstName} ${guest.lastName}`} onClose={onClose}>
      <p className="muted">{guest.email} · {guest.phone} · הצטרף/ה ב-{shortDate(guest.joinedOn)}</p>
      <h4 className="mt">היסטוריית הזמנות</h4>
      {loading && <Loading />}
      {data?.length === 0 && <p className="muted">אין הזמנות עדיין</p>}
      <ul className="history">
        {data?.map((r) => (
          <li key={r.id}>
            <span className="grow"><strong>{r.unitName}</strong> · {shortDate(r.arrival)} – {shortDate(r.departure)}</span>
            <span>{shekel(r.amount)}</span>
            <StatusTag status={r.status} />
          </li>
        ))}
      </ul>
    </Modal>
  );
}
