import axiosClient from '@/api/axiosClient'

export default {
  getStudentOverview() {
    return axiosClient.get('/instructor/performance/student-overview')
  },

  getStudentItems() {
    return axiosClient.get('/instructor/performance/students')
  }
}