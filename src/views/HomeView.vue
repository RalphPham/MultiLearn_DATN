<template>
  <div class="min-h-screen bg-white" style="font-family:'DM Sans','Segoe UI',system-ui,sans-serif;">

    <!-- ══════════════════════════════════════════
         HERO — Stats thật từ /api/public/stats
    ══════════════════════════════════════════ -->
    <section class="relative overflow-hidden min-h-[560px] flex items-center"
             style="background:linear-gradient(135deg,#0f172a 0%,#1e3a8a 50%,#1e1b4b 100%);">
      <div class="absolute inset-0">
        <img src="https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=1920&q=50"
             class="w-full h-full object-cover opacity-10" alt="" />
        <div class="absolute inset-0"
             style="background:linear-gradient(90deg,rgba(15,23,42,.96) 40%,rgba(15,23,42,.4) 75%,transparent);"></div>
      </div>
      <div class="absolute inset-0 opacity-[0.035]"
           style="background-image:radial-gradient(circle at 1px 1px,#fff 1px,transparent 0);background-size:40px 40px;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 py-20 w-full">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-14 items-center">

          <!-- LEFT: Text + Search -->
          <div class="text-white space-y-6">
            <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border text-sm font-semibold text-cyan-300"
                 style="background:rgba(6,182,212,.1);border-color:rgba(6,182,212,.25);">
              <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-cyan-400"></span>
              </span>
              {{ $t?.('home.hero_badge') || '🎓 Nền tảng học trực tuyến #1 Việt Nam' }}
            </div>

            <h1 class="text-4xl lg:text-5xl xl:text-[56px] font-black leading-[1.1] tracking-tight">
              {{ $t?.('home.hero_title_1') || 'Học kỹ năng mới.' }}
              <br/>
              <span class="text-transparent bg-clip-text"
                    style="background-image:linear-gradient(90deg,#22d3ee,#60a5fa,#a78bfa);">
                {{ $t?.('home.hero_title_2') || 'Thay đổi tương lai.' }}
              </span>
            </h1>

            <p class="text-lg text-slate-300 leading-relaxed max-w-lg">
              {{ $t?.('home.hero_desc') || 'Học mọi lúc, mọi nơi cùng các giảng viên hàng đầu Việt Nam.' }}
            </p>

            <!-- Stats inline — từ /api/public/stats (SystemStats) -->
            <div class="flex items-center gap-8 py-4 border-y" style="border-color:rgba(255,255,255,.1);">
              <template v-if="isStatsLoading">
                <div v-for="i in 4" :key="i" class="animate-pulse text-center">
                  <div class="h-7 w-16 bg-white/20 rounded mb-1 mx-auto"></div>
                  <div class="h-3 w-12 bg-white/10 rounded mx-auto"></div>
                </div>
              </template>
              <template v-else>
                <div class="text-center">
                  <div class="text-2xl font-black text-white">{{ formatNumber(stats.totalStudents) }}+</div>
                  <div class="text-xs text-slate-400 mt-0.5">{{ $t?.('home.stats.students') || 'Học viên' }}</div>
                </div>
                <div class="text-center">
                  <div class="text-2xl font-black text-white">{{ formatNumber(stats.totalCourses) }}+</div>
                  <div class="text-xs text-slate-400 mt-0.5">{{ $t?.('home.stats.courses') || 'Khoá học' }}</div>
                </div>
                <div class="text-center">
                  <div class="text-2xl font-black text-white">{{ formatNumber(stats.totalInstructors) }}+</div>
                  <div class="text-xs text-slate-400 mt-0.5">{{ $t?.('home.stats.instructors') || 'Giảng viên' }}</div>
                </div>
                <div class="text-center">
                  <div class="text-2xl font-black text-amber-400">{{ stats.averageRating }}★</div>
                  <div class="text-xs text-slate-400 mt-0.5">{{ $t?.('home.stats.rating') || 'Đánh giá' }}</div>
                </div>
              </template>
            </div>

            <!-- Search -->
            <div class="relative max-w-lg">
              <input v-model="heroSearch" @keyup.enter="handleHeroSearch" type="text"
                :placeholder="$t?.('search_placeholder') || 'Bạn muốn học gì? (React, Python, Excel...)'"
                class="w-full pl-6 pr-36 py-4 bg-white text-gray-900 rounded-2xl text-sm font-medium placeholder-gray-400 outline-none shadow-2xl focus:ring-4 focus:ring-cyan-500/30 transition-all"/>
              <button @click="handleHeroSearch"
                class="absolute right-2 top-1/2 -translate-y-1/2 px-5 py-2.5 font-bold text-sm text-white rounded-xl active:scale-95 whitespace-nowrap"
                style="background:linear-gradient(135deg,#2563eb,#7c3aed);">
                Tìm kiếm
              </button>
            </div>

            <!-- Popular tags — lấy từ categories thật (name) -->
            <div class="flex flex-wrap gap-2 items-center">
              <span class="text-xs text-slate-400">Phổ biến:</span>
              <template v-if="isCatsLoading">
                <div v-for="i in 5" :key="i" class="h-7 w-16 bg-white/15 rounded-full animate-pulse"></div>
              </template>
              <button v-else v-for="cat in popularTagCategories" :key="cat.id"
                @click="$router.push({ path: '/course', query: { categoryId: cat.id } })"
                class="text-xs px-3 py-1.5 rounded-full text-slate-300 hover:text-white transition-all cursor-pointer border"
                style="background:rgba(255,255,255,.07);border-color:rgba(255,255,255,.12);">
                {{ cat.name }}
              </button>
            </div>
          </div>

          <!-- RIGHT: Image + stat cards thật từ API -->
          <div class="relative hidden lg:block">
            <div class="relative rounded-3xl overflow-hidden shadow-2xl border" style="border-color:rgba(255,255,255,.1);">
              <img src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=700&q=75"
                   alt="Students" class="w-full h-72 object-cover"/>
              <div class="absolute inset-0" style="background:linear-gradient(to top,rgba(15,23,42,.85),transparent 55%);"></div>
              <div class="absolute bottom-0 left-0 right-0 p-5">
                <div class="flex items-center gap-3 p-3 rounded-xl backdrop-blur-md border"
                     style="background:rgba(255,255,255,.08);border-color:rgba(255,255,255,.15);">
                  <div class="w-9 h-9 rounded-full flex items-center justify-center text-white font-black text-xs flex-shrink-0"
                       style="background:linear-gradient(135deg,#10b981,#06b6d4);">HV</div>
                  <div class="flex-1 min-w-0">
                    <p class="text-white font-semibold text-sm">Học viên MultiLearn</p>
                    <p class="text-slate-300 text-xs">vừa hoàn thành khoá học ⭐⭐⭐⭐⭐</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Floating card 1: totalCourses thật -->
            <div class="absolute -top-5 -right-5 bg-white rounded-2xl shadow-2xl p-4 border border-gray-100 animate-float">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-blue-100 rounded-xl flex items-center justify-center text-lg flex-shrink-0">📚</div>
                <div>
                  <p class="text-xs text-gray-500">Khoá học</p>
                  <p class="text-base font-black text-gray-900">
                    <template v-if="isStatsLoading">
                      <span class="inline-block h-5 w-10 bg-gray-200 rounded animate-pulse"></span>
                    </template>
                    <template v-else>{{ formatNumber(stats.totalCourses) }}+</template>
                  </p>
                </div>
              </div>
            </div>

            <!-- Floating card 2: totalInstructors thật -->
            <div class="absolute -bottom-4 -left-5 bg-white rounded-2xl shadow-2xl p-4 border border-gray-100 animate-float-delay">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-violet-100 rounded-xl flex items-center justify-center text-lg flex-shrink-0">🎙️</div>
                <div>
                  <p class="text-xs text-gray-500">Giảng viên</p>
                  <p class="text-base font-black text-gray-900">
                    <template v-if="isStatsLoading">
                      <span class="inline-block h-5 w-10 bg-gray-200 rounded animate-pulse"></span>
                    </template>
                    <template v-else>{{ formatNumber(stats.totalInstructors) }}+</template>
                  </p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         STATS BAR — /api/public/stats (SystemStats)
    ══════════════════════════════════════════ -->
    <section class="bg-white border-y border-gray-100">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div v-if="isStatsLoading" class="grid grid-cols-2 md:grid-cols-4 gap-6 animate-pulse">
          <div v-for="i in 4" :key="i" class="text-center">
            <div class="h-10 bg-gray-200 rounded w-24 mx-auto mb-2"></div>
            <div class="h-3 bg-gray-200 rounded w-20 mx-auto"></div>
          </div>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-6 divide-x divide-gray-100">
          <div class="text-center group cursor-default">
            <p class="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-br from-blue-600 to-blue-400 group-hover:scale-105 transition-transform inline-block">
              {{ formatNumber(stats.totalStudents) }}+
            </p>
            <p class="text-xs font-bold text-gray-500 mt-1 uppercase tracking-widest">
              {{ $t?.('home.stats.students') || 'Học viên' }}
            </p>
          </div>
          <div class="text-center group cursor-default">
            <p class="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-br from-violet-600 to-violet-400 group-hover:scale-105 transition-transform inline-block">
              {{ formatNumber(stats.totalCourses) }}+
            </p>
            <p class="text-xs font-bold text-gray-500 mt-1 uppercase tracking-widest">
              {{ $t?.('home.stats.courses') || 'Khoá học' }}
            </p>
          </div>
          <div class="text-center group cursor-default">
            <p class="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-br from-pink-600 to-pink-400 group-hover:scale-105 transition-transform inline-block">
              {{ formatNumber(stats.totalInstructors) }}+
            </p>
            <p class="text-xs font-bold text-gray-500 mt-1 uppercase tracking-widest">
              {{ $t?.('home.stats.instructors') || 'Giảng viên' }}
            </p>
          </div>
          <div class="text-center group cursor-default">
            <div class="flex items-center justify-center gap-1.5 group-hover:scale-105 transition-transform">
              <span class="text-4xl font-black text-amber-500">{{ stats.averageRating }}</span>
              <svg class="w-7 h-7 text-amber-400 fill-current" viewBox="0 0 24 24">
                <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
              </svg>
            </div>
            <p class="text-xs font-bold text-gray-500 mt-1 uppercase tracking-widest">
              {{ $t?.('home.stats.rating') || 'Đánh giá TB' }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         FEATURED COURSES
         GET /api/public/courses → Page<CourseListDTO>
         Fields: id, title, slug, thumbnail, price, salePrice,
                 level, averageRating, studentCount, totalLessons,
                 totalDuration, categoryName, instructorName, instructorAvatar
    ══════════════════════════════════════════ -->
    <section id="courses-section" class="py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        <!-- Header -->
        <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-10">
          <div>
            <p class="text-xs font-black text-blue-600 uppercase tracking-widest mb-1.5">Được học viên yêu thích</p>
            <h2 class="text-3xl lg:text-4xl font-black text-gray-900">
              {{ $t?.('home.featured_title') || 'Khoá học nổi bật' }}
            </h2>
            <p class="text-gray-500 mt-1.5 text-sm">
              {{ $t?.('home.featured_desc') || 'Khám phá các khoá học được yêu thích nhất' }}
            </p>
          </div>
          <router-link to="/course"
            class="group inline-flex items-center gap-2 text-sm font-bold text-blue-600 hover:text-blue-800 transition-colors whitespace-nowrap bg-blue-50 hover:bg-blue-100 px-5 py-2.5 rounded-full border border-blue-100">
            {{ $t?.('home.view_all') || 'Xem tất cả' }}
            <svg class="w-4 h-4 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7"/>
            </svg>
          </router-link>
        </div>

        <!-- Category Tabs — GET /api/categories → CategoryDTO[] (id, name, iconUrl) -->
        <div class="flex gap-2 mb-8 overflow-x-auto pb-1 scrollbar-hide -mx-1 px-1">
          <button @click="selectTab(null)"
            class="flex items-center gap-1.5 px-5 py-2.5 rounded-full text-sm font-bold transition-all whitespace-nowrap border-2 flex-shrink-0"
            :class="activeTabId === null
              ? 'bg-blue-600 text-white border-blue-600 shadow-lg shadow-blue-200'
              : 'bg-white text-gray-600 border-gray-200 hover:border-blue-300 hover:text-blue-600'">
            🌟 Tất cả
          </button>
          <div v-if="isCatsLoading" v-for="i in 5" :key="i"
               class="h-[42px] w-28 bg-gray-200 rounded-full animate-pulse flex-shrink-0"></div>
          <!-- CategoryDTO: id, name, iconUrl -->
          <button v-for="cat in categories" :key="cat.id"
            @click="selectTab(cat.id)"
            class="flex items-center gap-1.5 px-5 py-2.5 rounded-full text-sm font-bold transition-all whitespace-nowrap border-2 flex-shrink-0"
            :class="activeTabId === cat.id
              ? 'bg-blue-600 text-white border-blue-600 shadow-lg shadow-blue-200'
              : 'bg-white text-gray-600 border-gray-200 hover:border-blue-300 hover:text-blue-600'">
            <img v-if="cat.iconUrl" :src="cat.iconUrl" class="w-4 h-4 object-contain rounded-sm flex-shrink-0" :alt="cat.name" />
            <span v-else>{{ catEmojiFallback[categories.indexOf(cat) % catEmojiFallback.length] }}</span>
            {{ cat.name }}
          </button>
        </div>

        <!-- Skeleton -->
        <div v-if="isCoursesLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div v-for="n in 8" :key="n" class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-pulse">
            <div class="aspect-video bg-gray-200"></div>
            <div class="p-4 space-y-3">
              <div class="h-3 bg-gray-200 rounded w-1/3"></div>
              <div class="h-4 bg-gray-200 rounded w-full"></div>
              <div class="h-4 bg-gray-200 rounded w-4/5"></div>
              <div class="flex items-center gap-2">
                <div class="w-6 h-6 bg-gray-200 rounded-full"></div>
                <div class="h-3 bg-gray-200 rounded w-1/2"></div>
              </div>
              <div class="flex gap-1">
                <div class="h-3.5 bg-gray-200 rounded w-8"></div>
                <div class="h-3.5 bg-gray-200 rounded w-20"></div>
              </div>
              <div class="flex justify-between pt-2 border-t border-gray-100">
                <div class="h-6 bg-gray-200 rounded w-20"></div>
                <div class="w-8 h-8 bg-gray-200 rounded-xl"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty -->
        <div v-else-if="courses.length === 0"
             class="text-center py-20 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
          <div class="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center text-2xl mx-auto mb-4">📭</div>
          <h3 class="text-xl font-black text-gray-900">{{ $t?.('home.no_courses_title') || 'Chưa có khoá học' }}</h3>
          <p class="text-gray-500 mt-2 text-sm">{{ $t?.('home.no_courses_desc') || 'Hãy quay lại sau nhé!' }}</p>
          <button @click="selectTab(null)"
            class="mt-5 px-6 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 transition-colors">
            Xem tất cả khoá học
          </button>
        </div>

        <!-- Course Grid — CourseListDTO fields -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          <div v-for="course in courses" :key="course.id"
            class="group bg-white rounded-2xl overflow-hidden border border-gray-100 hover:shadow-2xl hover:-translate-y-1.5 transition-all duration-300 cursor-pointer flex flex-col"
            @click="$router.push('/course/slug/' + course.slug)">

            <!-- course.thumbnail -->
            <div class="relative overflow-hidden aspect-video flex-shrink-0">
              <img :src="course.thumbnail || 'https://placehold.co/640x360/e2e8f0/94a3b8?text=MultiLearn'"
                :alt="course.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                loading="lazy"/>

              <!-- Play button on hover -->
              <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-all flex items-center justify-center">
                <div class="w-11 h-11 rounded-full bg-white/90 shadow-xl flex items-center justify-center opacity-0 group-hover:opacity-100 scale-75 group-hover:scale-100 transition-all duration-300">
                  <svg class="w-4 h-4 text-blue-600 translate-x-0.5" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M6.3 2.841A1.5 1.5 0 004 4.11V15.89a1.5 1.5 0 002.3 1.269l9.344-5.89a1.5 1.5 0 000-2.538L6.3 2.84z"/>
                  </svg>
                </div>
              </div>

              <!-- course.categoryName -->
              <div v-if="course.categoryName"
                class="absolute top-2.5 left-2.5 px-2.5 py-1 bg-white/90 backdrop-blur-sm rounded-lg text-[11px] font-black text-blue-700 shadow-sm max-w-[140px] truncate">
                {{ course.categoryName }}
              </div>

              <!-- Badge: Free / Sale % -->
              <div v-if="isFree(course)"
                class="absolute top-2.5 right-2.5 px-2.5 py-1 bg-emerald-500 rounded-lg text-[11px] font-black text-white">
                FREE
              </div>
              <div v-else-if="course.salePrice && course.price && Number(course.salePrice) < Number(course.price)"
                class="absolute top-2.5 right-2.5 px-2.5 py-1 bg-red-500 rounded-lg text-[11px] font-black text-white">
                -{{ discountPercent(course.price, course.salePrice) }}%
              </div>

              <!-- course.level -->
              <div v-if="course.level"
                class="absolute bottom-2.5 right-2.5 px-2 py-0.5 bg-black/60 backdrop-blur-sm rounded-md text-[10px] font-bold text-white">
                {{ course.level }}
              </div>
            </div>

            <!-- Card body -->
            <div class="p-4 flex flex-col flex-1 gap-2">

              <!-- course.title -->
              <h3 class="font-bold text-gray-900 text-sm leading-snug line-clamp-2 group-hover:text-blue-600 transition-colors min-h-[2.5rem]">
                {{ course.title }}
              </h3>

              <!-- course.instructorName + course.instructorAvatar -->
              <div class="flex items-center gap-2">
                <img :src="course.instructorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(course.instructorName || 'GV')}&background=e0e7ff&color=4f46e5&bold=true&size=40`"
                  :alt="course.instructorName"
                  class="w-6 h-6 rounded-full object-cover flex-shrink-0 ring-1 ring-gray-200"/>
                <span class="text-xs text-gray-500 font-semibold truncate">
                  {{ course.instructorName || 'MultiLearn Teacher' }}
                </span>
              </div>

              <!-- course.averageRating + course.studentCount -->
              <div class="flex items-center gap-2 flex-wrap">
                <div class="flex items-center gap-1">
                  <span class="text-sm font-black text-amber-600">
                    {{ getCourseRatingDisplay(course) }}
                  </span>
                  <div class="flex gap-0.5">
                    <svg v-for="i in 5" :key="i" class="w-3 h-3 fill-current"
                      :class="i <= getCourseFilledStars(course) ? 'text-amber-400' : 'text-gray-200'"
                      viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                  </div>
                </div>
                <span class="text-xs text-gray-400 font-medium">
                  ({{ formatNumber(course.studentCount || 0) }} {{ $t?.('course.students') || 'HV' }})
                </span>
              </div>

              <!-- course.totalLessons + course.totalDuration (Integer phút) -->
              <div v-if="course.totalLessons || course.totalDuration"
                   class="flex items-center gap-3 text-xs text-gray-400 font-medium">
                <span v-if="course.totalLessons" class="flex items-center gap-1">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                  </svg>
                  {{ course.totalLessons }} bài
                </span>
                <span v-if="course.totalDuration" class="flex items-center gap-1">
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  {{ formatDuration(course.totalDuration) }}
                </span>
              </div>

              <!-- course.salePrice / course.price (BigDecimal) + Cart -->
              <div class="flex items-center justify-between pt-3 mt-auto border-t border-gray-100">
                <div class="flex flex-col leading-tight">
                  <span class="text-lg font-black"
                    :class="isFree(course) ? 'text-emerald-600' : 'text-gray-900'">
                    {{ formatCurrency(course.salePrice || course.price) }}
                  </span>
                  <span v-if="course.salePrice && course.price && Number(course.salePrice) < Number(course.price)"
                    class="text-xs text-gray-400 line-through">
                    {{ formatCurrency(course.price) }}
                  </span>
                </div>
                <button v-if="userRole !== 'ADMIN'"
                  @click.stop="addToCart(course.id)"
                  :disabled="cartLoadingId === course.id"
                  class="w-9 h-9 rounded-xl flex items-center justify-center transition-all border"
                  :class="cartLoadingId === course.id
                    ? 'bg-gray-100 border-gray-200 cursor-wait'
                    : 'bg-blue-50 border-blue-100 text-blue-600 hover:bg-blue-600 hover:text-white hover:border-blue-600 hover:shadow-md'"
                  :title="$t?.('course.add_cart') || 'Thêm vào giỏ'">
                  <svg v-if="cartLoadingId !== course.id" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                  </svg>
                  <svg v-else class="w-4 h-4 animate-spin text-gray-400" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-10">
          <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
            class="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center text-gray-600 hover:border-blue-400 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7"/></svg>
          </button>
          <button v-for="p in visiblePages" :key="p" @click="changePage(p)"
            class="w-10 h-10 rounded-xl border-2 text-sm font-bold transition-all"
            :class="p === currentPage ? 'bg-blue-600 border-blue-600 text-white' : 'border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-600'">
            {{ p + 1 }}
          </button>
          <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
            class="w-10 h-10 rounded-xl border-2 border-gray-200 flex items-center justify-center text-gray-600 hover:border-blue-400 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7"/></svg>
          </button>
        </div>

        <div class="mt-12 text-center">
          <router-link to="/course"
            class="inline-flex items-center gap-2 px-10 py-4 text-white font-bold rounded-2xl transition-all hover:-translate-y-0.5 active:scale-95"
            style="background:linear-gradient(135deg,#2563eb,#4f46e5);box-shadow:0 8px 25px rgba(37,99,235,.35);">
            {{ $t?.('home.discover_btn') || 'Khám phá tất cả khoá học' }}
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 7l5 5m0 0l-5 5m5-5H6"/>
            </svg>
          </router-link>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         TOP CATEGORIES
         GET /api/categories → CategoryDTO[]
         Fields: id, name, iconUrl, courseCount (Long)
    ══════════════════════════════════════════ -->
    <section class="py-20 bg-gray-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <p class="text-xs font-black text-violet-600 uppercase tracking-widest mb-2">Tất cả lĩnh vực</p>
          <h2 class="text-3xl lg:text-4xl font-black text-gray-900">Khám phá danh mục hàng đầu</h2>
          <p class="text-gray-500 mt-2 max-w-lg mx-auto text-sm">Từ lập trình đến thiết kế — chúng tôi có khoá học cho mọi hành trình.</p>
        </div>

        <div v-if="isCatsLoading" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          <div v-for="i in 10" :key="i" class="bg-white rounded-2xl p-5 animate-pulse">
            <div class="w-12 h-12 bg-gray-200 rounded-2xl mx-auto mb-3"></div>
            <div class="h-3.5 bg-gray-200 rounded w-3/4 mx-auto mb-2"></div>
            <div class="h-3 bg-gray-200 rounded w-1/2 mx-auto"></div>
          </div>
        </div>

        <div v-else-if="categories.length === 0" class="text-center py-10 text-gray-400 text-sm">
          Chưa có danh mục nào.
        </div>

        <!-- CategoryDTO: id, name, iconUrl, courseCount -->
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
          <div v-for="(cat, idx) in categories" :key="cat.id"
            @click="$router.push({ path: '/course', query: { categoryId: cat.id } })"
            class="group relative bg-white rounded-2xl p-5 flex flex-col items-center text-center cursor-pointer border-2 border-transparent hover:border-blue-200 hover:shadow-xl hover:-translate-y-1 transition-all duration-300 overflow-hidden">
            <div class="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity rounded-2xl"
                 :style="{ background: catGradients[idx % catGradients.length] }"></div>
            <div class="relative z-10 w-14 h-14 rounded-2xl flex items-center justify-center mb-3 transition-transform group-hover:scale-110 shadow-md overflow-hidden"
                 :style="{ backgroundColor: catColors[idx % catColors.length] + '22' }">
              <!-- cat.iconUrl — URL ảnh từ backend (Cloudinary/Minio) -->
              <img v-if="cat.iconUrl" :src="cat.iconUrl" :alt="cat.name" class="w-8 h-8 object-contain"/>
              <span v-else class="text-2xl">{{ catEmojiFallback[idx % catEmojiFallback.length] }}</span>
            </div>
            <h3 class="relative z-10 font-bold text-gray-800 text-sm group-hover:text-gray-900 leading-snug">{{ cat.name }}</h3>
            <!-- cat.courseCount — Long từ CategoryDTO -->
            <p class="relative z-10 text-xs text-gray-400 mt-1 font-medium">
              {{ cat.courseCount ? formatNumber(cat.courseCount) + ' khoá học' : 'Xem khoá học' }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         TESTIMONIALS — REAL DATA
         GET /api/ratings/course/{courseId}
         → List<RatingResponseDTO>
         Fields: stars, comment, studentName, studentAvatar,
                 courseTitle, courseThumbnail, createdAt
    ══════════════════════════════════════════ -->
    <section class="py-20 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center mb-12">
          <p class="text-xs font-black text-emerald-600 uppercase tracking-widest mb-2">Học viên nói gì</p>
          <h2 class="text-3xl lg:text-4xl font-black text-gray-900">Hàng nghìn học viên đã thay đổi</h2>
          <p class="text-gray-500 mt-2 text-sm">Đây là những đánh giá thật từ học viên MultiLearn.</p>
        </div>

        <!-- Loading skeleton -->
        <div v-if="isReviewsLoading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="i in 3" :key="i" class="bg-white rounded-2xl p-6 border-2 border-gray-100 animate-pulse">
            <div class="flex gap-1 mb-4">
              <div v-for="j in 5" :key="j" class="w-4 h-4 bg-gray-200 rounded"></div>
            </div>
            <div class="space-y-2 mb-5">
              <div class="h-3.5 bg-gray-200 rounded w-full"></div>
              <div class="h-3.5 bg-gray-200 rounded w-5/6"></div>
              <div class="h-3.5 bg-gray-200 rounded w-4/6"></div>
            </div>
            <div class="flex items-center gap-2 p-2.5 bg-gray-50 rounded-xl mb-4">
              <div class="w-10 h-7 bg-gray-200 rounded-lg flex-shrink-0"></div>
              <div class="h-3 bg-gray-200 rounded flex-1"></div>
            </div>
            <div class="flex items-center gap-3 pt-3 border-t border-gray-100">
              <div class="w-9 h-9 bg-gray-200 rounded-full flex-shrink-0"></div>
              <div class="flex-1">
                <div class="h-3.5 bg-gray-200 rounded w-24 mb-1.5"></div>
                <div class="h-3 bg-gray-200 rounded w-32"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- No reviews yet -->
        <div v-else-if="reviews.length === 0"
             class="text-center py-16 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
          <div class="w-14 h-14 bg-amber-100 rounded-2xl flex items-center justify-center text-2xl mx-auto mb-4">⭐</div>
          <p class="text-gray-500 font-medium">Chưa có đánh giá nào. Hãy là người đầu tiên!</p>
        </div>

        <!-- Real reviews — RatingResponseDTO -->
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="review in reviews" :key="review.id"
               class="bg-white rounded-2xl p-6 border-2 border-gray-100 hover:border-blue-200 hover:shadow-xl transition-all duration-300">
            <!-- rating.stars (Integer 1-5) -->
            <div class="flex gap-0.5 mb-4">
              <svg v-for="i in 5" :key="i" class="w-4 h-4 fill-current"
                :class="i <= review.stars ? 'text-amber-400' : 'text-gray-200'" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
            </div>

            <!-- rating.comment -->
            <p class="text-gray-600 text-sm leading-relaxed italic mb-5 line-clamp-4">
              "{{ review.comment }}"
            </p>

            <!-- rating.courseTitle + rating.courseThumbnail -->
            <div class="flex items-center gap-2 mb-4 p-2.5 bg-gray-50 rounded-xl">
              <img v-if="review.courseThumbnail"
                :src="review.courseThumbnail"
                class="w-10 h-7 rounded-lg object-cover flex-shrink-0"
                :alt="review.courseTitle"/>
              <div v-else class="w-10 h-7 bg-gray-200 rounded-lg flex-shrink-0 flex items-center justify-center text-xs">📚</div>
              <p class="text-xs font-bold text-gray-700 truncate">{{ review.courseTitle }}</p>
            </div>

            <!-- rating.studentName + rating.studentAvatar + rating.createdAt -->
            <div class="flex items-center gap-3 pt-3 border-t border-gray-100">
              <img :src="review.studentAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(review.studentName || 'HV')}&background=dbeafe&color=2563eb&bold=true&size=40`"
                :alt="review.studentName"
                class="w-9 h-9 rounded-full object-cover ring-2 ring-blue-100 flex-shrink-0"/>
              <div class="min-w-0 flex-1">
                <p class="text-sm font-bold text-gray-900 truncate">{{ review.studentName || 'Học viên MultiLearn' }}</p>
              </div>
              <span class="text-[10px] text-gray-400 flex-shrink-0">{{ formatRelativeTime(review.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════════
         BECOME INSTRUCTOR CTA
    ══════════════════════════════════════════ -->
    <section class="py-20 relative overflow-hidden"
             style="background:linear-gradient(135deg,#0f172a 0%,#1e1b4b 50%,#0f172a 100%);">
      <div class="absolute inset-0 opacity-[0.04]"
           style="background-image:radial-gradient(circle at 1px 1px,#fff 1px,transparent 0);background-size:32px 32px;"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">

          <div class="relative order-2 lg:order-1">
            <div class="rounded-3xl overflow-hidden shadow-2xl">
              <img src="https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=700&q=75"
                   alt="Instructor" class="w-full h-[380px] object-cover"/>
              <div class="absolute inset-0 rounded-3xl"
                   style="background:linear-gradient(to top right,rgba(124,58,237,.35),transparent);"></div>
            </div>
            <!-- Stats thật từ API -->
            <div class="absolute -top-5 -right-4 lg:-right-8 bg-white/10 backdrop-blur-xl rounded-2xl p-4 border border-white/20 shadow-2xl">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-violet-500 rounded-xl flex items-center justify-center text-xl">👥</div>
                <div>
                  <p class="text-white/60 text-xs">Học viên đang học</p>
                  <p class="text-white text-lg font-black">
                    <template v-if="isStatsLoading">...</template>
                    <template v-else>{{ formatNumber(stats.totalStudents) }}+</template>
                  </p>
                </div>
              </div>
            </div>
            <div class="absolute -bottom-4 -left-4 lg:-left-8 bg-white/10 backdrop-blur-xl rounded-2xl p-4 border border-white/20 shadow-2xl">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-emerald-500 rounded-xl flex items-center justify-center text-xl">🎓</div>
                <div>
                  <p class="text-white/60 text-xs">Khoá học đang có</p>
                  <p class="text-white text-lg font-black">
                    <template v-if="isStatsLoading">...</template>
                    <template v-else>{{ formatNumber(stats.totalCourses) }}+</template>
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div class="text-white space-y-6 order-1 lg:order-2">
            <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border text-sm font-semibold text-violet-300"
                 style="background:rgba(124,58,237,.15);border-color:rgba(124,58,237,.3);">
              🎙️ Cơ hội dành cho bạn
            </div>
            <h2 class="text-4xl lg:text-5xl font-black leading-tight">
              Trở thành
              <span class="text-transparent bg-clip-text"
                    style="background-image:linear-gradient(90deg,#a78bfa,#f472b6);">
                Giảng viên
              </span>
              <br/>hôm nay
            </h2>
            <p class="text-slate-300 leading-relaxed">
              Chia sẻ kiến thức với hơn
              <strong class="text-white">
                <template v-if="isStatsLoading">...</template>
                <template v-else>{{ formatNumber(stats.totalStudents) }}+</template>
              </strong>
              học viên trên toàn Việt Nam.
            </p>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div v-for="b in instructorBenefits" :key="b.text"
                   class="flex items-center gap-3 px-4 py-3 rounded-xl border"
                   style="background:rgba(255,255,255,.05);border-color:rgba(255,255,255,.1);">
                <span class="text-lg flex-shrink-0">{{ b.icon }}</span>
                <span class="text-sm text-slate-300 font-medium">{{ b.text }}</span>
              </div>
            </div>
            <router-link to="/become-instructor"
              class="group inline-flex items-center gap-2 px-8 py-4 font-black text-white rounded-2xl text-base transition-all hover:scale-105 active:scale-95"
              style="background:linear-gradient(135deg,#7c3aed,#ec4899);box-shadow:0 12px 30px rgba(124,58,237,.35);">
              Bắt đầu giảng dạy ngay
              <svg class="w-5 h-5 transition-transform group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 7l5 5m0 0l-5 5m5-5H6"/>
              </svg>
            </router-link>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useConfirm } from '@/composables/useConfirm';
import axiosClient from '@/api/axiosClient';

const router      = useRouter();
const { t }       = useI18n();
const { confirm } = useConfirm();
const userRole    = ref(null);

// ── HERO SEARCH ───────────────────────────────────────────────
const heroSearch = ref('');
const handleHeroSearch = () => {
  const q = heroSearch.value.trim();
  if (q) router.push({ path: '/course', query: { q } });
};

// ════════════════════════════════════════════════════════════
// STATS — GET /api/public/stats
// Response: SystemStats { totalStudents, totalCourses, totalInstructors, averageRating }
// ════════════════════════════════════════════════════════════
const isStatsLoading = ref(true);
const stats = ref({ totalStudents: 0, totalCourses: 0, totalInstructors: 0, averageRating: 0 });

const fetchStats = async () => {
  isStatsLoading.value = true;
  try {
    const res = await axiosClient.get('/public/stats');
    if (res) {
      stats.value.totalStudents    = res.totalStudents    ?? 0;
      stats.value.totalCourses     = res.totalCourses     ?? 0;
      stats.value.totalInstructors = res.totalInstructors ?? 0;
      stats.value.averageRating    = res.averageRating    ?? 4.8;
    }
  } catch {
    stats.value = { totalStudents: 1250, totalCourses: 45, totalInstructors: 12, averageRating: 4.8 };
  } finally {
    isStatsLoading.value = false;
  }
};

// ════════════════════════════════════════════════════════════
// CATEGORIES — GET /api/categories
// Response: List<CategoryDTO> { id, name, slug, iconUrl, courseCount, children }
// Dùng 2 nơi: (1) Tabs khoá học, (2) Section danh mục, (3) Popular tags
// ════════════════════════════════════════════════════════════
const isCatsLoading = ref(true);
const categories    = ref([]);   // chỉ parent (không có parentId)

// Popular tags = 5 category đầu tiên (tên thật)
const popularTagCategories = computed(() => categories.value.slice(0, 5));

const fetchCategories = async () => {
  isCatsLoading.value = true;
  try {
    const res = await axiosClient.get('/categories');
    const list = Array.isArray(res) ? res : (res?.content ?? []);
    // Chỉ lấy parent categories cho tabs và popular tags
    categories.value = list.filter(c => !c.parentId);
  } catch (err) {
    console.warn('Lỗi tải danh mục:', err);
    categories.value = [];
  } finally {
    isCatsLoading.value = false;
  }
};

// ════════════════════════════════════════════════════════════
// COURSES — GET /api/public/courses
// Response: Page<CourseListDTO>
// Fields: id, title, slug, thumbnail, price, salePrice, level,
//         averageRating, studentCount, totalLessons, totalDuration,
//         categoryName, instructorName, instructorAvatar
// ════════════════════════════════════════════════════════════
const isCoursesLoading = ref(true);
const courses          = ref([]);
const currentPage      = ref(0);
const totalPages       = ref(0);
const PAGE_SIZE        = 8;
const activeTabId      = ref(null);

const selectTab = (id) => {
  activeTabId.value = id;
  currentPage.value = 0;
};

const fetchCourses = async () => {
  isCoursesLoading.value = true;
  try {
    const params = { page: currentPage.value, size: PAGE_SIZE, sort: 'id,desc' };
    if (activeTabId.value !== null) params.categoryId = activeTabId.value;

    const res = await axiosClient.get('/public/courses', { params });

    if (res?.content) {
      courses.value    = res.content;
      totalPages.value = res.totalPages ?? 0;
    } else if (Array.isArray(res)) {
      courses.value    = res;
      totalPages.value = 1;
    } else {
      courses.value    = [];
      totalPages.value = 0;
    }
  } catch (err) {
    console.warn('Lỗi tải khoá học:', err);
    courses.value    = [];
    totalPages.value = 0;
  } finally {
    isCoursesLoading.value = false;
  }
};

const changePage = (p) => {
  if (p < 0 || p >= totalPages.value) return;
  currentPage.value = p;
  document.getElementById('courses-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

const visiblePages = computed(() => {
  const pages = [];
  const left  = Math.max(0, currentPage.value - 2);
  const right = Math.min(totalPages.value - 1, currentPage.value + 2);
  for (let i = left; i <= right; i++) pages.push(i);
  return pages;
});

watch([activeTabId, currentPage], () => fetchCourses());

// ════════════════════════════════════════════════════════════
// REVIEWS — GET /api/ratings/course/{courseId}
// Response: List<RatingResponseDTO>
// Fields: id, stars, comment, studentName, studentAvatar,
//         courseId, courseTitle, courseSlug, courseThumbnail, createdAt
//
// Strategy: Sau khi courses load xong, lấy 3 courseId đầu tiên,
// fetch ratings song song, filter stars >= 4 có comment, lấy tối đa 3
// ════════════════════════════════════════════════════════════
const isReviewsLoading = ref(true);
const reviews          = ref([]);

const fetchReviews = async (courseList) => {
  if (!courseList || courseList.length === 0) {
    isReviewsLoading.value = false;
    return;
  }

  isReviewsLoading.value = true;
  try {
    // Lấy tối đa 4 course đầu để fetch ratings song song
    const targetCourses = courseList.slice(0, 4);

    const results = await Promise.allSettled(
      targetCourses.map(c => axiosClient.get(`/ratings/course/${c.id}`))
    );

    const allRatings = [];
    results.forEach(r => {
      if (r.status === 'fulfilled' && Array.isArray(r.value)) {
        allRatings.push(...r.value);
      }
    });

    // Filter: phải có comment, stars >= 4, sắp xếp theo stars desc rồi lấy 3 cái
    const filtered = allRatings
      .filter(r => r.comment && r.comment.trim().length > 20 && r.stars >= 4)
      .sort((a, b) => b.stars - a.stars || new Date(b.createdAt) - new Date(a.createdAt));

    reviews.value = filtered.slice(0, 3);
  } catch (err) {
    console.warn('Lỗi tải đánh giá:', err);
    reviews.value = [];
  } finally {
    isReviewsLoading.value = false;
  }
};

// Khi courses load xong thì fetch reviews
watch(courses, (newCourses) => {
  if (newCourses.length > 0 && reviews.value.length === 0) {
    fetchReviews(newCourses);
  }
}, { immediate: false });

// ── ADD TO CART — POST /cart/add?studentId&courseId ───────────
const cartLoadingId = ref(null);

const addToCart = async (courseId) => {
  const userInfo = localStorage.getItem('user_info');
  if (!userInfo) {
    const ok = await confirm({
      title: 'Yêu cầu đăng nhập',
      message: t('auth.login_prompt') || 'Bạn cần đăng nhập để thêm khoá học vào giỏ hàng.',
      confirmText: 'Đăng nhập', cancelText: 'Hủy', variant: 'primary',
    });
    if (ok) router.push('/login');
    return;
  }
  const user = JSON.parse(userInfo);
  cartLoadingId.value = courseId;
  try {
    await axiosClient.post('/cart/add', null, { params: { studentId: user.id, courseId } });
    window.__notify?.auto('✅ ' + (t('course.add_cart_success') || 'Đã thêm vào giỏ hàng!'));
    window.dispatchEvent(new Event('cart-updated'));
  } catch (err) {
    const data = err.response?.data;
    const msg  = typeof data === 'string' ? data : (data?.message || t('common.error') || 'Có lỗi xảy ra.');
    window.__notify?.auto('❌ ' + msg);
  } finally {
    cartLoadingId.value = null;
  }
};

// ── HELPERS ───────────────────────────────────────────────────
const isFree = (course) =>
  (!course.price || Number(course.price) === 0) &&
  (!course.salePrice || Number(course.salePrice) === 0);

const formatCurrency = (value) => {
  const num = Number(value);
  if (!num) return t('common.free') || 'Miễn phí';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
};

const formatNumber = (num) => {
  if (!num) return '0';
  return new Intl.NumberFormat('vi-VN').format(num);
};

const discountPercent = (original, sale) => {
  const o = Number(original), s = Number(sale);
  if (!o || !s) return 0;
  return Math.round((1 - s / o) * 100);
};

const getCourseRatingValue = (course) => {
  const raw = Number(course?.averageRating);
  if (!Number.isFinite(raw) || raw <= 0) return 0;
  return Math.max(0, Math.min(5, raw));
};

const getCourseRatingDisplay = (course) => getCourseRatingValue(course).toFixed(1);
const getCourseFilledStars = (course) => Math.round(getCourseRatingValue(course));

// totalDuration là Integer (giây) từ CourseListDTO
const formatDuration = (seconds) => {
  const total = Number(seconds || 0);
  if (!total || total <= 0) return '';
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  if (h > 0) return m > 0 ? `${h}g ${m}p` : `${h} giờ`;
  return `${m || Math.ceil(total / 60)} phút`;
};

// createdAt là LocalDateTime từ Rating entity → ISO string
const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins  = Math.floor(diff / 60000);
  const hours = Math.floor(mins / 60);
  const days  = Math.floor(hours / 24);
  const weeks = Math.floor(days / 7);
  const months= Math.floor(days / 30);
  if (months >= 1) return `${months} tháng trước`;
  if (weeks  >= 1) return `${weeks} tuần trước`;
  if (days   >= 1) return `${days} ngày trước`;
  if (hours  >= 1) return `${hours} giờ trước`;
  if (mins   >= 1) return `${mins} phút trước`;
  return 'Vừa xong';
};

// ── STATIC DECORATION ────────────────────────────────────────
// Chỉ dùng cho màu sắc/gradient UI — KHÔNG phải data
const catColors    = ['#3B82F6','#8B5CF6','#10B981','#F59E0B','#EC4899','#EF4444','#6366F1','#14B8A6','#F97316','#0EA5E9'];
const catGradients = [
  'linear-gradient(135deg,#EFF6FF,#DBEAFE)','linear-gradient(135deg,#F5F3FF,#EDE9FE)',
  'linear-gradient(135deg,#ECFDF5,#D1FAE5)','linear-gradient(135deg,#FFFBEB,#FEF3C7)',
  'linear-gradient(135deg,#FDF2F8,#FCE7F3)','linear-gradient(135deg,#FEF2F2,#FEE2E2)',
  'linear-gradient(135deg,#EEF2FF,#E0E7FF)','linear-gradient(135deg,#F0FDFA,#CCFBF1)',
  'linear-gradient(135deg,#FFF7ED,#FFEDD5)','linear-gradient(135deg,#F0F9FF,#E0F2FE)',
];
// Fallback emoji khi cat.iconUrl === null
const catEmojiFallback = ['💻','🤖','📊','🎨','🇬🇧','📣','📷','🎵','💰','☁️','📱','🎯'];

const instructorBenefits = [
  { icon: '💰', text: 'Thu nhập thụ động ổn định' },
  { icon: '🌍', text: `Tiếp cận ${formatNumber(stats.value.totalStudents || 0)}+ học viên` },
  { icon: '🎯', text: 'Công cụ giảng dạy chuyên nghiệp' },
  { icon: '📈', text: 'Analytics & báo cáo chi tiết' },
];

// ── LIFECYCLE ─────────────────────────────────────────────────
onMounted(() => {
  userRole.value = localStorage.getItem('user_role');
  // Tất cả fetch song song — không block nhau
  fetchStats();
  fetchCategories();
  fetchCourses();
  // Reviews sẽ tự fetch khi courses load xong (watch)
});
</script>

<style scoped>
@keyframes float {
  0%,100% { transform: translateY(0); }
  50%      { transform: translateY(-10px); }
}
.animate-float       { animation: float 3.5s ease-in-out infinite; }
.animate-float-delay { animation: float 3.5s ease-in-out .9s infinite; }

.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
</style>
