<template>
  <div class="instructor-list-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Hồ Sơ Giảng Viên</h1>
        <p class="page-sub">Danh sách giảng viên, thông tin ngân hàng và số dư ví</p>
      </div>
    </div>

    <!-- Search -->
    <div class="toolbar">
      <div class="search-wrap">
        <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
        </svg>
        <input v-model="searchQ" @input="onSearch" placeholder="Tìm tên, email giảng viên..." class="search-input" />
      </div>
    </div>

    <!-- Table -->
    <div class="table-card">
      <div v-if="loading" class="loading-center">
        <div class="spinner"></div>
      </div>

      <template v-else>
        <table class="data-table">
          <thead>
            <tr>
              <th>Giảng Viên</th>
              <th>Liên Hệ</th>
              <th>Ngân Hàng</th>
              <th>Số Dư Ví</th>
              <th>Khóa Học</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ins in instructors" :key="ins.id">
              <td>
                <div class="instructor-cell">
                  <img :src="ins.avatarUrl || '/default-avatar.png'" class="avatar" alt="" />
                  <div>
                    <div class="name">{{ ins.fullName }}</div>
                    <div class="email-sub">{{ ins.email }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="text-sm">{{ ins.phone || '—' }}</div>
              </td>
              <td>
                <div v-if="ins.bankName || ins.bankAccount" class="bank-info">
                  <div class="bank-name">{{ ins.bankName || '—' }}</div>
                  <div class="bank-account">{{ ins.bankAccount || '—' }}</div>
                </div>
                <span v-else class="no-bank">Chưa cập nhật</span>
              </td>
              <td>
                <span class="balance" :class="{ 'balance--positive': (ins.walletBalance || 0) > 0 }">
                  {{ formatCurrency(ins.walletBalance) }}
                </span>
              </td>
              <td>
                <span class="course-count">{{ ins.totalCourses ?? 0 }}</span>
              </td>
              <td>
                <span :class="['status-badge', ins.isActive ? 'status--active' : 'status--locked']">
                  {{ ins.isActive ? 'Hoạt động' : 'Bị khóa' }}
                </span>
              </td>
              <td>
                <button class="btn-detail" @click="goToProfile(ins.userId)">
                  Xem chi tiết
                </button>
              </td>
            </tr>
            <tr v-if="instructors.length === 0">
              <td colspan="7" class="empty">Không tìm thấy giảng viên nào.</td>
            </tr>
          </tbody>
        </table>

        <!-- Pagination -->
        <div class="pagination" v-if="totalPages > 1">
          <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)" class="page-btn">←</button>
          <span class="page-info">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
          <button :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)" class="page-btn">→</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'

const router = useRouter()
const loading = ref(false)
const instructors = ref([])
const currentPage = ref(0)
const totalPages = ref(0)
const searchQ = ref('')
let searchTimer = null

async function fetchInstructors() {
  loading.value = true
  try {
    const res = await axiosClient.get('/admin/instructors', {
      params: { q: searchQ.value || undefined, page: currentPage.value, size: 15 }
    })
    const data = res?.data ?? res
    instructors.value = data.content ?? []
    totalPages.value = data.totalPages ?? 0
  } catch {
    instructors.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 0
    fetchInstructors()
  }, 400)
}

function changePage(p) {
  currentPage.value = p
  fetchInstructors()
}

function goToProfile(userId) {
  router.push(`/admin/instructors/${userId}/profile`)
}

function formatCurrency(val) {
  if (!val && val !== 0) return '0đ'
  return Number(val).toLocaleString('vi-VN') + 'đ'
}

onMounted(fetchInstructors)
</script>

<style scoped>
.instructor-list-page { padding: 28px 32px; font-family: 'Plus Jakarta Sans', sans-serif; }

.page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #64748B; margin: 0; }

.toolbar { margin-bottom: 16px; }
.search-wrap { position: relative; max-width: 360px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94A3B8; pointer-events: none; }
.search-input {
  width: 100%; padding: 9px 12px 9px 38px;
  border: 1.5px solid #E2E8F0; border-radius: 10px;
  font-size: 13.5px; color: #1E293B; background: white;
  outline: none; transition: border .18s;
}
.search-input:focus { border-color: #6EE7B7; }

.table-card { background: white; border-radius: 14px; border: 1px solid #E2E8F0; overflow: hidden; }
.loading-center { display: flex; justify-content: center; padding: 60px; }
.spinner { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #6EE7B7; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.data-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.data-table th {
  padding: 12px 16px; text-align: left;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px;
  color: #64748B; background: #F8FAFC; border-bottom: 1px solid #E2E8F0;
}
.data-table td { padding: 14px 16px; border-bottom: 1px solid #F1F5F9; vertical-align: middle; }
.data-table tr:last-child td { border-bottom: none; }
.data-table tr:hover td { background: #F8FAFC; }

.instructor-cell { display: flex; align-items: center; gap: 10px; }
.avatar { width: 38px; height: 38px; border-radius: 10px; object-fit: cover; border: 1.5px solid #E2E8F0; flex-shrink: 0; }
.name { font-weight: 600; color: #0F172A; }
.email-sub { font-size: 12px; color: #64748B; margin-top: 1px; }
.text-sm { font-size: 13px; color: #475569; }

.bank-info { }
.bank-name { font-weight: 600; color: #0F172A; font-size: 13px; }
.bank-account { font-size: 12px; color: #64748B; font-family: monospace; margin-top: 2px; }
.no-bank { font-size: 12px; color: #94A3B8; font-style: italic; }

.balance { font-weight: 700; color: #64748B; }
.balance--positive { color: #10B981; }

.course-count { font-weight: 600; color: #3B82F6; }

.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 600; }
.status--active { background: #DCFCE7; color: #15803D; }
.status--locked { background: #FEE2E2; color: #DC2626; }

.btn-detail {
  padding: 6px 14px; border-radius: 8px; font-size: 12.5px; font-weight: 600; cursor: pointer;
  background: #F1F5F9; border: 1px solid #E2E8F0; color: #475569; transition: all .18s;
}
.btn-detail:hover { background: #0F1117; color: #6EE7B7; border-color: #0F1117; }

.empty { text-align: center; padding: 48px; color: #94A3B8; font-size: 14px; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 16px; border-top: 1px solid #F1F5F9; }
.page-btn { padding: 7px 16px; border-radius: 8px; border: 1px solid #E2E8F0; background: white; cursor: pointer; font-size: 13px; font-weight: 600; color: #475569; transition: all .18s; }
.page-btn:disabled { opacity: .4; cursor: not-allowed; }
.page-btn:not(:disabled):hover { background: #0F1117; color: #6EE7B7; border-color: #0F1117; }
.page-info { font-size: 13px; color: #64748B; }
</style>
