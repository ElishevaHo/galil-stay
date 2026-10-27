import { useCallback, useEffect, useState } from 'react';

/**
 * טוען נתונים מהשרת ומחזיר { data, loading, error, reload }.
 * loader צריכה להיות פונקציה שמחזירה Promise.
 */
export function useLoad(loader, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(loader, deps);

  const reload = useCallback(() => {
    setLoading(true);
    setError('');
    return run()
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [run]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, error, reload, setData };
}
