import axios from 'axios';
import notify from '@/utils/notify';
import { normalizeMojibakeDeep } from '@/utils/textEncoding';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
});

axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

const forceLogout = () => {
  ['access_token', 'refresh_token', 'user_info', 'user_role', 'admin_role', 'admin_permissions']
    .forEach((k) => localStorage.removeItem(k));
  window.dispatchEvent(new Event('user-info-updated'));
  window.location.href = '/login';
};

axiosClient.interceptors.response.use(
  (response) => normalizeMojibakeDeep(response.data ?? response),
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    // BẮT LỖI 403 - KHÔNG CÓ QUYỀN TRUY CẬP
    // Chỉ hiện thông báo, KHÔNG redirect — router guard đã xử lý role-based navigation.
    // Redirect ở đây sẽ đẩy user ra khỏi trang hợp lệ khi API nền trả 403.
    if (status === 403) {
      const url = originalRequest.url || '';
      const silentUrls = ['/unread-count', '/cart', '/ratings/', '/campaigns/', '/wishlist'];
      const isSilent = silentUrls.some((u) => url.includes(u));
      if (!isSilent) {
        notify.warning('Bạn không có quyền truy cập vùng này.');
      }
      return Promise.reject(error);
    }

    // XỬ LÝ LỖI 401 - LÀM MỚI TOKEN TỰ ĐỘNG
    if (status === 401 && !originalRequest._retry) {
      const refreshToken = localStorage.getItem('refresh_token');
      const accessToken = localStorage.getItem('access_token');

      // Tài khoản bị khóa giữa phiên đang dùng -> logout sạch & báo người dùng,
      // không cố refresh (refresh cũng sẽ fail và làm mất context lỗi gốc).
      const serverMsg = error.response?.data?.message || error.response?.data || '';
      const isAccountLocked = typeof serverMsg === 'string' && /bị khóa/i.test(serverMsg);
      if (isAccountLocked) {
        if (accessToken || refreshToken) {
          notify.warning('Tài khoản của bạn đã bị khóa. Bạn sẽ được chuyển ra trang đăng nhập.');
          setTimeout(forceLogout, 1500);
        }
        return Promise.reject(error);
      }

      // Nếu user chưa đăng nhập (guest) -> không forceLogout, chỉ reject.
      if (!accessToken && !refreshToken) {
        return Promise.reject(error);
      }

      if (!refreshToken) {
        forceLogout();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/auth/refresh`,
          { refreshToken }
        );
        const newToken = res.data.accessToken;
        localStorage.setItem('access_token', newToken);
        axiosClient.defaults.headers.common.Authorization = `Bearer ${newToken}`;
        processQueue(null, newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        forceLogout();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
