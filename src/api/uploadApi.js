const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

function extractErrorMessage(xhr) {
  const raw = xhr?.responseText;

  if (!raw) {
    return `Upload thất bại! Mã lỗi: ${xhr?.status || 'unknown'}`;
  }

  try {
    const parsed = JSON.parse(raw);
    return parsed?.message || parsed?.error || parsed?.data || raw;
  } catch {
    return raw;
  }
}

function parseSuccessResponse(xhr) {
  const raw = xhr?.responseText;

  if (!raw) return '';

  try {
    const parsed = JSON.parse(raw);
    return parsed?.url || parsed?.fileName || parsed?.data || parsed;
  } catch {
    return raw;
  }
}

export function uploadVideo(file, onProgress) {
  return new Promise((resolve, reject) => {
    if (!file) {
      reject(new Error('Không có file để upload'));
      return;
    }

    if (file.size > 3 * 1024 * 1024 * 1024) {
      reject(new Error('File quá lớn (>3GB).'));
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    const xhr = new XMLHttpRequest();

    xhr.upload.addEventListener('progress', (event) => {
      if (!event.lengthComputable) return;
      const percent = Math.round((event.loaded / event.total) * 100);
      onProgress?.(percent);
    });

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve(parseSuccessResponse(xhr));
        return;
      }

      reject(new Error(extractErrorMessage(xhr)));
    };

    xhr.onerror = () => reject(new Error('Lỗi mạng khi upload video'));
    xhr.ontimeout = () => reject(new Error('Upload video bị timeout'));
    xhr.onabort = () => reject(new Error('Upload video đã bị hủy'));

    xhr.open('POST', `${BASE_URL}/media/upload/video`);

    const token = localStorage.getItem('access_token');
    if (token) {
      xhr.setRequestHeader('Authorization', `Bearer ${token}`);
    }

    xhr.timeout = 60 * 60 * 1000;
    xhr.send(formData);
  });
}