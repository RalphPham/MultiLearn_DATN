<template>
  <div class="perf-root">

    <!-- SIDEBAR -->
    <aside class="perf-sidebar">
      <div class="perf-sidebar__brand">
        <div class="perf-sidebar__icon">
          <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
          </svg>
        </div>
        <div>
          <p class="perf-sidebar__title">Hiệu suất</p>
          <p class="perf-sidebar__sub">Analytics & Reports</p>
        </div>
      </div>

      <nav class="perf-sidebar__nav">
        <p class="perf-sidebar__section-label">Phân tích</p>

        <button @click="goTab('overview')" :class="tabButtonClass('overview')">
          <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
          </svg>
          Tổng quan
        </button>

        <button @click="goTab('revenue')" :class="tabButtonClass('revenue')">
          <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          Doanh thu
        </button>

        <button @click="goTab('students')" :class="tabButtonClass('students')">
          <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
          Học viên
        </button>

        <button @click="goTab('reviews')" :class="tabButtonClass('reviews')">
          <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/>
          </svg>
          Đánh giá
        </button>

        <button @click="goTab('analytics')" :class="tabButtonClass('analytics')">
          <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
          </svg>
          Phân tích nội dung
        </button>
      </nav>
    </aside>

    <!-- MAIN CONTENT -->
    <main class="flex-1 overflow-auto">

      <!-- TAB OVERVIEW -->
      <template v-if="tab === 'overview'">
        <div class="p-8">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Tổng quan</h1>
              <p class="text-sm text-gray-400 mt-0.5">Dữ liệu thời gian thực từ hệ thống ghi danh của bạn</p>
            </div>
            <button @click="fetchOverview" class="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-700 hover:border-purple-300 hover:bg-purple-50 hover:text-purple-700 transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
              Làm mới
            </button>
          </div>

          <div v-if="loading" class="flex items-center justify-center py-24">
            <div class="flex flex-col items-center gap-3">
              <div class="animate-spin rounded-full h-10 w-10 border-4 border-violet-500 border-t-transparent"></div>
              <p class="text-sm text-gray-400 font-medium">Đang tải dữ liệu...</p>
            </div>
          </div>

          <div v-else-if="error" class="rounded-2xl bg-red-50 border border-red-200 p-5 flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <p class="text-sm text-red-700 font-medium">{{ error }}</p>
          </div>

          <div v-else class="space-y-8">
            <!-- Stat Cards -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="bg-white border border-gray-200 rounded-xl p-5 flex items-center gap-4 shadow-sm">
                <div class="w-10 h-10 rounded-lg bg-violet-50 flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-violet-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
                </div>
                <div>
                  <p class="text-xs text-gray-400 font-medium">Khóa học</p>
                  <p class="text-2xl font-bold text-gray-900 mt-0.5">{{ overview.totalCourses }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">đang hoạt động</p>
                </div>
              </div>

              <div class="bg-white border border-gray-200 rounded-xl p-5 flex items-center gap-4 shadow-sm">
                <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
                </div>
                <div>
                  <p class="text-xs text-gray-400 font-medium">Học viên duy nhất</p>
                  <p class="text-2xl font-bold text-gray-900 mt-0.5">{{ formatNumber(overview.totalUniqueStudents) }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">trên toàn bộ khóa học</p>
                </div>
              </div>

              <div class="bg-white border border-gray-200 rounded-xl p-5 flex items-center gap-4 shadow-sm">
                <div class="w-10 h-10 rounded-lg bg-emerald-50 flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                </div>
                <div>
                  <p class="text-xs text-gray-400 font-medium">Lượt ghi danh</p>
                  <p class="text-2xl font-bold text-gray-900 mt-0.5">{{ formatNumber(overview.totalActiveEnrollments) }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">đang active</p>
                </div>
              </div>
            </div>

            <!-- Course Table -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
              <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                <div>
                  <h2 class="text-base font-bold text-gray-900">Học viên theo khóa học</h2>
                  <p class="text-xs text-gray-500 mt-0.5">Sắp xếp giảm dần theo số học viên</p>
                </div>
                <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-gray-100 text-xs font-bold text-gray-600">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
                  {{ overview.courses.length }} khóa học
                </span>
              </div>

              <div v-if="overview.courses.length === 0" class="py-16 text-center">
                <svg class="w-12 h-12 text-gray-200 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
                <p class="text-sm text-gray-400 font-medium">Chưa có dữ liệu khóa học</p>
              </div>

              <div v-else class="overflow-x-auto">
                <table class="w-full text-left">
                  <thead>
                    <tr class="bg-gray-50 border-b border-gray-100">
                      <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">#</th>
                      <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Khóa học</th>
                      <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Học viên</th>
                      <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Active</th>
                      <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Tỷ lệ</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                    <tr v-for="(course, index) in overview.courses" :key="course.courseId" class="hover:bg-purple-50/20 transition group">
                      <td class="px-6 py-4">
                        <span class="w-7 h-7 rounded-lg bg-gray-100 flex items-center justify-center text-xs font-bold text-gray-500">{{ index + 1 }}</span>
                      </td>
                      <td class="px-6 py-4">
                        <div class="font-semibold text-gray-900 text-sm">{{ course.courseTitle }}</div>
                        <div class="text-xs text-gray-400 mt-0.5 font-mono">{{ course.courseSlug }}</div>
                      </td>
                      <td class="px-6 py-4">
                        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-bold">
                          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                          {{ formatNumber(course.uniqueStudents) }}
                        </span>
                      </td>
                      <td class="px-6 py-4">
                        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold">
                          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                          {{ formatNumber(course.activeEnrollments) }}
                        </span>
                      </td>
                      <td class="px-6 py-4 min-w-[120px]">
                        <div class="flex items-center gap-2">
                          <div class="flex-1 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                            <div class="h-full bg-purple-500 rounded-full transition-all"
                              :style="`width: ${overview.totalUniqueStudents > 0 ? Math.round(course.uniqueStudents / overview.totalUniqueStudents * 100) : 0}%`">
                            </div>
                          </div>
                          <span class="text-xs font-semibold text-gray-500 w-10 text-right">
                            {{ overview.totalUniqueStudents > 0 ? Math.round(course.uniqueStudents / overview.totalUniqueStudents * 100) : 0 }}%
                          </span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- TAB REVENUE -->
      <template v-else-if="tab === 'revenue'">
        <div class="p-8 space-y-6">
          <div class="flex flex-col xl:flex-row xl:items-center xl:justify-between gap-5">
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Doanh thu</h1>
              <p class="text-sm text-gray-400 mt-0.5 max-w-xl">Phân tích doanh thu chi tiết · {{ revenuePeriodLabel }}</p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <div class="flex gap-1 p-1 bg-gray-100 rounded-xl">
                <button v-for="p in PRESETS" :key="p.key"
                  @click="revenuePreset = p.key; fetchRevenue()"
                  type="button"
                  class="px-3 py-1.5 rounded-lg text-xs font-semibold transition"
                  :class="revenuePreset === p.key ? 'bg-white text-purple-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
                  {{ p.label }}
                </button>
              </div>
              <template v-if="revenuePreset === 'custom'">
                <input v-model="revenueFromMonth" type="month"
                  class="h-9 rounded-xl border border-gray-200 px-3 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 transition"/>
                <span class="text-gray-400 text-xs">đến</span>
                <input v-model="revenueToMonth" type="month"
                  class="h-9 rounded-xl border border-gray-200 px-3 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 transition"/>
                <button @click="fetchRevenue" type="button"
                  class="h-9 px-4 rounded-xl bg-purple-600 text-white text-sm font-semibold hover:bg-purple-700 transition">
                  Áp dụng
                </button>
              </template>
              <button @click="fetchRevenue" class="h-9 inline-flex items-center gap-2 px-3 rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-700 hover:border-purple-300 hover:bg-purple-50 hover:text-purple-700 transition shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
              </button>
            </div>
          </div>

          <!-- Sub tabs -->
          <div class="flex gap-1 p-1 bg-gray-100 rounded-xl w-fit">
            <button @click="revenueSubTab = 'summary'" type="button"
              class="px-5 py-2 rounded-lg text-sm font-semibold transition"
              :class="revenueSubTab === 'summary' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
              Tổng quan
            </button>
            <button @click="revenueSubTab = 'courses'" type="button"
              class="px-5 py-2 rounded-lg text-sm font-semibold transition"
              :class="revenueSubTab === 'courses' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
              Khóa học
            </button>
            <button @click="revenueSubTab = 'transactions'" type="button"
              class="px-5 py-2 rounded-lg text-sm font-semibold transition"
              :class="revenueSubTab === 'transactions' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
              Giao dịch
            </button>
          </div>

          <div v-if="revenueLoading" class="flex items-center justify-center py-24">
            <div class="flex flex-col items-center gap-3">
              <div class="animate-spin rounded-full h-10 w-10 border-4 border-violet-500 border-t-transparent"></div>
              <p class="text-sm text-gray-400 font-medium">Đang tải dữ liệu doanh thu...</p>
            </div>
          </div>

          <div v-else-if="revenueError" class="rounded-2xl bg-red-50 border border-red-200 p-5 flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <p class="text-sm text-red-700 font-medium">{{ revenueError }}</p>
          </div>

          <div v-else>
            <!-- SUBTAB: SUMMARY -->
            <div v-if="revenueSubTab === 'summary'" class="space-y-6">
              <!-- Metric cards -->
              <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-5 gap-4">
                <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition">
                  <div class="flex items-center justify-between mb-3">
                    <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
                      <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/></svg>
                    </div>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-gray-300">Gross</span>
                  </div>
                  <h3 class="text-xl font-bold text-gray-900 leading-tight">{{ formatCurrency(revenueMetrics.grossRevenue) }}</h3>
                  <p class="text-xs text-gray-400 mt-1.5">Tổng tiền bán được</p>
                </div>

                <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition">
                  <div class="flex items-center justify-between mb-3">
                    <div class="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center">
                      <svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"/></svg>
                    </div>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-gray-300">Refund</span>
                  </div>
                  <h3 class="text-xl font-bold text-red-500 leading-tight">{{ formatCurrency(revenueMetrics.refundAmount) }}</h3>
                  <p class="text-xs text-gray-400 mt-1.5">Tiền hoàn trả</p>
                </div>

                <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition">
                  <div class="flex items-center justify-between mb-3">
                    <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
                      <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                    </div>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-gray-300">Net</span>
                  </div>
                  <h3 class="text-xl font-bold text-emerald-600 leading-tight">{{ formatCurrency(revenueMetrics.netRevenue) }}</h3>
                  <p class="text-xs text-gray-400 mt-1.5">Thực nhận của tôi</p>
                </div>

                <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition">
                  <div class="flex items-center justify-between mb-3">
                    <div class="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center">
                      <svg class="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
                    </div>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-gray-300">HV</span>
                  </div>
                  <h3 class="text-xl font-bold text-gray-900 leading-tight">{{ formatNumber(revenueMetrics.studentsCount) }}</h3>
                  <p class="text-xs text-gray-400 mt-1.5">Học viên trong kỳ</p>
                </div>

                <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition">
                  <div class="flex items-center justify-between mb-3">
                    <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center">
                      <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"/></svg>
                    </div>
                    <span class="text-[10px] font-bold uppercase tracking-wider text-gray-300">Bán</span>
                  </div>
                  <h3 class="text-xl font-bold text-gray-900 leading-tight">{{ formatNumber(revenueMetrics.salesCount) }}</h3>
                  <p class="text-xs text-gray-400 mt-1.5">Lượt bán trong kỳ</p>
                </div>
              </div>

              <!-- Chart.js Line Chart -->
              <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100">
                  <h2 class="text-base font-bold text-gray-900">Biểu đồ doanh thu</h2>
                  <p class="text-xs text-gray-400 mt-0.5">{{ revenuePeriodLabel }}</p>
                </div>
                <div v-if="revenueChartRows.length === 0" class="py-16 text-center">
                  <svg class="w-12 h-12 text-gray-200 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
                  <p class="text-sm text-gray-400 font-medium">Chưa có dữ liệu cho kỳ này</p>
                </div>
                <div v-else class="p-6" style="height:320px">
                  <Line :data="revenueChartData" :options="revenueChartOptions" />
                </div>
              </div>

              <!-- Monthly Breakdown Table -->
              <div v-if="revenueChartRows.length > 0" class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h2 class="text-base font-bold text-gray-900">Chi tiết theo tháng</h2>
                    <p class="text-xs text-gray-400 mt-0.5">{{ revenuePeriodLabel }}</p>
                  </div>
                </div>
                <div class="overflow-x-auto">
                  <table class="w-full min-w-[600px] text-left">
                    <thead>
                      <tr class="bg-gray-50 border-b border-gray-100">
                        <th class="px-6 py-3 text-[11px] font-bold uppercase tracking-wider text-gray-400">Tháng</th>
                        <th class="px-6 py-3 text-[11px] font-bold uppercase tracking-wider text-gray-400 text-right">Doanh thu gộp</th>
                        <th class="px-6 py-3 text-[11px] font-bold uppercase tracking-wider text-gray-400 text-right">Hoàn tiền</th>
                        <th class="px-6 py-3 text-[11px] font-bold uppercase tracking-wider text-gray-400 text-right">Thực nhận</th>
                        <th class="px-6 py-3 text-[11px] font-bold uppercase tracking-wider text-gray-400 text-center">Tỉ lệ giữ</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
                      <tr v-for="row in revenueChartRows" :key="`${row.year}-${row.month}`" class="hover:bg-purple-50/20 transition">
                        <td class="px-6 py-3.5 text-sm font-semibold text-gray-700">
                          {{ row.year ? `T${row.month}/${row.year}` : `Tháng ${row.month}` }}
                        </td>
                        <td class="px-6 py-3.5 text-sm text-right font-medium text-blue-700">{{ formatCurrency(row.gross) }}</td>
                        <td class="px-6 py-3.5 text-sm text-right font-medium text-red-500">
                          {{ row.refund > 0 ? `-${formatCurrency(row.refund)}` : '—' }}
                        </td>
                        <td class="px-6 py-3.5 text-sm text-right font-bold text-emerald-600">{{ formatCurrency(row.net) }}</td>
                        <td class="px-6 py-3.5 text-center">
                          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold"
                            :class="row.gross > 0 && ((row.net/row.gross)*100) >= 80
                              ? 'bg-emerald-50 text-emerald-700'
                              : row.gross > 0 && ((row.net/row.gross)*100) >= 60
                              ? 'bg-amber-50 text-amber-700'
                              : 'bg-red-50 text-red-600'">
                            {{ row.gross > 0 ? `${((row.net / row.gross) * 100).toFixed(1)}%` : '—' }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                    <tfoot>
                      <tr class="bg-purple-50 border-t-2 border-purple-200">
                        <td class="px-6 py-3.5 text-sm font-bold text-purple-900">Tổng cộng</td>
                        <td class="px-6 py-3.5 text-sm text-right font-bold text-blue-700">{{ formatCurrency(revenueMetrics.grossRevenue) }}</td>
                        <td class="px-6 py-3.5 text-sm text-right font-bold text-red-500">
                          {{ revenueMetrics.refundAmount > 0 ? `-${formatCurrency(revenueMetrics.refundAmount)}` : '—' }}
                        </td>
                        <td class="px-6 py-3.5 text-sm text-right font-bold text-emerald-700">{{ formatCurrency(revenueMetrics.netRevenue) }}</td>
                        <td class="px-6 py-3.5 text-center">
                          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-100 text-purple-700">{{ netRate }}</span>
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              <!-- Quick Summary -->
              <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
                  <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">Tỉ lệ hoàn tiền</p>
                  <div class="flex items-end justify-between mb-2">
                    <span class="text-2xl font-bold text-gray-900">{{ refundRate }}</span>
                    <span class="text-xs font-medium text-gray-400">Refund / Gross</span>
                  </div>
                  <div class="bg-gray-100 rounded-full h-1.5 overflow-hidden">
                    <div class="h-full bg-red-400 rounded-full" :style="`width: ${refundRate}`"></div>
                  </div>
                </div>

                <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-5">
                  <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">TB / lượt bán</p>
                  <div class="flex items-end justify-between mb-2">
                    <span class="text-2xl font-bold text-gray-900">{{ avgRevenuePerSale }}</span>
                    <span class="text-xs font-medium text-gray-400">Gross / Sales</span>
                  </div>
                  <p class="text-[11px] text-gray-400">Doanh thu trung bình mỗi giao dịch</p>
                </div>

                <div class="bg-white rounded-2xl border border-emerald-100 shadow-sm p-5">
                  <p class="text-[10px] font-bold uppercase tracking-widest text-emerald-500 mb-2">Hiệu suất Net</p>
                  <div class="flex items-end justify-between mb-2">
                    <span class="text-2xl font-bold text-emerald-700">{{ netRate }}</span>
                    <span class="text-xs font-medium text-emerald-400">Net / Gross</span>
                  </div>
                  <div class="bg-emerald-100 rounded-full h-1.5 overflow-hidden">
                    <div class="h-full bg-emerald-500 rounded-full" :style="`width: ${netRate}`"></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- SUBTAB: COURSES -->
            <div v-else-if="revenueSubTab === 'courses'" class="space-y-5">
              <div class="flex flex-col md:flex-row gap-3">
                <div class="relative flex-1">
                  <svg class="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                  <input v-model="courseRevenueKeyword" type="text" placeholder="Tìm theo tên khóa học..."
                    class="w-full h-10 pl-10 pr-4 rounded-xl border border-gray-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition"/>
                </div>
                <select v-model="courseRevenuePageSize"
                  class="h-10 rounded-xl border border-gray-200 px-4 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition">
                  <option :value="5">5 / trang</option>
                  <option :value="10">10 / trang</option>
                  <option :value="20">20 / trang</option>
                </select>
              </div>

              <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h2 class="text-base font-bold text-gray-900">Khóa học tạo doanh thu</h2>
                    <p class="text-xs text-gray-400 mt-0.5">{{ revenuePeriodLabel }}</p>
                  </div>
                  <span class="px-3 py-1 rounded-full bg-gray-100 text-xs font-bold text-gray-600">{{ filteredRevenueTopCourses.length }} kết quả</span>
                </div>

                <div v-if="pagedRevenueTopCourses.length === 0" class="py-16 text-center">
                  <p class="text-sm text-gray-400 font-medium">Không có khóa học phù hợp.</p>
                </div>

                <div v-else class="overflow-x-auto">
                  <table class="w-full min-w-[800px] text-left">
                    <thead>
                      <tr class="bg-gray-50 border-b border-gray-100">
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Khóa học</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Ảnh</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Lượt bán</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Doanh thu</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
                      <tr v-for="item in pagedRevenueTopCourses" :key="item.courseId" class="hover:bg-purple-50/20 transition">
                        <td class="px-6 py-4">
                          <div class="font-semibold text-gray-900 text-sm">{{ item.courseTitle || '—' }}</div>
                        </td>
                        <td class="px-6 py-4">
                          <img :src="item.thumbnail || defaultCourseImage" alt="course"
                            class="w-24 h-14 rounded-xl object-cover border border-gray-100 bg-gray-50 shadow-sm"/>
                        </td>
                        <td class="px-6 py-4">
                          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-bold">
                            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
                            {{ formatNumber(item.students) }}
                          </span>
                        </td>
                        <td class="px-6 py-4">
                          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold">{{ formatCurrency(item.revenue) }}</span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
                  <span class="text-xs text-gray-400">Trang {{ courseRevenuePage }} / {{ courseRevenueTotalPages }}</span>
                  <div class="flex gap-2">
                    <button @click="courseRevenuePage = Math.max(1, courseRevenuePage - 1)" :disabled="courseRevenuePage <= 1" type="button"
                      class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">← Trước</button>
                    <button @click="courseRevenuePage = Math.min(courseRevenueTotalPages, courseRevenuePage + 1)" :disabled="courseRevenuePage >= courseRevenueTotalPages" type="button"
                      class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">Sau →</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- SUBTAB: TRANSACTIONS -->
            <div v-else class="space-y-5">
              <div class="flex flex-col md:flex-row gap-3">
                <div class="relative flex-1">
                  <svg class="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                  <input v-model="transactionKeyword" type="text" placeholder="Tìm theo học viên hoặc khóa học..."
                    class="w-full h-10 pl-10 pr-4 rounded-xl border border-gray-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition"/>
                </div>
                <select v-model="transactionPageSize"
                  class="h-10 rounded-xl border border-gray-200 px-4 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition">
                  <option :value="5">5 / trang</option>
                  <option :value="10">10 / trang</option>
                  <option :value="20">20 / trang</option>
                </select>
              </div>

              <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h2 class="text-base font-bold text-gray-900">Giao dịch gần đây</h2>
                    <p class="text-xs text-gray-400 mt-0.5">{{ revenuePeriodLabel }}</p>
                  </div>
                  <span class="px-3 py-1 rounded-full bg-gray-100 text-xs font-bold text-gray-600">{{ filteredRevenueTransactions.length }} giao dịch</span>
                </div>

                <div v-if="pagedRevenueTransactions.length === 0" class="py-16 text-center">
                  <p class="text-sm text-gray-400 font-medium">Không có giao dịch phù hợp.</p>
                </div>

                <div v-else class="overflow-x-auto">
                  <table class="w-full min-w-[800px] text-left">
                    <thead>
                      <tr class="bg-gray-50 border-b border-gray-100">
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Học viên</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Khóa học</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Số tiền</th>
                        <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Ngày giao dịch</th>
                      </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-50">
                      <tr v-for="(item, index) in pagedRevenueTransactions" :key="`${item.studentName}-${item.courseTitle}-${item.createdAt}-${index}`"
                        class="hover:bg-purple-50/20 transition">
                        <td class="px-6 py-4">
                          <div class="flex items-center gap-2.5">
                            <div class="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center text-purple-600 text-xs font-bold uppercase flex-shrink-0">
                              {{ item.studentName?.charAt(0) || 'U' }}
                            </div>
                            <span class="font-semibold text-gray-900 text-sm">{{ item.studentName || '—' }}</span>
                          </div>
                        </td>
                        <td class="px-6 py-4 text-sm text-gray-600">{{ item.courseTitle || '—' }}</td>
                        <td class="px-6 py-4">
                          <span class="inline-flex items-center px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold">{{ formatCurrency(item.amount) }}</span>
                        </td>
                        <td class="px-6 py-4 text-xs text-gray-400 font-medium">{{ formatDateTime(item.createdAt) || '—' }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
                  <span class="text-xs text-gray-400">Trang {{ transactionPage }} / {{ transactionTotalPages }}</span>
                  <div class="flex gap-2">
                    <button @click="transactionPage = Math.max(1, transactionPage - 1)" :disabled="transactionPage <= 1" type="button"
                      class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">← Trước</button>
                    <button @click="transactionPage = Math.min(transactionTotalPages, transactionPage + 1)" :disabled="transactionPage >= transactionTotalPages" type="button"
                      class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">Sau →</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- TAB STUDENTS -->
      <template v-else-if="tab === 'students'">
        <div class="p-8">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Học viên</h1>
              <p class="text-sm text-gray-400 mt-0.5">Danh sách học viên đang theo học các khóa của bạn</p>
            </div>
            <div class="flex items-center gap-2">
              <button
                @click="exportStudentsExcel"
                :disabled="exportingStudents || loading"
                class="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-emerald-200 bg-emerald-50 text-sm font-semibold text-emerald-700 hover:bg-emerald-100 hover:border-emerald-300 transition shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v12m0 0l-4-4m4 4l4-4M4 17v1a3 3 0 003 3h10a3 3 0 003-3v-1"/>
                </svg>
                {{ exportingStudents ? 'Đang xuất...' : 'Xuất Excel' }}
              </button>

              <button @click="refreshStudentsTab" class="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-700 hover:border-purple-300 hover:bg-purple-50 hover:text-purple-700 transition shadow-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                Làm mới
              </button>
            </div>
          </div>

          <!-- Summary pills -->
          <div class="grid grid-cols-3 gap-4 mb-6">
            <div class="bg-white border border-gray-200 rounded-xl p-4 flex items-center gap-3 shadow-sm">
              <div class="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0">
                <svg class="w-4.5 h-4.5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
              </div>
              <div>
                <p class="text-xs text-gray-400">Học viên duy nhất</p>
                <p class="text-xl font-bold text-gray-900 leading-tight">{{ formatNumber(overview.totalUniqueStudents) }}</p>
              </div>
            </div>
            <div class="bg-white border border-gray-200 rounded-xl p-4 flex items-center gap-3 shadow-sm">
              <div class="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center flex-shrink-0">
                <svg class="w-4.5 h-4.5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              </div>
              <div>
                <p class="text-xs text-gray-400">Lượt ghi danh Active</p>
                <p class="text-xl font-bold text-gray-900 leading-tight">{{ formatNumber(overview.totalActiveEnrollments) }}</p>
              </div>
            </div>
            <div class="bg-white border border-gray-200 rounded-xl p-4 flex items-center gap-3 shadow-sm">
              <div class="w-9 h-9 rounded-lg bg-violet-50 flex items-center justify-center flex-shrink-0">
                <svg class="w-4.5 h-4.5 text-violet-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
              </div>
              <div>
                <p class="text-xs text-gray-400">Tổng khóa học</p>
                <p class="text-xl font-bold text-gray-900 leading-tight">{{ formatNumber(overview.totalCourses) }}</p>
              </div>
            </div>
          </div>

          <!-- Search + page size -->
          <div class="flex flex-col md:flex-row gap-3 mb-5">
            <div class="relative flex-1">
              <svg class="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
              <input v-model="keyword" type="text" placeholder="Tìm theo tên, email, tên khóa học..."
                class="w-full h-10 pl-10 pr-4 rounded-xl border border-gray-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition"/>
            </div>
            <select v-model="studentPageSize" class="h-10 rounded-xl border border-gray-200 px-4 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition">
              <option :value="10">10 / trang</option>
              <option :value="20">20 / trang</option>
              <option :value="50">50 / trang</option>
            </select>
          </div>

          <div v-if="loading" class="flex items-center justify-center py-24">
            <div class="animate-spin rounded-full h-10 w-10 border-4 border-violet-500 border-t-transparent"></div>
          </div>

          <div v-else-if="error" class="rounded-2xl bg-red-50 border border-red-200 p-5 flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <p class="text-sm text-red-700 font-medium">{{ error }}</p>
          </div>

          <!-- At-risk alert -->
          <div v-if="atRiskStudents.length > 0" class="bg-orange-50 border border-orange-200 rounded-2xl p-5 mb-2">
            <div class="flex items-center gap-2 mb-3">
              <svg class="w-5 h-5 text-orange-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <p class="text-sm font-bold text-orange-800">{{ atRiskStudents.length }} học viên cần chú ý</p>
              <span class="text-xs text-orange-600">— Ghi danh hơn 7 ngày nhưng chưa học bài nào</span>
            </div>
            <div class="space-y-2">
              <div v-for="item in atRiskStudents" :key="item.enrollmentId"
                class="flex items-center gap-3 bg-white rounded-xl px-4 py-3 border border-orange-100">
                <img :src="item.studentAvatar || defaultAvatar" class="w-8 h-8 rounded-full object-cover flex-shrink-0"/>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-900 truncate">{{ item.studentName || item.studentEmail }}</p>
                  <p class="text-xs text-gray-400 truncate">{{ item.courseTitle }}</p>
                </div>
                <div class="text-right flex-shrink-0">
                  <p class="text-xs font-bold text-orange-600">{{ item.progress ?? 0 }}%</p>
                  <p class="text-[10px] text-gray-400">
                    {{ item.enrolledAt ? Math.floor((Date.now() - new Date(item.enrolledAt)) / 86400000) + ' ngày trước' : '' }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div v-if="filteredStudentItems.length === 0" class="bg-white rounded-xl border border-gray-200 py-16 text-center shadow-sm">
            <svg class="w-12 h-12 text-gray-200 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            <p class="text-sm text-gray-400 font-medium">Không có học viên phù hợp để hiển thị.</p>
          </div>

          <div v-else class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full text-left min-w-[900px]">
                <thead>
                  <tr class="bg-gray-50 border-b border-gray-100">
                    <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Học viên</th>
                    <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Khóa học</th>
                    <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400 w-48">Tiến độ</th>
                    <th class="px-6 py-3.5 text-[11px] font-bold uppercase tracking-wider text-gray-400">Ghi danh</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  <tr v-for="item in pagedStudentItems" :key="item.enrollmentId" class="hover:bg-gray-50/60 transition">
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-3">
                        <img :src="item.studentAvatar || defaultAvatar" alt="avatar"
                          class="w-9 h-9 rounded-full object-cover border-2 border-white shadow-sm flex-shrink-0"/>
                        <div>
                          <div class="font-semibold text-gray-900 text-sm">{{ item.studentName || 'Chưa có tên' }}</div>
                          <div class="text-[11px] text-gray-400 mt-0.5">{{ item.studentEmail }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-3">
                        <img :src="item.courseThumbnail || defaultCourseImage" alt="course"
                          class="w-16 h-10 rounded-lg object-cover border border-gray-100 flex-shrink-0"/>
                        <div class="font-semibold text-gray-900 text-sm truncate max-w-[180px]">{{ item.courseTitle || '—' }}</div>
                      </div>
                    </td>
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-2">
                        <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                          <div class="h-full rounded-full transition-all"
                            :class="(item.progress||0) >= 75 ? 'bg-emerald-500' : (item.progress||0) >= 30 ? 'bg-blue-500' : 'bg-orange-400'"
                            :style="`width: ${item.progress || 0}%`">
                          </div>
                        </div>
                        <span class="text-xs font-bold w-10 text-right"
                          :class="(item.progress||0) >= 75 ? 'text-emerald-600' : (item.progress||0) >= 30 ? 'text-blue-600' : 'text-orange-500'">
                          {{ item.progress ?? 0 }}%
                        </span>
                      </div>
                    </td>
                    <td class="px-6 py-4 text-xs text-gray-400">
                      {{ item.enrolledAt ? new Date(item.enrolledAt).toLocaleDateString('vi-VN') : '—' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
              <span class="text-xs text-gray-400">
                {{ filteredStudentItems.length }} kết quả · Trang {{ studentPage }} / {{ studentTotalPages }}
              </span>
              <div class="flex gap-2">
                <button @click="studentPage = Math.max(1, studentPage - 1)" :disabled="studentPage <= 1" type="button"
                  class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">← Trước</button>
                <button @click="studentPage = Math.min(studentTotalPages, studentPage + 1)" :disabled="studentPage >= studentTotalPages" type="button"
                  class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">Sau →</button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- TAB REVIEWS -->
      <template v-else-if="tab === 'reviews'">
        <div class="p-8">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
            <div>
              <h1 class="text-xl font-semibold text-gray-900">Đánh giá</h1>
              <p class="text-sm text-gray-400 mt-0.5">Toàn bộ review học viên để lại cho các khóa học của bạn</p>
            </div>
            <button @click="fetchReviews" class="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-gray-200 bg-white text-sm font-semibold text-gray-700 hover:border-purple-300 hover:bg-purple-50 hover:text-purple-700 transition shadow-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
              Làm mới
            </button>
          </div>

          <!-- Stats -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-7">
            <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm flex items-center gap-5">
              <div class="w-14 h-14 rounded-2xl bg-amber-50 flex items-center justify-center flex-shrink-0">
                <svg class="w-7 h-7 text-amber-500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
              </div>
              <div>
                <p class="text-xs font-bold uppercase tracking-wider text-gray-400">Điểm trung bình</p>
                <div class="flex items-end gap-2 mt-1">
                  <span class="text-3xl font-bold text-gray-900">{{ overallReviewAverage }}</span>
                  <span class="text-sm font-semibold text-gray-400 mb-1">/ 5.0</span>
                </div>
                <div class="flex gap-0.5 mt-1">
                  <svg v-for="i in 5" :key="i" class="w-4 h-4" :class="i <= Math.round(Number(overallReviewAverage)) ? 'text-amber-400' : 'text-gray-200'" fill="currentColor" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
              <p class="text-xs font-bold uppercase tracking-wider text-gray-400 mb-4">Phân bổ đánh giá</p>
              <div class="space-y-2">
                <div v-for="star in [5,4,3,2,1]" :key="star" class="flex items-center gap-2.5">
                  <span class="text-xs font-semibold text-gray-500 w-8">{{ star }}★</span>
                  <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                    <div class="h-full bg-amber-400 rounded-full transition-all"
                      :style="`width: ${reviews.length > 0 ? Math.round(reviews.filter(r => Number(r.stars) === star).length / reviews.length * 100) : 0}%`">
                    </div>
                  </div>
                  <span class="text-[11px] font-semibold text-gray-400 w-6 text-right">{{ reviews.filter(r => Number(r.stars) === star).length }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Filters -->
          <div class="flex flex-col lg:flex-row gap-3 mb-6">
            <div class="relative flex-1">
              <svg class="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
              <input v-model="reviewKeyword" type="text" placeholder="Tìm theo học viên, khóa học, nội dung..."
                class="w-full h-10 pl-10 pr-4 rounded-xl border border-gray-200 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition"/>
            </div>
            <div class="flex gap-1 p-1 bg-gray-100 rounded-xl">
              <button @click="starFilter = ''" type="button"
                class="px-3 py-1.5 rounded-lg text-xs font-semibold transition"
                :class="starFilter === '' ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
                Tất cả
              </button>
              <button v-for="s in [5,4,3,2,1]" :key="s" @click="starFilter = String(s)" type="button"
                class="px-3 py-1.5 rounded-lg text-xs font-semibold transition"
                :class="starFilter === String(s) ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'">
                {{ s }}★
              </button>
            </div>
            <select v-model="reviewPageSize" class="h-10 rounded-xl border border-gray-200 px-4 text-sm bg-white focus:outline-none transition">
              <option :value="6">6 / trang</option>
              <option :value="8">8 / trang</option>
              <option :value="12">12 / trang</option>
            </select>
          </div>

          <div v-if="reviewLoading" class="flex items-center justify-center py-24">
            <div class="animate-spin rounded-full h-10 w-10 border-4 border-violet-500 border-t-transparent"></div>
          </div>

          <div v-else-if="reviewError" class="rounded-2xl bg-red-50 border border-red-200 p-5 flex items-start gap-3">
            <svg class="w-5 h-5 text-red-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <p class="text-sm text-red-700 font-medium">{{ reviewError }}</p>
          </div>

          <div v-else-if="filteredReviews.length === 0" class="bg-white rounded-xl border border-gray-200 py-16 text-center shadow-sm">
            <svg class="w-12 h-12 text-gray-200 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg>
            <p class="text-sm text-gray-400 font-medium">Chưa có đánh giá nào phù hợp.</p>
          </div>

          <div v-else>
            <div class="bg-white rounded-xl border border-gray-200 shadow-sm divide-y divide-gray-100 overflow-hidden">
              <div v-for="item in pagedReviews" :key="item.id"
                class="flex items-start gap-4 px-4 py-3 hover:bg-gray-50 transition">
                <!-- Avatar -->
                <img :src="item.studentAvatar || defaultAvatar" alt="student"
                  class="w-9 h-9 rounded-full object-cover border border-gray-100 flex-shrink-0 mt-0.5"/>

                <!-- Main -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="font-semibold text-gray-900 text-sm truncate">{{ item.studentName || 'Học viên' }}</span>
                    <span class="flex items-center gap-0.5">
                      <svg v-for="i in 5" :key="i" class="w-3 h-3"
                        :class="i <= Number(item.stars || 0) ? 'text-amber-400' : 'text-gray-200'"
                        fill="currentColor" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>
                      <span class="text-[11px] font-bold text-amber-500 ml-0.5">{{ item.stars }}</span>
                    </span>
                    <span class="text-[11px] text-gray-400">·</span>
                    <span class="text-[11px] text-gray-400 truncate">{{ item.courseTitle || 'Khóa học' }}</span>
                    <span class="text-[11px] text-gray-300 ml-auto whitespace-nowrap">{{ formatDateTime(item.createdAt) }}</span>
                  </div>
                  <p class="text-sm text-gray-600 leading-snug mt-1 line-clamp-2">
                    {{ item.comment || 'Không có bình luận.' }}
                  </p>
                </div>

                <!-- Action -->
                <router-link v-if="item.courseSlug" :to="`/course/${item.courseSlug}#reviews-section`"
                  class="flex-shrink-0 inline-flex items-center gap-1 text-xs font-semibold text-purple-600 hover:text-purple-700 transition mt-1">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/></svg>
                  Xem
                </router-link>
              </div>
            </div>
            <!-- Pagination reviews -->
            <div class="mt-4 flex items-center justify-between">
              <span class="text-xs text-gray-400">{{ filteredReviews.length }} đánh giá · Trang {{ reviewPage }} / {{ reviewTotalPages }}</span>
              <div class="flex gap-2">
                <button @click="reviewPage = Math.max(1, reviewPage - 1)" :disabled="reviewPage <= 1" type="button"
                  class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">← Trước</button>
                <button @click="reviewPage = Math.min(reviewTotalPages, reviewPage + 1)" :disabled="reviewPage >= reviewTotalPages" type="button"
                  class="px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 disabled:opacity-40 hover:bg-gray-50 transition">Sau →</button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- TAB ANALYTICS -->
      <template v-else-if="tab === 'analytics'">
        <div class="p-8 space-y-6">
          <!-- Header -->
          <div>
            <h1 class="text-xl font-semibold text-gray-900">Phân tích nội dung</h1>
            <p class="text-sm text-gray-400 mt-0.5">Điểm dropout, section yếu và xu hướng đánh giá của bạn</p>
          </div>

          <!-- ── RATING TREND SECTION ───────────────────────────── -->
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="w-7 h-7 rounded-lg bg-amber-50 flex items-center justify-center">
                  <svg class="w-4 h-4 text-amber-500" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                  </svg>
                </div>
                <h2 class="text-sm font-bold text-gray-800">Xu hướng đánh giá</h2>
              </div>
              <div v-if="ratingTrend" class="flex items-center gap-3">
                <!-- Trend badge -->
                <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold"
                  :class="{
                    'bg-emerald-50 text-emerald-700': ratingTrend.trendDirection === 'UP',
                    'bg-red-50 text-red-600':         ratingTrend.trendDirection === 'DOWN',
                    'bg-gray-100 text-gray-500':      ratingTrend.trendDirection === 'STABLE'
                  }">
                  <span v-if="ratingTrend.trendDirection === 'UP'">↑ Đang tăng</span>
                  <span v-else-if="ratingTrend.trendDirection === 'DOWN'">↓ Đang giảm</span>
                  <span v-else>→ Ổn định</span>
                </span>
                <span class="text-xs text-gray-400">{{ ratingTrend.totalRatings }} đánh giá</span>
              </div>
            </div>

            <div v-if="ratingTrendLoading" class="flex items-center justify-center py-10">
              <div class="animate-spin rounded-full h-8 w-8 border-4 border-violet-500 border-t-transparent"></div>
            </div>

            <div v-else-if="ratingTrend" class="p-6">
              <div class="grid grid-cols-1 xl:grid-cols-[1fr_220px] gap-6">

                <!-- Monthly chart (CSS bars) -->
                <div>
                  <p class="text-xs font-bold uppercase tracking-widest text-gray-400 mb-4">Đánh giá trung bình 6 tháng gần nhất</p>
                  <div class="flex items-end gap-3 h-28">
                    <div v-for="pt in ratingTrend.monthlyTrend" :key="pt.label"
                      class="flex-1 flex flex-col items-center gap-1 group">
                      <span class="text-[10px] font-bold text-gray-500 opacity-0 group-hover:opacity-100 transition">
                        {{ pt.avgRating ? pt.avgRating.toFixed(1) : '—' }}
                      </span>
                      <div class="w-full rounded-t-md transition-all"
                        :style="{
                          height: pt.avgRating ? (pt.avgRating / 5 * 80) + 'px' : '4px',
                          background: pt.avgRating >= 4.5 ? '#10b981' : pt.avgRating >= 3.5 ? '#f59e0b' : pt.avgRating ? '#ef4444' : '#e5e7eb'
                        }">
                      </div>
                      <span class="text-[10px] text-gray-400">{{ pt.label.slice(0,2) }}/{{ pt.label.slice(5) }}</span>
                    </div>
                  </div>
                  <!-- Overall avg -->
                  <div class="mt-4 flex items-center gap-3">
                    <span class="text-3xl font-bold text-gray-900">{{ ratingTrend.overallAvg?.toFixed(1) }}</span>
                    <div>
                      <div class="flex items-center gap-0.5">
                        <svg v-for="s in 5" :key="s" class="w-4 h-4"
                          :class="s <= Math.round(ratingTrend.overallAvg) ? 'text-amber-400' : 'text-gray-200'"
                          fill="currentColor" viewBox="0 0 24 24">
                          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                        </svg>
                      </div>
                      <p class="text-xs text-gray-400 mt-0.5">Điểm trung bình tổng</p>
                    </div>
                  </div>
                </div>

                <!-- Star distribution -->
                <div>
                  <p class="text-xs font-bold uppercase tracking-widest text-gray-400 mb-4">Phân phối số sao</p>
                  <div class="space-y-2">
                    <div v-for="star in [5,4,3,2,1]" :key="star" class="flex items-center gap-2">
                      <span class="text-xs text-gray-500 w-4 text-right">{{ star }}</span>
                      <svg class="w-3.5 h-3.5 text-amber-400 flex-shrink-0" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                      </svg>
                      <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                        <div class="h-full rounded-full transition-all"
                          :class="star >= 4 ? 'bg-emerald-400' : star === 3 ? 'bg-amber-400' : 'bg-red-400'"
                          :style="{ width: ratingTrend.totalRatings > 0 ? ((ratingTrend.starDistribution[star] || 0) / ratingTrend.totalRatings * 100) + '%' : '0%' }">
                        </div>
                      </div>
                      <span class="text-xs text-gray-400 w-6 text-right">{{ ratingTrend.starDistribution[star] || 0 }}</span>
                    </div>
                  </div>
                </div>

              </div>
            </div>

            <div v-else class="py-10 text-center text-sm text-gray-400">Chưa có đánh giá nào.</div>
          </div>

          <!-- ── DROPOUT SECTION ────────────────────────────────── -->
          <div class="flex flex-col xl:flex-row xl:items-center xl:justify-between gap-5">
            <div>
              <h2 class="text-base font-semibold text-gray-900">Phân tích dropout theo khóa học</h2>
              <p class="text-sm text-gray-400 mt-0.5">Tỉ lệ hoàn thành từng bài — phát hiện điểm rơi để cải thiện</p>
            </div>
            <div class="flex items-center gap-2">
              <select v-model="analyticsCourseId" @change="fetchDropoff"
                class="h-10 min-w-[220px] rounded-xl border border-gray-200 bg-white px-4 text-sm font-semibold text-gray-700 shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-200 focus:border-purple-400 transition">
                <option value="">-- Chọn khóa học --</option>
                <option v-for="c in overview.courses" :key="c.courseId" :value="c.courseId">{{ c.courseTitle }}</option>
              </select>
              <button
                v-if="analyticsCourseId"
                @click="openInstructorLeaderboard"
                class="h-10 px-4 rounded-xl border border-yellow-300 bg-yellow-50 text-yellow-700 text-sm font-semibold hover:bg-yellow-100 transition flex items-center gap-1.5 flex-shrink-0"
              >
                🏆 Xếp hạng
              </button>
            </div>
          </div>

          <!-- Loading -->
          <div v-if="analyticsLoading" class="flex items-center justify-center py-24">
            <div class="flex flex-col items-center gap-3">
              <div class="animate-spin rounded-full h-10 w-10 border-4 border-violet-500 border-t-transparent"></div>
              <p class="text-sm text-gray-400 font-medium">Đang tải dữ liệu...</p>
            </div>
          </div>

          <!-- Empty: no course selected -->
          <div v-else-if="!analyticsCourseId" class="bg-white rounded-2xl border border-gray-200 py-20 text-center shadow-sm">
            <svg class="w-14 h-14 text-gray-200 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
            </svg>
            <p class="text-base font-semibold text-gray-400">Chọn khóa học để xem phân tích</p>
          </div>

          <!-- Empty: no data -->
          <div v-else-if="analyticsData.length === 0" class="bg-white rounded-2xl border border-gray-200 py-20 text-center shadow-sm">
            <p class="text-sm text-gray-400 font-medium">Chưa có dữ liệu học tập cho khóa học này.</p>
          </div>

          <div v-else class="space-y-5">
            <!-- Summary cards -->
            <div class="grid grid-cols-2 xl:grid-cols-4 gap-4">
              <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">Tổng bài học</p>
                <p class="text-2xl font-bold text-gray-900">{{ analyticsData.length }}</p>
                <p class="text-xs text-gray-400 mt-1">trong {{ analyticsSectionStats.length }} chương</p>
              </div>
              <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">HT trung bình</p>
                <p class="text-2xl font-bold text-violet-600">{{ analyticsAvgRate }}%</p>
                <p class="text-xs text-gray-400 mt-1">trên toàn khóa</p>
              </div>
              <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">Học viên</p>
                <p class="text-2xl font-bold text-gray-900">{{ analyticsData[0]?.enrolledCount ?? 0 }}</p>
                <p class="text-xs text-gray-400 mt-1">đã ghi danh</p>
              </div>
              <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
                <p class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">Điểm rơi</p>
                <p class="text-2xl font-bold text-red-500">{{ analyticsWorstLessons.length }}</p>
                <p class="text-xs text-gray-400 mt-1">bài dưới 40%</p>
              </div>
            </div>

            <!-- Biggest drop-off insight -->
            <div v-if="analyticsBiggestDropoff" class="rounded-2xl border p-5 flex items-start gap-4"
              :class="analyticsBiggestDropoff.drop > 20 ? 'bg-orange-50 border-orange-200' : 'bg-blue-50 border-blue-200'">
              <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                :class="analyticsBiggestDropoff.drop > 20 ? 'bg-orange-100' : 'bg-blue-100'">
                <svg class="w-5 h-5" :class="analyticsBiggestDropoff.drop > 20 ? 'text-orange-600' : 'text-blue-600'"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6"/>
                </svg>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-bold mb-0.5"
                  :class="analyticsBiggestDropoff.drop > 20 ? 'text-orange-800' : 'text-blue-800'">
                  Điểm rơi lớn nhất phát hiện
                </p>
                <p class="text-sm" :class="analyticsBiggestDropoff.drop > 20 ? 'text-orange-700' : 'text-blue-700'">
                  Học viên giảm
                  <strong>-{{ analyticsBiggestDropoff.drop }}%</strong>
                  khi chuyển từ
                  <strong>"{{ analyticsBiggestDropoff.from.lessonTitle }}"</strong>
                  ({{ analyticsBiggestDropoff.from.completionRate }}%)
                  sang
                  <strong>"{{ analyticsBiggestDropoff.to.lessonTitle }}"</strong>
                  ({{ analyticsBiggestDropoff.to.completionRate }}%)
                </p>
              </div>
            </div>

            <!-- Main content: chart + section stats side by side -->
            <div class="grid grid-cols-1 2xl:grid-cols-[1fr_320px] gap-5">
              <!-- Drop-off chart -->
              <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                  <div>
                    <h2 class="text-base font-bold text-gray-900">Tỉ lệ hoàn thành từng bài</h2>
                    <p class="text-xs text-gray-400 mt-0.5">Màu đỏ = điểm rơi cần cải thiện</p>
                  </div>
                  <div class="flex items-center gap-3 text-[11px] font-semibold text-gray-400">
                    <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm bg-emerald-500 inline-block"></span>≥70%</span>
                    <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm bg-amber-400 inline-block"></span>40–69%</span>
                    <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-sm bg-red-400 inline-block"></span>&lt;40%</span>
                  </div>
                </div>
                <div class="p-6 space-y-3 max-h-[520px] overflow-y-auto">
                  <div v-for="(lesson, idx) in analyticsData" :key="lesson.lessonId">
                    <div class="flex items-center gap-3 mb-1.5">
                      <span class="text-[11px] font-bold text-gray-300 w-5 text-right flex-shrink-0">{{ idx + 1 }}</span>
                      <span class="text-xs font-semibold text-gray-800 flex-1 truncate" :title="lesson.lessonTitle">{{ lesson.lessonTitle }}</span>
                      <span class="text-[11px] text-gray-400 flex-shrink-0 tabular-nums">{{ lesson.completedCount }}/{{ lesson.enrolledCount }}</span>
                      <span class="text-xs font-bold flex-shrink-0 w-11 text-right tabular-nums"
                        :class="lesson.completionRate >= 70 ? 'text-emerald-600' : lesson.completionRate >= 40 ? 'text-amber-600' : 'text-red-500'">
                        {{ lesson.completionRate }}%
                      </span>
                    </div>
                    <div class="flex items-center gap-3">
                      <span class="w-5 flex-shrink-0"></span>
                      <div class="flex-1 bg-gray-100 rounded-full h-3 overflow-hidden">
                        <div class="h-full rounded-full transition-all duration-700"
                          :class="lesson.completionRate >= 70 ? 'bg-emerald-500' : lesson.completionRate >= 40 ? 'bg-amber-400' : 'bg-red-400'"
                          :style="`width: ${lesson.completionRate}%`">
                        </div>
                      </div>
                    </div>
                    <!-- Drop indicator between lessons -->
                    <div v-if="idx < analyticsData.length - 1 && (analyticsData[idx].completionRate - analyticsData[idx+1].completionRate) >= 15"
                      class="flex items-center gap-3 mt-1">
                      <span class="w-5 flex-shrink-0"></span>
                      <span class="text-[10px] text-orange-500 font-bold">
                        ↓ {{ (analyticsData[idx].completionRate - analyticsData[idx+1].completionRate).toFixed(1) }}% drop
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Section stats -->
              <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                <div class="px-5 py-5 border-b border-gray-100">
                  <h2 class="text-base font-bold text-gray-900">Theo chương</h2>
                  <p class="text-xs text-gray-400 mt-0.5">Tỉ lệ HT trung bình mỗi chương</p>
                </div>
                <div class="p-5 space-y-4">
                  <div v-for="sec in analyticsSectionStats" :key="sec.sectionTitle">
                    <div class="flex items-center justify-between mb-1.5">
                      <span class="text-xs font-semibold text-gray-700 truncate flex-1 mr-2" :title="sec.sectionTitle">{{ sec.sectionTitle }}</span>
                      <span class="text-xs font-semibold flex-shrink-0"
                        :class="sec.avgRate >= 70 ? 'text-emerald-600' : sec.avgRate >= 40 ? 'text-amber-600' : 'text-red-500'">
                        {{ sec.avgRate }}%
                      </span>
                    </div>
                    <div class="flex items-center gap-2">
                      <div class="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
                        <div class="h-full rounded-full transition-all duration-700"
                          :class="sec.avgRate >= 70 ? 'bg-emerald-500' : sec.avgRate >= 40 ? 'bg-amber-400' : 'bg-red-400'"
                          :style="`width: ${sec.avgRate}%`">
                        </div>
                      </div>
                      <span class="text-[10px] text-gray-400 flex-shrink-0">{{ sec.count }} bài</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Best & Worst side by side -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div v-if="analyticsBestLesson" class="bg-emerald-50 border border-emerald-200 rounded-2xl p-5">
                <div class="flex items-center gap-2 mb-3">
                  <svg class="w-4 h-4 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                  <p class="text-sm font-bold text-emerald-800">Bài học tốt nhất</p>
                </div>
                <p class="text-sm font-semibold text-emerald-900 truncate">{{ analyticsBestLesson.lessonTitle }}</p>
                <p class="text-xs text-emerald-600 mt-1">{{ analyticsBestLesson.sectionTitle }}</p>
                <p class="text-2xl font-bold text-emerald-700 mt-2">{{ analyticsBestLesson.completionRate }}%</p>
              </div>

              <div v-if="analyticsWorstLesson" class="bg-red-50 border border-red-200 rounded-2xl p-5">
                <div class="flex items-center gap-2 mb-3">
                  <svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                  <p class="text-sm font-bold text-red-700">Bài học cần cải thiện nhất</p>
                </div>
                <p class="text-sm font-semibold text-red-900 truncate">{{ analyticsWorstLesson.lessonTitle }}</p>
                <p class="text-xs text-red-500 mt-1">{{ analyticsWorstLesson.sectionTitle }}</p>
                <p class="text-2xl font-bold text-red-500 mt-2">{{ analyticsWorstLesson.completionRate }}%</p>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- FALLBACK -->
      <template v-else>
        <div class="h-full flex items-center justify-center">
          <div class="text-center">
            <div class="text-2xl font-bold text-gray-800 mb-2">Nội dung đang cập nhật</div>
            <p class="text-gray-500">Tab này sẽ được hoàn thiện sau.</p>
          </div>
        </div>
      </template>
    </main>
  </div>

  <!-- ── LEADERBOARD MODAL (Instructor) ───────────────────────────── -->
  <Teleport to="body">
    <div v-if="showLeaderboardModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="showLeaderboardModal = false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[85vh] flex flex-col">

        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <span class="text-xl">🏆</span>
            <div>
              <h2 class="text-base font-bold text-gray-900">Bảng xếp hạng học viên</h2>
              <p class="text-xs text-gray-400">{{ overview.courses?.find(c => c.courseId == analyticsCourseId)?.courseTitle }}</p>
            </div>
          </div>
          <button @click="showLeaderboardModal = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto px-4 py-3">
          <div v-if="leaderboardLoading" class="flex items-center justify-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-4 border-yellow-400 border-t-transparent"></div>
          </div>

          <div v-else-if="leaderboardData.length === 0" class="text-center py-10 text-sm text-gray-400">
            Chưa có học viên nào trong khóa học này.
          </div>

          <div v-else class="space-y-1.5">
            <div v-for="entry in leaderboardData" :key="entry.rank"
              class="flex items-center gap-3 px-3 py-3 rounded-xl hover:bg-gray-50 transition">
              <!-- Rank badge -->
              <div class="w-8 h-8 flex items-center justify-center rounded-full flex-shrink-0 text-sm font-bold"
                :class="{
                  'bg-yellow-400 text-white': entry.rank === 1,
                  'bg-gray-300 text-white':   entry.rank === 2,
                  'bg-amber-600 text-white':  entry.rank === 3,
                  'bg-gray-100 text-gray-500': entry.rank > 3
                }">
                {{ entry.rank <= 3 ? ['🥇','🥈','🥉'][entry.rank - 1] : entry.rank }}
              </div>
              <!-- Avatar -->
              <img v-if="entry.avatar" :src="entry.avatar" class="w-8 h-8 rounded-full object-cover flex-shrink-0" alt=""/>
              <div v-else class="w-8 h-8 rounded-full bg-violet-100 flex items-center justify-center flex-shrink-0 text-violet-600 text-xs font-bold">
                {{ (entry.displayName || '?')[0].toUpperCase() }}
              </div>
              <!-- Info -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold text-gray-900 truncate">
                  {{ entry.displayName }}
                  <span v-if="entry.isCourseCompleted" class="ml-1 text-xs text-emerald-600 font-normal">✓ Hoàn thành</span>
                </p>
                <div class="flex items-center gap-2 mt-1">
                  <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all"
                      :class="entry.isCourseCompleted ? 'bg-emerald-400' : 'bg-violet-400'"
                      :style="{ width: entry.progress + '%' }">
                    </div>
                  </div>
                  <span class="text-xs text-gray-400 flex-shrink-0">{{ Math.round(entry.progress) }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="px-6 py-3 border-t border-gray-100 text-center text-xs text-gray-400">
          {{ leaderboardData.length }} học viên · Sắp xếp theo tiến độ
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'
import instructorPerformanceService from '@/services/instructorPerformance.service'
import instructorRevenueService from '@/services/instructorRevenue.service'
import RatingService from '@/services/rating.service'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement,
  Tooltip, Legend, Filler
} from 'chart.js'
import { Line } from 'vue-chartjs'
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend, Filler)

const route = useRoute()
const router = useRouter()

const tab = ref(route.params.tab || 'overview')
const loading = ref(false)
const error = ref('')
const keyword = ref('')

const reviewLoading = ref(false)
const reviewError = ref('')
const reviewKeyword = ref('')
const starFilter = ref('')
const reviews = ref([])

// ── Leaderboard (Instructor) ──────────────────────────────────────────────
const showLeaderboardModal = ref(false)
const leaderboardLoading = ref(false)
const leaderboardData = ref([])

const openInstructorLeaderboard = async () => {
  if (!analyticsCourseId.value) return
  showLeaderboardModal.value = true
  leaderboardLoading.value = true
  leaderboardData.value = []
  try {
    const res = await import('@/api/axiosClient').then(m => m.default.get(
      `/courses/${analyticsCourseId.value}/leaderboard`
    ))
    leaderboardData.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('Lỗi leaderboard:', e)
  } finally {
    leaderboardLoading.value = false
  }
}

// ── Rating Trend ──────────────────────────────────────────────────────────
const ratingTrendLoading = ref(false)
const ratingTrend = ref(null)

const fetchRatingTrend = async () => {
  ratingTrendLoading.value = true
  try {
    const res = await import('@/api/axiosClient').then(m => m.default.get('/instructor/performance/rating-trend'))
    ratingTrend.value = res
  } catch (e) {
    console.error('Lỗi rating trend:', e)
  } finally {
    ratingTrendLoading.value = false
  }
}

// ── Analytics (drop-off) ──────────────────────────────────────────────────
const analyticsLoading = ref(false)
const analyticsCourseId = ref('')
const analyticsData = ref([])

const analyticsAvgRate = computed(() => {
  if (!analyticsData.value.length) return 0
  const sum = analyticsData.value.reduce((acc, l) => acc + l.completionRate, 0)
  return (sum / analyticsData.value.length).toFixed(1)
})

const analyticsWorstLessons = computed(() =>
  analyticsData.value.filter(l => l.completionRate < 40)
)

const analyticsBestLesson = computed(() => {
  if (analyticsData.value.length < 2) return null
  const sorted = [...analyticsData.value].sort((a, b) => b.completionRate - a.completionRate)
  return sorted[0]
})

const analyticsWorstLesson = computed(() => {
  if (analyticsData.value.length < 2) return null
  const sorted = [...analyticsData.value].sort((a, b) => a.completionRate - b.completionRate)
  // Only show if actually different from best
  return sorted[0].completionRate < analyticsBestLesson.value?.completionRate ? sorted[0] : null
})

const analyticsBiggestDropoff = computed(() => {
  if (analyticsData.value.length < 2) return null
  let maxDrop = 0, result = null
  for (let i = 0; i < analyticsData.value.length - 1; i++) {
    const drop = analyticsData.value[i].completionRate - analyticsData.value[i + 1].completionRate
    if (drop > maxDrop) {
      maxDrop = drop
      result = { from: analyticsData.value[i], to: analyticsData.value[i + 1], drop: drop.toFixed(1) }
    }
  }
  return result
})

const analyticsSectionStats = computed(() => {
  if (!analyticsData.value.length) return []
  const map = {}
  for (const lesson of analyticsData.value) {
    const key = lesson.sectionTitle || 'Chưa phân chương'
    if (!map[key]) map[key] = { sectionTitle: key, total: 0, count: 0 }
    map[key].total += lesson.completionRate
    map[key].count++
  }
  return Object.values(map).map(s => ({
    ...s,
    avgRate: (s.total / s.count).toFixed(1)
  }))
})

const fetchDropoff = async () => {
  if (!analyticsCourseId.value) return
  analyticsLoading.value = true
  analyticsData.value = []
  try {
    const res = await import('@/api/axiosClient').then(m => m.default.get(
      `/instructor/performance/course/${analyticsCourseId.value}/dropoff`
    ))
    analyticsData.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('Lỗi lấy drop-off:', e)
  } finally {
    analyticsLoading.value = false
  }
}
// ─────────────────────────────────────────────────────────────────────────

const revenueLoading = ref(false)
const revenueError = ref('')
const revenueYear = ref(new Date().getFullYear())
const revenueSubTab = ref('summary')
const revenuePreset = ref('year')
const revenueFromMonth = ref('')
const revenueToMonth = ref('')

const PRESETS = [
  { key: 'year', label: 'Năm nay' },
  { key: 'lastyear', label: 'Năm ngoái' },
  { key: '6M', label: '6 Tháng' },
  { key: '3M', label: '3 Tháng' },
  { key: 'custom', label: 'Tùy chọn' }
]

const revenuePeriodLabel = computed(() => {
  const now = new Date()
  if (revenuePreset.value === 'year') return `Năm ${now.getFullYear()}`
  if (revenuePreset.value === 'lastyear') return `Năm ${now.getFullYear() - 1}`
  if (revenuePreset.value === '6M') return '6 tháng gần nhất'
  if (revenuePreset.value === '3M') return '3 tháng gần nhất'
  if (revenuePreset.value === 'custom') {
    const parts = []
    if (revenueFromMonth.value) parts.push(`Từ ${revenueFromMonth.value}`)
    if (revenueToMonth.value) parts.push(`đến ${revenueToMonth.value}`)
    return parts.length ? parts.join(' ') : 'Tùy chọn'
  }
  return ''
})

const buildRevenueParams = () => {
  const now = new Date()
  const pad = n => String(n).padStart(2, '0')
  const fmt = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  if (revenuePreset.value === 'year') return { year: now.getFullYear() }
  if (revenuePreset.value === 'lastyear') return { year: now.getFullYear() - 1 }
  if (revenuePreset.value === '3M') {
    const from = new Date(now)
    from.setMonth(from.getMonth() - 2)
    from.setDate(1)
    return { fromDate: fmt(from), toDate: fmt(now) }
  }
  if (revenuePreset.value === '6M') {
    const from = new Date(now)
    from.setMonth(from.getMonth() - 5)
    from.setDate(1)
    return { fromDate: fmt(from), toDate: fmt(now) }
  }
  if (revenuePreset.value === 'custom') {
    return {
      fromDate: revenueFromMonth.value ? `${revenueFromMonth.value}-01` : undefined,
      toDate: revenueToMonth.value ? (() => {
        const [y, m] = revenueToMonth.value.split('-').map(Number)
        const lastDay = new Date(y, m, 0).getDate()
        return `${revenueToMonth.value}-${String(lastDay).padStart(2, '0')}`
      })() : undefined
    }
  }
  return { year: now.getFullYear() }
}

const revenueMetrics = ref({
  grossRevenue: 0,
  refundAmount: 0,
  netRevenue: 0,
  studentsCount: 0,
  salesCount: 0
})
const revenueChart = ref([])
const revenueTopCourses = ref([])
const revenueTransactions = ref([])

const courseRevenueKeyword = ref('')
const courseRevenuePage = ref(1)
const courseRevenuePageSize = ref(5)

const transactionKeyword = ref('')
const transactionPage = ref(1)
const transactionPageSize = ref(5)

const defaultAvatar = 'https://via.placeholder.com/44x44?text=U'
const defaultCourseImage = 'https://via.placeholder.com/96x56?text=Course'

const overview = ref({
  instructorEmail: '',
  totalCourses: 0,
  totalUniqueStudents: 0,
  totalActiveEnrollments: 0,
  courses: []
})

const studentItems = ref([])
const atRiskStudents = ref([])
const studentPage = ref(1)
const studentPageSize = ref(10)
const exportingStudents = ref(false)

const revenueYearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 5 }, (_, index) => currentYear - index)
})

const filteredStudentItems = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return studentItems.value
  return studentItems.value.filter((item) => {
    return (
      String(item.studentName || '').toLowerCase().includes(key) ||
      String(item.studentEmail || '').toLowerCase().includes(key) ||
      String(item.courseTitle || '').toLowerCase().includes(key) ||
      String(item.courseSlug || '').toLowerCase().includes(key)
    )
  })
})

const studentTotalPages = computed(() => {
  const total = Math.ceil(filteredStudentItems.value.length / Number(studentPageSize.value || 10))
  return total > 0 ? total : 1
})

const pagedStudentItems = computed(() => {
  const page = Number(studentPage.value || 1)
  const size = Number(studentPageSize.value || 10)
  return filteredStudentItems.value.slice((page - 1) * size, page * size)
})

const reviewPage = ref(1)
const reviewPageSize = ref(8)

const filteredReviews = computed(() => {
  let result = [...reviews.value]
  if (starFilter.value) {
    result = result.filter(item => Number(item.stars) === Number(starFilter.value))
  }
  const key = reviewKeyword.value.trim().toLowerCase()
  if (key) {
    result = result.filter(item => (
      String(item.studentName || '').toLowerCase().includes(key) ||
      String(item.courseTitle || '').toLowerCase().includes(key) ||
      String(item.courseSlug || '').toLowerCase().includes(key) ||
      String(item.comment || '').toLowerCase().includes(key)
    ))
  }
  return result
})

const reviewTotalPages = computed(() => {
  const total = Math.ceil(filteredReviews.value.length / Number(reviewPageSize.value || 8))
  return total > 0 ? total : 1
})

const pagedReviews = computed(() => {
  const page = Number(reviewPage.value || 1)
  const size = Number(reviewPageSize.value || 8)
  return filteredReviews.value.slice((page - 1) * size, page * size)
})

const filteredRevenueTopCourses = computed(() => {
  const key = courseRevenueKeyword.value.trim().toLowerCase()
  if (!key) return revenueTopCourses.value

  return revenueTopCourses.value.filter(item =>
    String(item.courseTitle || '').toLowerCase().includes(key)
  )
})

const courseRevenueTotalPages = computed(() => {
  const total = Math.ceil(filteredRevenueTopCourses.value.length / Number(courseRevenuePageSize.value || 5))
  return total > 0 ? total : 1
})

const pagedRevenueTopCourses = computed(() => {
  const page = Number(courseRevenuePage.value || 1)
  const size = Number(courseRevenuePageSize.value || 5)
  const start = (page - 1) * size
  return filteredRevenueTopCourses.value.slice(start, start + size)
})

const filteredRevenueTransactions = computed(() => {
  const key = transactionKeyword.value.trim().toLowerCase()
  if (!key) return revenueTransactions.value

  return revenueTransactions.value.filter(item =>
    String(item.studentName || '').toLowerCase().includes(key) ||
    String(item.courseTitle || '').toLowerCase().includes(key)
  )
})

const transactionTotalPages = computed(() => {
  const total = Math.ceil(filteredRevenueTransactions.value.length / Number(transactionPageSize.value || 5))
  return total > 0 ? total : 1
})

const pagedRevenueTransactions = computed(() => {
  const page = Number(transactionPage.value || 1)
  const size = Number(transactionPageSize.value || 5)
  const start = (page - 1) * size
  return filteredRevenueTransactions.value.slice(start, start + size)
})

const overallReviewAverage = computed(() => {
  if (!reviews.value.length) return '0.0'
  const total = reviews.value.reduce((sum, item) => sum + Number(item.stars || 0), 0)
  return (total / reviews.value.length).toFixed(1)
})

const revenueChartRows = computed(() => {
  if (!Array.isArray(revenueChart.value)) return []
  return [...revenueChart.value].sort((a, b) => {
    const ya = Number(a.year || 0), yb = Number(b.year || 0)
    if (ya !== yb) return ya - yb
    return Number(a.month || 0) - Number(b.month || 0)
  })
})

const revenueChartData = computed(() => {
  const rows = revenueChartRows.value
  const labels = rows.map(r => r.year ? `T${r.month}/${String(r.year).slice(2)}` : `T${r.month}`)
  return {
    labels,
    datasets: [
      {
        label: 'Doanh thu gộp',
        data: rows.map(r => Number(r.gross || 0)),
        borderColor: '#3B82F6',
        backgroundColor: 'rgba(59,130,246,0.08)',
        fill: true,
        tension: 0.3,
        pointRadius: 4,
        pointHoverRadius: 6
      },
      {
        label: 'Hoàn tiền',
        data: rows.map(r => Number(r.refund || 0)),
        borderColor: '#EF4444',
        backgroundColor: 'rgba(239,68,68,0.06)',
        fill: true,
        tension: 0.3,
        pointRadius: 3,
        pointHoverRadius: 5
      },
      {
        label: 'Thực nhận',
        data: rows.map(r => Number(r.net || 0)),
        borderColor: '#10B981',
        backgroundColor: 'rgba(16,185,129,0.10)',
        fill: true,
        tension: 0.3,
        pointRadius: 4,
        pointHoverRadius: 6
      }
    ]
  }
})

const revenueChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: { position: 'top', labels: { font: { size: 11 }, usePointStyle: true, padding: 16 } },
    tooltip: {
      callbacks: {
        label: ctx => {
          const val = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(ctx.parsed.y)
          return ` ${ctx.dataset.label}: ${val}`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        font: { size: 10 },
        callback: val => {
          if (val >= 1_000_000) return `${(val / 1_000_000).toFixed(0)}M`
          if (val >= 1_000) return `${(val / 1_000).toFixed(0)}K`
          return val
        }
      },
      grid: { color: 'rgba(0,0,0,0.04)' }
    },
    x: { ticks: { font: { size: 10 } }, grid: { display: false } }
  }
}

const refundRate = computed(() => {
  const gross = Number(revenueMetrics.value.grossRevenue || 0)
  const refund = Number(revenueMetrics.value.refundAmount || 0)

  if (gross <= 0) return '0%'
  return `${((refund / gross) * 100).toFixed(1)}%`
})

const avgRevenuePerSale = computed(() => {
  const gross = Number(revenueMetrics.value.grossRevenue || 0)
  const sales = Number(revenueMetrics.value.salesCount || 0)

  if (sales <= 0) return formatCurrency(0)
  return formatCurrency(gross / sales)
})

const netRate = computed(() => {
  const gross = Number(revenueMetrics.value.grossRevenue || 0)
  const net = Number(revenueMetrics.value.netRevenue || 0)

  if (gross <= 0) return '0%'
  return `${((net / gross) * 100).toFixed(1)}%`
})

const formatNumber = (value) => {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(value || 0))
}

const formatDateTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('vi-VN')
}

const getFileDate = () => {
  const now = new Date()
  const pad = (num) => String(num).padStart(2, '0')
  return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}`
}

const downloadBlobFile = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.click()
  URL.revokeObjectURL(url)
}

const escapeHtml = (value) => {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

const exportStudentsExcelFallback = () => {
  const rows = studentItems.value.map((item, index) => ({
    stt: index + 1,
    studentName: item.studentName || '',
    studentEmail: item.studentEmail || '',
    courseTitle: item.courseTitle || '',
    courseSlug: item.courseSlug || '',
    status: item.enrollmentStatus || item.status || '',
    progress: Number(item.progress ?? 0),
    enrolledAt: item.enrolledAt ? formatDateTime(item.enrolledAt) : '',
    studentAvatar: item.studentAvatar || '',
    courseThumbnail: item.courseThumbnail || ''
  }))

  const body = rows.length
    ? rows.map((row) => `
      <tr>
        <td>${row.stt}</td>
        <td>${escapeHtml(row.studentName)}</td>
        <td>${escapeHtml(row.studentEmail)}</td>
        <td>${escapeHtml(row.courseTitle)}</td>
        <td>${escapeHtml(row.courseSlug)}</td>
        <td>${escapeHtml(row.status)}</td>
        <td>${row.progress}%</td>
        <td>${escapeHtml(row.enrolledAt)}</td>
        <td>${escapeHtml(row.studentAvatar)}</td>
        <td>${escapeHtml(row.courseThumbnail)}</td>
      </tr>
    `).join('')
    : `
      <tr>
        <td colspan=\"10\">Không có dữ liệu học viên để xuất.</td>
      </tr>
    `

  const html = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset=\"UTF-8\" />
      </head>
      <body>
        <table border=\"1\">
          <thead>
            <tr>
              <th>STT</th>
              <th>Học viên</th>
              <th>Email</th>
              <th>Khóa học</th>
              <th>Slug khóa học</th>
              <th>Trạng thái ghi danh</th>
              <th>Tiến độ</th>
              <th>Ngày ghi danh</th>
              <th>Avatar học viên</th>
              <th>Ảnh khóa học</th>
            </tr>
          </thead>
          <tbody>${body}</tbody>
        </table>
      </body>
    </html>
  `

  const blob = new Blob(['\uFEFF', html], {
    type: 'application/vnd.ms-excel;charset=utf-8;'
  })
  downloadBlobFile(blob, `students_${getFileDate()}.xls`)
}

const exportStudentsExcel = async () => {
  exportingStudents.value = true
  try {
    const blob = await axiosClient.get('/instructor/performance/students/export', {
      responseType: 'blob'
    })
    if (!(blob instanceof Blob) || blob.size === 0) {
      throw new Error('Invalid export payload')
    }
    downloadBlobFile(blob, `students_${getFileDate()}.xlsx`)
  } catch (err) {
    console.warn('Không thể export từ API, chuyển sang export tại frontend:', err)
    exportStudentsExcelFallback()
  } finally {
    exportingStudents.value = false
  }
}

const renderStars = (stars) => {
  const count = Math.max(0, Math.min(5, Number(stars || 0)))
  return '★'.repeat(count) + '☆'.repeat(5 - count)
}

const goTab = (nextTab) => {
  router.push(`/instructor/performance/${nextTab}`)
}

const tabButtonClass = (name) => {
  return [
    'w-full text-left px-3 py-2.5 rounded-xl text-sm font-semibold transition flex items-center',
    tab.value === name
      ? 'bg-purple-50 text-purple-700'
      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
  ]
}

const statusClass = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'bg-emerald-50 text-emerald-700 border border-emerald-100'
    case 'COMPLETED':
      return 'bg-blue-50 text-blue-700 border border-blue-100'
    case 'CANCELLED':
      return 'bg-red-50 text-red-700 border border-red-100'
    default:
      return 'bg-gray-100 text-gray-600 border border-gray-200'
  }
}

const fetchOverview = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await instructorPerformanceService.getStudentOverview()
    overview.value = {
      instructorEmail: response.instructorEmail || '',
      totalCourses: Number(response.totalCourses || 0),
      totalUniqueStudents: Number(response.totalUniqueStudents || 0),
      totalActiveEnrollments: Number(response.totalActiveEnrollments || 0),
      courses: Array.isArray(response.courses) ? response.courses : []
    }
  } catch (err) {
    console.error('Lỗi lấy dữ liệu overview:', err)
    error.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải dữ liệu tổng quan.'
  } finally {
    loading.value = false
  }
}

const fetchStudentItems = async () => {
  loading.value = true
  error.value = ''

  try {
    const [items, atRisk] = await Promise.all([
      instructorPerformanceService.getStudentItems(),
      axiosClient.get('/instructor/performance/at-risk').catch(() => [])
    ])
    studentItems.value = Array.isArray(items) ? items : []
    atRiskStudents.value = Array.isArray(atRisk) ? atRisk : []
  } catch (err) {
    console.error('Lỗi lấy danh sách học viên:', err)
    error.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải danh sách học viên.'
  } finally {
    loading.value = false
  }
}

const fetchReviews = async () => {
  reviewLoading.value = true
  reviewError.value = ''

  try {
    const response = await RatingService.getInstructorReviews()
    reviews.value = Array.isArray(response) ? response : []
  } catch (err) {
    console.error('Lỗi lấy đánh giá:', err)
    reviewError.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải danh sách đánh giá.'
  } finally {
    reviewLoading.value = false
  }
}

const fetchRevenue = async () => {
  revenueLoading.value = true
  revenueError.value = ''

  try {
    const response = await instructorRevenueService.getRevenueSummary(buildRevenueParams())

    revenueMetrics.value = {
      grossRevenue: Number(response?.metrics?.grossRevenue || 0),
      refundAmount: Number(response?.metrics?.refundAmount || 0),
      netRevenue: Number(response?.metrics?.netRevenue || 0),
      studentsCount: Number(response?.metrics?.studentsCount || 0),
      salesCount: Number(response?.metrics?.salesCount || 0)
    }

    revenueChart.value = Array.isArray(response?.chart) ? response.chart : []
    revenueTopCourses.value = Array.isArray(response?.topCourses) ? response.topCourses : []
    revenueTransactions.value = Array.isArray(response?.transactions) ? response.transactions : []

    courseRevenuePage.value = 1
    transactionPage.value = 1
  } catch (err) {
    console.error('Lỗi lấy doanh thu:', err)
    revenueError.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải dữ liệu doanh thu.'
  } finally {
    revenueLoading.value = false
  }
}

const refreshStudentsTab = async () => {
  await Promise.all([fetchOverview(), fetchStudentItems()])
}

watch(keyword, () => { studentPage.value = 1 })
watch(studentPageSize, () => { studentPage.value = 1 })
watch(reviewKeyword, () => { reviewPage.value = 1 })
watch(starFilter, () => { reviewPage.value = 1 })
watch(reviewPageSize, () => { reviewPage.value = 1 })

watch(courseRevenueKeyword, () => {
  courseRevenuePage.value = 1
})

watch(courseRevenuePageSize, () => {
  courseRevenuePage.value = 1
})

watch(transactionKeyword, () => {
  transactionPage.value = 1
})

watch(transactionPageSize, () => {
  transactionPage.value = 1
})

watch(
  () => route.params.tab,
  async (newTab) => {
    tab.value = newTab || 'overview'

    if (tab.value === 'overview') {
      await fetchOverview()
    } else if (tab.value === 'students') {
      await Promise.all([fetchOverview(), fetchStudentItems()])
    } else if (tab.value === 'reviews') {
      await fetchReviews()
    } else if (tab.value === 'revenue') {
      await fetchRevenue()
    } else if (tab.value === 'analytics') {
      if (!overview.value.courses.length) await fetchOverview()
      analyticsCourseId.value = ''
      analyticsData.value = []
    }
  },
  { immediate: true }
)

onMounted(async () => {
  if (tab.value === 'overview') {
    await fetchOverview()
  } else if (tab.value === 'students') {
    await Promise.all([fetchOverview(), fetchStudentItems()])
  } else if (tab.value === 'reviews') {
    await fetchReviews()
  } else if (tab.value === 'revenue') {
    await fetchRevenue()
  } else if (tab.value === 'analytics') {
    await Promise.all([fetchOverview(), fetchRatingTrend()])
  }
})

watch(tab, (newTab) => {
  if (newTab === 'analytics' && !ratingTrend.value) {
    fetchRatingTrend()
  }
})
</script>

<style scoped>
.perf-root {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

/* ── Sidebar ── */
.perf-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  padding: 20px 12px;
}

.perf-sidebar__brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 4px 16px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 12px;
}

.perf-sidebar__icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.perf-sidebar__title {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  line-height: 1.2;
}

.perf-sidebar__sub {
  font-size: 10px;
  color: #94a3b8;
  margin: 0;
  margin-top: 1px;
}

.perf-sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.perf-sidebar__section-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94a3b8;
  padding: 8px 8px 4px;
  margin: 0;
}

/* ── Scrollbar ── */
::-webkit-scrollbar { width: 5px; height: 5px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background: #cbd5e1; }
</style>
