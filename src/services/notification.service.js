import axiosClient from "@/api/axiosClient";

export const notificationService = {
  getRecent() {
    return axiosClient.get("/notifications/my-recent");
  },

  getUnreadCount() {
    return axiosClient.get("/notifications/unread-count");
  },

  markAsRead(id) {
    return axiosClient.put(`/notifications/${id}/read`);
  },

  markAllAsRead() {
    return axiosClient.put("/notifications/mark-all-read");
  },

  search(params) {
    return axiosClient.get("/notifications/search", { params });
  },

  getAll(params) {
    return axiosClient.get("/notifications/my-all", { params });
  },
};

export default notificationService;