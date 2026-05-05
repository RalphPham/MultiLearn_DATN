// // src/data/courses.js
// // File data chung để tất cả các trang sử dụng

// import khoahoc1 from '@/assets/image/khoahoc1.webp'
// import khoahoc2 from '@/assets/image/khoahoc2.jpg'
// import khoahoc3 from '@/assets/image/khoahoc3.jpg'
// import khoahoc4 from '@/assets/image/khoahoc4.png'
// import khoahoc5 from '@/assets/image/khoahoc5.png'
// import khoahoc6 from '@/assets/image/khoahoc6.jpg'

// export const COURSES = [
//   {
//     id: 1,
//     title: 'Java Spring Boot từ A-Z',
//     shortDescription: 'Khóa học Java Spring Boot toàn diện từ cơ bản đến nâng cao',
//     instructor: 'Nguyễn Phú Thái',
//     price: 1299000,
//     originalPrice: 2499000,
//     rating: 4.8,
//     reviewCount: 1234,
//     studentCount: 12450,
//     category: 'CNTT',
//     image: khoahoc1,
//     thumbnail: khoahoc1,
    
//     learningPoints: [
//       'Nắm vững Java Core và OOP',
//       'Xây dựng RESTful API với Spring Boot',
//       'Làm việc với JPA/Hibernate',
//       'Security với Spring Security & JWT',
//       'Triển khai ứng dụng lên server',
//       'Microservices architecture',
//       'Testing với JUnit và Mockito',
//       'Docker và CI/CD'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: Giới thiệu Java và Spring Boot',
//         lessons: [
//           { title: 'Giới thiệu về Java và Spring Boot', duration: '05:00', preview: true },
//           { title: 'Cài đặt môi trường phát triển', duration: '10:00', preview: true },
//           { title: 'Project đầu tiên với Spring Boot', duration: '15:00', preview: false }
//         ]
//       },
//       {
//         title: 'Chương 2: Spring Boot Core',
//         lessons: [
//           { title: 'Dependency Injection và IoC', duration: '20:00', preview: false },
//           { title: 'Spring Boot Configuration', duration: '18:00', preview: false },
//           { title: 'RESTful API cơ bản', duration: '25:00', preview: false }
//         ]
//       },
//       {
//         title: 'Chương 3: Database và JPA',
//         lessons: [
//           { title: 'Kết nối Database', duration: '15:00', preview: false },
//           { title: 'JPA và Hibernate', duration: '30:00', preview: false },
//           { title: 'CRUD Operations', duration: '25:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'Kiến thức Java cơ bản',
//       'Hiểu về OOP',
//       'Máy tính cài đặt IntelliJ IDEA hoặc Eclipse',
//       'Đam mê học hỏi'
//     ],
    
//     description: `
//       <p><strong>Khóa học Spring Boot toàn diện nhất cho người mới bắt đầu!</strong></p>
//       <p>Trong khóa học này, bạn sẽ học cách xây dựng ứng dụng web hiện đại với Spring Boot từ A-Z.</p>
//       <ul>
//         <li>Xây dựng RESTful API chuyên nghiệp</li>
//         <li>Tích hợp Database với JPA/Hibernate</li>
//         <li>Bảo mật ứng dụng với Spring Security</li>
//         <li>Triển khai lên AWS, Heroku</li>
//       </ul>
//     `,
    
//     reviews: [
//       {
//         id: 1,
//         userName: 'Trần Văn A',
//         rating: 5,
//         date: '2 tuần trước',
//         comment: 'Khóa học rất chi tiết, giảng viên giải thích dễ hiểu. Rất phù hợp cho người mới bắt đầu.'
//       },
//       {
//         id: 2,
//         userName: 'Nguyễn Thị B',
//         rating: 5,
//         date: '1 tháng trước',
//         comment: 'Nội dung chất lượng cao, project thực tế. Sau khóa học mình đã apply được vào vị trí Backend Developer.'
//       }
//     ]
//   },
//   {
//     id: 2,
//     title: 'Vue.js 3 & Composition API',
//     shortDescription: 'Làm chủ Vue.js 3 với Composition API và xây dựng ứng dụng SPA hiện đại',
//     instructor: 'Nguyễn Phú Thái',
//     price: 999000,
//     originalPrice: 1999000,
//     rating: 4.9,
//     reviewCount: 856,
//     studentCount: 8320,
//     category: 'CNTT',
//     image: khoahoc2,
//     thumbnail: khoahoc2,
    
//     learningPoints: [
//       'Nắm vững Vue.js 3 Composition API',
//       'Xây dựng SPA với Vue Router',
//       'Quản lý state với Pinia',
//       'Tích hợp API với Axios',
//       'Form validation và error handling',
//       'Authentication và Authorization',
//       'Deployment và optimization',
//       'Best practices và coding standards'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: Vue.js Fundamentals',
//         lessons: [
//           { title: 'Giới thiệu Vue.js 3', duration: '08:00', preview: true },
//           { title: 'Reactive và Ref', duration: '15:00', preview: true },
//           { title: 'Components và Props', duration: '20:00', preview: false }
//         ]
//       },
//       {
//         title: 'Chương 2: Composition API',
//         lessons: [
//           { title: 'Setup và Lifecycle Hooks', duration: '18:00', preview: false },
//           { title: 'Computed và Watch', duration: '20:00', preview: false },
//           { title: 'Composables và Reusability', duration: '25:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'HTML, CSS, JavaScript cơ bản',
//       'Hiểu về ES6+',
//       'Node.js và npm đã cài đặt',
//       'Text editor (VS Code khuyến nghị)'
//     ],
    
//     description: `
//       <p><strong>Khóa học Vue.js 3 từ cơ bản đến nâng cao!</strong></p>
//       <p>Học cách xây dựng ứng dụng web hiện đại với Vue.js 3 và Composition API.</p>
//     `,
    
//     reviews: [
//       {
//         id: 1,
//         userName: 'Lê Văn C',
//         rating: 5,
//         date: '1 tuần trước',
//         comment: 'Khóa học tuyệt vời! Giảng viên nhiệt tình, nội dung cập nhật.'
//       }
//     ]
//   },
//   {
//     id: 3,
//     title: 'React Native - Xây dựng App',
//     shortDescription: 'Xây dựng ứng dụng di động đa nền tảng với React Native',
//     instructor: 'Nguyễn Phú Thái',
//     price: 1499000,
//     originalPrice: 2999000,
//     rating: 4.7,
//     reviewCount: 678,
//     studentCount: 6780,
//     category: 'CNTT',
//     image: khoahoc3,
//     thumbnail: khoahoc3,
    
//     learningPoints: [
//       'Xây dựng app iOS và Android',
//       'React Native Components',
//       'Navigation và Routing',
//       'State Management với Redux',
//       'API Integration',
//       'Push Notifications',
//       'Publishing lên App Store',
//       'Performance Optimization'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: React Native Basics',
//         lessons: [
//           { title: 'Giới thiệu React Native', duration: '10:00', preview: true },
//           { title: 'Setup Environment', duration: '15:00', preview: true },
//           { title: 'First App', duration: '20:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'JavaScript và React cơ bản',
//       'Node.js đã cài đặt',
//       'Xcode (Mac) hoặc Android Studio',
//       'Kiên nhẫn và đam mê'
//     ],
    
//     description: `
//       <p><strong>Trở thành Mobile Developer với React Native!</strong></p>
//       <p>Xây dựng app đa nền tảng với một code base duy nhất.</p>
//     `,
    
//     reviews: [
//       {
//         id: 1,
//         userName: 'Phạm Thị D',
//         rating: 5,
//         date: '2 tuần trước',
//         comment: 'Khóa học thực chiến cao, làm được app thật từ đầu đến cuối.'
//       }
//     ]
//   },
//   {
//     id: 4,
//     title: 'Node.js & Express API',
//     shortDescription: 'Xây dựng RESTful API mạnh mẽ với Node.js và Express',
//     instructor: 'Nguyễn Phú Thái',
//     price: 899000,
//     originalPrice: 1799000,
//     rating: 4.6,
//     reviewCount: 523,
//     studentCount: 7230,
//     category: 'CNTT',
//     image: khoahoc4,
//     thumbnail: khoahoc4,
    
//     learningPoints: [
//       'Node.js fundamentals',
//       'Express.js framework',
//       'RESTful API design',
//       'MongoDB và Mongoose',
//       'Authentication với JWT',
//       'Error handling',
//       'API testing',
//       'Deployment strategies'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: Node.js Basics',
//         lessons: [
//           { title: 'Giới thiệu Node.js', duration: '12:00', preview: true },
//           { title: 'NPM và Package Management', duration: '15:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'JavaScript cơ bản',
//       'Node.js installed',
//       'Hiểu về HTTP/HTTPS',
//       'Database basics'
//     ],
    
//     description: `
//       <p><strong>Master Backend Development với Node.js!</strong></p>
//       <p>Xây dựng API chuyên nghiệp, scalable và secure.</p>
//     `,
    
//     reviews: []
//   },
//   {
//     id: 5,
//     title: 'AWS Cloud Architect',
//     shortDescription: 'Trở thành AWS Solution Architect với kiến thức toàn diện',
//     instructor: 'Nguyễn Phú Thái',
//     price: 1799000,
//     originalPrice: 3499000,
//     rating: 4.9,
//     reviewCount: 789,
//     studentCount: 5670,
//     category: 'Design',
//     image: khoahoc5,
//     thumbnail: khoahoc5,
    
//     learningPoints: [
//       'AWS Core Services',
//       'EC2 và Load Balancing',
//       'S3 và Storage Solutions',
//       'RDS và Database Services',
//       'Lambda và Serverless',
//       'VPC và Networking',
//       'Security Best Practices',
//       'Cost Optimization'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: AWS Fundamentals',
//         lessons: [
//           { title: 'Giới thiệu AWS', duration: '10:00', preview: true },
//           { title: 'IAM và Security', duration: '20:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'Kiến thức Linux cơ bản',
//       'Networking basics',
//       'AWS account (free tier)',
//       'Tài khoản tín dụng quốc tế'
//     ],
    
//     description: `
//       <p><strong>Chuẩn bị cho AWS Solution Architect Certification!</strong></p>
//       <p>Học cách thiết kế và triển khai hệ thống trên AWS.</p>
//     `,
    
//     reviews: []
//   },
//   {
//     id: 6,
//     title: 'Docker & Kubernetes',
//     shortDescription: 'Container orchestration và DevOps với Docker và K8s',
//     instructor: 'Nguyễn Phú Thái',
//     price: 1399000,
//     originalPrice: 2699000,
//     rating: 4.7,
//     reviewCount: 445,
//     studentCount: 4890,
//     category: 'Marketing',
//     image: khoahoc6,
//     thumbnail: khoahoc6,
    
//     learningPoints: [
//       'Docker fundamentals',
//       'Container best practices',
//       'Kubernetes architecture',
//       'Deploy applications',
//       'Service mesh',
//       'Monitoring và Logging',
//       'CI/CD with K8s',
//       'Production strategies'
//     ],
    
//     curriculum: [
//       {
//         title: 'Chương 1: Docker Basics',
//         lessons: [
//           { title: 'Giới thiệu Docker', duration: '15:00', preview: true },
//           { title: 'Dockerfile và Images', duration: '25:00', preview: false }
//         ]
//       }
//     ],
    
//     requirements: [
//       'Linux command line',
//       'Docker installed',
//       'Basic networking',
//       'Minikube hoặc K8s cluster'
//     ],
    
//     description: `
//       <p><strong>Master Container Orchestration!</strong></p>
//       <p>Từ Docker cơ bản đến Kubernetes production-ready.</p>
//     `,
    
//     reviews: []
//   }
// ];

// // Helper function: Lấy khóa học theo ID
// export const getCourseById = (id) => {
//   return COURSES.find(course => course.id === parseInt(id));
// };

// // Helper function: Lấy khóa học theo category
// export const getCoursesByCategory = (category) => {
//   if (!category || category === 'all') return COURSES;
//   return COURSES.filter(course => course.category === category);
// };

// // Helper function: Filter và sort khóa học
// export const filterCourses = (filters) => {
//   let filtered = [...COURSES];
  
//   // Filter by categories
//   if (filters.categories && filters.categories.length > 0) {
//     filtered = filtered.filter(course => 
//       filters.categories.includes(course.category)
//     );
//   }
  
//   // Filter by price
//   if (filters.priceType === 'free') {
//     filtered = filtered.filter(course => course.price === 0);
//   } else if (filters.priceType === 'paid') {
//     filtered = filtered.filter(course => course.price > 0);
//   }
  
//   // Filter by rating
//   if (filters.rating === '4') {
//     filtered = filtered.filter(course => course.rating >= 4);
//   } else if (filters.rating === '3') {
//     filtered = filtered.filter(course => course.rating >= 3);
//   }
  
//   return filtered;
// };