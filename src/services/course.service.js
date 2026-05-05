import axiosClient from "@/api/axiosClient";

class CourseService {
    // SỬA: Thêm = {} để tránh lỗi khi gọi hàm không truyền tham số
    getAll(params = {}) { 
        const cleanParams = {};
        
        // Kiểm tra an toàn trước khi lặp keys
        if (params && typeof params === 'object') {
            Object.keys(params).forEach(key => {
                if (params[key] !== null && params[key] !== '' && params[key] !== undefined) {
                    cleanParams[key] = params[key];
                }
            });
        }
        
        console.log("Đang gọi API /courses với params:", cleanParams);
        
        return axiosClient.get('/courses', { params: cleanParams })
            .then(res => {
                console.log("Dữ liệu khóa học nhận được từ Server:", res);
                return res;
            })
            .catch(err => {
                console.error("Lỗi khi gọi API courses:", err);
                throw err;
            });
    }

   getDetail(slug) {
    return axiosClient.get(`/courses/slug/${slug}`);
}
}

export default new CourseService();