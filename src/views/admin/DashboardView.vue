<!-- ── DashboardView.vue -->
<template>
  <div class="db-page">

    <!-- HERO -->
    <div class="db-hero">
      <div class="db-hero__glow"></div>
      <div class="db-hero__content">
        <div>
          <h1 class="db-hero__title">Xin chào, Admin 👋</h1>
          <p class="db-hero__sub">{{ todayLabel }} · Cập nhật lúc {{ lastUpdated }}</p>
        </div>
        <button class="btn-refresh" @click="reloadData" :disabled="isLoadingStats">
          <svg :class="isLoadingStats && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
          </svg>
          Làm mới
        </button>
      </div>
    </div>

    <!-- KPI CARDS (6 cards, 3 col × 2 rows) -->
    <div class="stat-grid">

      <!-- 1. Học viên -->
      <div class="stat-card stat-card--blue">
        <div class="stat-card__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Tổng Học Viên</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ fmtNum(stats.totalStudents) }}</span>
          </div>
          <div class="stat-card__sub">+{{ fmtNum(stats.newStudentsThisMonth) }} tháng này</div>
        </div>
      </div>

      <!-- 2. Khóa học -->
      <div class="stat-card stat-card--indigo">
        <div class="stat-card__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Tổng Khóa Học</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ fmtNum(stats.totalCourses) }}</span>
          </div>
          <div class="stat-card__sub">{{ fmtNum(stats.pendingCourses) }} chờ duyệt</div>
        </div>
      </div>

      <!-- 3. Doanh thu + sparkline -->
      <div class="stat-card stat-card--emerald stat-card--wide">
        <div class="stat-card__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="1" x2="12" y2="23"/>
            <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Doanh Thu Toàn Sàn</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ formatCurrency(stats.totalRevenue) }}</span>
          </div>
          <div class="stat-card__sub">7 ngày gần nhất</div>
        </div>
        <!-- SVG Sparkline -->
        <div class="sparkline-wrap">
          <svg v-if="sparklinePoints.length > 1" class="sparkline" viewBox="0 0 80 32" preserveAspectRatio="none">
            <defs>
              <linearGradient id="spark-grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#6EE7B7" stop-opacity="0.3"/>
                <stop offset="100%" stop-color="#6EE7B7" stop-opacity="0"/>
              </linearGradient>
            </defs>
            <polygon :points="sparklineFillPoints" fill="url(#spark-grad)"/>
            <polyline :points="sparklinePoints" fill="none" stroke="#6EE7B7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <div v-else-if="isLoadingStats" class="sparkline-loading"></div>
        </div>
      </div>

      <!-- 4. Khóa chờ duyệt -->
      <div class="stat-card stat-card--amber" :class="stats.pendingCourses > 0 && 'stat-card--urgent'">
        <div class="stat-card__icon" style="position:relative">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
            <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          <span v-if="stats.pendingCourses > 0" class="ping-dot"></span>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Khóa Chờ Duyệt</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ fmtNum(stats.pendingCourses) }}</span>
          </div>
        </div>
        <router-link to="/admin/courses" class="stat-card__action">
          Xử lý ngay
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" stroke-linecap="round"/></svg>
        </router-link>
      </div>

      <!-- 5. Đơn pending -->
      <div class="stat-card stat-card--violet">
        <div class="stat-card__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Đơn Chờ Xử Lý</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ fmtNum(stats.newOrders) }}</span>
          </div>
          <div class="stat-card__sub">Trạng thái PENDING</div>
        </div>
      </div>

      <!-- 6. Hoàn tiền -->
      <div class="stat-card stat-card--rose">
        <div class="stat-card__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="1 4 1 10 7 10"/>
            <path d="M3.51 15a9 9 0 102.13-9.36L1 10"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__label">Đơn Hoàn Tiền</div>
          <div class="stat-card__value">
            <span v-if="isLoadingStats" class="skeleton skeleton--num"></span>
            <span v-else>{{ fmtNum(stats.refundedOrders) }}</span>
          </div>
          <div class="stat-card__sub">{{ fmtNum(stats.cancelledOrders) }} đã huỷ</div>
        </div>
      </div>

    </div>

    <!-- ACTION CENTER + SIDE -->
    <div class="action-grid">

      <!-- Việc cần xử lý -->
      <div class="action-card">
        <div class="action-card__header">
          <h2 class="action-card__title">Việc cần xử lý</h2>
          <p class="action-card__sub">Các mục yêu cầu chú ý ngay</p>
        </div>
        <div class="action-list">

          <router-link to="/admin/courses" class="action-item" :class="stats.pendingCourses > 0 && 'action-item--warn'">
            <div class="action-item__icon action-item__icon--amber">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/>
              </svg>
              <span v-if="stats.pendingCourses > 0" class="ping-dot"></span>
            </div>
            <div class="action-item__body">
              <p class="action-item__label">Khóa học chờ duyệt</p>
              <p class="action-item__sub">Cần xét duyệt nội dung và phê duyệt</p>
            </div>
            <div class="action-item__right">
              <span v-if="isLoadingStats" class="skeleton" style="width:36px;height:28px;border-radius:8px"></span>
              <span v-else class="action-badge" :class="stats.pendingCourses > 0 ? 'action-badge--warn' : 'action-badge--ok'">{{ stats.pendingCourses }}</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" stroke-linecap="round"/></svg>
            </div>
          </router-link>

          <router-link to="/admin/orders" class="action-item" :class="stats.newOrders > 0 && 'action-item--info'">
            <div class="action-item__icon action-item__icon--violet">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
                <line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/>
              </svg>
            </div>
            <div class="action-item__body">
              <p class="action-item__label">Đơn hàng đang chờ</p>
              <p class="action-item__sub">Đơn ở trạng thái PENDING</p>
            </div>
            <div class="action-item__right">
              <span v-if="isLoadingStats" class="skeleton" style="width:36px;height:28px;border-radius:8px"></span>
              <span v-else class="action-badge" :class="stats.newOrders > 0 ? 'action-badge--info' : 'action-badge--ok'">{{ stats.newOrders }}</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" stroke-linecap="round"/></svg>
            </div>
          </router-link>

          <router-link to="/admin/orders" class="action-item" :class="stats.refundedOrders > 0 && 'action-item--rose'">
            <div class="action-item__icon action-item__icon--rose">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/>
              </svg>
            </div>
            <div class="action-item__body">
              <p class="action-item__label">Đơn hoàn tiền</p>
              <p class="action-item__sub">Cần xác nhận và xử lý hoàn</p>
            </div>
            <div class="action-item__right">
              <span v-if="isLoadingStats" class="skeleton" style="width:36px;height:28px;border-radius:8px"></span>
              <span v-else class="action-badge" :class="stats.refundedOrders > 0 ? 'action-badge--rose' : 'action-badge--ok'">{{ stats.refundedOrders }}</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6" stroke-linecap="round"/></svg>
            </div>
          </router-link>

        </div>
      </div>

      <!-- Side: Completion + Quick nav -->
      <div class="side-col">

        <!-- Tỉ lệ hoàn thành đơn -->
        <div class="completion-card">
          <h2 class="action-card__title">Tình trạng đơn hàng</h2>
          <div v-if="isLoadingStats" class="completion-skeleton"></div>
          <template v-else>
            <div class="completion-nums">
              <div class="cn-item cn-item--green">
                <span class="cn-val">{{ fmtNum(stats.completedOrders) }}</span>
                <span class="cn-label">Hoàn thành</span>
              </div>
              <div class="cn-item cn-item--red">
                <span class="cn-val">{{ fmtNum(stats.cancelledOrders) }}</span>
                <span class="cn-label">Đã huỷ</span>
              </div>
              <div class="cn-item cn-item--violet">
                <span class="cn-val">{{ fmtNum(stats.refundedOrders) }}</span>
                <span class="cn-label">Hoàn tiền</span>
              </div>
            </div>
            <div class="crb-label">
              <span>Tỉ lệ hoàn thành</span>
              <span class="crb-val">{{ stats.completionRate }}%</span>
            </div>
            <div class="crb-track">
              <div class="crb-fill" :style="{ width: stats.completionRate + '%' }"></div>
            </div>
          </template>
        </div>

        <!-- Quick Links -->
        <div class="quick-nav">
          <h2 class="action-card__title" style="margin-bottom:12px">Truy cập nhanh</h2>
          <div class="quick-nav-grid">
            <router-link to="/admin/stats" class="qnav-item qnav-item--purple">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>
              <span>Thống kê</span>
            </router-link>
            <router-link to="/admin/courses" class="qnav-item qnav-item--blue">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z"/><path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z"/></svg>
              <span>Khóa học</span>
            </router-link>
            <router-link to="/admin/users" class="qnav-item qnav-item--teal">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
              <span>Người dùng</span>
            </router-link>
            <router-link to="/admin/orders" class="qnav-item qnav-item--amber">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg>
              <span>Đơn hàng</span>
            </router-link>
            <router-link to="/admin/tickets" class="qnav-item qnav-item--rose">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
              <span>Khiếu nại</span>
            </router-link>
            <router-link to="/admin/audit-logs" class="qnav-item qnav-item--slate">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
              <span>Nhật ký</span>
            </router-link>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axiosClient from '@/api/axiosClient'

const stats = ref({
  totalStudents: 0, totalCourses: 0,
  totalRevenue: 0, platformRevenue: 0, instructorPayout: 0,
  pendingCourses: 0, newOrders: 0,
  newStudentsThisMonth: 0,
  completedOrders: 0, cancelledOrders: 0, refundedOrders: 0,
  completionRate: 0
})
const isLoadingStats = ref(false)
const sparklineRaw = ref([])
const lastUpdated = ref('—')

// Sparkline: 7-day revenue as SVG polyline points
const sparklinePoints = computed(() => {
  const vals = sparklineRaw.value.map(i => Number(i.value || 0))
  if (vals.length < 2) return ''
  const max = Math.max(...vals, 1)
  const w = 80, h = 32, pad = 2
  return vals.map((v, i) => {
    const x = pad + (i / (vals.length - 1)) * (w - pad * 2)
    const y = h - pad - (v / max) * (h - pad * 2)
    return `${x.toFixed(1)},${y.toFixed(1)}`
  }).join(' ')
})

const sparklineFillPoints = computed(() => {
  if (!sparklinePoints.value) return ''
  const pts = sparklinePoints.value
  const last = pts.split(' ').at(-1).split(',')[0]
  const first = pts.split(' ')[0].split(',')[0]
  return `${first},32 ${pts} ${last},32`
})

const fetchStats = async () => {
  isLoadingStats.value = true
  try {
    const r = await axiosClient.get('/admin/dashboard/stats')
    if (r) stats.value = r
    lastUpdated.value = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
  } catch (e) { console.error(e) }
  finally { isLoadingStats.value = false }
}

const fetchSparkline = async () => {
  try {
    const now = new Date()
    const pad = n => String(n).padStart(2, '0')
    const fmt = d => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
    const from = new Date(now); from.setDate(from.getDate() - 6)
    const r = await axiosClient.get('/admin/dashboard/chart', {
      params: { from: fmt(from), to: fmt(now) }
    })
    sparklineRaw.value = Array.isArray(r) ? r : []
  } catch { sparklineRaw.value = [] }
}

const reloadData = () => { fetchStats(); fetchSparkline() }
const todayLabel = new Date().toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' })

const fmtNum = n => (n || 0).toLocaleString('vi-VN')
const formatCurrency = v => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v || 0)

onMounted(() => { fetchStats(); fetchSparkline() })
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.db-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  padding: 28px 32px 56px;
  max-width: 1240px;
  color: #1E293B;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Hero */
.db-hero { background: #0F172A; border-radius: 20px; padding: 28px 32px; position: relative; overflow: hidden; }
.db-hero__glow { position: absolute; width: 360px; height: 360px; background: radial-gradient(circle, rgba(110,231,183,.15) 0%, transparent 70%); right: -60px; top: -100px; pointer-events: none; }
.db-hero__content { position: relative; display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.db-hero__title { font-size: 24px; font-weight: 800; color: white; letter-spacing: -.4px; margin: 0 0 5px; }
.db-hero__sub { font-size: 13px; color: rgba(255,255,255,.4); font-weight: 500; margin: 0; }
.btn-refresh { display: flex; align-items: center; gap: 8px; background: rgba(255,255,255,.08); border: 1px solid rgba(255,255,255,.12); color: rgba(255,255,255,.7); border-radius: 12px; padding: 9px 16px; font-family: inherit; font-size: 13px; font-weight: 700; cursor: pointer; transition: all .18s; white-space: nowrap; }
.btn-refresh:hover:not(:disabled) { background: rgba(110,231,183,.15); border-color: rgba(110,231,183,.4); color: #6EE7B7; }
.btn-refresh:disabled { opacity: .5; cursor: not-allowed; }
@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin .8s linear infinite; }

/* Stat grid — 3 cols × 2 rows */
.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
@media (max-width: 900px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 540px) { .stat-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: white; border: 1px solid #E2E8F0; border-radius: 18px;
  padding: 20px; display: flex; align-items: flex-start; gap: 14px;
  position: relative; overflow: hidden;
  transition: box-shadow .2s, transform .2s;
}
.stat-card:hover { box-shadow: 0 6px 24px rgba(0,0,0,.08); transform: translateY(-2px); }
.stat-card--urgent { border-color: #FCD34D; background: #FFFBEB; }
.stat-card--urgent::before { content: ''; position: absolute; right: 0; top: 0; bottom: 0; width: 4px; background: #F59E0B; border-radius: 0 18px 18px 0; }
.stat-card--wide { grid-column: span 1; }

.stat-card__icon { width: 42px; height: 42px; border-radius: 13px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-card--blue   .stat-card__icon { background: #EFF6FF; color: #1D4ED8; }
.stat-card--indigo .stat-card__icon { background: #EEF2FF; color: #4338CA; }
.stat-card--emerald .stat-card__icon { background: #ECFDF5; color: #059669; }
.stat-card--amber  .stat-card__icon { background: #FFFBEB; color: #D97706; }
.stat-card--violet .stat-card__icon { background: #F5F3FF; color: #7C3AED; }
.stat-card--rose   .stat-card__icon { background: #FFF1F2; color: #E11D48; }

.stat-card__body { flex: 1; min-width: 0; }
.stat-card__label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .6px; color: #94A3B8; margin-bottom: 4px; }
.stat-card__value { font-size: 24px; font-weight: 800; color: #0F172A; line-height: 1.1; letter-spacing: -.5px; }
.stat-card__sub   { font-size: 11.5px; color: #94A3B8; font-weight: 500; margin-top: 4px; }
.stat-card__action { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 700; color: #D97706; text-decoration: none; margin-top: 8px; transition: gap .15s; }
.stat-card__action:hover { gap: 7px; }

.ping-dot { position: absolute; top: -3px; right: -3px; width: 9px; height: 9px; border-radius: 50%; background: #EF4444; border: 2px solid white; animation: ping 1.2s ease-in-out infinite; }
@keyframes ping { 0%,100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.5); opacity: .6; } }

/* Sparkline */
.sparkline-wrap { flex-shrink: 0; width: 80px; height: 32px; align-self: center; }
.sparkline { width: 80px; height: 32px; display: block; }
.sparkline-loading { width: 80px; height: 32px; background: linear-gradient(90deg,#F1F5F9 25%,#E2E8F0 50%,#F1F5F9 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 6px; }

/* Skeleton */
.skeleton { display: inline-block; background: linear-gradient(90deg,#F1F5F9 25%,#E2E8F0 50%,#F1F5F9 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 6px; }
.skeleton--num { width: 90px; height: 26px; display: block; }
@keyframes shimmer { 0%{background-position:200% 0} 100%{background-position:-200% 0} }

/* Action Grid */
.action-grid { display: grid; grid-template-columns: 1fr 360px; gap: 18px; align-items: start; }
@media (max-width: 960px) { .action-grid { grid-template-columns: 1fr; } }

.action-card { background: white; border: 1px solid #E2E8F0; border-radius: 18px; overflow: hidden; }
.action-card__header { padding: 18px 20px 14px; border-bottom: 1px solid #F1F5F9; }
.action-card__title { font-size: 14px; font-weight: 800; color: #0F172A; margin: 0 0 2px; }
.action-card__sub   { font-size: 12px; color: #94A3B8; font-weight: 500; margin: 0; }
.action-list { display: flex; flex-direction: column; }

.action-item { display: flex; align-items: center; gap: 12px; padding: 15px 20px; border-bottom: 1px solid #F8FAFC; text-decoration: none; color: inherit; transition: background .15s; }
.action-item:last-child { border-bottom: none; }
.action-item:hover { background: #F8FAFC; }
.action-item--warn { background: #FFFBEB; }
.action-item--warn:hover { background: #FEF3C7; }
.action-item--info { background: #EFF6FF; }
.action-item--info:hover { background: #DBEAFE; }
.action-item--rose { background: #FFF1F2; }
.action-item--rose:hover { background: #FFE4E6; }

.action-item__icon { width: 36px; height: 36px; border-radius: 11px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; position: relative; }
.action-item__icon--amber  { background: #FFFBEB; color: #D97706; }
.action-item__icon--violet { background: #F5F3FF; color: #7C3AED; }
.action-item__icon--rose   { background: #FFF1F2; color: #E11D48; }

.action-item__body { flex: 1; }
.action-item__label { font-size: 13px; font-weight: 700; color: #1E293B; margin: 0 0 2px; }
.action-item__sub   { font-size: 11.5px; color: #94A3B8; font-weight: 500; margin: 0; }
.action-item__right { display: flex; align-items: center; gap: 6px; flex-shrink: 0; color: #CBD5E1; }

.action-badge { min-width: 32px; text-align: center; font-size: 13px; font-weight: 800; padding: 3px 9px; border-radius: 8px; }
.action-badge--warn   { background: #FEF3C7; color: #92400E; }
.action-badge--info   { background: #DBEAFE; color: #1E40AF; }
.action-badge--rose   { background: #FFE4E6; color: #BE123C; }
.action-badge--ok     { background: #F1F5F9; color: #94A3B8; }

/* Side column */
.side-col { display: flex; flex-direction: column; gap: 14px; }

/* Completion card */
.completion-card { background: white; border: 1px solid #E2E8F0; border-radius: 18px; padding: 18px 20px; }
.completion-skeleton { height: 80px; background: linear-gradient(90deg,#F1F5F9 25%,#E2E8F0 50%,#F1F5F9 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 10px; margin-top: 12px; }

.completion-nums { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 12px 0; }
.cn-item { display: flex; flex-direction: column; align-items: center; padding: 10px 6px; border-radius: 12px; border: 1px solid; }
.cn-item--green  { background: #ECFDF5; border-color: #A7F3D0; }
.cn-item--red    { background: #FFF1F2; border-color: #FECDD3; }
.cn-item--violet { background: #F5F3FF; border-color: #DDD6FE; }
.cn-val   { font-size: 18px; font-weight: 800; color: #0F172A; }
.cn-label { font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: .4px; color: #94A3B8; margin-top: 3px; }

.crb-label { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 12px; font-weight: 600; color: #64748B; }
.crb-val   { font-weight: 800; color: #0F172A; }
.crb-track { height: 8px; background: #F1F5F9; border-radius: 99px; overflow: hidden; }
.crb-fill  { height: 100%; background: linear-gradient(90deg, #10B981, #34D399); border-radius: 99px; transition: width .6s ease; }

/* Quick nav */
.quick-nav { background: white; border: 1px solid #E2E8F0; border-radius: 18px; padding: 18px; }
.quick-nav-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.qnav-item { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; padding: 14px 4px; border-radius: 13px; text-decoration: none; font-size: 11.5px; font-weight: 700; border: 1px solid transparent; transition: all .15s; }
.qnav-item span { color: #475569; }
.qnav-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.qnav-item--purple { background: #F5F3FF; color: #7C3AED; border-color: #EDE9FE; }
.qnav-item--blue   { background: #EFF6FF; color: #1D4ED8; border-color: #DBEAFE; }
.qnav-item--teal   { background: #F0FDFA; color: #0D9488; border-color: #CCFBF1; }
.qnav-item--amber  { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.qnav-item--rose   { background: #FFF1F2; color: #E11D48; border-color: #FECDD3; }
.qnav-item--slate  { background: #F8FAFC; color: #475569; border-color: #E2E8F0; }
</style>
