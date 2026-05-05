<template>
  <div class="rev-page">

    <!-- Header -->
    <div class="rev-header">
      <div>
        <h1 class="rev-title">Thống Kê Doanh Thu</h1>
        <p class="rev-sub">Thu nhập thực sau khi trừ hoàn tiền × hoa hồng 30%</p>
      </div>
      <button @click="fetchRevenue" class="btn-reload" :disabled="loading">
        <svg :class="loading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <div class="preset-group">
        <button v-for="p in presets" :key="p.key"
          :class="['preset-btn', activePreset === p.key && 'preset-btn--on']"
          @click="applyPreset(p)">{{ p.label }}</button>
      </div>
      <div class="filter-sep"></div>
      <div class="date-range-group">
        <label class="range-label">Từ</label>
        <input type="month" v-model="fromMonth" class="month-input" :max="toMonth || undefined"/>
        <label class="range-label">đến</label>
        <input type="month" v-model="toMonth" class="month-input" :min="fromMonth || undefined"/>
        <button @click="applyCustomRange" class="btn-apply">Áp dụng</button>
      </div>
    </div>

    <!-- Period label -->
    <div v-if="periodLabel" class="period-label">
      Đang xem: <strong>{{ periodLabel }}</strong>
    </div>

    <!-- Error -->
    <div v-if="errorMessage" class="rev-error">{{ errorMessage }}</div>

    <!-- Metric cards -->
    <div class="metric-grid">
      <div class="metric-card metric-card--green">
        <div class="metric-icon metric-icon--green">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
          </svg>
        </div>
        <div>
          <div class="metric-label">Doanh Thu Gộp</div>
          <div class="metric-value">
            <span v-if="loading" class="skel"></span>
            <span v-else>{{ fmt(metrics.grossRevenue) }}</span>
          </div>
          <div class="metric-sub">Tổng tiền học viên đã trả</div>
        </div>
      </div>

      <div class="metric-card metric-card--red">
        <div class="metric-icon metric-icon--red">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12h18M3 6h18M3 18h18"/>
          </svg>
        </div>
        <div>
          <div class="metric-label">Đã Hoàn Tiền</div>
          <div class="metric-value metric-value--red">
            <span v-if="loading" class="skel"></span>
            <span v-else>- {{ fmt(metrics.refundAmount) }}</span>
          </div>
          <div class="metric-sub">
            <span v-if="!loading && metrics.grossRevenue > 0" class="badge badge--red">
              {{ refundRate }}% bị hoàn
            </span>
          </div>
        </div>
      </div>

      <div class="metric-card metric-card--indigo metric-card--highlight">
        <div class="metric-icon metric-icon--indigo">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
        <div>
          <div class="metric-label">Thu Nhập Thực</div>
          <div class="metric-value metric-value--indigo">
            <span v-if="loading" class="skel"></span>
            <span v-else>{{ fmt(metrics.netRevenue) }}</span>
          </div>
          <div class="metric-sub">
            <span v-if="!loading && metrics.grossRevenue > 0" class="badge badge--indigo">
              Giữ {{ retentionRate }}% doanh thu
            </span>
          </div>
        </div>
      </div>

      <div class="metric-card metric-card--blue">
        <div class="metric-icon metric-icon--blue">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
          </svg>
        </div>
        <div>
          <div class="metric-label">Học Viên</div>
          <div class="metric-value">
            <span v-if="loading" class="skel skel--sm"></span>
            <span v-else>{{ fmtNum(metrics.studentsCount) }}</span>
          </div>
          <div class="metric-sub">Học viên đã mua kỳ này</div>
        </div>
      </div>

      <div class="metric-card metric-card--amber">
        <div class="metric-icon metric-icon--amber">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/>
          </svg>
        </div>
        <div>
          <div class="metric-label">Đơn Bán</div>
          <div class="metric-value">
            <span v-if="loading" class="skel skel--sm"></span>
            <span v-else>{{ fmtNum(metrics.salesCount) }}</span>
          </div>
          <div class="metric-sub">Tổng lượt mua thành công</div>
        </div>
      </div>
    </div>

    <!-- Chart -->
    <div class="chart-card">
      <div class="chart-card__header">
        <div>
          <h2 class="chart-title">Biểu Đồ Doanh Thu Theo Tháng</h2>
          <p class="chart-sub">
            <span class="legend-dot legend-dot--green"></span> Doanh thu gộp &nbsp;
            <span class="legend-dot legend-dot--red"></span> Hoàn tiền &nbsp;
            <span class="legend-dot legend-dot--indigo"></span> Thu nhập thực
          </p>
        </div>
        <button @click="exportChartCSV" class="btn-csv" title="Xuất CSV">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke-linecap="round"/>
          </svg>
          CSV
        </button>
      </div>
      <div class="chart-body">
        <div v-if="loading" class="chart-center"><div class="spinner"></div></div>
        <Line v-else-if="chartDataReady" :data="chartData" :options="chartOptions" style="width:100%;height:100%"/>
        <div v-else class="chart-center chart-empty">Chưa có dữ liệu trong kỳ này</div>
      </div>
    </div>

    <!-- Monthly breakdown table -->
    <div class="table-card" v-if="chartRaw.length > 0 || loading">
      <div class="table-card__header">
        <h2 class="table-card__title">📅 Chi Tiết Từng Tháng</h2>
        <button @click="exportTableCSV" class="btn-csv">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke-linecap="round"/>
          </svg>
          CSV
        </button>
      </div>
      <div v-if="loading" class="table-loading">Đang tải...</div>
      <div v-else class="table-wrap">
        <table class="breakdown-table">
          <thead>
            <tr>
              <th>Tháng</th>
              <th class="num">Doanh Thu Gộp</th>
              <th class="num">Hoàn Tiền</th>
              <th class="num">Thu Nhập Thực</th>
              <th class="num">Tỉ Lệ Giữ</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in sortedChart" :key="`${row.year}-${row.month}`">
              <td class="month-cell">{{ fmtMonthLabel(row) }}</td>
              <td class="num green">{{ fmt(row.gross) }}</td>
              <td class="num red">{{ row.refund > 0 ? '- ' + fmt(row.refund) : '—' }}</td>
              <td class="num indigo bold">{{ fmt(row.net) }}</td>
              <td class="num">
                <span v-if="row.gross > 0" class="pct-badge" :class="retentionClass(row)">
                  {{ Math.round((row.net / row.gross) * 100) }}%
                </span>
                <span v-else class="text-muted">—</span>
              </td>
            </tr>
            <tr class="total-row">
              <td><strong>Tổng cộng</strong></td>
              <td class="num green bold">{{ fmt(metrics.grossRevenue) }}</td>
              <td class="num red bold">{{ metrics.refundAmount > 0 ? '- ' + fmt(metrics.refundAmount) : '—' }}</td>
              <td class="num indigo bold">{{ fmt(metrics.netRevenue) }}</td>
              <td class="num"><span class="pct-badge pct-badge--indigo">{{ retentionRate }}%</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Bottom: Top courses + Transactions -->
    <div class="bottom-grid">

      <div class="table-card">
        <div class="table-card__header">
          <h2 class="table-card__title">🔥 Khóa Học Doanh Thu Cao</h2>
          <span class="table-card__sub">Trong kỳ đã chọn</span>
        </div>
        <div v-if="loading" class="table-loading">Đang tải...</div>
        <div v-else-if="!topCourses.length" class="table-empty">Chưa có dữ liệu</div>
        <div v-else class="top-list">
          <div v-for="(c, i) in topCourses" :key="c.courseId" class="top-item">
            <span class="top-rank" :class="`top-rank--${i+1}`">{{ i + 1 }}</span>
            <img v-if="c.thumbnail" :src="c.thumbnail" class="top-thumb" alt=""/>
            <div v-else class="top-thumb top-thumb--empty">📚</div>
            <div class="top-info">
              <div class="top-name">{{ c.courseTitle }}</div>
              <div class="top-meta">{{ fmtNum(c.students) }} học viên</div>
            </div>
            <div>
              <div class="top-rev">{{ fmtShort(c.revenue) }}</div>
              <div v-if="metrics.grossRevenue > 0" class="top-pct">
                {{ Math.round((c.revenue / metrics.grossRevenue) * 100) }}% tổng
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="table-card">
        <div class="table-card__header">
          <h2 class="table-card__title">🧾 Giao Dịch Gần Đây</h2>
          <span class="table-card__sub">Hoàn thành & hoàn tiền</span>
        </div>
        <div v-if="loading" class="table-loading">Đang tải...</div>
        <div v-else-if="!transactions.length" class="table-empty">Chưa có giao dịch</div>
        <div v-else class="tx-list">
          <div v-for="(tx, i) in transactions" :key="i" class="tx-item">
            <div class="tx-avatar">{{ (tx.studentName || '?')[0].toUpperCase() }}</div>
            <div class="tx-info">
              <div class="tx-student">{{ tx.studentName }}</div>
              <div class="tx-course">{{ tx.courseTitle }}</div>
            </div>
            <div class="tx-right">
              <div class="tx-amount">{{ fmtShort(tx.amount) }}</div>
              <div class="tx-date">{{ fmtDate(tx.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS, CategoryScale, LinearScale,
  PointElement, LineElement, Title, Tooltip, Legend, Filler
} from 'chart.js'
import instructorRevenueService from '@/services/instructorRevenue.service'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

// ── STATE
const loading       = ref(false)
const errorMessage  = ref('')
const activePreset  = ref('year')
const fromMonth     = ref('')
const toMonth       = ref('')
const periodLabel   = ref('')

const metrics      = ref({ grossRevenue: 0, refundAmount: 0, netRevenue: 0, studentsCount: 0, salesCount: 0 })
const chartRaw     = ref([])
const topCourses   = ref([])
const transactions = ref([])

// ── PRESETS
const now = new Date()
const presets = [
  { key: '3m',   label: '3 tháng gần nhất' },
  { key: '6m',   label: '6 tháng gần nhất' },
  { key: 'year', label: 'Năm nay' },
  { key: 'last', label: 'Năm ngoái' },
]

const applyPreset = (p) => {
  activePreset.value = p.key
  fromMonth.value = ''
  toMonth.value = ''
  fetchRevenue()
}

const applyCustomRange = () => {
  if (!fromMonth.value && !toMonth.value) return
  activePreset.value = 'custom'
  fetchRevenue()
}

// Build API params from current filter state
const buildParams = () => {
  if (activePreset.value === 'custom') {
    return {
      fromDate: fromMonth.value ? fromMonth.value + '-01' : undefined,
      toDate:   toMonth.value   ? toMonth.value + '-28'  : undefined,
    }
  }
  if (activePreset.value === 'year') return { year: now.getFullYear() }
  if (activePreset.value === 'last') return { year: now.getFullYear() - 1 }
  if (activePreset.value === '6m') {
    const d = new Date(now); d.setMonth(d.getMonth() - 5)
    return { fromDate: `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-01` }
  }
  if (activePreset.value === '3m') {
    const d = new Date(now); d.setMonth(d.getMonth() - 2)
    return { fromDate: `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-01` }
  }
  return {}
}

const buildPeriodLabel = () => {
  if (activePreset.value === 'year')   return `Năm ${now.getFullYear()}`
  if (activePreset.value === 'last')   return `Năm ${now.getFullYear() - 1}`
  if (activePreset.value === '6m')     return '6 tháng gần nhất'
  if (activePreset.value === '3m')     return '3 tháng gần nhất'
  if (activePreset.value === 'custom') {
    const f = fromMonth.value ? fromMonth.value.replace('-', '/') : '...'
    const t = toMonth.value   ? toMonth.value.replace('-', '/')   : '...'
    return `${f} → ${t}`
  }
  return ''
}

// ── COMPUTED
const sortedChart = computed(() =>
  [...chartRaw.value].sort((a, b) => {
    const ay = a.year ?? now.getFullYear(), by = b.year ?? now.getFullYear()
    return ay !== by ? ay - by : (a.month ?? 0) - (b.month ?? 0)
  })
)

const refundRate = computed(() => {
  if (!metrics.value.grossRevenue) return 0
  return Math.round((metrics.value.refundAmount / metrics.value.grossRevenue) * 100)
})

const retentionRate = computed(() => {
  if (!metrics.value.grossRevenue) return 0
  return Math.round((metrics.value.netRevenue / metrics.value.grossRevenue) * 100)
})

const retentionClass = (row) => {
  const pct = Math.round((row.net / row.gross) * 100)
  if (pct >= 25) return 'pct-badge--indigo'
  if (pct >= 15) return 'pct-badge--amber'
  return 'pct-badge--red'
}

// ── CHART
const chartDataReady = computed(() => sortedChart.value.length > 0)

const chartData = computed(() => ({
  labels: sortedChart.value.map(p => fmtMonthLabel(p)),
  datasets: [
    {
      label: 'Doanh thu gộp',
      data: sortedChart.value.map(p => p.gross ?? 0),
      borderColor: '#10B981', backgroundColor: 'rgba(16,185,129,0.07)',
      pointBackgroundColor: '#fff', pointBorderColor: '#10B981', pointBorderWidth: 2, pointRadius: 4,
      fill: true, tension: 0.35,
    },
    {
      label: 'Hoàn tiền',
      data: sortedChart.value.map(p => p.refund ?? 0),
      borderColor: '#EF4444', backgroundColor: 'rgba(239,68,68,0.05)',
      pointBackgroundColor: '#fff', pointBorderColor: '#EF4444', pointBorderWidth: 2, pointRadius: 4,
      fill: true, tension: 0.35,
    },
    {
      label: 'Thu nhập thực',
      data: sortedChart.value.map(p => p.net ?? 0),
      borderColor: '#6366F1', backgroundColor: 'rgba(99,102,241,0.08)',
      pointBackgroundColor: '#fff', pointBorderColor: '#6366F1', pointBorderWidth: 2, pointRadius: 4,
      fill: true, tension: 0.35,
    },
  ]
}))

const chartOptions = {
  responsive: true, maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: {
      display: false,
    },
    tooltip: {
      backgroundColor: '#0F172A', titleColor: '#94A3B8', bodyColor: '#F8FAFC',
      borderColor: 'rgba(148,163,184,0.2)', borderWidth: 1, padding: 12, cornerRadius: 8,
      callbacks: {
        label: (ctx) => `  ${ctx.dataset.label}: ${fmt(ctx.raw)}`
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true, grid: { color: '#F1F5F9' }, border: { display: false },
      ticks: {
        color: '#94A3B8', font: { size: 11, weight: '600' },
        callback: v => v >= 1_000_000 ? (v/1_000_000).toFixed(1)+'M' : v >= 1000 ? (v/1000).toFixed(0)+'K' : v
      }
    },
    x: {
      grid: { display: false }, border: { display: false },
      ticks: { color: '#94A3B8', font: { size: 11, weight: '600' } }
    }
  }
}

// ── UTILS
const fmt = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(v) || 0)
const fmtNum = (n) => (Number(n) || 0).toLocaleString('vi-VN')
const fmtShort = (v) => {
  const n = Number(v) || 0
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + ' tr'
  if (n >= 1_000)     return (n / 1_000).toFixed(0) + 'K'
  return n.toLocaleString('vi-VN')
}
const fmtDate = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
const fmtMonthLabel = (p) => {
  const y = p.year ?? now.getFullYear()
  return `${String(p.month).padStart(2, '0')}/${y}`
}

// ── FETCH
const fetchRevenue = async () => {
  loading.value = true
  errorMessage.value = ''
  periodLabel.value = buildPeriodLabel()
  try {
    const res = await instructorRevenueService.getRevenueSummary(buildParams())
    const payload = res?.data ?? res ?? {}
    const m = payload.metrics ?? {}
    metrics.value = {
      grossRevenue:  Number(m.grossRevenue  ?? 0),
      refundAmount:  Number(m.refundAmount  ?? 0),
      netRevenue:    Number(m.netRevenue    ?? 0),
      studentsCount: Number(m.studentsCount ?? 0),
      salesCount:    Number(m.salesCount    ?? 0),
    }
    chartRaw.value     = Array.isArray(payload.chart)         ? payload.chart         : []
    topCourses.value   = Array.isArray(payload.topCourses)    ? payload.topCourses    : []
    transactions.value = Array.isArray(payload.transactions)  ? payload.transactions  : []
  } catch (e) {
    console.error(e)
    errorMessage.value = e?.response?.data?.message || e?.message || 'Không thể tải dữ liệu.'
  } finally {
    loading.value = false
  }
}

// ── CSV EXPORT
const downloadCSV = (filename, rows) => {
  const csv = rows.map(r => r.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = filename; a.click()
  URL.revokeObjectURL(url)
}

const exportChartCSV = () => {
  const rows = [['Tháng', 'Doanh Thu Gộp', 'Hoàn Tiền', 'Thu Nhập Thực']]
  sortedChart.value.forEach(r => rows.push([fmtMonthLabel(r), r.gross ?? 0, r.refund ?? 0, r.net ?? 0]))
  downloadCSV('doanhthu_thang.csv', rows)
}

const exportTableCSV = exportChartCSV

onMounted(fetchRevenue)
</script>

<style scoped>
.rev-page {
  padding: 28px 32px 56px;
  max-width: 1200px;
  display: flex; flex-direction: column; gap: 20px;
  font-family: 'Inter', 'Plus Jakarta Sans', sans-serif;
  color: #1E293B;
}

/* ── Header */
.rev-header {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap;
}
.rev-title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 4px; }
.rev-sub   { font-size: 13px; color: #64748B; margin: 0; }
.btn-reload {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 10px;
  background: #0F172A; border: none; color: white;
  font-family: inherit; font-size: 13px; font-weight: 700; cursor: pointer; transition: background .16s;
  white-space: nowrap;
}
.btn-reload:hover:not(:disabled) { background: #1E293B; }
.btn-reload:disabled { opacity: .5; cursor: not-allowed; }
@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin .8s linear infinite; }

/* ── Filter bar */
.filter-bar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  background: white; border: 1px solid #E2E8F0; border-radius: 16px;
  padding: 14px 18px;
}
.preset-group { display: flex; gap: 6px; flex-wrap: wrap; }
.preset-btn {
  padding: 6px 14px; border-radius: 8px; border: 1px solid #E2E8F0;
  background: white; font-family: inherit; font-size: 12px; font-weight: 700;
  color: #64748B; cursor: pointer; transition: all .15s; white-space: nowrap;
}
.preset-btn:hover   { border-color: #6366F1; color: #4338CA; }
.preset-btn--on     { background: #0F172A; border-color: #0F172A; color: white; }

.filter-sep { width: 1px; height: 32px; background: #E2E8F0; flex-shrink: 0; }

.date-range-group { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.range-label { font-size: 12px; font-weight: 600; color: #94A3B8; }
.month-input {
  border: 1px solid #E2E8F0; border-radius: 8px; padding: 6px 10px;
  font-family: inherit; font-size: 12px; font-weight: 600; color: #475569;
  background: #F8FAFC; outline: none; cursor: pointer;
}
.month-input:focus { border-color: #6366F1; background: white; }
.btn-apply {
  padding: 6px 14px; border-radius: 8px;
  background: #6366F1; border: none; color: white;
  font-family: inherit; font-size: 12px; font-weight: 700; cursor: pointer; transition: background .15s;
}
.btn-apply:hover { background: #4F46E5; }

.period-label {
  font-size: 12.5px; color: #64748B; font-weight: 500;
  padding: 0 4px;
}

.rev-error {
  background: #FFF1F2; border: 1px solid #FECDD3; color: #BE123C;
  border-radius: 12px; padding: 14px 18px; font-size: 13px; font-weight: 600;
}

/* ── Metric cards */
.metric-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px;
}
@media (max-width: 1100px) { .metric-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 680px)  { .metric-grid { grid-template-columns: repeat(2, 1fr); } }

.metric-card {
  background: white; border: 1px solid #E2E8F0; border-radius: 16px;
  padding: 18px 16px; display: flex; align-items: flex-start; gap: 12px;
  transition: box-shadow .2s, transform .2s;
}
.metric-card:hover { box-shadow: 0 6px 24px rgba(0,0,0,.07); transform: translateY(-2px); }
.metric-card--highlight { border-color: #C7D2FE; background: #F5F3FF; }

.metric-icon {
  width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.metric-icon--green  { background: #ECFDF5; color: #059669; }
.metric-icon--red    { background: #FFF1F2; color: #E11D48; }
.metric-icon--indigo { background: #EEF2FF; color: #4338CA; }
.metric-icon--blue   { background: #EFF6FF; color: #1D4ED8; }
.metric-icon--amber  { background: #FFFBEB; color: #D97706; }

.metric-card--green  { border-left: 3px solid #10B981; }
.metric-card--red    { border-left: 3px solid #EF4444; }
.metric-card--indigo { border-left: 3px solid #6366F1; }
.metric-card--blue   { border-left: 3px solid #3B82F6; }
.metric-card--amber  { border-left: 3px solid #F59E0B; }

.metric-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .6px; color: #94A3B8; margin-bottom: 4px; }
.metric-value { font-size: 20px; font-weight: 800; color: #0F172A; line-height: 1.2; margin-bottom: 4px; }
.metric-value--red    { color: #E11D48; }
.metric-value--indigo { color: #4338CA; }
.metric-sub { font-size: 11px; color: #94A3B8; font-weight: 500; min-height: 18px; }

.badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 700;
}
.badge--red    { background: #FFF1F2; color: #BE123C; }
.badge--indigo { background: #EEF2FF; color: #3730A3; }

.skel {
  display: inline-block; width: 100px; height: 24px; border-radius: 6px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%; animation: shimmer 1.4s infinite;
}
.skel--sm { width: 50px; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* ── Chart */
.chart-card {
  background: white; border: 1px solid #E2E8F0; border-radius: 20px; overflow: hidden;
}
.chart-card__header {
  padding: 18px 22px 14px; border-bottom: 1px solid #F1F5F9;
  display: flex; align-items: flex-start; justify-content: space-between; gap: 12px;
}
.chart-title { font-size: 15px; font-weight: 800; color: #0F172A; margin: 0 0 6px; }
.chart-sub { font-size: 12px; color: #94A3B8; font-weight: 500; margin: 0; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.legend-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; }
.legend-dot--green  { background: #10B981; }
.legend-dot--red    { background: #EF4444; }
.legend-dot--indigo { background: #6366F1; }
.chart-body {
  height: 300px; padding: 16px 22px;
  display: flex; align-items: center; justify-content: center;
}
.chart-center { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.chart-empty { font-size: 13px; color: #94A3B8; font-weight: 500; }
.spinner {
  width: 36px; height: 36px; border-radius: 50%;
  border: 3px solid #E2E8F0; border-top-color: #6366F1;
  animation: spinC .7s linear infinite;
}
@keyframes spinC { to { transform: rotate(360deg); } }

.btn-csv {
  display: flex; align-items: center; gap: 5px;
  padding: 6px 12px; border-radius: 8px;
  border: 1px solid #E2E8F0; background: #F8FAFC; color: #64748B;
  font-family: inherit; font-size: 11px; font-weight: 700; cursor: pointer; white-space: nowrap;
}
.btn-csv:hover { background: #F1F5F9; }

/* ── Breakdown table */
.table-card {
  background: white; border: 1px solid #E2E8F0; border-radius: 20px; overflow: hidden;
}
.table-card__header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px 12px; border-bottom: 1px solid #F1F5F9;
}
.table-card__title { font-size: 15px; font-weight: 800; color: #0F172A; margin: 0; }
.table-card__sub   { font-size: 12px; color: #94A3B8; font-weight: 500; }
.table-loading, .table-empty {
  padding: 32px; text-align: center; color: #94A3B8; font-size: 13px; font-weight: 500;
}

.table-wrap { overflow-x: auto; }
.breakdown-table {
  width: 100%; border-collapse: collapse; font-size: 13px;
}
.breakdown-table th {
  padding: 10px 16px; text-align: left;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px;
  color: #94A3B8; background: #F8FAFC; border-bottom: 1px solid #F1F5F9;
}
.breakdown-table th.num { text-align: right; }
.breakdown-table td {
  padding: 11px 16px; border-bottom: 1px solid #F8FAFC;
}
.breakdown-table tr:last-child td { border-bottom: none; }
.breakdown-table tr:hover td { background: #FAFBFF; }
.breakdown-table td.num { text-align: right; font-variant-numeric: tabular-nums; }
.breakdown-table td.green  { color: #059669; font-weight: 600; }
.breakdown-table td.red    { color: #E11D48; font-weight: 600; }
.breakdown-table td.indigo { color: #4338CA; }
.breakdown-table td.bold   { font-weight: 800; }
.breakdown-table .text-muted { color: #CBD5E1; }
.month-cell { font-weight: 700; color: #1E293B; white-space: nowrap; }
.total-row td { background: #F5F3FF; font-weight: 700; border-top: 2px solid #E0E7FF; }

.pct-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 700;
}
.pct-badge--indigo { background: #EEF2FF; color: #3730A3; }
.pct-badge--amber  { background: #FFFBEB; color: #92400E; }
.pct-badge--red    { background: #FFF1F2; color: #BE123C; }

/* ── Bottom grid */
.bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 860px) { .bottom-grid { grid-template-columns: 1fr; } }

/* Top courses */
.top-list { padding: 6px 0; }
.top-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; transition: background .14s;
}
.top-item:hover { background: #F8FAFC; }
.top-rank {
  width: 24px; height: 24px; border-radius: 7px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 800; background: #F1F5F9; color: #64748B;
}
.top-rank--1 { background: #FEF3C7; color: #B45309; }
.top-rank--2 { background: #E2E8F0; color: #475569; }
.top-rank--3 { background: #FFEDD5; color: #C2410C; }
.top-thumb {
  width: 48px; height: 32px; border-radius: 8px; object-fit: cover;
  border: 1px solid #E2E8F0; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.top-thumb--empty { background: #F8FAFC; font-size: 16px; }
.top-info { flex: 1; min-width: 0; }
.top-name { font-size: 13px; font-weight: 700; color: #1E293B; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.top-meta { font-size: 11px; color: #94A3B8; margin-top: 2px; }
.top-rev  { font-size: 13px; font-weight: 800; color: #059669; text-align: right; }
.top-pct  { font-size: 10px; color: #94A3B8; text-align: right; margin-top: 2px; }

/* Transactions */
.tx-list { padding: 6px 0; max-height: 360px; overflow-y: auto; }
.tx-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; transition: background .14s;
}
.tx-item:hover { background: #F8FAFC; }
.tx-avatar {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #6366F1, #8B5CF6);
  color: white; font-size: 13px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.tx-info  { flex: 1; min-width: 0; }
.tx-student { font-size: 13px; font-weight: 700; color: #1E293B; }
.tx-course  { font-size: 11px; color: #94A3B8; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tx-right { text-align: right; flex-shrink: 0; }
.tx-amount { font-size: 13px; font-weight: 800; color: #059669; }
.tx-date   { font-size: 11px; color: #94A3B8; margin-top: 2px; }
</style>
