// Cấu hình URL của API Server
export const API_URL = 'http://localhost:8080';

// Hàm xử lý URL ảnh
export const getImageUrl = (url) => {
    if (!url) return 'https://placehold.co/600x400?text=No+Image';
    // Nếu là link online (http/https) thì giữ nguyên
    if (url.startsWith('http')) return url;
    // Nếu là link upload từ server (vd: /uploads/abc.jpg) thì nối thêm domain
    return `${API_URL}${url.startsWith('/') ? '' : '/'}${url}`;
};

// Hàm format tiền tệ (VNĐ)
export const formatPrice = (price) => {
    if (price === undefined || price === null) return 'Liên hệ';
    if (price === 0) return 'Miễn phí';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
};

// Hàm tính % giảm giá
export const calculateDiscount = (original, sale) => {
    if (!original || !sale || original <= sale) return 0;
    return Math.round(((original - sale) / original) * 100);
};