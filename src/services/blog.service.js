import axiosClient from '@/api/axiosClient';

export const blogService = {
  // Public
  getPosts(params) {
    return axiosClient.get('/public/blog/posts', { params });
  },
  getPostById(id) {
    return axiosClient.get(`/public/blog/posts/${id}`);
  },

  // Admin
  getAllPosts(params) {
    return axiosClient.get('/admin/blog/posts', { params });
  },
  generatePost(data) {
    return axiosClient.post('/admin/blog/generate', data);
  },
  publishPost(id) {
    return axiosClient.put(`/admin/blog/posts/${id}/publish`);
  },
  deletePost(id) {
    return axiosClient.delete(`/admin/blog/posts/${id}`);
  },
};

export default blogService;
