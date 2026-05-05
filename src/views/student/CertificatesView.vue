<template>
  <div class="certificates-page min-h-screen pb-12 pt-8">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
      <section class="hero-shell mb-8 overflow-hidden rounded-3xl border border-slate-800/60 px-6 py-7 text-white sm:px-8 lg:px-10">
        <div class="hero-orb hero-orb--one"></div>
        <div class="hero-orb hero-orb--two"></div>
        <div class="hero-grid relative z-10 grid gap-6 lg:grid-cols-[1fr_auto] lg:items-center">
          <div>
            <p class="hero-kicker mb-3 inline-flex items-center rounded-full border border-white/30 bg-white/10 px-3 py-1 text-xs font-bold uppercase tracking-[0.22em]">
              Certificate Center
            </p>
            <h1 class="text-3xl font-black leading-tight sm:text-4xl">Bộ sưu tập chứng chỉ</h1>
            <p class="mt-3 max-w-2xl text-sm text-blue-100 sm:text-base">
              Lưu trữ toàn bộ chứng chỉ hoàn thành khóa học của bạn. Mỗi chứng chỉ có mã xác thực riêng và có thể tải PDF bất kỳ lúc nào.
            </p>

            <div class="mt-5 grid gap-3 sm:grid-cols-3">
              <div class="stat-chip">
                <p class="stat-label">Tổng chứng chỉ</p>
                <p class="stat-value">{{ certificates.length }}</p>
              </div>
              <div class="stat-chip">
                <p class="stat-label">Mới nhất</p>
                <p class="stat-value text-base sm:text-lg">{{ latestIssuedLabel }}</p>
              </div>
              <div class="stat-chip">
                <p class="stat-label">Đã lọc</p>
                <p class="stat-value">{{ filteredCertificates.length }}</p>
              </div>
            </div>
          </div>

          <div class="hero-badge mx-auto hidden w-full max-w-[230px] lg:block">
            <div class="hero-badge-inner">
              <p class="text-xs font-semibold uppercase tracking-[0.2em] text-blue-200">Verified</p>
              <p class="mt-2 text-2xl font-black">100%</p>
              <p class="mt-1 text-xs text-slate-300">Tải xuống và xác minh trực tuyến</p>
            </div>
          </div>
        </div>
      </section>

      <section class="mb-6 rounded-2xl border border-slate-200 bg-white/90 p-4 shadow-sm backdrop-blur sm:p-5">
        <div class="flex flex-col gap-3 md:flex-row">
          <div class="relative flex-1">
            <svg class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm theo tên khóa học hoặc mã chứng chỉ..."
              class="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm text-slate-700 outline-none transition focus:border-blue-400 focus:ring-2 focus:ring-blue-200"
            />
          </div>

          <button
            class="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:border-blue-300 hover:text-blue-700"
            @click="fetchCertificates"
          >
            Làm mới
          </button>
        </div>
      </section>

      <div v-if="isLoading" class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
        <div v-for="n in 6" :key="n" class="certificate-card animate-pulse rounded-2xl border border-slate-200 bg-white p-5">
          <div class="mb-4 h-5 w-28 rounded bg-slate-200"></div>
          <div class="mb-2 h-7 w-4/5 rounded bg-slate-200"></div>
          <div class="mb-5 h-4 w-3/5 rounded bg-slate-100"></div>
          <div class="space-y-2">
            <div class="h-4 w-full rounded bg-slate-100"></div>
            <div class="h-4 w-4/5 rounded bg-slate-100"></div>
          </div>
          <div class="mt-6 h-10 w-full rounded-xl bg-slate-200"></div>
        </div>
      </div>

      <div
        v-else-if="certificates.length === 0"
        class="rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm"
      >
        <p class="text-xl font-bold text-slate-900">Bạn chưa có chứng chỉ nào</p>
        <p class="mx-auto mt-2 max-w-lg text-sm text-slate-500">
          Hãy hoàn thành khóa học để nhận chứng chỉ đầu tiên của bạn.
        </p>
        <router-link
          to="/my-courses"
          class="mt-6 inline-flex rounded-xl bg-slate-900 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700"
        >
          Quay lại khóa học của tôi
        </router-link>
      </div>

      <div
        v-else-if="filteredCertificates.length === 0"
        class="rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm"
      >
        <p class="text-lg font-bold text-slate-900">Không tìm thấy chứng chỉ phù hợp</p>
        <p class="mt-2 text-sm text-slate-500">Thử từ khóa khác hoặc bấm “Làm mới”.</p>
      </div>

      <div v-else class="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
        <article
          v-for="cert in filteredCertificates"
          :key="cert.id"
          class="certificate-card group flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-lg"
        >
          <div class="mb-4 flex items-start justify-between gap-4">
            <div>
              <p class="inline-flex rounded-full bg-blue-50 px-2.5 py-1 text-xs font-bold uppercase tracking-wide text-blue-700">
                Chứng chỉ hoàn thành
              </p>
              <h2 class="mt-3 line-clamp-2 text-xl font-black leading-snug text-slate-900">
                {{ cert.courseName || 'Khóa học chưa đặt tên' }}
              </h2>
            </div>

            <span class="inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
              <span class="mr-1 h-1.5 w-1.5 rounded-full bg-emerald-500"></span>
              Verified
            </span>
          </div>

          <dl class="space-y-2 text-sm text-slate-600">
            <div class="flex items-center justify-between gap-4">
              <dt class="text-slate-500">Ngày cấp</dt>
              <dd class="font-semibold text-slate-800">{{ formatDate(cert.issueDate) }}</dd>
            </div>
            <div class="space-y-1">
              <dt class="text-slate-500">Mã xác thực</dt>
              <dd class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 font-mono text-xs font-semibold tracking-wide text-slate-700">
                {{ cert.verifyCode || 'N/A' }}
              </dd>
            </div>
          </dl>

          <div class="mt-6 grid grid-cols-2 gap-2">
            <button
              class="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm font-semibold text-slate-700 transition hover:border-blue-300 hover:text-blue-700"
              @click="copyVerifyCode(cert.verifyCode)"
            >
              Sao chép mã
            </button>
            <button
              class="inline-flex items-center justify-center rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 px-3 py-2.5 text-sm font-semibold text-white transition hover:from-blue-700 hover:to-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="downloadingCourseId === cert.courseId"
              @click="downloadCertificate(cert)"
            >
              <span v-if="downloadingCourseId === cert.courseId">Đang tải...</span>
              <span v-else>Tải PDF</span>
            </button>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axiosClient from '@/api/axiosClient'
import notify, { getErrorMessage } from '@/utils/notify'

const certificates = ref([])
const isLoading = ref(false)
const downloadingCourseId = ref(null)
const searchQuery = ref('')

const formatDate = (value) => {
  if (!value) return 'N/A'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'N/A'
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date)
}

const normalizeSearch = (value) => {
  const raw = String(value || '').trim().toLowerCase()
  return raw.normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

const filteredCertificates = computed(() => {
  const keyword = normalizeSearch(searchQuery.value)
  if (!keyword) return certificates.value

  return certificates.value.filter((cert) => {
    const courseName = normalizeSearch(cert?.courseName)
    const verifyCode = normalizeSearch(cert?.verifyCode)
    return courseName.includes(keyword) || verifyCode.includes(keyword)
  })
})

const latestIssuedLabel = computed(() => {
  if (!certificates.value.length) return 'Chưa có'

  const latest = certificates.value
    .map((cert) => ({
      issueDate: cert?.issueDate,
      ts: cert?.issueDate ? new Date(cert.issueDate).getTime() : 0
    }))
    .filter((item) => Number.isFinite(item.ts) && item.ts > 0)
    .sort((a, b) => b.ts - a.ts)[0]

  return latest ? formatDate(latest.issueDate) : 'Chưa có'
})

const fetchCertificates = async () => {
  isLoading.value = true
  try {
    const data = await axiosClient.get('/certificates/my-certificates')
    certificates.value = Array.isArray(data) ? data : []
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không tải được danh sách chứng chỉ.'))
  } finally {
    isLoading.value = false
  }
}

const copyVerifyCode = async (code) => {
  if (!code) {
    notify.warning('Chứng chỉ này chưa có mã xác thực.')
    return
  }

  try {
    await navigator.clipboard.writeText(code)
    notify.success('Đã sao chép mã chứng chỉ.')
  } catch {
    notify.error('Không thể sao chép. Vui lòng thử lại.')
  }
}

const downloadCertificate = async (cert) => {
  if (!cert?.courseId) return

  downloadingCourseId.value = cert.courseId
  try {
    const blob = await axiosClient.get(`/certificates/download/${cert.courseId}`, {
      responseType: 'blob'
    })

    const fileBlob = blob instanceof Blob ? blob : new Blob([blob], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(fileBlob)
    const link = document.createElement('a')
    const safeName = (cert.courseName || `course_${cert.courseId}`).replace(/[^a-zA-Z0-9_-]/g, '_')

    link.href = url
    link.download = `certificate_${safeName}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không tải được file chứng chỉ.'))
  } finally {
    downloadingCourseId.value = null
  }
}

onMounted(fetchCertificates)
</script>

<style scoped>
.certificates-page {
  background:
    radial-gradient(circle at 92% 8%, rgba(99, 102, 241, 0.18), transparent 26%),
    radial-gradient(circle at 8% 88%, rgba(14, 165, 233, 0.13), transparent 24%),
    linear-gradient(180deg, #f8fafc 0%, #eef2ff 38%, #f8fafc 100%);
}

.hero-shell {
  position: relative;
  background: linear-gradient(135deg, #0f172a 0%, #172554 42%, #1d4ed8 100%);
  box-shadow: 0 24px 48px rgba(30, 41, 59, 0.2);
}

.hero-orb {
  position: absolute;
  border-radius: 9999px;
  filter: blur(2px);
}

.hero-orb--one {
  width: 180px;
  height: 180px;
  top: -60px;
  right: -34px;
  background: radial-gradient(circle at center, rgba(56, 189, 248, 0.38), rgba(59, 130, 246, 0));
}

.hero-orb--two {
  width: 220px;
  height: 220px;
  left: -70px;
  bottom: -110px;
  background: radial-gradient(circle at center, rgba(167, 139, 250, 0.24), rgba(30, 64, 175, 0));
}

.stat-chip {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  border-radius: 0.9rem;
  padding: 0.65rem 0.75rem;
}

.stat-label {
  font-size: 0.68rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #bfdbfe;
  margin-bottom: 0.15rem;
}

.stat-value {
  font-size: 1.25rem;
  line-height: 1.2;
  font-weight: 800;
  color: #ffffff;
}

.hero-badge {
  border-radius: 1rem;
  padding: 0.8rem;
  background: linear-gradient(140deg, rgba(15, 23, 42, 0.62), rgba(30, 58, 138, 0.48));
  border: 1px solid rgba(191, 219, 254, 0.25);
}

.hero-badge-inner {
  border-radius: 0.8rem;
  padding: 1rem;
  background: rgba(2, 6, 23, 0.35);
  border: 1px solid rgba(125, 211, 252, 0.2);
}

@media (max-width: 640px) {
  .hero-shell {
    border-radius: 1.2rem;
  }
}
</style>
