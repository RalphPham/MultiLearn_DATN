<template>
  <div class="profile-page">
    <!-- Back -->
    <button class="btn-back" @click="$router.back()">
      ← Quay lại
    </button>

    <div v-if="loading" class="loading-center">
      <div class="spinner"></div>
    </div>

    <template v-else-if="profile">
      <!-- Header card -->
      <div class="hero-card">
        <img :src="profile.avatarUrl || '/default-avatar.png'" class="hero-avatar" alt="avatar" />
        <div class="hero-info">
          <h1 class="hero-name">{{ profile.fullName }}</h1>
          <p class="hero-email">{{ profile.email }}</p>
          <p v-if="profile.phone" class="hero-phone">📞 {{ profile.phone }}</p>
          <span :class="['status-badge', profile.isActive ? 'active' : 'blocked']">
            {{ profile.isActive ? 'Đang hoạt động' : 'Bị khóa' }}
          </span>
        </div>

        <!-- Stats -->
        <div class="hero-stats">
          <div class="stat-box">
            <span class="stat-num">{{ profile.totalCourses }}</span>
            <span class="stat-lbl">Khóa học</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ profile.totalStudents }}</span>
            <span class="stat-lbl">Học viên</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ formatCurrency(profile.totalRevenue) }}</span>
            <span class="stat-lbl">Doanh thu</span>
          </div>
          <div class="stat-box">
            <span class="stat-num">{{ formatCurrency(profile.walletBalance) }}</span>
            <span class="stat-lbl">Số dư ví</span>
          </div>
        </div>
      </div>

      <!-- Details -->
      <div class="detail-grid">
        <!-- Bio -->
        <div class="detail-card" v-if="profile.bio">
          <h2 class="card-title">Giới thiệu</h2>
          <p class="bio-text">{{ profile.bio }}</p>
        </div>

        <!-- Bank info -->
        <div class="detail-card">
          <h2 class="card-title">Thông tin ngân hàng</h2>
          <div class="info-row">
            <span class="info-lbl">Ngân hàng</span>
            <span class="info-val">{{ profile.bankName || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-lbl">Số tài khoản</span>
            <span class="info-val">{{ profile.bankAccount || '—' }}</span>
          </div>
        </div>

        <!-- System info -->
        <div class="detail-card">
          <h2 class="card-title">Thông tin hệ thống</h2>
          <div class="info-row">
            <span class="info-lbl">ID giảng viên</span>
            <span class="info-val">#{{ profile.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-lbl">Ngày tham gia</span>
            <span class="info-val">{{ formatDate(profile.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Courses table -->
      <div class="courses-card">
        <h2 class="card-title">Danh sách khóa học ({{ courses.length }})</h2>
        <table class="course-table">
          <thead>
            <tr>
              <th>Khóa học</th>
              <th>Trạng thái</th>
              <th>Giá</th>
              <th>Học viên</th>
              <th>Ngày tạo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="courses.length === 0"><td colspan="5" class="empty">Chưa có khóa học nào</td></tr>
            <tr v-for="c in courses" :key="c.id">
              <td class="course-title-cell">
                <img v-if="c.thumbnail" :src="c.thumbnail" class="course-thumb" />
                <router-link :to="`/admin/courses/${c.id}`" class="course-link">{{ c.title }}</router-link>
              </td>
              <td><span :class="['status-sm', statusClass(c.status)]">{{ c.status }}</span></td>
              <td>{{ c.salePrice ? formatCurrency(c.salePrice) : formatCurrency(c.price) }}</td>
              <td>{{ c.studentCount ?? 0 }}</td>
              <td>{{ formatDate(c.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <div v-else class="not-found">Không tìm thấy thông tin giảng viên.</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const route   = useRoute();
const userId  = route.params.userId;
const loading = ref(true);
const profile = ref(null);
const courses = ref([]);

async function fetchProfile() {
  try {
    profile.value = await axiosClient.get(`/admin/instructors/${userId}/profile`);
  } catch (e) {
    console.error('Lỗi tải profile giảng viên:', e);
  }
}

async function fetchCourses() {
  try {
    const res = await axiosClient.get(`/admin/users/${userId}/courses`);
    courses.value = Array.isArray(res) ? res : (res.content ?? []);
  } catch (e) {
    console.error('Lỗi tải khóa học:', e);
  }
}

onMounted(async () => {
  await Promise.all([fetchProfile(), fetchCourses()]);
  loading.value = false;
});

function formatCurrency(val) {
  if (!val && val !== 0) return '—';
  return Number(val).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('vi-VN');
}

function statusClass(s) {
  const m = { PUBLISHED: 'st-green', DRAFT: 'st-gray', PENDING_APPROVAL: 'st-yellow', PENDING: 'st-yellow', REJECTED: 'st-red', INACTIVE: 'st-gray' };
  return m[s] ?? '';
}
</script>

<style scoped>
.profile-page { padding: 28px 32px; max-width: 1100px; }
.btn-back {
  background: none; border: 1px solid #e2e8f0; border-radius: 8px;
  padding: 8px 16px; cursor: pointer; color: #475569; font-size: 13px; margin-bottom: 20px;
}
.btn-back:hover { background: #f1f5f9; }

/* Hero */
.hero-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 16px;
  padding: 28px; display: flex; gap: 24px; align-items: flex-start;
  margin-bottom: 24px; flex-wrap: wrap;
}
.hero-avatar {
  width: 90px; height: 90px; border-radius: 50%; object-fit: cover;
  border: 3px solid #e2e8f0; flex-shrink: 0;
}
.hero-info { flex: 1; min-width: 180px; }
.hero-name  { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.hero-email { font-size: 13px; color: #64748b; margin: 0 0 4px; }
.hero-phone { font-size: 13px; color: #64748b; margin: 0 0 8px; }
.status-badge {
  display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.status-badge.active  { background: #dcfce7; color: #16a34a; }
.status-badge.blocked { background: #fee2e2; color: #dc2626; }

.hero-stats { display: flex; gap: 16px; flex-wrap: wrap; margin-left: auto; }
.stat-box {
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;
  padding: 14px 20px; text-align: center; min-width: 110px;
}
.stat-num { display: block; font-size: 18px; font-weight: 700; color: #6366f1; }
.stat-lbl { display: block; font-size: 11px; color: #64748b; margin-top: 2px; }

/* Detail grid */
.detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px; }
.detail-card, .courses-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px;
}
.card-title { font-size: 15px; font-weight: 600; color: #1e293b; margin: 0 0 16px; }
.bio-text   { font-size: 13.5px; color: #475569; line-height: 1.6; }
.info-row   { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; font-size: 13.5px; }
.info-row:last-child { border-bottom: none; }
.info-lbl { color: #64748b; }
.info-val { font-weight: 500; color: #1e293b; }

/* Courses table */
.courses-card { margin-bottom: 24px; }
.course-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.course-table th {
  padding: 10px 12px; background: #f8fafc; text-align: left;
  font-size: 12px; font-weight: 600; color: #64748b; border-bottom: 1px solid #e2e8f0;
}
.course-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.course-title-cell { display: flex; align-items: center; gap: 10px; }
.course-thumb { width: 48px; height: 32px; object-fit: cover; border-radius: 6px; }
.course-link { color: #6366f1; text-decoration: none; font-weight: 500; }
.course-link:hover { text-decoration: underline; }

.status-sm { padding: 2px 8px; border-radius: 6px; font-size: 11.5px; font-weight: 600; }
.st-green  { background: #dcfce7; color: #16a34a; }
.st-gray   { background: #f1f5f9; color: #64748b; }
.st-yellow { background: #fef9c3; color: #a16207; }
.st-red    { background: #fee2e2; color: #dc2626; }
.empty     { text-align: center; padding: 32px; color: #94a3b8; }

.loading-center { display: flex; justify-content: center; padding: 80px; }
.spinner {
  width: 32px; height: 32px; border: 3px solid #e2e8f0;
  border-top-color: #6366f1; border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.not-found { text-align: center; padding: 80px; color: #94a3b8; font-size: 15px; }
</style>
