import axios from 'axios'; // Hoặc axiosClient của bạn

const API_URL = '/api/courses';

class LearningService {
  
  // 1. Lấy toàn bộ nội dung khóa học để vào học
  getCourseContent(slug) {
    // Hiện tại chưa có API thì return Mock Data (giả vờ như API thật)
    // Sau này có API thì bỏ comment dòng dưới:
    // return axios.get(`${API_URL}/${slug}/full-content`);
    
    console.log("Đang gọi API giả lập lấy khóa học:", slug);
    return Promise.resolve(null); // Trả về null để component tự dùng mock data
  }

  // 2. Cập nhật tiến độ học
  updateProgress(lessonId, progressPercent) {
    console.log(`[API Fake] Đang lưu tiến độ bài ${lessonId}: ${progressPercent}%`);
    // return axios.post('/api/progress', { lessonId, progressPercent });
  }

  // 3. Nộp bài Quiz
  submitQuiz(lessonId, answers) {
    console.log(`[API Fake] Nộp bài Quiz ${lessonId}`, answers);
    // return axios.post('/api/quiz/submit', { lessonId, answers });
  }
}

export default new LearningService();