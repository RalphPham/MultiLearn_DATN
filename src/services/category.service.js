import axiosClient from "@/api/axiosClient";
class CategoryService {
  getAll() {
    return axiosClient.get('/categories');
  }
}

export default new CategoryService();