import axios from 'axios';

const http = axios.create({
  baseURL: (process.env.REACT_APP_SERVER || 'http://localhost:8080') + '/api',
});

// כל שגיאה מהשרת מגיעה כ-Error עם הודעה בעברית
http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const message = err.response?.data?.message
      || (err.response ? 'אירעה שגיאה בשרת' : 'אין חיבור לשרת - האם הוא פועל?');
    return Promise.reject(new Error(message));
  }
);

export default http;
