import { reportsApi, reservationsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, PageHead, Stars, StatusTag } from '../components/Bits';
import { useNotify } from '../components/Notifier';
import { shekel, shortDate, weekday } from '../utils';

export default function Overview({ go }) {
  const notify = useNotify();
  const stats = useLoad(() => reportsApi.overview());
  const pending = useLoad(() => reservationsApi.list('PENDING'));

  const approve = async (id) => {
    try {
      await reservationsApi.confirm(id);
      notify('ההזמנה אושרה');
      pending.reload();
      stats.reload();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  if (stats.loading && !stats.data) return <Loading />;
  if (stats.error) return <ErrorBox message={stats.error} onRetry={stats.reload} />;

  const s = stats.data;
  const today = new Date().toLocaleDateString('he-IL', { weekday: 'long', day: 'numeric', month: 'long' });

  return (
    <>
      <PageHead title="בוקר טוב 👋" sub={today}>
        <button className="btn primary" onClick={() => go('search')}>+ הזמנה חדשה</button>
      </PageHead>

      <section className="kpis">
        <Kpi label="מגיעים היום" value={s.arrivalsToday} />
        <Kpi label="עוזבים היום" value={s.departuresToday} />
        <Kpi label="יחידות מאוכלסות" value={`${s.staysInHouse}/${s.listedUnits}`} />
        <Kpi label="ממתינות לאישור" value={s.pendingApprovals} alert={s.pendingApprovals > 0} />
        <Kpi label="הכנסות החודש" value={shekel(s.revenueThisMonth)} />
        <Kpi label="תפוסה החודש" value={`${s.occupancyThisMonth}%`} bar={s.occupancyThisMonth} />
      </section>

      <div className="two-col">
        <section className="panel">
          <div className="panel-head">
            <h3>הגעות בשבוע הקרוב</h3>
            <button className="link" onClick={() => go('occupancy')}>ללוח התפוסה ←</button>
          </div>
          {s.nextArrivals.length === 0 && <p className="muted">אין הגעות מתוכננות השבוע</p>}
          <ul className="arrivals">
            {s.nextArrivals.map((r) => (
              <li key={r.id}>
                <div className="date-box">
                  <small>{weekday(r.arrival)}</small>
                  <strong>{shortDate(r.arrival).slice(0, 5)}</strong>
                </div>
                <div className="grow">
                  <strong>{r.guestName}</strong>
                  <p className="muted small">{r.unitName} · {r.nights} לילות · {r.adults + r.children} אורחים</p>
                </div>
                <StatusTag status={r.status} />
              </li>
            ))}
          </ul>
        </section>

        <section className="panel">
          <div className="panel-head">
            <h3>מחכות לאישור שלך</h3>
            {s.averageStars != null && (
              <span className="muted small">דירוג ממוצע <Stars value={s.averageStars} /> {s.averageStars}</span>
            )}
          </div>
          {pending.data?.length === 0 && <p className="muted">אין הזמנות שממתינות 🎉</p>}
          <ul className="pending">
            {pending.data?.map((r) => (
              <li key={r.id}>
                <div className="grow">
                  <strong>{r.guestName}</strong> · {r.unitName}
                  <p className="muted small">{shortDate(r.arrival)} – {shortDate(r.departure)} · {shekel(r.amount)}</p>
                </div>
                <button className="btn primary sm" onClick={() => approve(r.id)}>אישור</button>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  );
}

function Kpi({ label, value, alert, bar }) {
  return (
    <div className={`kpi ${alert ? 'kpi-alert' : ''}`}>
      <span className="kpi-label">{label}</span>
      <span className="kpi-value">{value}</span>
      {bar != null && <span className="meter"><span style={{ width: `${bar}%` }} /></span>}
    </div>
  );
}
