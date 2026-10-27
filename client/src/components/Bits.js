import { STATUS } from '../utils';

export function StatusTag({ status }) {
  const s = STATUS[status] || { label: status, tone: 'slate' };
  return <span className={`tag tag-${s.tone}`}>{s.label}</span>;
}

export function Stars({ value, size = 'sm' }) {
  if (value == null) return <span className="muted small">אין דירוג</span>;
  const full = Math.round(value);
  return (
    <span className={`stars stars-${size}`} title={`${value} מתוך 5`}>
      {'★'.repeat(full)}
      <span className="stars-off">{'★'.repeat(5 - full)}</span>
    </span>
  );
}

export function Loading() {
  return (
    <div className="state">
      <div className="spinner" />
      טוען...
    </div>
  );
}

export function ErrorBox({ message, onRetry }) {
  return (
    <div className="state state-error">
      <p>{message}</p>
      {onRetry && <button className="btn ghost" onClick={onRetry}>לנסות שוב</button>}
    </div>
  );
}

export function Empty({ text, children }) {
  return (
    <div className="state">
      <p>{text}</p>
      {children}
    </div>
  );
}

export function PageHead({ title, sub, children }) {
  return (
    <div className="page-head">
      <div>
        <h2>{title}</h2>
        {sub && <p className="muted">{sub}</p>}
      </div>
      <div className="page-actions">{children}</div>
    </div>
  );
}

export function Field({ label, error, children, hint }) {
  return (
    <label className={`field ${error ? 'field-bad' : ''}`}>
      <span className="field-label">{label}</span>
      {children}
      {hint && !error && <span className="field-hint">{hint}</span>}
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}
