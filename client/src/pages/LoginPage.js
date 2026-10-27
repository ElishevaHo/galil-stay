import { useEffect, useState } from 'react';
import { auth, unitsApi } from '../api/endpoints';
import { Field } from '../components/Bits';

export default function LoginPage({ onSignedIn }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [waking, setWaking] = useState(false);

  // שרת חינמי נרדם כשאין פעילות - מעירים אותו כבר בכניסה לדף
  useEffect(() => {
    const slow = setTimeout(() => setWaking(true), 2500);
    unitsApi.list().catch(() => {}).finally(() => { clearTimeout(slow); setWaking(false); });
    return () => clearTimeout(slow);
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      onSignedIn(await auth.login(username, password));
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login">
      <div className="login-art" aria-hidden>
        <div className="hills">
          <span className="hill h1" />
          <span className="hill h2" />
          <span className="hill h3" />
          <span className="sun" />
        </div>
        <blockquote>
          "הדרך הכי טובה לנהל מתחם נופש - <br />כשהכל מסודר במקום אחד."
        </blockquote>
      </div>

      <form className="login-card" onSubmit={submit}>
        <span className="brand-mark big" aria-hidden>⛰</span>
        <h1>GalilStay</h1>
        <p className="muted">כניסה לניהול המתחם</p>

        <Field label="שם משתמש">
          <input autoFocus value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        </Field>
        <Field label="סיסמה">
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        </Field>

        {waking && <div className="waking"><span className="spinner" /> השרת מתעורר, זה יכול לקחת עד דקה...</div>}
        {error && <div className="alert">{error}</div>}

        <button className="btn primary block" disabled={busy || !username || !password}>
          {busy ? 'מתחבר...' : 'כניסה'}
        </button>
        <p className="muted small center">משתמש לדוגמה: host / galil2026</p>
      </form>
    </div>
  );
}
