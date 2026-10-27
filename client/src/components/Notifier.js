import { createContext, useCallback, useContext, useState } from 'react';

const NotifyContext = createContext(() => {});

export const useNotify = () => useContext(NotifyContext);

/** הודעות קצרות בתחתית המסך: notify('נשמר') או notify('שגיאה', 'error') */
export function NotifierProvider({ children }) {
  const [items, setItems] = useState([]);

  const notify = useCallback((text, kind = 'ok') => {
    const id = Date.now() + Math.random();
    setItems((list) => [...list, { id, text, kind }]);
    setTimeout(() => setItems((list) => list.filter((i) => i.id !== id)), 3800);
  }, []);

  return (
    <NotifyContext.Provider value={notify}>
      {children}
      <div className="notes">
        {items.map((i) => (
          <div key={i.id} className={`note note-${i.kind}`} role="status">
            <span>{i.kind === 'error' ? '!' : '✓'}</span>
            {i.text}
          </div>
        ))}
      </div>
    </NotifyContext.Provider>
  );
}
