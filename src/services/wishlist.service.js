import axiosClient from "@/api/axiosClient";

class WishlistService {
  // 1. Lấy danh sách yêu thích của sinh viên
  // Giả định API Backend: GET /api/wishlist/user/{studentId}
  async getWishlist(studentId) {
    return await axiosClient.get(`/wishlist/user/${studentId}`);
  }

  // 2. Thêm vào yêu thích
  // Giả định API Backend: POST /api/wishlist/add?studentId=...&courseId=...
  async addToWishlist(studentId, courseId) {
    return await axiosClient.post(`/wishlist/add`, null, {
      params: { studentId, courseId }
    });
  }

  // 3. Xóa khỏi yêu thích
  // Giả định API Backend: DELETE /api/wishlist/remove?studentId=...&courseId=...
  async removeFromWishlist(studentId, courseId) {
    return await axiosClient.delete(`/wishlist/remove`, {
      params: { studentId, courseId }
    });
  }

  // 4. Kiểm tra trạng thái (Có thể dùng nếu Backend có API check riêng)
  // Nếu không, ta sẽ check thủ công từ danh sách đã lấy
}

export default new WishlistService();