<template>
  <div class="min-h-screen bg-gray-50 p-8 font-sans text-gray-900">
    <div class="max-w-6xl mx-auto">
      <div class="mb-10">
        <h1 class="text-3xl font-bold text-gray-900 mb-3">Công cụ giảng dạy</h1>
        <p class="text-gray-500 text-lg">Các tiện ích hỗ trợ bạn tạo khóa học chất lượng cao và tiếp cận học viên tốt hơn.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">

        <!-- Card: Kiểm tra Video -->
        <div
          class="bg-white p-8 rounded-xl border border-blue-200 shadow-sm hover:shadow-lg transition-all duration-300 group cursor-pointer flex flex-col"
          @click="openVideoChecker"
        >
          <div class="w-16 h-16 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-3">Kiểm tra Video & Âm thanh</h3>
          <p class="text-gray-500 text-sm mb-8 leading-relaxed flex-1">Tải lên video mẫu để kiểm tra resolution, bitrate, định dạng và âm thanh trước khi quay toàn bộ khóa học.</p>
          <div class="mt-auto">
            <button class="text-blue-600 font-bold text-sm hover:underline flex items-center gap-1 group-hover:gap-2 transition-all">
              Kiểm tra ngay
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3" /></svg>
            </button>
          </div>
        </div>

        <!-- Card: Thông tin thị trường -->
        <div
          class="bg-white p-8 rounded-xl border border-purple-200 shadow-sm hover:shadow-lg transition-all duration-300 group cursor-pointer flex flex-col"
          @click="openMarketModal"
        >
          <div class="w-16 h-16 bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-purple-600 group-hover:text-white transition-colors duration-300">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-3">Thông tin thị trường</h3>
          <p class="text-gray-500 text-sm mb-8 leading-relaxed flex-1">Khám phá nhu cầu của học viên và các chủ đề đang "hot" để lên kế hoạch cho khóa học tiếp theo của bạn.</p>
          <div class="mt-auto">
            <button class="text-purple-600 font-bold text-sm hover:underline flex items-center gap-1 group-hover:gap-2 transition-all">
              Phân tích thị trường
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3" /></svg>
            </button>
          </div>
        </div>

        <!-- Card: Coupon do nền tảng quản lý -->
        <div
          class="bg-white p-8 rounded-xl border border-green-200 shadow-sm transition-all duration-300 group cursor-pointer flex flex-col"
          @click="openCouponModal"
        >
          <div class="w-16 h-16 bg-green-100 text-green-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-green-600 group-hover:text-white transition-colors duration-300">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" /></svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-3">Coupon do nền tảng quản lý</h3>
          <p class="text-gray-500 text-sm mb-8 leading-relaxed flex-1">Giảng viên không tự tạo mã giảm giá. Vui lòng gửi đề xuất chiến dịch cho Admin/SuperAdmin.</p>
          <div class="mt-auto">
            <button class="text-green-600 font-bold text-sm flex items-center gap-1">
              Gửi đề xuất cho Admin
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3" /></svg>
            </button>
          </div>
        </div>

        <!-- Card: Cộng đồng -->
        <div class="bg-white p-8 rounded-xl border border-gray-200 shadow-sm hover:shadow-lg transition-all duration-300 group cursor-pointer flex flex-col">
          <div class="w-16 h-16 bg-orange-100 text-orange-600 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-orange-600 group-hover:text-white transition-colors duration-300">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-3">Cộng đồng giảng viên</h3>
          <p class="text-gray-500 text-sm mb-8 leading-relaxed flex-1">Tham gia thảo luận, chia sẻ kinh nghiệm và nhận hỗ trợ từ hàng ngàn giảng viên khác trên hệ thống.</p>
          <div class="mt-auto">
            <span class="inline-block px-3 py-1 bg-gray-100 text-gray-400 text-xs rounded-full">Sắp ra mắt</span>
          </div>
        </div>

      </div>
    </div>

    <!-- ════════════════════════════════════════════════════════
         MODAL VIDEO CHECKER
         ════════════════════════════════════════════════════════ -->
    <Teleport to="body">
      <div v-if="showVideoModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="showVideoModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-100 flex items-center justify-center">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" /></svg>
              </div>
              <div>
                <h2 class="text-lg font-bold text-gray-900">Kiểm tra chất lượng Video</h2>
                <p class="text-xs text-gray-400">Phân tích trực tiếp trong trình duyệt · Không upload lên server</p>
              </div>
            </div>
            <button @click="showVideoModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-500">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5">

            <!-- Drop zone -->
            <div
              class="border-2 border-dashed rounded-xl p-8 text-center transition-colors cursor-pointer"
              :class="videoDragging ? 'border-blue-400 bg-blue-50' : 'border-gray-200 hover:border-blue-300 hover:bg-gray-50'"
              @click="$refs.videoInput.click()"
              @dragover.prevent="videoDragging = true"
              @dragleave="videoDragging = false"
              @drop.prevent="onVideoDrop"
            >
              <svg class="w-10 h-10 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
              </svg>
              <p class="text-sm font-semibold text-gray-700">Kéo thả hoặc click để chọn video</p>
              <p class="text-xs text-gray-400 mt-1">MP4, MOV, AVI, MKV, WebM — tối đa phân tích metadata</p>
              <input ref="videoInput" type="file" accept="video/*" class="hidden" @change="onVideoSelected" />
            </div>

            <!-- Analyzing -->
            <div v-if="videoAnalyzing" class="flex items-center justify-center gap-3 py-6">
              <div class="animate-spin rounded-full h-6 w-6 border-4 border-blue-500 border-t-transparent"></div>
              <span class="text-sm text-gray-500 font-medium">Đang đọc metadata video...</span>
            </div>

            <!-- Results -->
            <div v-if="videoResult && !videoAnalyzing" class="space-y-4">

              <!-- File info bar -->
              <div class="bg-gray-50 rounded-xl px-4 py-3 flex items-center gap-3">
                <svg class="w-5 h-5 text-blue-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-800 truncate">{{ videoResult.fileName }}</p>
                  <p class="text-xs text-gray-400">{{ videoResult.fileSize }} · {{ videoResult.mimeType }}</p>
                </div>
                <!-- Overall badge -->
                <span class="flex-shrink-0 px-3 py-1 rounded-full text-xs font-bold"
                  :class="{
                    'bg-emerald-100 text-emerald-700': videoResult.overallScore === 'GOOD',
                    'bg-amber-100 text-amber-700':    videoResult.overallScore === 'WARNING',
                    'bg-red-100 text-red-600':        videoResult.overallScore === 'FAIL'
                  }">
                  {{ videoResult.overallScore === 'GOOD' ? '✓ Đạt yêu cầu' : videoResult.overallScore === 'WARNING' ? '⚠ Cần cải thiện' : '✗ Không đạt' }}
                </span>
              </div>

              <!-- Checks grid -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div v-for="check in videoResult.checks" :key="check.label"
                  class="rounded-xl border p-4"
                  :class="{
                    'border-emerald-200 bg-emerald-50': check.status === 'GOOD',
                    'border-amber-200 bg-amber-50':    check.status === 'WARNING',
                    'border-red-200 bg-red-50':        check.status === 'FAIL',
                    'border-gray-200 bg-gray-50':      check.status === 'INFO'
                  }">
                  <div class="flex items-start justify-between gap-2">
                    <p class="text-xs font-bold uppercase tracking-wide"
                      :class="{
                        'text-emerald-700': check.status === 'GOOD',
                        'text-amber-700':   check.status === 'WARNING',
                        'text-red-600':     check.status === 'FAIL',
                        'text-gray-500':    check.status === 'INFO'
                      }">{{ check.label }}</p>
                    <span class="text-base flex-shrink-0">
                      {{ check.status === 'GOOD' ? '✓' : check.status === 'WARNING' ? '⚠' : check.status === 'FAIL' ? '✗' : 'ℹ' }}
                    </span>
                  </div>
                  <p class="text-base font-bold text-gray-900 mt-1">{{ check.value }}</p>
                  <p class="text-xs mt-0.5"
                    :class="{
                      'text-emerald-600': check.status === 'GOOD',
                      'text-amber-600':   check.status === 'WARNING',
                      'text-red-500':     check.status === 'FAIL',
                      'text-gray-400':    check.status === 'INFO'
                    }">{{ check.note }}</p>
                </div>
              </div>

              <!-- Suggestions -->
              <div v-if="videoResult.suggestions.length" class="bg-blue-50 border border-blue-100 rounded-xl p-4">
                <p class="text-sm font-bold text-blue-700 mb-2">💡 Gợi ý cải thiện</p>
                <ul class="space-y-1">
                  <li v-for="s in videoResult.suggestions" :key="s" class="text-sm text-blue-700 flex items-start gap-2">
                    <span class="mt-0.5 flex-shrink-0">•</span>{{ s }}
                  </li>
                </ul>
              </div>

              <!-- Reset button -->
              <button @click="resetVideoChecker" class="w-full py-2.5 border border-gray-200 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50 transition">
                Kiểm tra video khác
              </button>
            </div>

          </div>
        </div>
      </div>
    </Teleport>

    <!-- ════════════════════════════════════════════════════════
         MODAL MARKET INSIGHT
         ════════════════════════════════════════════════════════ -->
    <Teleport to="body">
      <div v-if="showMarketModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="showMarketModal = false">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] flex flex-col">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-purple-100 flex items-center justify-center">
                <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              </div>
              <div>
                <h2 class="text-lg font-bold text-gray-900">Phân tích thị trường AI</h2>
                <p class="text-xs text-gray-400">Powered by Gemini AI · Dữ liệu thực từ nền tảng</p>
              </div>
            </div>
            <button @click="showMarketModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-500">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>

          <!-- Body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5">

            <!-- Input -->
            <div class="flex gap-3">
              <input
                v-model="marketTopic"
                type="text"
                placeholder="Nhập chủ đề bạn quan tâm... (VD: Lập trình Python, Thiết kế UI/UX) — hoặc để trống để phân tích tổng quát"
                class="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                @keyup.enter="runMarketAnalysis"
              />
              <button
                @click="runMarketAnalysis"
                :disabled="analyzing"
                class="px-5 py-2.5 bg-purple-600 text-white rounded-xl text-sm font-semibold hover:bg-purple-700 disabled:opacity-60 flex items-center gap-2 flex-shrink-0"
              >
                <svg v-if="analyzing" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                {{ analyzing ? 'Đang phân tích...' : 'Phân tích' }}
              </button>
            </div>

            <!-- Error -->
            <div v-if="marketError" class="p-4 bg-red-50 border border-red-200 rounded-xl text-sm text-red-600">
              {{ marketError }}
            </div>

            <!-- Loading skeleton -->
            <div v-if="analyzing" class="space-y-4">
              <div v-for="i in 4" :key="i" class="bg-gray-50 rounded-xl p-4 space-y-2 animate-pulse">
                <div class="h-4 bg-gray-200 rounded w-1/3"></div>
                <div class="h-3 bg-gray-200 rounded w-full"></div>
                <div class="h-3 bg-gray-200 rounded w-4/5"></div>
              </div>
            </div>

            <!-- Results -->
            <template v-if="marketResult && !analyzing">

              <!-- Hot courses từ DB thực tế -->
              <div v-if="marketResult.hotCourses && marketResult.hotCourses.length" class="bg-gray-50 rounded-xl p-5">
                <h3 class="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                  <svg class="w-4 h-4 text-orange-500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z"/></svg>
                  Top khóa học phổ biến nhất trên nền tảng
                </h3>
                <div class="grid grid-cols-2 gap-2">
                  <div
                    v-for="(c, i) in marketResult.hotCourses" :key="i"
                    class="flex items-center gap-2 bg-white rounded-lg px-3 py-2 border border-gray-100"
                  >
                    <span class="text-xs font-bold text-gray-300 w-4 flex-shrink-0">{{ i + 1 }}</span>
                    <div class="flex-1 min-w-0">
                      <p class="text-xs font-semibold text-gray-800 truncate">{{ c.title }}</p>
                      <p class="text-xs text-gray-400">{{ c.category }} · {{ c.students }} học viên · ⭐ {{ c.rating?.toFixed(1) ?? '—' }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- AI Sections -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

                <!-- Trending -->
                <div class="bg-purple-50 border border-purple-100 rounded-xl p-5">
                  <h3 class="text-sm font-bold text-purple-700 mb-2 flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
                    Chủ đề đang trending
                  </h3>
                  <div class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ marketResult.trendingTopics }}</div>
                </div>

                <!-- Demand -->
                <div class="bg-blue-50 border border-blue-100 rounded-xl p-5">
                  <h3 class="text-sm font-bold text-blue-700 mb-2 flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
                    Nhu cầu học viên
                  </h3>
                  <div class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ marketResult.demandAnalysis }}</div>
                </div>

                <!-- Opportunities -->
                <div class="bg-emerald-50 border border-emerald-100 rounded-xl p-5">
                  <h3 class="text-sm font-bold text-emerald-700 mb-2 flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" /></svg>
                    Cơ hội cho bạn
                  </h3>
                  <div class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ marketResult.opportunities }}</div>
                </div>

                <!-- Recommendations -->
                <div class="bg-amber-50 border border-amber-100 rounded-xl p-5">
                  <h3 class="text-sm font-bold text-amber-700 mb-2 flex items-center gap-1.5">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" /></svg>
                    Lời khuyên
                  </h3>
                  <div class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ marketResult.recommendations }}</div>
                </div>

              </div>

              <p class="text-center text-xs text-gray-400 pt-2">Phân tích dựa trên dữ liệu nền tảng thực tế · Gemini AI</p>
            </template>

            <!-- Empty state -->
            <div v-if="!marketResult && !analyzing && !marketError" class="text-center py-10 text-gray-400">
              <svg class="w-12 h-12 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              <p class="text-sm">Nhập chủ đề hoặc nhấn <strong>Phân tích</strong> để xem thị trường</p>
            </div>

          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import axiosClient from '@/api/axiosClient'

// ── Video Checker ───────────────────────────────────────────────────────────
const showVideoModal = ref(false)
const videoAnalyzing = ref(false)
const videoDragging = ref(false)
const videoResult = ref(null)

const openVideoChecker = () => {
  showVideoModal.value = true
  videoResult.value = null
}

const resetVideoChecker = () => {
  videoResult.value = null
}

const onVideoDrop = (e) => {
  videoDragging.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file && file.type.startsWith('video/')) analyzeVideo(file)
}

const onVideoSelected = (e) => {
  const file = e.target.files?.[0]
  if (file) analyzeVideo(file)
  e.target.value = ''
}

const analyzeVideo = (file) => {
  videoAnalyzing.value = true
  videoResult.value = null

  const url = URL.createObjectURL(file)
  const video = document.createElement('video')
  video.preload = 'metadata'
  video.src = url

  video.onloadedmetadata = () => {
    const w = video.videoWidth
    const h = video.videoHeight
    const duration = video.duration // seconds
    const fileBytes = file.size
    const fileMB = (fileBytes / 1024 / 1024).toFixed(1)
    const bitrateKbps = duration > 0 ? Math.round((fileBytes * 8) / duration / 1000) : null

    URL.revokeObjectURL(url)

    const checks = []
    const suggestions = []
    let failCount = 0
    let warnCount = 0

    // 1. Resolution
    const resLabel = w && h ? `${w} × ${h}` : 'Không đọc được'
    let resStatus = 'INFO', resNote = ''
    if (w >= 1920 && h >= 1080) {
      resStatus = 'GOOD'; resNote = 'Full HD trở lên — xuất sắc'
    } else if (w >= 1280 && h >= 720) {
      resStatus = 'WARNING'; resNote = 'HD 720p — chấp nhận được nhưng nên quay 1080p'; warnCount++
      suggestions.push('Nên quay ở độ phân giải 1920×1080 (Full HD) để hình ảnh sắc nét hơn.')
    } else if (w > 0) {
      resStatus = 'FAIL'; resNote = 'Dưới 720p — quá thấp cho khóa học online'; failCount++
      suggestions.push('Độ phân giải quá thấp. Cần ít nhất 1280×720 (HD).')
    } else {
      resStatus = 'INFO'; resNote = 'Không thể đọc — định dạng có thể không được hỗ trợ'
    }
    checks.push({ label: 'Độ phân giải', value: resLabel, status: resStatus, note: resNote })

    // 2. Bitrate
    if (bitrateKbps !== null) {
      let brStatus, brNote
      if (bitrateKbps >= 4000) {
        brStatus = 'GOOD'; brNote = 'Bitrate cao — chất lượng tốt'
      } else if (bitrateKbps >= 1500) {
        brStatus = 'WARNING'; brNote = 'Bitrate trung bình — ổn cho nội dung ít chuyển động'; warnCount++
        suggestions.push('Bitrate ~' + bitrateKbps + ' kbps có thể gây mờ ở cảnh chuyển động nhanh. Khuyến nghị ≥ 4000 kbps.')
      } else {
        brStatus = 'FAIL'; brNote = 'Bitrate thấp — video có thể bị mờ hoặc vỡ hình'; failCount++
        suggestions.push('Bitrate quá thấp (' + bitrateKbps + ' kbps). Xuất video ở bitrate ≥ 1500 kbps.')
      }
      checks.push({ label: 'Bitrate ước tính', value: bitrateKbps.toLocaleString() + ' kbps', status: brStatus, note: brNote })
    }

    // 3. Duration
    if (duration && isFinite(duration)) {
      const mins = Math.floor(duration / 60)
      const secs = Math.floor(duration % 60)
      const durLabel = mins > 0 ? `${mins} phút ${secs} giây` : `${secs} giây`
      let durStatus = 'GOOD', durNote = 'Độ dài bài học hợp lý'
      if (duration < 60) {
        durStatus = 'WARNING'; durNote = 'Dưới 1 phút — bài học có thể quá ngắn'; warnCount++
      } else if (duration > 3600) {
        durStatus = 'WARNING'; durNote = 'Hơn 1 tiếng — nên chia thành nhiều bài ngắn hơn'; warnCount++
        suggestions.push('Video dài hơn 60 phút. Nên chia thành các bài 10-20 phút để học viên dễ theo dõi.')
      }
      checks.push({ label: 'Độ dài', value: durLabel, status: durStatus, note: durNote })
    }

    // 4. File size
    let sizeStatus = 'GOOD', sizeNote = 'Kích thước hợp lý'
    if (fileBytes > 2 * 1024 * 1024 * 1024) {
      sizeStatus = 'WARNING'; sizeNote = 'Trên 2GB — có thể chậm khi upload'; warnCount++
      suggestions.push('File quá lớn (> 2GB). Hãy xuất lại với cài đặt nén tốt hơn (H.264 / H.265).')
    } else if (fileBytes < 1024 * 1024) {
      sizeStatus = 'WARNING'; sizeNote = 'Dưới 1MB — có thể bị nén quá mức'; warnCount++
    }
    checks.push({ label: 'Kích thước file', value: fileMB + ' MB', status: sizeStatus, note: sizeNote })

    // 5. Format
    const mime = file.type || 'unknown'
    const ext = file.name.split('.').pop()?.toLowerCase()
    let fmtStatus = 'GOOD', fmtNote = 'Định dạng được hỗ trợ tốt'
    if (mime === 'video/mp4' || ext === 'mp4') {
      fmtNote = 'MP4 — định dạng tốt nhất cho web'
    } else if (['webm', 'ogg'].includes(ext)) {
      fmtNote = 'Tương thích tốt trên web'
    } else if (['mov', 'avi', 'mkv', 'flv'].includes(ext)) {
      fmtStatus = 'WARNING'; fmtNote = 'Nên chuyển sang MP4 (H.264) trước khi upload'; warnCount++
      suggestions.push(`Định dạng .${ext} có thể không tương thích tốt. Hãy xuất sang MP4 (codec H.264).`)
    }
    checks.push({ label: 'Định dạng', value: (ext || mime).toUpperCase(), status: fmtStatus, note: fmtNote })

    // Overall score
    const overallScore = failCount > 0 ? 'FAIL' : warnCount > 0 ? 'WARNING' : 'GOOD'

    videoResult.value = {
      fileName: file.name,
      fileSize: fileMB + ' MB',
      mimeType: file.type || 'video/' + ext,
      overallScore,
      checks,
      suggestions
    }
    videoAnalyzing.value = false
  }

  video.onerror = () => {
    URL.revokeObjectURL(url)
    videoAnalyzing.value = false
    videoResult.value = {
      fileName: file.name,
      fileSize: (file.size / 1024 / 1024).toFixed(1) + ' MB',
      mimeType: file.type,
      overallScore: 'FAIL',
      checks: [{ label: 'Lỗi đọc file', value: 'Không thể đọc video', status: 'FAIL', note: 'Định dạng không được hỗ trợ hoặc file bị hỏng.' }],
      suggestions: ['Thử chuyển file sang định dạng MP4 (H.264) rồi kiểm tra lại.']
    }
  }
}

// ── Market Insight ──────────────────────────────────────────────────────────
const showMarketModal = ref(false)
const marketTopic = ref('')
const analyzing = ref(false)
const marketResult = ref(null)
const marketError = ref(null)

const openMarketModal = () => {
  showMarketModal.value = true
}

const runMarketAnalysis = async () => {
  analyzing.value = true
  marketError.value = null
  marketResult.value = null
  try {
    marketResult.value = await axiosClient.post('/instructor/market-insight', {
      topic: marketTopic.value.trim() || null
    })
  } catch (e) {
    marketError.value = e?.response?.data || e?.message || 'Có lỗi xảy ra khi phân tích.'
  } finally {
    analyzing.value = false
  }
}

const openCouponModal = () => {
  window.__notify?.info?.('Coupon do nền tảng quản lý tập trung. Vui lòng gửi đề xuất cho Admin/SuperAdmin.')
}
</script>
