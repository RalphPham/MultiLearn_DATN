<template>
  <div class="min-h-screen bg-gray-50" style="font-family:'DM Sans','Segoe UI',system-ui,sans-serif;">

    <!-- ── Hero ── -->
    <div class="relative h-56 bg-cover bg-center overflow-hidden"
         style="background-image:url('https://images.unsplash.com/photo-1567427017947-545c5f8d16ad?w=1920&q=60')">
      <div class="absolute inset-0 bg-slate-900/70 backdrop-blur-[1px]"></div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-full flex flex-col justify-center">
        <p class="text-yellow-300 text-xs font-black uppercase tracking-widest mb-2">Hành trình của tôi</p>
        <h1 class="text-3xl lg:text-4xl font-black text-white">Thành tích & Huy hiệu</h1>
        <div v-if="!isLoading" class="flex flex-wrap gap-3 mt-4">
          <div class="flex items-center gap-2 bg-yellow-500/30 backdrop-blur text-yellow-200 px-4 py-1.5 rounded-full text-sm font-semibold border border-yellow-400/30">
            🏅 {{ earnedCount }}/{{ badges.length }} huy hiệu
          </div>
          <div class="flex items-center gap-2 bg-orange-500/30 backdrop-blur text-orange-200 px-4 py-1.5 rounded-full text-sm font-semibold border border-orange-400/30">
            🔥 Streak {{ currentStreak }} ngày
          </div>
          <div class="flex items-center gap-2 bg-white/10 backdrop-blur text-white px-4 py-1.5 rounded-full text-sm font-semibold border border-white/20">
            {{ Math.round(earnedCount / (badges.length || 1) * 100) }}% hoàn thành
          </div>
        </div>
      </div>
    </div>

    <!-- ── Content ── -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">

      <!-- ── Loading ── -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-24">
        <div class="animate-spin rounded-full h-14 w-14 border-b-2 border-yellow-500"></div>
        <p class="mt-5 text-gray-500">Đang tải huy hiệu...</p>
      </div>

      <template v-else>

        <!-- ── Tổng quan + streak nổi bật ── -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">

          <!-- Progress tổng -->
          <div class="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 class="font-bold text-gray-800 text-lg mb-4">Tiến trình tổng thể</h2>
            <div class="flex items-center gap-6 mb-4">
              <div class="text-center">
                <div class="text-4xl font-black text-blue-600">{{ earnedCount }}</div>
                <div class="text-xs text-gray-400 mt-0.5">Đã đạt</div>
              </div>
              <div class="flex-1">
                <div class="w-full bg-gray-100 rounded-full h-4 overflow-hidden">
                  <div
                    class="h-4 rounded-full bg-gradient-to-r from-yellow-400 to-orange-500 transition-all duration-700"
                    :style="{ width: (earnedCount / (badges.length || 1) * 100) + '%' }"
                  ></div>
                </div>
                <div class="flex justify-between text-xs text-gray-400 mt-1.5">
                  <span>0</span>
                  <span>{{ badges.length }} huy hiệu</span>
                </div>
              </div>
              <div class="text-center">
                <div class="text-4xl font-black text-gray-300">{{ badges.length - earnedCount }}</div>
                <div class="text-xs text-gray-400 mt-0.5">Còn lại</div>
              </div>
            </div>
            <!-- Category mini-bars -->
            <div class="space-y-2.5 mt-4">
              <div v-for="cat in categories" :key="cat.key" class="flex items-center gap-3">
                <span class="text-sm w-36 text-gray-600 font-medium flex items-center gap-1.5">
                  {{ cat.icon }} {{ cat.label }}
                </span>
                <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                  <div
                    class="h-2 rounded-full transition-all duration-500"
                    :class="cat.barColor"
                    :style="{ width: catProgress(cat.key) + '%' }"
                  ></div>
                </div>
                <span class="text-xs text-gray-400 w-12 text-right">
                  {{ badgesByCategory(cat.key).filter(b => b.isEarned).length }}/{{ badgesByCategory(cat.key).length }}
                </span>
              </div>
            </div>
          </div>

          <!-- Streak card -->
          <div class="bg-gradient-to-br from-orange-400 to-rose-500 rounded-2xl p-6 text-white shadow-lg flex flex-col justify-between">
            <div>
              <p class="text-orange-100 text-sm font-medium mb-2">Streak hiện tại</p>
              <div class="flex items-end gap-2">
                <span class="text-6xl font-black leading-none">{{ currentStreak }}</span>
                <span class="text-orange-200 text-lg mb-1">ngày</span>
              </div>
            </div>
            <div class="text-7xl mt-4 opacity-80 text-right">🔥</div>
            <p class="text-orange-100 text-sm mt-2">
              {{ currentStreak === 0
                ? 'Học bài hôm nay để bắt đầu streak!'
                : currentStreak >= 7
                  ? 'Xuất sắc! Duy trì đà học tập nhé!'
                  : `Còn ${7 - currentStreak} ngày để mở huy hiệu 🌟` }}
            </p>
          </div>
        </div>

        <!-- ── Badge groups ── -->
        <div v-for="cat in categories" :key="cat.key" class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h3 class="font-bold text-gray-700 text-sm uppercase tracking-widest mb-5 flex items-center gap-2">
            <span>{{ cat.icon }}</span> {{ cat.label }}
            <span class="ml-auto text-xs font-semibold text-gray-400 normal-case tracking-normal">
              {{ badgesByCategory(cat.key).filter(b => b.isEarned).length }}/{{ badgesByCategory(cat.key).length }} đã đạt
            </span>
          </h3>
          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            <div
              v-for="badge in badgesByCategory(cat.key)"
              :key="badge.key"
              :class="[
                'relative rounded-2xl p-5 flex flex-col items-center text-center transition-all duration-200 border-2',
                badge.isEarned
                  ? 'bg-gradient-to-b from-yellow-50 to-white border-yellow-300 shadow-md hover:shadow-lg'
                  : 'bg-gray-50 border-gray-100 opacity-50 cursor-not-allowed'
              ]"
            >
              <!-- Earned checkmark -->
              <div v-if="badge.isEarned"
                class="absolute -top-2.5 -right-2.5 w-7 h-7 bg-green-500 rounded-full flex items-center justify-center shadow-md">
                <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                </svg>
              </div>

              <!-- Lock icon -->
              <div v-else class="absolute -top-2.5 -right-2.5 w-7 h-7 bg-gray-300 rounded-full flex items-center justify-center shadow">
                <svg class="w-3.5 h-3.5 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
                </svg>
              </div>

              <div :class="['text-5xl mb-3', badge.isEarned ? '' : 'grayscale opacity-60']">
                {{ badge.icon }}
              </div>
              <p :class="['font-bold text-sm mb-1', badge.isEarned ? 'text-gray-900' : 'text-gray-400']">
                {{ badge.name }}
              </p>
              <p class="text-xs text-gray-400 leading-snug">{{ badge.description }}</p>

              <div class="mt-3">
                <span v-if="badge.isEarned && badge.earnedAt"
                  class="text-xs text-yellow-600 font-semibold bg-yellow-50 px-2 py-0.5 rounded-full">
                  {{ formatDate(badge.earnedAt) }}
                </span>
                <span v-else-if="badge.isEarned"
                  class="text-xs text-green-600 font-semibold bg-green-50 px-2 py-0.5 rounded-full">
                  ✓ Đã đạt được
                </span>
                <span v-else class="text-xs text-gray-300">Chưa mở khóa</span>
              </div>
            </div>
          </div>
        </div>

      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axiosClient from '@/api/axiosClient'

const badges = ref([])
const streakValue = ref(0)
const isLoading = ref(false)

const categories = [
  { key: 'COURSE',  icon: '📖', label: 'Hoàn thành khóa học', barColor: 'bg-blue-500' },
  { key: 'STREAK',  icon: '🔥', label: 'Streak học tập',      barColor: 'bg-orange-500' },
  { key: 'SPECIAL', icon: '⭐', label: 'Thành tích đặc biệt', barColor: 'bg-violet-500' },
]

const earnedCount = computed(() => badges.value.filter(b => b.isEarned).length)

const currentStreak = computed(() => streakValue.value)

const badgesByCategory = (cat) => badges.value.filter(b => b.category === cat)

const catProgress = (cat) => {
  const list = badgesByCategory(cat)
  if (!list.length) return 0
  return list.filter(b => b.isEarned).length / list.length * 100
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await axiosClient.get('/student/achievements')
    badges.value = Array.isArray(data?.badges) ? data.badges : (Array.isArray(data) ? data : [])
    streakValue.value = data?.currentStreak ?? 0
  } catch {
    badges.value = []
  } finally {
    isLoading.value = false
  }
})
</script>
