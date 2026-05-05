<template>
  <div class="ss-page">
    <!-- Header -->
    <div class="ss-header">
      <div>
        <h1 class="ss-title">Thống Kê Hệ Thống</h1>
        <p class="ss-sub">Phân tích toàn diện · {{ periodLabel }} · Cập nhật {{ lastUpdated }}</p>
      </div>
      <div class="filter-bar">
        <!-- Mode toggle -->
        <div class="mode-tabs">
          <button class="mode-tab" :class="filterMode === 'year' && 'mode-tab--active'" @click="filterMode = 'year'; fetchAll()">Theo năm</button>
          <button class="mode-tab" :class="filterMode === 'custom' && 'mode-tab--active'" @click="filterMode = 'custom'">Khoảng thời gian</button>
        </div>
        <!-- Year controls -->
        <div v-if="filterMode === 'year'" class="year-ctrl">
          <button class="year-btn" @click="changeYear(-1)" :disabled="isLoading">←</button>
          <span class="year-label">{{ currentYear }}</span>
          <button class="year-btn" @click="changeYear(1)" :disabled="isLoading || currentYear >= new Date().getFullYear()">→</button>
        </div>
        <!-- Custom date range -->
        <div v-else class="date-range-ctrl">
          <div class="date-field">
            <label class="date-lbl">Từ ngày</label>
            <input type="date" v-model="customFrom" class="date-input" :max="customTo || undefined" />
          </div>
          <span class="date-sep">→</span>
          <div class="date-field">
            <label class="date-lbl">Đến ngày</label>
            <input type="date" v-model="customTo" class="date-input" :min="customFrom || undefined" :max="todayStr" />
          </div>
          <button class="btn-apply" @click="fetchAll" :disabled="isLoading || !customFrom || !customTo">Áp dụng</button>
        </div>
        <button class="btn-refresh" @click="fetchAll" :disabled="isLoading">
          <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
          </svg>
          Làm mới
        </button>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="kpi-grid">
      <div v-for="card in kpiCards" :key="card.key" class="kpi-card">
        <div v-if="isLoading" class="kpi-skeleton"></div>
        <template v-else>
          <div class="kpi-icon" :style="{ background: card.iconBg, color: card.iconColor }">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="card.iconSvg" />
          </div>
          <p class="kpi-label">{{ card.label }}</p>
          <p class="kpi-val">{{ card.value }}</p>
          <p v-if="card.sub" class="kpi-sub">{{ card.sub }}</p>
        </template>
      </div>
    </div>

    <!-- Revenue Split -->
    <div class="section-card" v-if="!isLoading && dashStats">
      <div class="section-header">
        <div>
          <h2 class="section-title">Phân chia doanh thu</h2>
          <p class="section-sub">Nền tảng vs Giảng viên · {{ periodLabel }}</p>
        </div>
      </div>
      <div class="split-grid">
        <div class="split-item split-item--platform">
          <div class="split-pct">{{ platformPct }}%</div>
          <div class="split-label">Nền tảng</div>
          <div class="split-amount">{{ formatCurrencyShort(splitPlatformAmount) }}</div>
        </div>
        <div class="split-bar-col">
          <div class="split-bar">
            <div class="split-bar-fill split-bar-fill--platform" :style="{ width: platformPct + '%' }"></div>
          </div>
          <div class="split-bar-labels">
            <span class="text-[11px] text-emerald-600 font-bold">Nền tảng</span>
            <span class="text-[11px] text-violet-600 font-bold">Giảng viên</span>
          </div>
        </div>
        <div class="split-item split-item--instructor">
          <div class="split-pct">{{ instructorPct }}%</div>
          <div class="split-label">Giảng viên</div>
          <div class="split-amount">{{ formatCurrencyShort(splitInstructorAmount) }}</div>
        </div>
      </div>
    </div>

    <!-- Charts row 1: Revenue Line + Order Doughnut -->
    <div class="charts-row-2">
      <!-- Revenue Line Chart -->
      <div class="section-card flex-[2]">
        <div class="section-header">
          <div>
            <h2 class="section-title">Doanh thu {{ filterMode === 'custom' ? 'theo ngày' : 'theo tháng' }}</h2>
            <p class="section-sub">Tổng: {{ formatCurrencyShort(totalRevenue) }} · TB: {{ formatCurrencyShort(avgRevenue) }}/{{ filterMode === 'custom' ? 'ngày' : 'tháng' }} · Đỉnh: {{ peakMonth.label }} {{ formatCurrencyShort(peakMonth.amount) }}</p>
          </div>
        </div>
        <div v-if="isLoading" class="chart-skeleton-bar">
          <div v-for="i in 12" :key="i" class="bar-skeleton" :style="{ height: (30 + Math.random() * 60) + '%' }"></div>
        </div>
        <div v-else style="height:280px; position:relative; width:100%;">
          <Line :data="revenueLineData" :options="lineOptions" style="width:100%;height:100%;"/>
        </div>
      </div>

      <!-- Order Status Doughnut -->
      <div class="section-card" style="min-width:260px; max-width:300px;">
        <div class="section-header">
          <div>
            <h2 class="section-title">Trạng thái đơn hàng</h2>
            <p class="section-sub">Tổng {{ totalOrders.toLocaleString() }} đơn</p>
          </div>
        </div>
        <div v-if="isLoading" class="h-40 flex items-center justify-center text-gray-300 text-sm">Đang tải...</div>
        <div v-else-if="totalOrders === 0" class="h-40 flex items-center justify-center text-gray-400 text-sm">Chưa có dữ liệu</div>
        <div v-else>
          <div style="height:180px; position:relative; width:100%;">
            <Doughnut :data="orderDoughnutData" :options="doughnutOptions" style="width:100%;height:100%;"/>
          </div>
          <div class="donut-legend">
            <div v-for="(item, i) in orderLegendItems" :key="item.label" class="donut-legend-row">
              <span class="donut-dot" :style="{ background: DONUT_COLORS[i] }"></span>
              <span class="donut-legend-label">{{ item.label }}</span>
              <span class="donut-legend-val">{{ item.count.toLocaleString() }}</span>
              <span class="donut-legend-pct">{{ item.pct }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts row 2: New Students + Approved Courses -->
    <div class="charts-row-2">
      <div class="section-card flex-1">
        <div class="section-header">
          <div>
            <h2 class="section-title">Học viên đăng ký mới</h2>
            <p class="section-sub">Tổng {{ totalNewUsers.toLocaleString() }} học viên mới · Năm {{ chartYear }}</p>
          </div>
        </div>
        <div v-if="isLoading" class="chart-skeleton-bar h-40">
          <div v-for="i in 12" :key="i" class="bar-skeleton" :style="{ height: (20+Math.random()*70)+'%' }"></div>
        </div>
        <div v-else style="height:200px; position:relative; width:100%;">
          <Bar :data="studentBarData" :options="barOptions" style="width:100%;height:100%;"/>
        </div>
      </div>

      <div class="section-card flex-1">
        <div class="section-header">
          <div>
            <h2 class="section-title">Khóa học được duyệt</h2>
            <p class="section-sub">Tổng {{ totalNewCourses.toLocaleString() }} khóa duyệt · Năm {{ chartYear }}</p>
          </div>
        </div>
        <div v-if="isLoading" class="chart-skeleton-bar h-40">
          <div v-for="i in 12" :key="i" class="bar-skeleton" :style="{ height: (20+Math.random()*70)+'%' }"></div>
        </div>
        <div v-else style="height:200px; position:relative; width:100%;">
          <Bar :data="courseBarData" :options="barOptionsViolet" style="width:100%;height:100%;"/>
        </div>
      </div>
    </div>

    <!-- Monthly/Daily Revenue Table -->
    <div class="section-card" v-if="!isLoading && revenueTableRows.length">
      <div class="section-header">
        <h2 class="section-title">Chi tiết doanh thu {{ filterMode === 'custom' ? 'theo ngày' : 'theo tháng' }}</h2>
        <p class="section-sub">{{ periodLabel }}</p>
      </div>
      <div class="overflow-x-auto">
        <table class="stats-table">
          <colgroup>
            <col style="width:100px">
            <col style="width:220px">
            <col style="width:220px">
            <col style="width:220px">
            <col style="min-width:160px">
          </colgroup>
          <thead>
            <tr>
              <th>{{ filterMode === 'custom' ? 'Ngày' : 'Tháng' }}</th>
              <th class="text-right">Doanh thu</th>
              <th class="text-right">Nền tảng nhận</th>
              <th class="text-right">Giảng viên nhận</th>
              <th class="text-center">% so với đỉnh</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in revenueTableRows" :key="row.month">
              <td class="font-semibold">{{ row.label }}</td>
              <td class="text-right text-blue-700 font-semibold">{{ formatCurrency(row.amount) }}</td>
              <td class="text-right text-emerald-600">{{ formatCurrency(row.platformAmount) }}</td>
              <td class="text-right text-violet-600">{{ formatCurrency(row.instructorAmount) }}</td>
              <td class="text-center">
                <div class="table-bar-wrap">
                  <div class="table-bar-track">
                    <div class="table-bar-fill" :style="{ width: (maxRevenue > 0 ? row.amount/maxRevenue*100 : 0) + '%' }"></div>
                  </div>
                  <span class="table-bar-pct">{{ maxRevenue > 0 ? Math.round(row.amount/maxRevenue*100) : 0 }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr>
              <td class="font-bold text-gray-900">Tổng cộng</td>
              <td class="text-right font-bold text-blue-800">{{ formatCurrency(totalRevenue) }}</td>
              <td class="text-right font-bold text-emerald-700">{{ formatCurrency(totalPlatformRevenue) }}</td>
              <td class="text-right font-bold text-violet-700">{{ formatCurrency(totalInstructorRevenue) }}</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>

    <!-- Top Courses + Top Instructors -->
    <div class="charts-row-2">
      <!-- Top Courses -->
      <div class="section-card flex-1">
        <div class="section-header">
          <h2 class="section-title">Top khóa học doanh thu</h2>
        </div>
        <div v-if="isLoading" class="space-y-3">
          <div v-for="i in 5" :key="i" class="h-10 rounded-xl bg-gray-100 animate-pulse"></div>
        </div>
        <div v-else-if="topCourses.length === 0" class="py-8 text-center text-sm text-gray-400">Chưa có dữ liệu</div>
        <div v-else class="space-y-2">
          <div v-for="(c, i) in topCourses" :key="c.id" class="top-row">
            <span class="top-rank" :class="i === 0 ? 'top-rank--gold' : i === 1 ? 'top-rank--silver' : i === 2 ? 'top-rank--bronze' : ''">{{ i+1 }}</span>
            <div class="top-info">
              <p class="top-name">{{ c.title || '—' }}</p>
              <p class="top-meta">{{ c.totalEnrollments || 0 }} học viên</p>
            </div>
            <div class="top-right">
              <span class="top-revenue">{{ formatCurrencyShort(c.totalRevenue) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Top Instructors -->
      <div class="section-card flex-1">
        <div class="section-header">
          <h2 class="section-title">Top giảng viên doanh thu</h2>
        </div>
        <div v-if="isLoading" class="space-y-3">
          <div v-for="i in 5" :key="i" class="h-10 rounded-xl bg-gray-100 animate-pulse"></div>
        </div>
        <div v-else-if="topInstructors.length === 0" class="py-8 text-center text-sm text-gray-400">Chưa có dữ liệu</div>
        <div v-else class="space-y-2">
          <div v-for="(ins, i) in topInstructors" :key="ins.id" class="top-row">
            <span class="top-rank" :class="i === 0 ? 'top-rank--gold' : i === 1 ? 'top-rank--silver' : i === 2 ? 'top-rank--bronze' : ''">{{ i+1 }}</span>
            <div class="w-8 h-8 rounded-full bg-violet-100 flex items-center justify-center text-violet-700 text-xs font-bold flex-shrink-0">
              {{ (ins.fullName || 'U').charAt(0).toUpperCase() }}
            </div>
            <div class="top-info">
              <p class="top-name">{{ ins.fullName || '—' }}</p>
              <p class="top-meta">{{ ins.totalCourses || 0 }} khóa học</p>
            </div>
            <div class="top-right">
              <span class="top-revenue">{{ formatCurrencyShort(ins.totalRevenue) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', 'toast--' + toast.type]">{{ toast.message }}</div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axiosClient from '@/api/axiosClient'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement,
  BarElement, ArcElement, Tooltip, Legend, Filler
} from 'chart.js'
import { Line, Bar, Doughnut } from 'vue-chartjs'
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Tooltip, Legend, Filler)

const isLoading = ref(false)
const currentYear = ref(new Date().getFullYear())
const filterMode = ref('year')
const customFrom = ref('')
const customTo = ref('')
const todayStr = new Date().toISOString().slice(0, 10)
const lastUpdated = ref('—')
const dashStats = ref(null)
const revenueData = ref([])
const userData = ref([])
const courseData = ref([])
const orderStatus = ref({})
const topCourses = ref([])
const topInstructors = ref([])
const toast = ref({ show: false, type: 'success', message: '' })
let toastTimer = null
onUnmounted(() => clearTimeout(toastTimer))

const showToast = (msg, type = 'success') => {
  clearTimeout(toastTimer)
  toast.value = { show: true, type, message: msg }
  toastTimer = setTimeout(() => { toast.value.show = false }, 3000)
}

const MONTH_LABELS = ['T1','T2','T3','T4','T5','T6','T7','T8','T9','T10','T11','T12']
const DONUT_COLORS = ['#10B981','#F59E0B','#EF4444','#8B5CF6']

const toMonthlySeries = (raw, year) => {
  const yr = year ?? currentYear.value
  const map = new Map()
  ;(Array.isArray(raw) ? raw : []).forEach(item => {
    const parts = String(item?.label || '').split('/')
    if (parts.length !== 2) return
    const mm = Number(parts[0]), yyyy = Number(parts[1])
    if (yyyy !== yr) return
    map.set(mm, {
      amount: Number(item?.value ?? 0),
      platformAmount: Number(item?.platformValue ?? 0),
      instructorAmount: Number(item?.instructorValue ?? 0)
    })
  })
  return MONTH_LABELS.map((label, i) => {
    const v = map.get(i+1) || { amount: 0, platformAmount: 0, instructorAmount: 0 }
    return { month: i+1, label, ...v }
  })
}

const toDailySeries = (raw) =>
  (Array.isArray(raw) ? raw : []).map(item => ({
    label: String(item?.label || ''),
    amount: Number(item?.value ?? 0),
    platformAmount: Number(item?.platformValue ?? 0),
    instructorAmount: Number(item?.instructorValue ?? 0)
  }))

const fmtDateVN = (str) => {
  if (!str) return ''
  const [y, m, d] = str.split('-')
  return `${d}/${m}/${y}`
}

const periodLabel = computed(() => {
  if (filterMode.value === 'year') return `Năm ${currentYear.value}`
  if (customFrom.value && customTo.value) return `${fmtDateVN(customFrom.value)} – ${fmtDateVN(customTo.value)}`
  if (customFrom.value) return `Từ ${fmtDateVN(customFrom.value)}`
  if (customTo.value) return `Đến ${fmtDateVN(customTo.value)}`
  return 'Khoảng tùy chỉnh'
})

const chartYear = computed(() => {
  if (filterMode.value === 'year') return currentYear.value
  if (customFrom.value) return Number(customFrom.value.slice(0, 4))
  return currentYear.value
})

const fetchAll = async () => {
  if (filterMode.value === 'custom' && (!customFrom.value || !customTo.value)) return
  isLoading.value = true
  try {
    if (filterMode.value === 'year') {
      const yr = currentYear.value
      // For year mode: compute first/last day of year to pass as date range to top endpoints
      const yearFrom = `${yr}-01-01`
      const yearTo   = `${yr}-12-31`
      const [statsRes, chartRes, studentRes, courseRes, orderRes, topCoursesRes, topInsRes] = await Promise.all([
        axiosClient.get('/admin/dashboard/stats', { params: { from: yearFrom, to: yearTo } }).catch(() => null),
        axiosClient.get('/admin/dashboard/chart/monthly', { params: { year: yr, months: 12 } }).catch(() => null),
        axiosClient.get('/admin/dashboard/new-students-chart', { params: { year: yr } }).catch(() => null),
        axiosClient.get('/admin/dashboard/approved-courses-chart', { params: { year: yr } }).catch(() => null),
        axiosClient.get('/admin/dashboard/order-status', { params: { from: yearFrom, to: yearTo } }).catch(() => null),
        axiosClient.get('/admin/dashboard/top-courses', { params: { limit: 5, from: yearFrom, to: yearTo } }).catch(() => null),
        axiosClient.get('/admin/dashboard/top-instructors', { params: { limit: 5, from: yearFrom, to: yearTo } }).catch(() => null)
      ])
      if (statsRes) dashStats.value = statsRes
      revenueData.value = toMonthlySeries(Array.isArray(chartRes) ? chartRes : (chartRes?.data || []))
      userData.value = toMonthlySeries(Array.isArray(studentRes) ? studentRes : (studentRes?.data || [])).map(i => ({ ...i, count: i.amount }))
      courseData.value = toMonthlySeries(Array.isArray(courseRes) ? courseRes : (courseRes?.data || [])).map(i => ({ ...i, count: i.amount }))
      orderStatus.value = orderRes || {}
      topCourses.value = Array.isArray(topCoursesRes) ? topCoursesRes : []
      topInstructors.value = Array.isArray(topInsRes) ? topInsRes : []
    } else {
      const yr = chartYear.value
      const [statsRes, chartRes, studentRes, courseRes, orderRes, topCoursesRes, topInsRes] = await Promise.all([
        axiosClient.get('/admin/dashboard/stats', { params: { from: customFrom.value, to: customTo.value } }).catch(() => null),
        axiosClient.get('/admin/dashboard/chart', { params: { from: customFrom.value, to: customTo.value } }).catch(() => null),
        axiosClient.get('/admin/dashboard/new-students-chart', { params: { year: yr } }).catch(() => null),
        axiosClient.get('/admin/dashboard/approved-courses-chart', { params: { year: yr } }).catch(() => null),
        axiosClient.get('/admin/dashboard/order-status', { params: { from: customFrom.value, to: customTo.value } }).catch(() => null),
        axiosClient.get('/admin/dashboard/top-courses', { params: { limit: 5, from: customFrom.value, to: customTo.value } }).catch(() => null),
        axiosClient.get('/admin/dashboard/top-instructors', { params: { limit: 5, from: customFrom.value, to: customTo.value } }).catch(() => null)
      ])
      if (statsRes) dashStats.value = statsRes
      revenueData.value = toDailySeries(Array.isArray(chartRes) ? chartRes : (chartRes?.data || []))
      userData.value = toMonthlySeries(Array.isArray(studentRes) ? studentRes : (studentRes?.data || []), yr).map(i => ({ ...i, count: i.amount }))
      courseData.value = toMonthlySeries(Array.isArray(courseRes) ? courseRes : (courseRes?.data || []), yr).map(i => ({ ...i, count: i.amount }))
      orderStatus.value = orderRes || {}
      topCourses.value = Array.isArray(topCoursesRes) ? topCoursesRes : []
      topInstructors.value = Array.isArray(topInsRes) ? topInsRes : []
    }
    lastUpdated.value = new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
  } catch {
    showToast('Lỗi tải dữ liệu thống kê', 'error')
  } finally {
    isLoading.value = false
  }
}

const changeYear = delta => { currentYear.value += delta; fetchAll() }

// Revenue computed
const maxRevenue = computed(() => Math.max(...revenueData.value.map(d => d.amount), 1))
const totalRevenue = computed(() => revenueData.value.reduce((s, d) => s + d.amount, 0))
const totalPlatformRevenue = computed(() => revenueData.value.reduce((s, d) => s + (d.platformAmount || 0), 0))
const totalInstructorRevenue = computed(() => revenueData.value.reduce((s, d) => s + (d.instructorAmount || 0), 0))
const avgRevenue = computed(() => {
  const n = revenueData.value.length
  return n > 0 ? totalRevenue.value / n : 0
})
const peakMonth = computed(() => revenueData.value.reduce((b, d) => d.amount > b.amount ? d : b, revenueData.value[0] || { amount: 0, label: 'T1' }))
const revenueTableRows = computed(() => revenueData.value.filter(r => r.amount > 0))

// Users / Courses
const totalNewUsers = computed(() => userData.value.reduce((s, d) => s + d.count, 0))
const totalNewCourses = computed(() => courseData.value.reduce((s, d) => s + d.count, 0))

// Order status
const totalOrders = computed(() => Object.values(orderStatus.value).reduce((s, v) => s + Number(v), 0))
const orderLegendItems = computed(() => {
  const labels = { COMPLETED: 'Hoàn thành', PENDING: 'Chờ xử lý', CANCELLED: 'Đã huỷ', REFUNDED: 'Hoàn tiền' }
  return Object.entries(orderStatus.value)
    .filter(([, v]) => Number(v) > 0)
    .map(([k, v]) => ({
      label: labels[k] || k,
      count: Number(v),
      pct: totalOrders.value > 0 ? ((Number(v) / totalOrders.value) * 100).toFixed(1) : '0'
    }))
})

// Revenue split
const splitPlatformAmount = computed(() => Number(dashStats.value?.platformRevenue || 0))
const splitInstructorAmount = computed(() => Number(dashStats.value?.instructorPayout || 0))
const platformPct = computed(() => {
  const total = Number(dashStats.value?.totalRevenue || 0)
  const plat  = Number(dashStats.value?.platformRevenue || 0)
  return total > 0 ? Math.round((plat / total) * 100) : 0
})
const instructorPct = computed(() => {
  const total = Number(dashStats.value?.totalRevenue || 0)
  const ins   = Number(dashStats.value?.instructorPayout || 0)
  return total > 0 ? Math.round((ins / total) * 100) : 0
})

// KPI Cards
const kpiCards = computed(() => {
  const s = dashStats.value
  return [
    { key: 'revenue', label: 'Tổng doanh thu', value: formatCurrencyShort(totalRevenue.value), sub: periodLabel.value, iconBg: '#ECFDF5', iconColor: '#059669', iconSvg: '<path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" stroke-linecap="round" stroke-linejoin="round"/>' },
    { key: 'platform', label: 'Nền tảng nhận', value: formatCurrencyShort(Number(s?.platformRevenue || 0)), sub: `${platformPct.value}% · ${periodLabel.value}`, iconBg: '#EFF6FF', iconColor: '#1D4ED8', iconSvg: '<rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><path d="M8 21h8M12 17v4" stroke-linecap="round"/>' },
    { key: 'instructor', label: 'Giảng viên nhận', value: formatCurrencyShort(Number(s?.instructorPayout || 0)), sub: `${instructorPct.value}% · ${periodLabel.value}`, iconBg: '#F5F3FF', iconColor: '#7C3AED', iconSvg: '<path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>' },
    { key: 'students', label: filterMode.value === 'year' ? 'Học viên mới' : 'Học viên đăng ký', value: (s?.totalStudents ?? 0).toLocaleString(), sub: filterMode.value === 'year' ? `+${(s?.newStudentsThisMonth ?? 0)} tháng này` : periodLabel.value, iconBg: '#FFF7ED', iconColor: '#C2410C', iconSvg: '<path d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" stroke-linecap="round"/>' },
    { key: 'courses', label: filterMode.value === 'year' ? 'Tổng khóa học' : 'Khóa học mới', value: (s?.totalCourses ?? 0).toLocaleString(), sub: `${(s?.pendingCourses ?? 0)} chờ duyệt`, iconBg: '#FFF1F2', iconColor: '#BE123C', iconSvg: '<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>' },
    { key: 'completion', label: 'Tỉ lệ hoàn thành', value: `${(s?.completionRate ?? 0).toFixed(1)}%`, sub: `${(s?.completedOrders ?? 0)} / ${((s?.completedOrders ?? 0) + (s?.cancelledOrders ?? 0) + (s?.refundedOrders ?? 0))} đơn`, iconBg: '#FFFBEB', iconColor: '#D97706', iconSvg: '<path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round" stroke-linejoin="round"/>' }
  ]
})

// Chart.js data
const revenueLineData = computed(() => ({
  labels: filterMode.value === 'custom' ? revenueData.value.map(d => d.label) : MONTH_LABELS,
  datasets: [
    {
      label: 'Tổng doanh thu',
      data: revenueData.value.map(d => d.amount),
      borderColor: '#3B82F6',
      backgroundColor: 'rgba(59,130,246,0.08)',
      fill: true, tension: 0.3, pointRadius: 4, pointHoverRadius: 6
    },
    {
      label: 'Nền tảng nhận',
      data: revenueData.value.map(d => d.platformAmount),
      borderColor: '#10B981',
      backgroundColor: 'rgba(16,185,129,0.06)',
      fill: true, tension: 0.3, pointRadius: 3, pointHoverRadius: 5, borderDash: [4,3]
    },
    {
      label: 'Giảng viên nhận',
      data: revenueData.value.map(d => d.instructorAmount),
      borderColor: '#8B5CF6',
      backgroundColor: 'rgba(139,92,246,0.06)',
      fill: true, tension: 0.3, pointRadius: 3, pointHoverRadius: 5, borderDash: [4,3]
    }
  ]
}))

const lineOptions = {
  responsive: true, maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { position: 'top', labels: { font: { size: 11 }, usePointStyle: true, padding: 14 } },
    tooltip: { callbacks: { label: ctx => ` ${ctx.dataset.label}: ${formatCurrencyShort(ctx.parsed.y)}` } }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { font: { size: 10 }, callback: v => v >= 1e6 ? (v/1e6).toFixed(0)+'M' : v >= 1e3 ? (v/1e3).toFixed(0)+'K' : v },
      grid: { color: 'rgba(0,0,0,0.04)' }
    },
    x: { ticks: { font: { size: 10 } }, grid: { display: false } }
  }
}

const makeBarData = (data, color, label) => ({
  labels: MONTH_LABELS,
  datasets: [{ label, data: data.map(d => d.count), backgroundColor: color, borderRadius: 5, borderSkipped: false }]
})

const studentBarData = computed(() => makeBarData(userData.value, 'rgba(59,130,246,0.8)', 'Học viên mới'))
const courseBarData = computed(() => makeBarData(courseData.value, 'rgba(139,92,246,0.8)', 'Khóa được duyệt'))

const barOptionsBase = {
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { display: false }, tooltip: { callbacks: { label: ctx => ` ${ctx.dataset.label}: ${ctx.parsed.y}` } } },
  scales: {
    y: { beginAtZero: true, ticks: { font: { size: 10 } }, grid: { color: 'rgba(0,0,0,0.04)' } },
    x: { ticks: { font: { size: 10 } }, grid: { display: false } }
  }
}
const barOptions = barOptionsBase
const barOptionsViolet = barOptionsBase

const orderDoughnutData = computed(() => {
  const labels = { COMPLETED: 'Hoàn thành', PENDING: 'Chờ xử lý', CANCELLED: 'Đã huỷ', REFUNDED: 'Hoàn tiền' }
  const entries = Object.entries(orderStatus.value).filter(([,v]) => Number(v) > 0)
  return {
    labels: entries.map(([k]) => labels[k] || k),
    datasets: [{ data: entries.map(([,v]) => Number(v)), backgroundColor: DONUT_COLORS.slice(0, entries.length), borderWidth: 2, borderColor: '#fff', hoverOffset: 4 }]
  }
})

const doughnutOptions = {
  responsive: true, maintainAspectRatio: false, cutout: '65%',
  plugins: {
    legend: { display: false },
    tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${ctx.parsed} đơn (${totalOrders.value > 0 ? ((ctx.parsed/totalOrders.value)*100).toFixed(1) : 0}%)` } }
  }
}

const formatCurrency = v => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v || 0)
const formatCurrencyShort = v => {
  if (!v) return '0₫'
  if (v >= 1e9) return (v/1e9).toFixed(1).replace('.0','') + ' tỷ'
  if (v >= 1e6) return (v/1e6).toFixed(1).replace('.0','') + ' tr'
  if (v >= 1e3) return (v/1e3).toFixed(0) + 'k'
  return v.toLocaleString('vi-VN') + '₫'
}

onMounted(fetchAll)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.ss-page { font-family: 'Plus Jakarta Sans', sans-serif; padding: 28px 32px 56px; max-width: 1280px; color: #1E293B; display: flex; flex-direction: column; gap: 24px; }

.ss-header { display: flex; align-items: flex-start; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.ss-title  { font-size: 26px; font-weight: 800; color: #0F172A; letter-spacing: -.5px; margin: 0 0 4px; }
.ss-sub    { font-size: 13px; color: #64748B; font-weight: 500; margin: 0; }

.filter-bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.mode-tabs { display: flex; background: #F1F5F9; border-radius: 10px; padding: 3px; gap: 2px; }
.mode-tab  { font-family: inherit; font-size: 12px; font-weight: 700; color: #64748B; background: none; border: none; padding: 6px 14px; border-radius: 8px; cursor: pointer; transition: all .15s; white-space: nowrap; }
.mode-tab--active { background: white; color: #0F172A; box-shadow: 0 1px 4px rgba(0,0,0,.1); }

.date-range-ctrl { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #E2E8F0; border-radius: 10px; padding: 6px 12px; }
.date-field { display: flex; flex-direction: column; gap: 2px; }
.date-lbl   { font-size: 10px; font-weight: 700; color: #94A3B8; text-transform: uppercase; letter-spacing: .4px; }
.date-input { font-family: inherit; font-size: 13px; font-weight: 600; color: #0F172A; border: none; outline: none; background: transparent; cursor: pointer; width: 130px; }
.date-sep   { color: #CBD5E1; font-size: 16px; }
.btn-apply  { font-family: inherit; font-size: 12px; font-weight: 800; color: white; background: #3B82F6; border: none; border-radius: 8px; padding: 7px 14px; cursor: pointer; transition: all .15s; white-space: nowrap; }
.btn-apply:hover:not(:disabled) { background: #2563EB; }
.btn-apply:disabled { opacity: .4; cursor: not-allowed; }

.flex { display: flex; }
.items-center { align-items: center; }
.gap-2 { gap: 8px; }
.flex-\[2\] { flex: 2; }
.flex-1 { flex: 1; }

.year-ctrl  { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #E2E8F0; border-radius: 10px; padding: 6px 12px; }
.year-btn   { width: 24px; height: 24px; border: none; background: none; font-size: 14px; color: #64748B; cursor: pointer; display: flex; align-items: center; justify-content: center; border-radius: 6px; transition: all .15s; }
.year-btn:hover:not(:disabled) { background: #F1F5F9; color: #0F172A; }
.year-btn:disabled { opacity: .4; cursor: not-allowed; }
.year-label { font-size: 14px; font-weight: 800; color: #0F172A; min-width: 36px; text-align: center; }
.btn-refresh { display: flex; align-items: center; gap: 7px; font-family: inherit; font-size: 13px; font-weight: 600; color: #475569; background: white; border: 1px solid #E2E8F0; border-radius: 10px; padding: 8px 16px; cursor: pointer; transition: all .18s; white-space: nowrap; }
.btn-refresh:hover:not(:disabled) { background: #0F172A; color: #6EE7B7; border-color: #0F172A; }
.btn-refresh:disabled { opacity: .5; cursor: not-allowed; }
@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin .8s linear infinite; }

/* KPI */
.kpi-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
@media(max-width:1100px) { .kpi-grid { grid-template-columns: repeat(3,1fr); } }
@media(max-width:600px)  { .kpi-grid { grid-template-columns: repeat(2,1fr); } }
.kpi-card { background: white; border: 1px solid #E2E8F0; border-radius: 16px; padding: 18px; display: flex; flex-direction: column; gap: 6px; box-shadow: 0 1px 3px rgba(0,0,0,.04); transition: box-shadow .2s; }
.kpi-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.07); }
.kpi-skeleton { height: 80px; background: linear-gradient(90deg,#F1F5F9 25%,#E2E8F0 50%,#F1F5F9 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 10px; }
@keyframes shimmer { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
.kpi-icon  { width: 38px; height: 38px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-bottom: 2px; }
.kpi-label { font-size: 11px; font-weight: 700; color: #94A3B8; text-transform: uppercase; letter-spacing: .5px; margin: 0; }
.kpi-val   { font-size: 22px; font-weight: 800; color: #0F172A; letter-spacing: -.5px; line-height: 1; margin: 0; }
.kpi-sub   { font-size: 11px; color: #94A3B8; font-weight: 500; margin: 0; }

/* Section cards */
.section-card { background: white; border: 1px solid #E2E8F0; border-radius: 18px; padding: 22px; box-shadow: 0 1px 4px rgba(0,0,0,.04); }
.section-header { margin-bottom: 16px; }
.section-title { font-size: 15px; font-weight: 800; color: #0F172A; margin: 0 0 3px; }
.section-sub { font-size: 12px; color: #94A3B8; font-weight: 500; margin: 0; }

/* Revenue split */
.split-grid { display: grid; grid-template-columns: 140px 1fr 140px; gap: 16px; align-items: center; }
.split-item { text-align: center; }
.split-item--platform .split-pct { color: #059669; }
.split-item--instructor .split-pct { color: #7C3AED; }
.split-pct   { font-size: 28px; font-weight: 800; }
.split-label { font-size: 11px; font-weight: 700; color: #94A3B8; text-transform: uppercase; margin: 3px 0; }
.split-amount { font-size: 14px; font-weight: 700; color: #0F172A; }
.split-bar-col { display: flex; flex-direction: column; gap: 6px; }
.split-bar { height: 12px; background: #F1F5F9; border-radius: 999px; overflow: hidden; }
.split-bar-fill { height: 100%; border-radius: 999px; background: linear-gradient(to right, #059669, #34D399); transition: width .6s ease; }
.split-bar-labels { display: flex; justify-content: space-between; }

/* Charts rows */
.charts-row-2 { display: flex; gap: 20px; align-items: flex-start; flex-wrap: wrap; }
.charts-row-2 > * { min-width: 0; overflow: hidden; }

/* Chart skeletons */
.chart-skeleton-bar { display: flex; align-items: flex-end; gap: 6px; }
.bar-skeleton { flex: 1; background: linear-gradient(90deg,#F1F5F9 25%,#E2E8F0 50%,#F1F5F9 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; border-radius: 5px 5px 0 0; }
.h-40 { height: 160px; }

/* Doughnut legend */
.donut-legend { margin-top: 14px; display: flex; flex-direction: column; gap: 7px; }
.donut-legend-row { display: flex; align-items: center; gap: 8px; }
.donut-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.donut-legend-label { font-size: 12px; font-weight: 600; color: #475569; flex: 1; }
.donut-legend-val { font-size: 12px; font-weight: 700; color: #0F172A; }
.donut-legend-pct { font-size: 11px; color: #94A3B8; width: 40px; text-align: right; }

/* Stats table */
.stats-table { width: 100%; border-collapse: collapse; }
.stats-table th { padding: 10px 14px; text-align: left; font-size: 11px; font-weight: 700; color: #94A3B8; text-transform: uppercase; letter-spacing: .5px; background: #F8FAFC; border-bottom: 1px solid #F1F5F9; }
.stats-table td { padding: 11px 14px; font-size: 13px; color: #334155; border-bottom: 1px solid #F8FAFC; }
.stats-table tfoot td { font-size: 13px; font-weight: 700; border-top: 2px solid #E2E8F0; border-bottom: none; background: #F8FAFC; white-space: nowrap; }
.stats-table tr:hover td { background: #F8FAFC; }
.text-right { text-align: right; }
.text-center { text-align: center; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.text-blue-700 { color: #1D4ED8; }
.text-blue-800 { color: #1e40af; }
.text-emerald-600 { color: #059669; }
.text-emerald-700 { color: #047857; }
.text-violet-600 { color: #7C3AED; }
.text-violet-700 { color: #6D28D9; }
.text-gray-900 { color: #111827; }

.table-bar-wrap { display: flex; align-items: center; gap: 8px; }
.table-bar-track { flex: 1; min-width: 80px; height: 6px; background: #F1F5F9; border-radius: 999px; overflow: hidden; }
.table-bar-fill { height: 100%; border-radius: 999px; background: linear-gradient(to right, #3B82F6, #06B6D4); transition: width .4s; }
.table-bar-pct { font-size: 10px; font-weight: 700; color: #94A3B8; white-space: nowrap; width: 34px; text-align: right; }

/* Top rows */
.top-row { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 12px; transition: background .15s; }
.top-row:hover { background: #F8FAFC; }
.top-rank { width: 24px; height: 24px; border-radius: 8px; background: #F1F5F9; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 800; color: #94A3B8; flex-shrink: 0; }
.top-rank--gold   { background: #FEF3C7; color: #D97706; }
.top-rank--silver { background: #F1F5F9; color: #64748B; }
.top-rank--bronze { background: #FEF0E7; color: #C2410C; }
.top-info { flex: 1; min-width: 0; }
.top-name { font-size: 13px; font-weight: 600; color: #0F172A; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin: 0; }
.top-meta { font-size: 11px; color: #94A3B8; margin: 1px 0 0; }
.top-right { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; }
.top-revenue { font-size: 13px; font-weight: 700; color: #059669; }
.top-students { font-size: 11px; color: #94A3B8; }

/* Toast */
.toast { position: fixed; bottom: 24px; right: 24px; z-index: 2000; padding: 13px 18px; border-radius: 12px; border: 1px solid; font-family: inherit; font-size: 13.5px; font-weight: 700; box-shadow: 0 8px 30px rgba(0,0,0,.15); }
.toast--success { background: #ECFDF5; color: #065F46; border-color: #A7F3D0; }
.toast--error   { background: #FFF1F2; color: #9F1239; border-color: #FECDD3; }
.toast-enter-active,.toast-leave-active { transition: all .25s; }
.toast-enter-from,.toast-leave-to { opacity: 0; transform: translateY(10px); }

/* animate pulse for skeleton */
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }
.animate-pulse { animation: pulse 2s ease-in-out infinite; }
.space-y-3 > * + * { margin-top: 12px; }
.space-y-2 > * + * { margin-top: 8px; }
.py-8 { padding-top: 32px; padding-bottom: 32px; }
.text-sm { font-size: 13px; }
.text-gray-400 { color: #9CA3AF; }
.text-violet-700 { color: #6D28D9; }
.text-xs { font-size: 11px; }
.text-emerald-600 { color: #059669; }
.text-violet-600 { color: #7C3AED; }
.h-10 { height: 40px; }
.rounded-xl { border-radius: 12px; }
.bg-gray-100 { background: #F3F4F6; }
.overflow-x-auto { overflow-x: auto; }
</style>
