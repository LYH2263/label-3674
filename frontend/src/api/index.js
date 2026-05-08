import http from './http';

export const authApi = {
  register: (payload) => http.post('/auth/register', payload),
  login: (payload) => http.post('/auth/login', payload),
  me: () => http.get('/auth/me'),
  logout: () => http.post('/auth/logout'),
};

export const systemApi = {
  overview: () => http.get('/system/overview'),
  providers: () => http.get('/system/providers'),
  intelligentNotices: () => http.get('/system/intelligent-notices'),
};

export const profileApi = {
  detail: () => http.get('/profile'),
  update: (payload) => http.put('/profile', payload),
};

export const repairApi = {
  create: (payload) => http.post('/repairs', payload),
  uploadImage: (file) => {
    const formData = new FormData();
    formData.append('image', file);
    return http.post('/repairs/upload-image', formData);
  },
  mine: () => http.get('/repairs/my'),
  board: () => http.get('/repairs/board'),
  updateStatus: (id, payload) => http.patch(`/repairs/${id}/status`, payload),
  rate: (id, payload) => http.patch(`/repairs/${id}/rating`, payload),
};

export const activityApi = {
  list: () => http.get('/activities'),
  mine: () => http.get('/activities/my'),
  create: (payload) => http.post('/activities', payload),
  signup: (id) => http.post(`/activities/${id}/signup`),
  checkinQr: (id) => http.get(`/activities/${id}/checkin-qrcode`),
  checkin: (id, payload) => http.post(`/activities/${id}/checkin`, payload),
  reviews: (id) => http.get(`/activities/${id}/reviews`),
  createReview: (id, payload) => http.post(`/activities/${id}/reviews`, payload),
};

export const messageApi = {
  list: () => http.get('/messages'),
  read: (id) => http.patch(`/messages/${id}/read`),
};

export const propertyApi = {
  overview: () => http.get('/property/overview'),
  createVisitor: (payload) => http.post('/property/visitors', payload),
  createPayIntent: (paymentId, payload) => http.post(`/property/payments/${paymentId}/pay-intent`, payload),
  confirmPayment: (paymentId, payload) => http.post(`/property/payments/${paymentId}/confirm`, payload),
};

export const neighborhoodApi = {
  list: (params) => http.get('/neighborhood/posts', { params }),
  create: (payload) => http.post('/neighborhood/posts', payload),
  close: (id) => http.patch(`/neighborhood/${id}/close`),
};
