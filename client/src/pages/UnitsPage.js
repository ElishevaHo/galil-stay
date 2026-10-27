import { useState } from 'react';
import { unitsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { Empty, ErrorBox, Field, Loading, PageHead, Stars } from '../components/Bits';
import Modal, { Confirm } from '../components/Modal';
import { useNotify } from '../components/Notifier';
import { shekel, UNIT_KINDS } from '../utils';

const BLANK = {
  name: '', kind: 'CABIN', region: '', weekdayRate: 700, weekendRate: 900, cleaningFee: 150,
  capacity: 2, bedrooms: 1, minNights: 1, privatePool: false, jacuzzi: false, petsAllowed: false,
  description: '', listed: true,
};

export default function UnitsPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useLoad(() => unitsApi.list());
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);

  const run = async (fn, okText) => {
    try {
      await fn();
      notify(okText);
      reload();
      return true;
    } catch (e) {
      notify(e.message, 'error');
      return false;
    }
  };

  return (
    <>
      <PageHead title="היחידות שלנו" sub="צימרים, וילות וסוויטות במתחם">
        <button className="btn primary" onClick={() => setEditing(BLANK)}>+ יחידה חדשה</button>
      </PageHead>

      {loading && !data && <Loading />}
      {error && <ErrorBox message={error} onRetry={reload} />}
      {data?.length === 0 && <Empty text="עוד לא הוגדרו יחידות" />}

      <div className="cards">
        {data?.map((u) => (
          <article key={u.id} className={`unit-card ${u.listed ? '' : 'is-hidden'}`}>
            <div className={`unit-art art-${u.kind.toLowerCase()}`}>
              <span>{UNIT_KINDS[u.kind].mark}</span>
              <em>{UNIT_KINDS[u.kind].label}</em>
            </div>
            <div className="unit-body">
              <div className="result-top">
                <h3>{u.name}</h3>
                {!u.listed && <span className="tag tag-slate">מוסתרת</span>}
              </div>
              <p className="muted small">{u.region} · {u.capacity} אורחים · {u.bedrooms} חדרים · מינ׳ {u.minNights} לילות</p>
              <div className="rates">
                <span><small>אמצ״ש</small>{shekel(u.weekdayRate)}</span>
                <span><small>סופ״ש</small>{shekel(u.weekendRate)}</span>
                <span><small>ניקיון</small>{shekel(u.cleaningFee)}</span>
              </div>
              <div className="unit-rating">
                <Stars value={u.averageStars} />
                {u.reviewCount > 0 && <span className="muted small">({u.reviewCount})</span>}
              </div>
            </div>
            <div className="unit-actions">
              <button className="btn ghost sm" onClick={() => setEditing(u)}>עריכה</button>
              <button className="btn ghost sm" onClick={() => run(() => unitsApi.toggleListing(u.id), u.listed ? 'היחידה הוסתרה' : 'היחידה פתוחה להזמנות')}>
                {u.listed ? 'הסתרה' : 'פרסום'}
              </button>
              <button className="btn text-danger sm" onClick={() => setRemoving(u)}>מחיקה</button>
            </div>
          </article>
        ))}
      </div>

      {editing && (
        <UnitForm
          initial={editing}
          onClose={() => setEditing(null)}
          onSave={async (unit) => {
            const ok = await run(
              () => (unit.id ? unitsApi.update(unit.id, unit) : unitsApi.create(unit)),
              unit.id ? 'היחידה עודכנה' : 'היחידה נוספה'
            );
            if (ok) setEditing(null);
          }}
        />
      )}

      {removing && (
        <Confirm
          danger
          title="מחיקת יחידה"
          okLabel="מחיקה"
          text={`למחוק את "${removing.name}"? אם יש לה הזמנות - אפשר רק להסתיר אותה.`}
          onClose={() => setRemoving(null)}
          onOk={() => { const id = removing.id; setRemoving(null); run(() => unitsApi.remove(id), 'היחידה נמחקה'); }}
        />
      )}
    </>
  );
}

function UnitForm({ initial, onClose, onSave }) {
  const [u, setU] = useState(initial);
  const set = (key, cast = (v) => v) => (e) =>
    setU({ ...u, [key]: e.target.type === 'checkbox' ? e.target.checked : cast(e.target.value) });

  return (
    <Modal
      wide
      title={u.id ? `עריכת ${initial.name}` : 'יחידה חדשה'}
      onClose={onClose}
      footer={<><button className="btn ghost" onClick={onClose}>ביטול</button><button className="btn primary" onClick={() => onSave(u)}>שמירה</button></>}
    >
      <div className="form-grid three">
        <Field label="שם"><input value={u.name} onChange={set('name')} /></Field>
        <Field label="סוג">
          <select value={u.kind} onChange={set('kind')}>
            {Object.entries(UNIT_KINDS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
          </select>
        </Field>
        <Field label="אזור"><input value={u.region} onChange={set('region')} /></Field>
        <Field label="מחיר אמצע שבוע"><input type="number" value={u.weekdayRate} onChange={set('weekdayRate', Number)} /></Field>
        <Field label="מחיר סוף שבוע" hint="חמישי ושישי בלילה"><input type="number" value={u.weekendRate} onChange={set('weekendRate', Number)} /></Field>
        <Field label="דמי ניקיון"><input type="number" value={u.cleaningFee} onChange={set('cleaningFee', Number)} /></Field>
        <Field label="מקסימום אורחים"><input type="number" value={u.capacity} onChange={set('capacity', Number)} /></Field>
        <Field label="חדרי שינה"><input type="number" value={u.bedrooms} onChange={set('bedrooms', Number)} /></Field>
        <Field label="מינימום לילות"><input type="number" value={u.minNights} onChange={set('minNights', Number)} /></Field>
      </div>
      <div className="checks">
        <label><input type="checkbox" checked={u.privatePool} onChange={set('privatePool')} /> בריכה פרטית</label>
        <label><input type="checkbox" checked={u.jacuzzi} onChange={set('jacuzzi')} /> ג׳קוזי</label>
        <label><input type="checkbox" checked={u.petsAllowed} onChange={set('petsAllowed')} /> מותר עם כלבים</label>
        <label><input type="checkbox" checked={u.listed} onChange={set('listed')} /> פתוחה להזמנות</label>
      </div>
      <Field label="תיאור">
        <textarea rows="3" value={u.description || ''} onChange={set('description')} />
      </Field>
    </Modal>
  );
}
