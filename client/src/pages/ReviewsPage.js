import { useState } from 'react';
import { reviewsApi } from '../api/endpoints';
import { useLoad } from '../hooks';
import { Empty, ErrorBox, Loading, PageHead, Stars } from '../components/Bits';
import { useNotify } from '../components/Notifier';
import { shortDate } from '../utils';

export default function ReviewsPage() {
  const notify = useNotify();
  const { data, loading, error, reload } = useLoad(() => reviewsApi.list());
  const [minStars, setMinStars] = useState(1);

  const remove = async (id) => {
    try {
      await reviewsApi.remove(id);
      notify('חוות הדעת הוסרה');
      reload();
    } catch (e) {
      notify(e.message, 'error');
    }
  };

  const list = (data || []).filter((r) => r.stars >= minStars);
  const avg = data?.length ? data.reduce((s, r) => s + r.stars, 0) / data.length : null;

  return (
    <>
      <PageHead title="חוות דעת" sub="מה האורחים אומרים עלינו">
        <select value={minStars} onChange={(e) => setMinStars(Number(e.target.value))}>
          <option value={1}>כל הדירוגים</option>
          <option value={4}>4 כוכבים ומעלה</option>
          <option value={5}>5 כוכבים בלבד</option>
        </select>
      </PageHead>

      {avg != null && (
        <div className="score panel">
          <span className="score-num">{avg.toFixed(1)}</span>
          <div>
            <Stars value={avg} size="lg" />
            <p className="muted small">ממוצע מתוך {data.length} חוות דעת</p>
          </div>
        </div>
      )}

      {loading && !data && <Loading />}
      {error && <ErrorBox message={error} onRetry={reload} />}
      {data && list.length === 0 && <Empty text="אין חוות דעת להצגה. אפשר להוסיף מתוך הזמנה שהסתיימה." />}

      <div className="quotes">
        {list.map((r) => (
          <figure key={r.id} className="quote">
            <Stars value={r.stars} />
            <blockquote>{r.text || <span className="muted">(ללא טקסט)</span>}</blockquote>
            <figcaption>
              <strong>{r.guestName}</strong>
              <span className="muted small">{r.unitName} · {shortDate(r.writtenOn)}</span>
            </figcaption>
            <button className="link danger small" onClick={() => remove(r.id)}>הסרה</button>
          </figure>
        ))}
      </div>
    </>
  );
}
