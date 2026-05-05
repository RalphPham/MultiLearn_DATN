<template>
  <div class="min-h-screen bg-gray-50" style="font-family:'DM Sans','Segoe UI',system-ui,sans-serif;">

    <div class="relative h-52 bg-cover bg-center overflow-hidden"
         style="background-image:url('https://images.unsplash.com/photo-1501504905252-473c47e087f8?w=1920&q=60')">
      <div class="absolute inset-0 bg-slate-900/65 backdrop-blur-[1px]"></div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-full flex items-center justify-between gap-4">
        <div>
          <p class="text-blue-300 text-xs font-black uppercase tracking-widest mb-1.5">
            {{ keyword ? 'Kết quả tìm kiếm' : 'Danh sách' }}
          </p>
          <h1 class="text-3xl lg:text-4xl font-black text-white leading-tight">
            {{ keyword ? `"${keyword}"` : activeCategoryName || $t?.('nav.courses') || 'Tất cả khoá học' }}
          </h1>
          <p v-if="!isLoading" class="text-slate-300 text-sm mt-1.5 font-medium">
            {{ totalElements.toLocaleString('vi-VN') }} kết quả
          </p>
        </div>
        <nav class="hidden sm:flex items-center gap-2 text-sm text-gray-300 flex-shrink-0">
          <router-link to="/" class="hover:text-white transition-colors">Trang chủ</router-link>
          <span class="text-gray-500">/</span>
          <span class="text-white font-semibold">{{ $t?.('nav.courses') || 'Khoá học' }}</span>
        </nav>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex flex-col lg:flex-row gap-7">

        <aside class="lg:w-72 flex-shrink-0">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 sticky top-24">

            <div class="flex justify-between items-center mb-6">
              <h2 class="text-base font-black text-gray-900 flex items-center gap-2">
                <svg class="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
                </svg>
                {{ $t?.('filter.title') || 'Bộ Lọc' }}
              </h2>
              <button @click="resetFilters"
                class="text-xs font-bold text-blue-600 hover:text-blue-800 transition-colors px-2 py-1 rounded-lg hover:bg-blue-50">
                Đặt lại
              </button>
            </div>

            <div class="mb-6">
              <h3 class="text-sm font-black text-gray-700 uppercase tracking-widest mb-3">
                {{ $t?.('filter.category') || 'Danh Mục' }}
              </h3>

              <div v-if="isCatsLoading" class="space-y-2.5 animate-pulse">
                <div v-for="i in 5" :key="i" class="flex items-center gap-2">
                  <div class="w-4 h-4 bg-gray-200 rounded-full flex-shrink-0"></div>
                  <div class="h-3.5 bg-gray-200 rounded w-3/4"></div>
                </div>
              </div>

              <div v-else class="space-y-0.5">
                <label class="flex items-center gap-2.5 px-2 py-2 rounded-xl cursor-pointer group transition-colors hover:bg-gray-50"
                       :class="!filters.categoryId ? 'bg-blue-50' : ''">
                  <input type="radio" :value="null" v-model="filters.categoryId"
                    @change="updateFilters"
                    class="w-4 h-4 text-blue-600 cursor-pointer flex-shrink-0"/>
                  <span class="text-sm font-semibold"
                    :class="!filters.categoryId ? 'text-blue-700' : 'text-gray-700 group-hover:text-blue-600'">
                    Tất cả danh mục
                  </span>
                </label>

                <div v-for="cat in categoriesTree" :key="cat.id">
                  <div class="flex items-center gap-1">
                    <label class="flex items-center gap-2.5 px-2 py-2 rounded-xl cursor-pointer group transition-colors hover:bg-gray-50 flex-1"
                           :class="String(filters.categoryId) === String(cat.id) ? 'bg-blue-50' : ''">
                      <input type="radio" :value="cat.id" v-model="filters.categoryId"
                        @change="updateFilters"
                        class="w-4 h-4 text-blue-600 cursor-pointer flex-shrink-0"/>
                      <div class="flex items-center gap-1.5 min-w-0">
                        <img v-if="cat.iconUrl" :src="cat.iconUrl" :alt="cat.name"
                          class="w-4 h-4 object-contain rounded-sm flex-shrink-0"/>
                        <span class="text-sm font-semibold truncate"
                          :class="String(filters.categoryId) === String(cat.id) ? 'text-blue-700' : 'text-gray-700 group-hover:text-blue-600'">
                          {{ cat.name }}
                        </span>
                      </div>
                    </label>
                    <button v-if="cat.children && cat.children.length > 0"
                      @click="toggleCatExpand(cat.id)"
                      class="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-all flex-shrink-0">
                      <svg class="w-3.5 h-3.5 transition-transform duration-200"
                        :class="expandedCats.includes(cat.id) ? 'rotate-180' : ''"
                        fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7"/>
                      </svg>
                    </button>
                  </div>

                  <Transition
                    enter-active-class="transition-all duration-200 ease-out overflow-hidden"
                    enter-from-class="max-h-0 opacity-0"
                    enter-to-class="max-h-96 opacity-100"
                    leave-active-class="transition-all duration-150 ease-in overflow-hidden"
                    leave-from-class="max-h-96 opacity-100"
                    leave-to-class="max-h-0 opacity-0"
                  >
                    <div v-if="cat.children && cat.children.length > 0 && expandedCats.includes(cat.id)"
                         class="ml-4 pl-3 border-l-2 border-blue-100 space-y-0.5 mt-0.5 mb-1">
                      <label v-for="child in cat.children" :key="child.id"
                        class="flex items-center gap-2 px-2 py-1.5 rounded-lg cursor-pointer group transition-colors hover:bg-gray-50"
                        :class="String(filters.categoryId) === String(child.id) ? 'bg-blue-50' : ''">
                        <input type="radio" :value="child.id" v-model="filters.categoryId"
                          @change="updateFilters"
                          class="w-3.5 h-3.5 text-blue-600 cursor-pointer flex-shrink-0"/>
                        <span class="text-xs font-medium truncate"
                          :class="String(filters.categoryId) === String(child.id) ? 'text-blue-700 font-semibold' : 'text-gray-600 group-hover:text-blue-600'">
                          {{ child.name }}
                        </span>
                        <span v-if="child.courseCount"
                          class="ml-auto text-[10px] text-gray-400 flex-shrink-0">
                          {{ child.courseCount }}
                        </span>
                      </label>
                    </div>
                  </Transition>
                </div>
              </div>
            </div>

            <div class="border-t border-gray-100 my-5"></div>

            <div class="mb-5">
              <h3 class="text-sm font-black text-gray-700 uppercase tracking-widest mb-3">
                {{ $t?.('filter.price_range') || 'Khoảng Giá' }}
              </h3>
              <label class="flex items-center gap-2.5 px-2 py-2 rounded-xl cursor-pointer group hover:bg-gray-50 transition-colors mb-2">
                <input type="checkbox" v-model="filters.freeOnly" @change="updateFilters"
                  class="w-4 h-4 text-emerald-600 cursor-pointer rounded"/>
                <span class="text-sm font-semibold text-emerald-700">🆓 Miễn phí</span>
              </label>
              <div v-if="!filters.freeOnly" class="flex flex-col gap-2 mt-2">
                <input type="number" v-model="filters.minPrice"
                  :placeholder="$t?.('filter.min') || 'Thấp Nhất'"
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm focus:border-blue-400 focus:ring-2 focus:ring-blue-100 outline-none transition-all"/>
                <input type="number" v-model="filters.maxPrice"
                  :placeholder="$t?.('filter.max') || 'Cao Nhất'"
                  class="w-full px-3 py-2 border-2 border-gray-200 rounded-xl text-sm focus:border-blue-400 focus:ring-2 focus:ring-blue-100 outline-none transition-all"/>
                <button @click="updateFilters"
                  class="w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-bold rounded-xl transition-colors active:scale-95">
                  {{ $t?.('filter.apply') || 'Áp Dụng' }}
                </button>
              </div>
            </div>

            <div class="border-t border-gray-100 my-5"></div>

            <div>
              <h3 class="text-sm font-black text-gray-700 uppercase tracking-widest mb-3">Trình độ</h3>
              <div class="space-y-0.5">
                <label v-for="lv in levels" :key="lv.value"
                  class="flex items-center gap-2.5 px-2 py-2 rounded-xl cursor-pointer group hover:bg-gray-50 transition-colors"
                  :class="filters.level === lv.value ? 'bg-blue-50' : ''">
                  <input type="radio" :value="lv.value" v-model="filters.level"
                    @change="updateFilters"
                    class="w-4 h-4 text-blue-600 cursor-pointer flex-shrink-0"/>
                  <span class="text-sm font-semibold"
                    :class="filters.level === lv.value ? 'text-blue-700' : 'text-gray-700 group-hover:text-blue-600'">
                    {{ lv.label }}
                  </span>
                </label>
              </div>
            </div>

          </div>
        </aside>

        <div class="flex-1 min-w-0">

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 px-5 py-3.5 mb-6 flex flex-col sm:flex-row justify-between items-center gap-3">
            <div class="flex items-center gap-3 flex-wrap">
              <p class="text-sm text-gray-600 font-medium">
                <template v-if="isLoading">
                  <span class="inline-block h-4 w-24 bg-gray-200 rounded animate-pulse"></span>
                </template>
                <template v-else>
                  <span class="font-black text-gray-900">{{ totalElements.toLocaleString('vi-VN') }}</span>
                  kết quả{{ activeCategoryName ? ` trong "${activeCategoryName}"` : '' }}
                </template>
              </p>

              <div class="flex flex-wrap gap-1.5">
                <span v-if="keyword"
                  class="inline-flex items-center gap-1.5 px-3 py-1 bg-blue-100 text-blue-700 text-xs font-bold rounded-full">
                  🔍 {{ keyword }}
                  <button @click="clearKeyword" class="hover:text-blue-900 transition-colors">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
                  </button>
                </span>
                <span v-if="filters.freeOnly"
                  class="inline-flex items-center gap-1.5 px-3 py-1 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-full">
                  🆓 Miễn phí
                  <button @click="filters.freeOnly = false; updateFilters()" class="hover:text-emerald-900 transition-colors">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
                  </button>
                </span>
                <span v-if="filters.level"
                  class="inline-flex items-center gap-1.5 px-3 py-1 bg-violet-100 text-violet-700 text-xs font-bold rounded-full">
                  {{ levels.find(l => l.value === filters.level)?.label }}
                  <button @click="filters.level = null; updateFilters()" class="hover:text-violet-900 transition-colors">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
                  </button>
                </span>
              </div>
            </div>

            <div class="flex items-center gap-2 flex-shrink-0">
              <label class="text-sm text-gray-500 font-medium whitespace-nowrap">Sắp xếp:</label>
              <select v-model="filters.sort" @change="updateFilters"
                class="border-2 border-gray-200 rounded-xl text-sm px-3 py-2 focus:outline-none focus:border-blue-400 bg-white font-semibold text-gray-700 cursor-pointer">
                <option value="id,desc">Mới nhất</option>
                <option value="studentCount,desc">Phổ biến nhất</option>
                <option value="averageRating,desc">Đánh giá cao nhất</option>
                <option value="price,asc">Giá tăng dần</option>
                <option value="price,desc">Giá giảm dần</option>
              </select>
            </div>
          </div>

          <div v-if="isLoading" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
            <div v-for="n in 9" :key="n" class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-pulse">
              <div class="aspect-video bg-gray-200"></div>
              <div class="p-4 space-y-3">
                <div class="flex gap-2">
                  <div class="h-5 bg-gray-200 rounded-full w-20"></div>
                </div>
                <div class="h-4 bg-gray-200 rounded w-full"></div>
                <div class="h-4 bg-gray-200 rounded w-4/5"></div>
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 bg-gray-200 rounded-full"></div>
                  <div class="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
                <div class="flex gap-1 items-center">
                  <div class="h-3.5 bg-gray-200 rounded w-6"></div>
                  <div class="flex gap-0.5">
                    <div v-for="i in 5" :key="i" class="w-3 h-3 bg-gray-200 rounded"></div>
                  </div>
                  <div class="h-3 bg-gray-200 rounded w-16"></div>
                </div>
                <div class="flex justify-between items-center pt-2 border-t border-gray-100">
                  <div class="h-6 bg-gray-200 rounded w-20"></div>
                  <div class="h-8 w-8 bg-gray-200 rounded-xl"></div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="courses.length === 0"
               class="text-center py-24 bg-white rounded-3xl border-2 border-dashed border-gray-200">
            <div class="w-16 h-16 bg-blue-100 rounded-2xl flex items-center justify-center text-3xl mx-auto mb-5">🔍</div>
            <h3 class="text-xl font-black text-gray-900 mb-2">Không tìm thấy khoá học nào</h3>
            <p class="text-gray-500 text-sm mb-6">Thử thay đổi từ khoá hoặc bộ lọc.</p>
            <button @click="resetFilters"
              class="px-7 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition-colors">
              Xoá bộ lọc
            </button>
          </div>

          <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
            <div v-for="course in courses" :key="course.id"
              class="group bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 flex flex-col cursor-pointer"
              @click="$router.push('/course/slug/' + course.slug)">

              <div class="relative overflow-hidden aspect-video flex-shrink-0">
                <img :src="course.thumbnail || 'https://placehold.co/640x360/e2e8f0/94a3b8?text=MultiLearn'"
                  :alt="course.title"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  loading="lazy"/>

                <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-all flex items-center justify-center">
                  <div class="w-11 h-11 rounded-full bg-white/90 shadow-xl flex items-center justify-center opacity-0 group-hover:opacity-100 scale-75 group-hover:scale-100 transition-all duration-300">
                    <svg class="w-4 h-4 text-blue-600 translate-x-0.5" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M6.3 2.841A1.5 1.5 0 004 4.11V15.89a1.5 1.5 0 002.3 1.269l9.344-5.89a1.5 1.5 0 000-2.538L6.3 2.84z"/>
                    </svg>
                  </div>
                </div>

                <div v-if="course.categoryName"
                  class="absolute top-2.5 left-2.5 px-2.5 py-1 bg-white/90 backdrop-blur-sm rounded-lg text-[11px] font-black text-blue-700 shadow-sm max-w-[150px] truncate">
                  {{ course.categoryName }}
                </div>

                <div v-if="isFree(course)"
                  class="absolute top-2.5 right-2.5 px-2.5 py-1 bg-emerald-500 rounded-lg text-[11px] font-black text-white">
                  FREE
                </div>
                <div v-else-if="course.salePrice && course.price && Number(course.salePrice) < Number(course.price)"
                  class="absolute top-2.5 right-2.5 px-2.5 py-1 bg-red-500 rounded-lg text-[11px] font-black text-white">
                  -{{ discountPct(course.price, course.salePrice) }}%
                </div>

                <button
                  v-if="currentUser?.role !== 'ADMIN'"
                  @click.stop="toggleWishlist(course)"
                  :disabled="wishlistLoadingId === course.id"
                  class="absolute bottom-2.5 right-2.5 p-2 rounded-xl transition-all shadow-md opacity-0 group-hover:opacity-100"
                  :class="isInWishlist(course.id)
                    ? 'bg-rose-500 text-white'
                    : 'bg-white/90 text-gray-400 hover:text-rose-500'"
                  :title="isInWishlist(course.id) ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                  </svg>
                </button>

                <div v-if="course.level"
                  class="absolute bottom-2.5 left-2.5 px-2 py-0.5 bg-black/60 backdrop-blur-sm rounded-md text-[10px] font-bold text-white">
                  {{ course.level }}
                </div>
              </div>

              <div class="p-4 flex flex-col flex-1 gap-2">

                <h3 class="font-bold text-gray-900 text-sm leading-snug line-clamp-2 group-hover:text-blue-600 transition-colors min-h-[2.5rem]">
                  {{ course.title }}
                </h3>

                <div class="flex items-center gap-2">
                  <img :src="course.instructorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(course.instructorName || 'GV')}&background=e0e7ff&color=4f46e5&bold=true&size=40`"
                    :alt="course.instructorName"
                    class="w-6 h-6 rounded-full object-cover ring-1 ring-gray-200 flex-shrink-0"/>
                  <span class="text-xs text-gray-500 font-semibold truncate">
                    {{ course.instructorName || 'MultiLearn Teacher' }}
                  </span>
                </div>

                <div class="flex items-center gap-2 flex-wrap">
                  <span class="text-sm font-black text-amber-600">
                    {{ course.averageRating ? Number(course.averageRating).toFixed(1) : '0.0' }}
                  </span>
                  <div class="flex gap-0.5">
                    <svg v-for="i in 5" :key="i" class="w-3 h-3 fill-current"
                      :class="i <= Math.round(Number(course.averageRating) || 0) ? 'text-amber-400' : 'text-gray-200'"
                      viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                  </div>
                  <span class="text-xs text-gray-400">
                    {{ Number(course.studentCount || 0).toLocaleString('vi-VN') }} học viên
                  </span>
                </div>

                <div v-if="course.totalLessons || course.totalDuration"
                     class="flex items-center gap-3 text-xs text-gray-400">
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

                <div class="flex items-center justify-between pt-3 mt-auto border-t border-gray-100">
                  <div class="flex flex-col leading-tight">
                    <span class="text-lg font-black"
                      :class="isFree(course) ? 'text-emerald-600' : 'text-gray-900'">
                      {{ formatPrice(course.salePrice || course.price) }}
                    </span>
                    <span v-if="course.salePrice && course.price && Number(course.salePrice) < Number(course.price)"
                      class="text-xs text-gray-400 line-through">
                      {{ formatPrice(course.price) }}
                    </span>
                  </div>

                  <div class="flex items-center gap-1.5">
                    <button
                      v-if="currentUser?.role !== 'ADMIN'"
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
          </div>

          <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-10 pb-6">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
              class="px-4 py-2.5 border-2 border-gray-200 rounded-xl text-sm font-bold text-gray-600 hover:border-blue-400 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all flex items-center gap-1.5">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7"/></svg>
              {{ $t?.('filter.prev') || 'Trước' }}
            </button>

            <div class="flex gap-1.5">
              <button v-for="p in visiblePages" :key="p" @click="changePage(p - 1)"
                class="w-10 h-10 rounded-xl border-2 text-sm font-bold transition-all"
                :class="currentPage === (p - 1)
                  ? 'bg-blue-600 border-blue-600 text-white shadow-md'
                  : 'border-gray-200 text-gray-600 hover:border-blue-400 hover:text-blue-600 bg-white'">
                {{ p }}
              </button>
            </div>

            <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages - 1"
              class="px-4 py-2.5 border-2 border-gray-200 rounded-xl text-sm font-bold text-gray-600 hover:border-blue-400 hover:text-blue-600 disabled:opacity-30 disabled:cursor-not-allowed transition-all flex items-center gap-1.5">
              {{ $t?.('filter.next') || 'Tiếp' }}
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7"/></svg>
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useConfirm } from '@/composables/useConfirm';
import axiosClient from '@/api/axiosClient';
import WishlistService from '@/services/wishlist.service';

const route  = useRoute();
const router = useRouter();
const { t }  = useI18n();
const { confirm } = useConfirm();

// ── USER ─────────────────────────────────────────────────────
const currentUser = ref(null);

// ── LOADING ──────────────────────────────────────────────────
const isLoading      = ref(false);
const isCatsLoading  = ref(true);
const cartLoadingId  = ref(null);
const wishlistLoadingId = ref(null);

// ── DATA ─────────────────────────────────────────────────────
const courses       = ref([]);
const totalPages    = ref(0);
const totalElements = ref(0);
const currentPage   = ref(parseInt(route.query.page) || 0);
const keyword       = ref(route.query.q || '');

// ── CATEGORIES ──
const categoriesTree = ref([]);
const expandedCats   = ref([]);

const activeCategoryName = computed(() => {
  if (!filters.categoryId) return '';
  for (const cat of categoriesTree.value) {
    if (String(cat.id) === String(filters.categoryId)) return cat.name;
    if (cat.children) {
      const child = cat.children.find(c => String(c.id) === String(filters.categoryId));
      if (child) return child.name;
    }
  }
  return '';
});

const toggleCatExpand = (id) => {
  const idx = expandedCats.value.indexOf(id);
  if (idx >= 0) expandedCats.value.splice(idx, 1);
  else expandedCats.value.push(id);
};

// ── FILTERS ───────────────────────────────────────────────────
const filters = reactive({
  categoryId : route.query.categoryId ? Number(route.query.categoryId) : null,
  minPrice   : route.query.minPrice   || null,
  maxPrice   : route.query.maxPrice   || null,
  sort       : route.query.sort       || 'id,desc',
  level      : route.query.level      || null,
  freeOnly   : route.query.free === 'true',
  size       : 9,
});

// ── WISHLIST ──────────────────────────────────────────────────
const wishlistIds = ref([]);
const isInWishlist = (id) => wishlistIds.value.includes(id);

// ── FETCH CATEGORIES ──────────────────────────────────────────
const fetchCategories = async () => {
  isCatsLoading.value = true;
  try {
    const res = await axiosClient.get('/categories');
    const list = Array.isArray(res) ? res : (res?.content ?? []);
    categoriesTree.value = list.filter(c => !c.parentId);

    if (filters.categoryId) {
      for (const cat of categoriesTree.value) {
        if (cat.children?.some(c => String(c.id) === String(filters.categoryId))) {
          if (!expandedCats.value.includes(cat.id)) expandedCats.value.push(cat.id);
        }
      }
    }
  } catch (err) {
    console.warn('Lỗi tải danh mục:', err);
    categoriesTree.value = [];
  } finally {
    isCatsLoading.value = false;
  }
};

// ── FETCH COURSES ─────────────────────────────────────────────
const fetchCourses = async () => {
  isLoading.value = true;
  try {
    const q = route.query;
    const rawParams = {
      page       : currentPage.value,
      size       : filters.size,
      sort       : q.sort       || filters.sort || 'id,desc',
      q          : q.q          || '',
      categoryId : q.categoryId || '',
      level      : q.level      || '',
      free       : q.free       || '',
      minPrice   : q.free === 'true' ? '' : (q.minPrice || ''),
      maxPrice   : q.free === 'true' ? '' : (q.maxPrice || ''),
    };
    // Bỏ các param rỗng để URL gọi API gọn
    const params = Object.fromEntries(
      Object.entries(rawParams).filter(([, v]) => v !== '' && v !== null && v !== undefined)
    );

    const endpoint = currentUser.value ? '/courses' : '/public/courses';
    const res = await axiosClient.get(endpoint, { params });

    if (res?.content) {
      courses.value       = res.content;
      totalPages.value    = res.totalPages  ?? 0;
      totalElements.value = res.totalElements ?? 0;
    } else if (Array.isArray(res)) {
      courses.value       = res;
      totalPages.value    = 1;
      totalElements.value = res.length;
    } else {
      courses.value       = [];
      totalPages.value    = 0;
      totalElements.value = 0;
    }
  } catch (err) {
    console.warn('Lỗi tải khoá học:', err);
    courses.value       = [];
    totalPages.value    = 0;
    totalElements.value = 0;
  } finally {
    isLoading.value = false;
  }
};

const loadWishlist = async () => {
  if (!currentUser.value || currentUser.value.role === 'ADMIN') return;
  try {
    const res = await WishlistService.getWishlist(currentUser.value.id);
    if (Array.isArray(res)) {
      wishlistIds.value = res.map(item => item.course?.id || item.courseId || item.id);
    }
  } catch { /* silent */ }
};

const addToCart = async (courseId) => {
  if (!currentUser.value) {
    const ok = await confirm({
      title: 'Yêu cầu đăng nhập',
      message: t('auth.login_prompt') || 'Vui lòng đăng nhập!',
      confirmText: 'Đăng nhập', cancelText: 'Hủy', variant: 'primary',
    });
    if (ok) router.push('/login');
    return;
  }
  cartLoadingId.value = courseId;
  try {
    await axiosClient.post('/cart/add', null, {
      params: { studentId: currentUser.value.id, courseId },
    });
    window.__notify?.auto('✅ ' + (t('course.add_cart_success') || 'Đã thêm vào giỏ hàng!'));
    window.dispatchEvent(new Event('cart-updated'));
  } catch (err) {
    const data = err.response?.data;
    const msg  = typeof data === 'string' ? data : (data?.message || 'Có lỗi xảy ra.');
    window.__notify?.auto('❌ ' + msg);
  } finally {
    cartLoadingId.value = null;
  }
};

const toggleWishlist = async (course) => {
  if (!currentUser.value) {
    const ok = await confirm({
      title: 'Yêu cầu đăng nhập',
      message: t('auth.login_prompt') || 'Vui lòng đăng nhập!',
      confirmText: 'Đăng nhập', cancelText: 'Hủy', variant: 'primary',
    });
    if (ok) router.push('/login');
    return;
  }
  wishlistLoadingId.value = course.id;
  try {
    if (isInWishlist(course.id)) {
      await WishlistService.removeFromWishlist(currentUser.value.id, course.id);
      wishlistIds.value = wishlistIds.value.filter(id => id !== course.id);
    } else {
      await WishlistService.addToWishlist(currentUser.value.id, course.id);
      wishlistIds.value.push(course.id);
    }
  } catch {
    window.__notify?.auto('❌ Có lỗi xảy ra.');
  } finally {
    wishlistLoadingId.value = null;
  }
};

const updateFilters = () => {
  currentPage.value = 0;
  syncUrl();
};

const resetFilters = () => {
  filters.categoryId = null;
  filters.minPrice   = null;
  filters.maxPrice   = null;
  filters.level      = null;
  filters.freeOnly   = false;
  filters.sort       = 'id,desc';
  keyword.value      = '';
  currentPage.value  = 0;
  router.push({ path: '/course' });
};

const clearKeyword = () => {
  keyword.value = '';
  updateFilters();
};

const changePage = (p) => {
  if (p < 0 || p >= totalPages.value) return;
  currentPage.value = p;
  syncUrl();
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const syncUrl = () => {
  const query = {};
  if (currentPage.value > 0)  query.page       = currentPage.value;
  if (keyword.value)          query.q          = keyword.value;
  if (filters.categoryId)     query.categoryId = filters.categoryId;
  if (filters.level)          query.level      = filters.level;
  if (filters.freeOnly)       query.free       = 'true';
  if (filters.minPrice && !filters.freeOnly) query.minPrice = filters.minPrice;
  if (filters.maxPrice && !filters.freeOnly) query.maxPrice = filters.maxPrice;
  if (filters.sort !== 'id,desc') query.sort   = filters.sort;
  router.push({ path: '/course', query });
};

const visiblePages = computed(() => {
  const pages = new Set();
  const total = totalPages.value;
  const cur   = currentPage.value + 1;
  pages.add(1);
  pages.add(total);
  for (let i = Math.max(1, cur - 1); i <= Math.min(total, cur + 1); i++) pages.add(i);
  return [...pages].sort((a, b) => a - b);
});

const isFree = (course) =>
  (!course.price || Number(course.price) === 0) &&
  (!course.salePrice || Number(course.salePrice) === 0);

const formatPrice = (val) => {
  const num = Number(val);
  if (!num) return t('common.free') || 'Miễn phí';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
};

const formatDuration = (seconds) => {
  const total = Number(seconds || 0);
  if (!total || total <= 0) return '';
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  if (h > 0) return m > 0 ? `${h}g ${m}p` : `${h} giờ`;
  return `${m || Math.ceil(total / 60)} phút`;
};

const discountPct = (original, sale) => {
  const o = Number(original), s = Number(sale);
  if (!o || !s) return 0;
  return Math.round((1 - s / o) * 100);
};

const levels = [
  { value: null,          label: 'Tất cả trình độ' },
  { value: 'Cơ bản',     label: 'Cơ bản' },
  { value: 'Trung cấp',  label: 'Trung cấp' },
  { value: 'Nâng cao',   label: 'Nâng cao' },
];

watch(() => route.query, (q) => {
  currentPage.value  = parseInt(q.page)      || 0;
  keyword.value      = q.q                   || '';
  filters.categoryId = q.categoryId ? Number(q.categoryId) : null;
  filters.minPrice   = q.minPrice            || null;
  filters.maxPrice   = q.maxPrice            || null;
  filters.sort       = q.sort                || 'id,desc';
  filters.level      = q.level               || null;
  filters.freeOnly   = q.free === 'true';
  fetchCourses();
}, { deep: true });

onMounted(() => {
  const userStr = localStorage.getItem('user_info');
  const role    = localStorage.getItem('user_role');
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr);
      if (role) currentUser.value.role = role;
    } catch { /* silent */ }
  }

  fetchCategories();
  fetchCourses();

  if (currentUser.value && currentUser.value.role !== 'ADMIN') {
    loadWishlist();
  }
});
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
</style>
