import { useState } from 'react';
import { extrasApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, PageHead } from '../components/Bits';
import { useNotify } from '../components/Notifier';
import { shekel } from '../utils';

const BLANK = { title: '', price: '', chargedPerNight: false };

export default function ExtrasPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useLoad(() => extrasApi.list());
  const [draft, setDraft] = useState(BLANK);
  const [editId, setEditId] = useState(null);

  const save = async (e) => {
    e.preventDefault();
    try {
      const body = { ...draft, price: Number(draft.price) };
      if (editId) await extrasApi.update(editId, body);
      else await extrasApi.create(body);
      notify(editId ? 'התוספת עודכנה' : 'התוספת נוספה');
      setDraft(BLANK);
      setEditId(null);
      reload();
    } catch (err) {
      notify(err.message, 'error');
    }
  };

  const remove = async (id) => {
    try {
      await extrasApi.remove(id);
      notify('התוספת נמחקה');
      reload();
    } catch (err) {
      notify(err.message, 'error');
    }
  };

  return (
    <>
      <PageHead title="תוספות" sub="שירותים שאפשר לצרף להזמנה" />

      <form className="inline-form panel" onSubmit={save}>
        <input placeholder="שם התוספת" value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} />
        <input type="number" placeholder="מחיר" min="0" value={draft.price} onChange={(e) => setDraft({ ...draft, price: e.target.value })} />
        <label className="check-inline">
          <input type="checkbox" checked={draft.chargedPerNight} onChange={(e) => setDraft({ ...draft, chargedPerNight: e.target.checked })} />
          לפי לילה
        </label>
        <button className="btn primary" disabled={!draft.title || draft.price === ''}>{editId ? 'עדכון' : 'הוספה'}</button>
        {editId && <button type="button" className="btn ghost" onClick={() => { setEditId(null); setDraft(BLANK); }}>ביטול</button>}
      </form>

      {loading && !data && <Loading />}
      {error && <ErrorBox message={error} onRetry={reload} />}

      <ul className="extras">
        {data?.map((x) => (
          <li key={x.id} className="panel">
            <div className="grow">
              <strong>{x.title}</strong>
              <p className="muted small">{x.chargedPerNight ? 'נגבה על כל לילה' : 'תשלום חד-פעמי'}</p>
            </div>
            <span className="price">{shekel(x.price)}</span>
            <button className="btn ghost sm" onClick={() => { setEditId(x.id); setDraft({ ...x }); }}>עריכה</button>
            <button className="btn text-danger sm" onClick={() => remove(x.id)}>מחיקה</button>
          </li>
        ))}
      </ul>
    </>
  );
}
