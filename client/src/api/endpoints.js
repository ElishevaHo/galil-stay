import http from './http';

export const auth = {
  login: (username, password) => http.post('/auth/login', { username, password }),
};

export const unitsApi = {
  list: () => http.get('/units'),
  available: (arrival, departure, guests) => http.get('/units/available', { params: { arrival, departure, guests } }),
  create: (unit) => http.post('/units', unit),
  update: (id, unit) => http.put(`/units/${id}`, unit),
  toggleListing: (id) => http.patch(`/units/${id}/listing`),
  remove: (id) => http.delete(`/units/${id}`),
};

export const guestsApi = {
  list: () => http.get('/guests'),
  history: (id) => http.get(`/guests/${id}/reservations`),
  create: (guest) => http.post('/guests', guest),
  update: (id, guest) => http.put(`/guests/${id}`, guest),
  remove: (id) => http.delete(`/guests/${id}`),
};

export const reservationsApi = {
  list: (status) => http.get('/reservations', { params: status ? { status } : {} }),
  timeline: (from, to) => http.get('/reservations/timeline', { params: { from, to } }),
  quote: (request) => http.post('/reservations/quote', request),
  create: (request) => http.post('/reservations', request),
  refundPreview: (id) => http.get(`/reservations/${id}/refund`),
  confirm: (id) => http.patch(`/reservations/${id}/confirm`),
  cancel: (id) => http.patch(`/reservations/${id}/cancel`),
  complete: (id) => http.patch(`/reservations/${id}/complete`),
};

export const extrasApi = {
  list: () => http.get('/extras'),
  create: (extra) => http.post('/extras', extra),
  update: (id, extra) => http.put(`/extras/${id}`, extra),
  remove: (id) => http.delete(`/extras/${id}`),
};

export const reviewsApi = {
  list: () => http.get('/reviews'),
  create: (review) => http.post('/reviews', review),
  remove: (id) => http.delete(`/reviews/${id}`),
};

export const reportsApi = {
  overview: () => http.get('/reports/overview'),
  months: (year) => http.get('/reports/months', { params: { year } }),
  units: () => http.get('/reports/units'),
};
