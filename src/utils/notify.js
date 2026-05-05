import { createToastInterface } from 'vue-toastification';
import { toastEventBus, toastOptions } from '@/plugins/toast';

// Use the same event bus as the installed plugin so calls from anywhere
// (including window.__notify) always target the mounted toast container.
const sharedToast = createToastInterface(toastEventBus);
let fallbackToast = null;

const DEFAULT_MESSAGES = {
  success: 'Thao tác thành công.',
  error: 'Đã có lỗi xảy ra. Vui lòng thử lại.',
  warning: 'Vui lòng kiểm tra lại thông tin.',
  info: 'Đã cập nhật thông tin.'
};

const toText = (value) => {
  if (typeof value === 'string') {
    return value.replace(/^[\u2705\u274c\u26a0\ufe0f\u2139]+/g, '').trim();
  }

  if (value === null || value === undefined) {
    return '';
  }

  return String(value).trim();
};

const normalizeMessage = (message, fallback) => {
  const text = toText(message);
  return text || fallback;
};

const getFallbackToast = () => {
  if (!fallbackToast) {
    fallbackToast = createToastInterface(toastOptions);
  }
  return fallbackToast;
};

const getToast = () => sharedToast;

const show = (type, message, options = {}) => {
  const toast = getToast();
  try {
    if (typeof toast?.[type] === 'function') {
      return toast[type](message, options);
    }
    if (typeof toast === 'function') {
      return toast(message, { ...options, type });
    }
  } catch (error) {
    const fallback = getFallbackToast();
    if (typeof fallback?.[type] === 'function') {
      return fallback[type](message, options);
    }
    if (typeof fallback === 'function') {
      return fallback(message, { ...options, type });
    }
  }
  return null;
};

const success = (message, options = {}) =>
  show('success', normalizeMessage(message, DEFAULT_MESSAGES.success), options);

const error = (message, options = {}) =>
  show('error', normalizeMessage(message, DEFAULT_MESSAGES.error), options);

const warning = (message, options = {}) =>
  show('warning', normalizeMessage(message, DEFAULT_MESSAGES.warning), options);

const info = (message, options = {}) =>
  show('info', normalizeMessage(message, DEFAULT_MESSAGES.info), options);

const auto = (message, options = {}) => {
  const normalized = normalizeMessage(message, DEFAULT_MESSAGES.info);
  const lower = normalized.toLowerCase();

  if (/(lỗi|error|thất bại|không thể|fail)/i.test(lower)) {
    return error(normalized, options);
  }

  if (/(vui lòng|cảnh báo|warning|không hợp lệ|chưa)/i.test(lower)) {
    return warning(normalized, options);
  }

  if (/(thành công|xong|hoàn tất|đã)/i.test(lower)) {
    return success(normalized, options);
  }

  return info(normalized, options);
};

export const getErrorMessage = (err, fallback = DEFAULT_MESSAGES.error) => {
  const raw =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.response?.data ||
    err?.message;

  return normalizeMessage(raw, fallback);
};

const notify = {
  success,
  error,
  warning,
  info,
  auto
};

export const useNotify = () => notify;
export default notify;
