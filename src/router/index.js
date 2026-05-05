import { createRouter, createWebHistory } from 'vue-router'
import { openConfirm } from '@/composables/useConfirm'
import notify from '@/utils/notify'
import { getFirstAllowedAdminPath, hasAdminPermission } from '@/utils/adminPermissions'

import DefaultLayout from '@/layouts/DefaultLayout.vue'
import InstructorLayout from '@/layouts/InstructorLayout.vue'
import AdminLayout from '@/views/admin/AdminLayout.vue'

import BecomeInstructor from '@/views/student/BecomeInstructor.vue'
import AdminCourseDetail from '@/views/admin/AdminCourseDetail.vue'

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'homeView', component: () => import('@/views/HomeView.vue'), meta: { title: 'Trang chủ' } },
      { path: 'course', name: 'courseListView', component: () => import('@/views/CourseListView.vue'), meta: { title: 'Danh sách khóa học' } },
      { path: 'blog', name: 'blog', component: () => import('@/views/Blogview.vue'), meta: { title: 'Tin tức và khuyến mãi' } },
      { path: 'blog/post/:id', name: 'blog-detail', component: () => import('@/views/BlogDetailView.vue'), meta: { title: 'Bài viết' } },
      { path: 'login', name: 'login', component: () => import('@/views/login/LoginView.vue'), meta: { title: 'Đăng nhập' } },
      { path: 'register', name: 'register', component: () => import('@/views/login/RegisterView.vue'), meta: { title: 'Đăng ký' } },
      { path: 'forgot-password', name: 'forgotPassword', component: () => import('@/views/login/ForgotPasswordView.vue'), meta: { title: 'Quên mật khẩu' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/student/ProfileView.vue'), meta: { title: 'Hồ sơ', requiresAuth: true } },
      { path: 'my-courses', name: 'myCourses', component: () => import('@/views/student/MyCoursesView.vue'), meta: { title: 'Khóa học của tôi', requiresAuth: true } },
      { path: 'wishlist', name: 'wishlist', component: () => import('@/views/student/WishlistView.vue'), meta: { title: 'Yêu thích', requiresAuth: true } },
      { path: 'messages', name: 'messages', component: () => import('@/views/student/MessagesView.vue'), meta: { title: 'Tin nhắn', requiresAuth: true } },
      { path: 'cart', name: 'cart', component: () => import('@/views/cart/CartView.vue'), meta: { title: 'Giỏ hàng' } },
      { path: 'checkout', name: 'checkout', component: () => import('@/views/payment/CheckoutView.vue'), meta: { title: 'Thanh toán', requiresAuth: true } },
      { path: 'payment-result', name: 'payment-result', component: () => import('@/views/payment/PaymentResult.vue'), meta: { title: 'Kết quả thanh toán' } },
      { path: 'transactions', name: 'transactions', component: () => import('@/views/payment/TransactionHistoryView.vue'), meta: { title: 'Lịch sử giao dịch', requiresAuth: true } },
      { path: 'achievements', name: 'achievements', component: () => import('@/views/student/AchievementsView.vue'), meta: { title: 'Thành tích', requiresAuth: true } },
      { path: 'certificates', name: 'certificates', component: () => import('@/views/student/CertificatesView.vue'), meta: { title: 'Bộ sưu tập chứng chỉ', requiresAuth: true } },
      { path: 'course/slug/:slug', name: 'courseDetailBySlug', component: () => import('@/views/CourseDetailView.vue'), meta: { title: 'Chi tiết khóa học' } },
      { path: 'course/:slug', name: 'courseDetailView', component: () => import('@/views/CourseDetailView.vue'), meta: { title: 'Chi tiết khóa học' } },
      { path: 'instructor/:id', name: 'instructorPublicProfile', component: () => import('@/views/instructor/public/InstructorPublicProfile.vue'), meta: { title: 'Hồ sơ giảng viên' } }
    ]
  },

  {
    path: '/learning/course/:id',
    name: 'learningView',
    component: () => import('@/views/learning/LearningView.vue'),
    meta: { title: 'Góc học tập', requiresAuth: true }
  },

  {
    path: '/become-instructor',
    name: 'become-instructor',
    component: BecomeInstructor,
    meta: { title: 'Trở thành giảng viên', requiresAuth: true }
  },

  {
    path: '/instructor',
    component: InstructorLayout,
    meta: { requiresAuth: true, role: 'INSTRUCTOR' },
    children: [
      { path: '', redirect: '/instructor/courses' },
      { path: 'courses', name: 'instructor-dashboard', component: () => import('@/views/InstructorStudio/InstructorDashboard.vue'), meta: { title: 'Quản lý nội dung' } },
      { path: 'courses/create', name: 'instructor-create-course', component: () => import('@/views/InstructorStudio/CreateCourse.vue') },
      { path: 'courses/:id/manage', redirect: to => `/instructor/course/${to.params.id}/manage` },
      { path: 'course/:id/manage', name: 'instructor-edit-course', component: () => import('@/views/InstructorStudio/CourseEditorView.vue') },
      { path: 'bundle/create', name: 'instructor-create-bundle', component: () => import('@/views/InstructorStudio/CourseBundleEditor.vue') },
      { path: 'bundle/:id/manage', name: 'instructor-edit-bundle', component: () => import('@/views/InstructorStudio/CourseBundleEditor.vue') },
      { path: 'performance', redirect: '/instructor/performance/overview', children: [{ path: ':tab', component: () => import('@/views/InstructorStudio/PerformanceView.vue') }] },
      {
        path: 'communication',
        component: () => import('@/views/InstructorStudio/CommunicationView.vue'),
        children: [
          { path: '', redirect: '/instructor/communication/qa' },
          { path: 'qa', name: 'instructor-qa', component: () => import('@/views/instructor/communication/QAView.vue'), meta: { title: 'Hỏi đáp (Q&A)' } },
          { path: 'messages', name: 'instructor-messages', component: () => import('@/views/instructor/communication/MessagesView.vue'), meta: { title: 'Tin nhắn' } },
          { path: 'assignments', name: 'instructor-assignments', component: () => import('@/views/instructor/communication/AssignmentsView.vue'), meta: { title: 'Bài tập' } },
          { path: 'announcements', name: 'instructor-announcements', component: () => import('@/views/instructor/communication/AnnouncementsView.vue'), meta: { title: 'Thông báo' } }
        ]
      },
      { path: 'resources', name: 'instructor-resources', component: () => import('@/views/InstructorStudio/ResourcesView.vue'), meta: { title: 'Tài nguyên' } },
      { path: 'tools', name: 'instructor-tools', component: () => import('@/views/InstructorStudio/ToolsView.vue'), meta: { title: 'Công cụ' } },
      { path: 'profile-settings', name: 'instructor-profile-settings', component: () => import('@/views/InstructorStudio/InstructorProfileSettings.vue'), meta: { title: 'Hồ sơ giảng viên' } },
      { path: 'withdraw', name: 'instructor-withdraw', component: () => import('@/views/InstructorStudio/WithdrawView.vue'), meta: { title: 'Rút tiền' } },
      { path: 'learning-preview/:id', name: 'course-learning-preview', component: () => import('@/views/InstructorStudio/CourseLearningView.vue'), meta: { title: 'Xem trước bài học' } }
    ]
  },

  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { adminPermission: 'DASHBOARD_VIEW' } },
      { path: 'courses', name: 'admin-courses', component: () => import('@/views/admin/CourseApproval.vue'), meta: { adminPermission: 'COURSE_VIEW' } },
      { path: 'courses/:id', name: 'admin-course-detail', component: AdminCourseDetail, meta: { title: 'Chi tiết duyệt khóa học', adminPermission: 'COURSE_VIEW' } },
      { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UserManagement.vue'), meta: { adminPermission: 'USER_VIEW' } },
      { path: 'orders', name: 'admin-orders', component: () => import('@/views/admin/OrderManagement.vue'), meta: { adminPermission: 'ORDER_VIEW' } },
      { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManagement.vue'), meta: { adminPermission: 'CATEGORY_VIEW' } },
      { path: 'vouchers', name: 'admin-vouchers', component: () => import('@/views/admin/VoucherManagement.vue'), meta: { adminPermission: 'VOUCHER_VIEW' } },
      { path: 'flash-sale', name: 'admin-flash-sale', component: () => import('@/views/admin/AdminFlashSale.vue'), meta: { title: 'Flash Sale', adminPermission: 'COURSE_BATCH_SALE' } },
      { path: 'campaigns', name: 'admin-campaigns', component: () => import('@/views/admin/AdminCampaign.vue'), meta: { adminPermission: 'CAMPAIGN_VIEW' } },
      { path: 'stats', name: 'admin-stats', component: () => import('@/views/admin/SystemStats.vue'), meta: { adminPermission: 'STATS_VIEW' } },
      { path: 'blog', name: 'admin-blog', component: () => import('@/views/admin/AdminBlogView.vue'), meta: { title: 'Quản lý blog', adminPermission: 'BLOG_VIEW' } },
      { path: 'tickets', name: 'admin-tickets', component: () => import('@/views/admin/TicketManagement.vue'), meta: { title: 'Quản lý khiếu nại', adminPermission: 'TICKET_VIEW' } },
      { path: 'audit-logs', name: 'admin-audit-logs', component: () => import('@/views/admin/AuditLogView.vue'), meta: { title: 'Nhật ký hoạt động', adminPermission: 'AUDIT_VIEW' } },
      { path: 'refunds', name: 'admin-refunds', component: () => import('@/views/admin/RefundManagement.vue'), meta: { title: 'Quản lý hoàn tiền', adminPermission: 'REFUND_VIEW' } },
      { path: 'accounts', name: 'admin-accounts', component: () => import('@/views/admin/AdminAccountsView.vue'), meta: { title: 'Quản lý admin' } },
      { path: 'instructors', name: 'admin-instructors', component: () => import('@/views/admin/AdminInstructorListView.vue'), meta: { title: 'Hồ sơ giảng viên', adminPermission: 'INSTRUCTOR_PROFILE_VIEW' } },
      { path: 'instructors/:userId/profile', name: 'admin-instructor-profile', component: () => import('@/views/admin/InstructorProfileView.vue'), meta: { title: 'Hồ sơ giảng viên', adminPermission: 'INSTRUCTOR_PROFILE_VIEW' } },
      { path: 'withdrawals', name: 'admin-withdrawals', component: () => import('@/views/admin/WithdrawManagement.vue'), meta: { title: 'Yêu cầu rút tiền', adminPermission: 'WITHDRAW_VIEW' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - MultiLearn` : 'MultiLearn'

  const token = localStorage.getItem('access_token')
  const userRole = localStorage.getItem('user_role')

  if ((to.meta.requiresAuth || to.meta.role) && !token) {
    sessionStorage.setItem('redirect_url', to.fullPath)
    const ok = await openConfirm({
      title: 'Yêu cầu đăng nhập',
      message: 'Bạn cần đăng nhập để tiếp tục. Chuyển tới trang đăng nhập?',
      confirmText: 'Đi đến đăng nhập',
      cancelText: 'Ở lại',
      variant: 'primary'
    })
    return ok ? '/login' : false
  }

  if (to.meta.role && token) {
    if (to.meta.role === 'ADMIN' && userRole !== 'ADMIN') {
      notify.error('Bạn không có quyền truy cập trang này.')
      return '/home'
    }
    if (to.meta.role === 'INSTRUCTOR' && userRole !== 'INSTRUCTOR' && userRole !== 'ADMIN') {
      notify.error('Bạn không có quyền truy cập trang này.')
      return '/home'
    }
  }

  if (userRole === 'ADMIN' && to.path.startsWith('/admin')) {
    if (to.meta?.adminPermission && !hasAdminPermission(to.meta.adminPermission)) {
      notify.error('Bạn không có quyền truy cập chức năng này.')
      const fallback = getFirstAllowedAdminPath()
      return fallback === to.path ? '/home' : fallback
    }

    if ((to.path === '/admin' || to.path === '/admin/dashboard') && !hasAdminPermission('DASHBOARD_VIEW')) {
      const fallback = getFirstAllowedAdminPath()
      return fallback === to.path ? '/home' : fallback
    }
  }

  if (to.path === '/admin/accounts' && token && userRole === 'ADMIN') {
    const adminRole = localStorage.getItem('admin_role') ?? 'STAFF'
    if (adminRole !== 'SUPER_ADMIN') {
      notify.error('Chỉ Super Admin mới có quyền truy cập trang này.')
      return '/admin/dashboard'
    }
  }

  if (to.path === '/become-instructor') {
    if (userRole === 'INSTRUCTOR') return '/instructor/courses'
    if (userRole === 'ADMIN') return '/admin/dashboard'
  }

  return true
})

export default router
