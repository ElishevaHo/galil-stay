import { useEffect, useState } from 'react';
import { NotifierProvider } from './components/Notifier';
import LoginPage from './pages/LoginPage';
import Overview from './pages/Overview';
import SearchPage from './pages/SearchPage';
import ReservationsPage from './pages/ReservationsPage';
import Occupancy from './pages/Occupancy';
import UnitsPage from './pages/UnitsPage';
import GuestsPage from './pages/GuestsPage';
import ExtrasPage from './pages/ExtrasPage';
import ReviewsPage from './pages/ReviewsPage';
import ReportsPage from './pages/ReportsPage';

const SECTIONS = [
  { key: 'overview', title: 'היום', Page: Overview },
  { key: 'search', title: 'חיפוש והזמנה', Page: SearchPage },
  { key: 'reservations', title: 'הזמנות', Page: ReservationsPage },
  { key: 'occupancy', title: 'לוח תפוסה', Page: Occupancy },
  { key: 'units', title: 'יחידות', Page: UnitsPage },
  { key: 'guests', title: 'אורחים', Page: GuestsPage },
  { key: 'extras', title: 'תוספות', Page: ExtrasPage },
  { key: 'reviews', title: 'חוות דעת', Page: ReviewsPage },
  { key: 'reports', title: 'דוחות', Page: ReportsPage },
];

const STORAGE_KEY = 'galilstay.host';

function readHost() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY));
  } catch {
    return null;
  }
}

// הדף הנוכחי נשמר ב-hash של הכתובת, כך שרענון לא מחזיר להתחלה
const currentSection = () => {
  const key = window.location.hash.replace('#', '');
  return SECTIONS.some((s) => s.key === key) ? key : 'overview';
};

export default function App() {
  const [host, setHost] = useState(readHost);
  const [section, setSection] = useState(currentSection);

  useEffect(() => {
    const onHash = () => setSection(currentSection());
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, []);

  const go = (key) => {
    window.location.hash = key;
    window.scrollTo(0, 0);
  };

  const signIn = (h) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(h));
    setHost(h);
  };

  const signOut = () => {
    localStorage.removeItem(STORAGE_KEY);
    setHost(null);
  };

  if (!host) {
    return (
      <NotifierProvider>
        <LoginPage onSignedIn={signIn} />
      </NotifierProvider>
    );
  }

  const { Page } = SECTIONS.find((s) => s.key === section);

  return (
    <NotifierProvider>
      <header className="topbar">
        <div className="topbar-row">
          <a className="brand" href="#overview">
            <span className="brand-mark" aria-hidden>⛰</span>
            <span>
              <strong>GalilStay</strong>
              <small>צימרים ווילות בצפון</small>
            </span>
          </a>
          <div className="host">
            <span className="host-name">{host.displayName}</span>
            <button className="btn ghost sm" onClick={signOut}>יציאה</button>
          </div>
        </div>
        <nav className="tabs" aria-label="ניווט ראשי">
          {SECTIONS.map((s) => (
            <button
              key={s.key}
              className={`tab ${s.key === section ? 'tab-on' : ''}`}
              onClick={() => go(s.key)}
            >
              {s.title}
            </button>
          ))}
        </nav>
      </header>

      <main className="wrap">
        <Page go={go} />
      </main>

      <footer className="foot">GalilStay · מערכת ניהול מתחם נופש</footer>
    </NotifierProvider>
  );
}
