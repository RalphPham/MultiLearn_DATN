const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export function uploadVideo(file, onProgress) {
  return new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append('file', file);

    const xhr = new XMLHttpRequest();

    xhr.upload.addEventListener('progress', (event) => {
      if (event.lengthComputable) {
        const percent = Math.round((event.loaded / event.total) * 100);
        if (onProgress) onProgress(percent);
      }
    });

    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        try {
          const response = JSON.parse(xhr.responseText);
          resolve(response.url || response.fileName || response.data || response);
        } catch (e) {
          resolve(xhr.responseText);
        }
      } else {
        reject(new Error(`Upload thất bại! Mã lỗi: ${xhr.status} - ${xhr.statusText}`));
      }
    };

    xhr.onerror = () => reject(new Error('Lỗi mạng khi upload video'));
    xhr.ontimeout = () => reject(new Error('Upload video bị timeout'));

    xhr.open('POST', `${BASE_URL}/media/upload/video`);

    const token = localStorage.getItem('access_token');
    if (token) {
      xhr.setRequestHeader('Authorization', `Bearer ${token}`);
    }

    xhr.timeout = 0;
    xhr.send(formData);
  });
}