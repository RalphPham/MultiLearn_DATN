import axiosClient from '@/api/axiosClient'

const RatingService = {
  getByCourse(courseId) {
    return axiosClient.get(`/ratings/course/${courseId}`)
  },

  getAverage(courseId) {
    return axiosClient.get(`/ratings/course/${courseId}/average`)
  },

  getCount(courseId) {
    return axiosClient.get(`/ratings/course/${courseId}/count`)
  },

  getEligibility(courseId) {
    return axiosClient.get(`/ratings/course/${courseId}/eligibility`)
  },

  checkStudentRated(courseId, studentId) {
    return axiosClient.get(`/ratings/course/${courseId}/student/${studentId}/check`)
  },

  create(courseId, payload) {
    return axiosClient.post(`/ratings/course/${courseId}`, payload)
  },

  update(courseId, payload) {
    return axiosClient.put(`/ratings/course/${courseId}`, payload)
  },

  delete(courseId) {
    return axiosClient.delete(`/ratings/course/${courseId}`)
  },

  getInstructorReviews() {
    return axiosClient.get('/ratings/instructor/me')
  },

  getInstructorReviewsById(instructorId) {
    return axiosClient.get(`/ratings/instructor/${instructorId}`)
  }
}

export default RatingService