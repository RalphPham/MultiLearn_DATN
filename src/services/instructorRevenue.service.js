import axiosClient from '@/api/axiosClient'

export default {
  getRevenueSummary({ year, fromDate, toDate } = {}) {
    return axiosClient.get('/instructor/performance/revenue', {
      params: { year, fromDate, toDate }
    })
  }
}
