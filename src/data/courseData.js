export const mockCourseData = {
    id: 101,
    title: "Java Spring Boot: Từ Zero đến Hero (Full Stack)",
    sections: [
      {
        id: 1,
        title: "Chương 1: Khởi động dự án",
        isOpen: true,
        lessons: [
          {
            id: 10,
            title: "Giới thiệu khóa học & Lộ trình",
            type: "VIDEO",
            duration: 596,
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            isCompleted: true
          },
          {
            id: 11,
            title: "Cài đặt JDK 17 và IntelliJ IDEA",
            type: "VIDEO",
            duration: 653,
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            isCompleted: false
          }
        ]
      },
      {
        id: 2,
        title: "Chương 2: Cơ sở dữ liệu & JPA",
        isOpen: false,
        lessons: [
          {
            id: 21,
            title: "Kết nối MySQL Database",
            type: "VIDEO",
            duration: 100,
            videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            isCompleted: false
          },
          {
            id: 22,
            title: "Bài trắc nghiệm kiến thức SQL",
            type: "QUIZ",
            isCompleted: false,
            // Dữ liệu câu hỏi trắc nghiệm
            questions: [
              {
                id: 1,
                text: "Câu lệnh SQL nào dùng để lấy dữ liệu từ bảng?",
                options: ["UPDATE", "SELECT", "DELETE", "INSERT"],
                correctAnswer: 1 // Index của mảng options (1 là SELECT)
              },
              {
                id: 2,
                text: "JPA là viết tắt của từ gì?",
                options: [
                  "Java Persistence API",
                  "Java Programming Application",
                  "Java Performance Action",
                  "Just Print Anything"
                ],
                correctAnswer: 0
              },
              {
                id: 3,
                text: "Trong Spring Boot, file cấu hình chính thường là?",
                options: ["pom.xml", "application.properties", "index.html", "config.json"],
                correctAnswer: 1
              }
            ]
          }
        ]
      }
    ]
  };