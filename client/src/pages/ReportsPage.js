import { useState } from 'react';
import { reportsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { ErrorBox, Loading, PageHead, Stars } from '../components/Bits';
import { MONTHS, shekel } from '../utils';

export default function ReportsPage() {
  const thisYear = new Date().getFullYear();
  const [year, setYear] = useState(thisYear);
  const months = useLoad(() => reportsApi.months(year), [year]);
  const units = useLoad(() => reportsApi.units());

  if (months.loading && !months.data) return <Loading />;
  if (months.error) return <ErrorBox message={months.error} onRetry={months.reload} />;

  const rows = months.data;
  const peak = Math.max(1, ...rows.map((m) => m.revenue));
  const yearTotal = rows.reduce((s, m) => s + m.revenue, 0);
  const nights = rows.reduce((s, m) => s + m.nightsSold, 0);
  const best = rows.reduce((a, b) => (b.revenue > a.revenue ? b : a), rows[0]);

  return (
    <>
      <PageHead title="דוחות" sub="הכנסות ותפוסה לאורך השנה">
        <div className="segmented">
          {[thisYear - 1, thisYear, thisYear + 1].map((y) => (
            <button key={y} className={y === year ? 'on' : ''} onClick={() => setYear(y)}>{y}</button>
          ))}
        </div>
      </PageHead>

      <section className="kpis three">
        <div className="kpi"><span className="kpi-label">הכנסות {year}</span><span className="kpi-value">{shekel(yearTotal)}</span></div>
        <div className="kpi"><span className="kpi-label">לילות שנמכרו</span><span className="kpi-value">{nights}</span></div>
        <div className="kpi">
          <span className="kpi-label">החודש החזק</span>
          <span className="kpi-value">{best.revenue > 0 ? MONTHS[best.month - 1] : '—'}</span>
        </div>
      </section>

      <section className="panel">
        <h3>הכנסות לפי חודש</h3>
        <div className="chart" role="img" aria-label="גרף הכנסות חודשי">
          {rows.map((m) => (
            <div key={m.month} className="chart-col" title={`${MONTHS[m.month - 1]}: ${shekel(m.revenue)} · תפוסה ${m.occupancy}%`}>
              <span className="chart-val">{m.revenue > 0 ? Math.round(m.revenue / 1000) + 'K' : ''}</span>
              <div className="chart-bar" style={{ height: `${(m.revenue / peak) * 100}%` }} />
              <span className="chart-occ">{m.occupancy}%</span>
              <span className="chart-label">{MONTHS[m.month - 1]}</span>
            </div>
          ))}
        </div>
        <p className="muted small">האחוז מתחת לכל עמודה הוא שיעור התפוסה של אותו חודש</p>
      </section>

      <section className="panel">
        <h3>ביצועים לפי יחידה</h3>
        {units.loading && <Loading />}
        {units.data && (
          <div className="table-card flat">
            <table>
              <thead><tr><th>יחידה</th><th>הזמנות</th><th>לילות</th><th>הכנסות</th><th>חלק מההכנסות</th><th>דירוג</th></tr></thead>
              <tbody>
                {units.data.map((u) => {
                  const total = units.data.reduce((s, x) => s + x.revenue, 0) || 1;
                  const share = Math.round((u.revenue / total) * 100);
                  return (
                    <tr key={u.unitId}>
                      <td><strong>{u.unitName}</strong></td>
                      <td>{u.reservations}</td>
                      <td>{u.nightsSold}</td>
                      <td>{shekel(u.revenue)}</td>
                      <td><span className="meter inline"><span style={{ width: `${share}%` }} /></span> {share}%</td>
                      <td><Stars value={u.averageStars} /></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
