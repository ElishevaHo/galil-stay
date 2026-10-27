# ⛰ GalilStay – ניהול צימרים ווילות נופש

מערכת לניהול מתחם נופש בצפון: יחידות אירוח, אורחים, הזמנות, תוספות, חוות דעת ודוחות.

```
galil-stay/
├── server/   # Java Spring Boot (REST API)
└── client/   # React
```

## טכנולוגיות

**שרת:** Java 17+, Spring Boot 4, Spring Data JPA, H2 (קובץ), Bean Validation, ModelMapper, BCrypt, Lombok
**לקוח:** React 19, Axios, Create React App

## הרצה

```bash
# שרת - http://localhost:8080
cd server
./mvnw spring-boot:run        # ב-Windows: .\mvnw spring-boot:run

# לקוח - http://localhost:3000
cd client
npm install
npm start
```

משתמש לדוגמה: `host` / `galil2026`
קונסולת H2: `http://localhost:8080/h2` (JDBC URL: `jdbc:h2:file:./data/galilstay`, משתמש `sa`)

בהפעלה הראשונה המסד מתמלא בנתוני דוגמה (6 יחידות, 6 אורחים, הזמנות וחוות דעת).
למחיקת הנתונים ולהתחלה מחדש – למחוק את התיקייה `server/data`.

## כללים עסקיים

| נושא | כלל |
|---|---|
| תמחור | לילות חמישי ושישי בתעריף סוף שבוע, שאר הלילות בתעריף אמצע שבוע |
| הנחת שהייה | 4+ לילות – 6%, 7+ לילות – 12% |
| תוספות | לפי לילה (למשל ארוחת בוקר) או חד-פעמי (עיסוי), ובנוסף דמי ניקיון |
| הזמנה | מינימום לילות לפי יחידה, לא יותר אורחים מהקיבולת, בלי חפיפה עם הזמנה פעילה |
| סטטוסים | ממתינה ← מאושרת ← הסתיימה, או בוטלה |
| ביטול | 14+ ימים לפני ההגעה – החזר מלא, 5–13 ימים – חצי, פחות – ללא החזר |
| חוות דעת | רק על שהייה שהסתיימה, אחת לכל הזמנה |
| מחיקה | יחידה או אורח עם הזמנות לא נמחקים (יחידה אפשר להסתיר) |

## API

| Method | נתיב | תיאור |
|---|---|---|
| POST | `/api/auth/login` | כניסת מנהל |
| GET/POST | `/api/units` | רשימה / הוספה |
| PUT/DELETE | `/api/units/{id}` | עדכון / מחיקה |
| PATCH | `/api/units/{id}/listing` | הסתרה / פרסום |
| GET | `/api/units/available?arrival&departure&guests` | יחידות פנויות + מחיר |
| GET/POST | `/api/guests` | רשימה / הוספה |
| PUT/DELETE | `/api/guests/{id}` | עדכון / מחיקה |
| GET | `/api/guests/{id}/reservations` | היסטוריית אורח |
| GET/POST | `/api/reservations` | רשימה (`?status=`) / יצירה |
| POST | `/api/reservations/quote` | הצעת מחיר בלי שמירה |
| GET | `/api/reservations/timeline?from&to` | הזמנות בטווח (ללוח תפוסה) |
| GET | `/api/reservations/{id}/refund` | כמה יוחזר בביטול |
| PATCH | `/api/reservations/{id}/confirm` · `/cancel` · `/complete` | שינוי סטטוס |
| GET/POST/PUT/DELETE | `/api/extras` | תוספות |
| GET/POST/DELETE | `/api/reviews` | חוות דעת |
| GET | `/api/reports/overview` · `/months?year` · `/units` | דוחות |

שגיאות מוחזרות בפורמט אחיד: `{ "status": 409, "message": "...", "fields": {...} }`
