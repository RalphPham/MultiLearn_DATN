import axiosClient from '@/api/axiosClient'

export default {
  getWalletBalance() {
    return axiosClient.get('/instructor/withdraw/wallet-balance')
  },

  getProfile() {
    return axiosClient.get('/instructor/profile')
  },

  createRequest(payload) {
    return axiosClient.post('/instructor/withdraw', payload)
  },

  getMyHistory() {
    return axiosClient.get('/instructor/withdraw/history')
  },

  adminGetAll(status = '', page = 0, size = 10) {
    return axiosClient.get('/admin/withdraw', {
      params: {
        status: status || undefined,
        page,
        size
      }
    })
  },

  adminProcess(id, action, note = '') {
    return axiosClient.put(`/admin/withdraw/${id}`, { action, note })
  },

  adminGetPendingCount() {
    return axiosClient.get('/admin/withdraw/pending-count')
  }
}

