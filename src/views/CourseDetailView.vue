<template>
  <div v-if="isLoading" class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="flex flex-col items-center gap-4">
      <div class="w-12 h-12 rounded-full border-4 border-blue-600 border-t-transparent animate-spin"></div>
      <p class="text-gray-500 font-medium">Đang tải khoá học...</p>
    </div>
  </div>

  <div v-else-if="course" class="min-h-screen bg-gray-50 font-sans">

    <div class="bg-gray-900 text-white relative overflow-hidden">
      <div class="absolute inset-0 opacity-[0.03]"
           style="background-image:radial-gradient(circle at 1px 1px,#fff 1px,transparent 0);background-size:32px 32px;"></div>
      <div class="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-blue-900/20 to-transparent pointer-events-none hidden lg:block"></div>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 relative z-10">
        <nav class="flex items-center gap-2 text-xs text-gray-400 mb-6 font-medium">
          <router-link to="/" class="hover:text-white transition-colors">Trang chủ</router-link>
          <span>/</span>
          <router-link to="/course" class="hover:text-white transition-colors">Khoá học</router-link>
          <span>/</span>
          <router-link v-if="course?.categoryName" :to="'/course?categoryId=' + course?.categoryId"
            class="hover:text-white transition-colors">{{ course?.categoryName }}</router-link>
          <span v-if="course?.categoryName">/</span>
          <span class="text-white truncate max-w-xs">{{ course?.title }}</span>
        </nav>

        <div class="max-w-3xl">
          <span v-if="course?.categoryName"
            class="inline-block px-3 py-1 bg-blue-600/30 border border-blue-500/40 text-blue-300 text-xs font-bold uppercase tracking-widest rounded-full mb-4">
            {{ course?.categoryName }}
          </span>

          <h1 class="text-2xl sm:text-3xl lg:text-4xl font-black leading-tight mb-4 text-white">
            {{ course?.title }}
          </h1>

          <p class="text-gray-300 text-base leading-relaxed mb-6 max-w-2xl">
            {{ course?.shortDescription }}
          </p>

          <div class="flex flex-wrap items-center gap-5 text-sm mb-6">
            <div class="flex items-center gap-2 bg-amber-400/10 border border-amber-400/20 px-3 py-1.5 rounded-full">
              <span class="font-black text-amber-400 text-base">{{ Number(displayAverageRating).toFixed(1) }}</span>
              <div class="flex gap-0.5">
                <svg v-for="i in 5" :key="i" class="w-3.5 h-3.5"
                  :class="i <= Math.round(displayAverageRating) ? 'text-amber-400 fill-current' : 'text-gray-600 fill-current'"
                  viewBox="0 0 24 24">
                  <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                </svg>
              </div>
              <span class="text-gray-400 text-xs">({{ ratingCount }} đánh giá)</span>
            </div>

            <div class="flex items-center gap-1.5 text-gray-300">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              <span class="font-bold text-white">{{ Number(course?.studentCount || 0).toLocaleString('vi-VN') }}</span>
              <span>học viên</span>
            </div>

            <div class="flex items-center gap-1.5 text-gray-300">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <span>{{ course?.totalLessons || 0 }} bài · {{ formatDuration(course?.totalDuration) }}</span>
            </div>

            <div v-if="course?.level" class="flex items-center gap-1.5 text-gray-300">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
              </svg>
              <span>{{ course?.level }}</span>
            </div>

            <div v-if="course?.language" class="flex items-center gap-1.5 text-gray-300">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/>
              </svg>
              <span>{{ course?.language }}</span>
            </div>

            <div class="text-gray-400 text-xs">
              Cập nhật {{ formatDate(course?.updatedAt) }}
            </div>
          </div>

          <div class="flex items-center gap-3">
            <img
              :src="course?.authorAvatar || course?.instructorAvatar || avatarFallback"
              :class="[
                'w-10 h-10 rounded-full object-cover border-2 border-gray-600 transition-transform',
                publicInstructorId ? 'cursor-pointer hover:scale-105' : ''
              ]"
              @click="goToInstructorProfile"
            />
            <div>
              <p class="text-gray-400 text-xs">Giảng viên</p>
              <p
                :class="[
                  'font-bold text-sm transition-colors',
                  publicInstructorId ? 'text-blue-400 hover:text-blue-300 cursor-pointer' : 'text-gray-200'
                ]"
                @click="goToInstructorProfile"
              >
                {{ course?.authorName || course?.instructorName || 'MultiLearn Teacher' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex flex-col lg:flex-row gap-8">

        <div class="flex-1 min-w-0 order-2 lg:order-1 space-y-6">

          <div v-if="course?.isFlashSale && course?.totalSlots"
            class="bg-gradient-to-r from-red-600 to-orange-500 rounded-2xl p-5 text-white">
            <div class="flex items-center justify-between gap-4 mb-3">
              <div class="flex items-center gap-2">
                <span class="text-lg">⚡</span>
                <span class="font-black text-lg">Flash Sale đang diễn ra!</span>
              </div>
              <span class="text-sm font-bold bg-white/20 px-3 py-1 rounded-full">
                Còn {{ (course?.totalSlots || 0) - (course?.soldSlots || 0) }} suất
              </span>
            </div>
            <div class="mb-1.5 flex justify-between text-xs font-bold text-red-100">
              <span>{{ course?.soldSlots || 0 }} đã mua</span>
              <span>{{ course?.totalSlots || 0 }} suất tổng</span>
            </div>
            <div class="h-2.5 bg-white/20 rounded-full overflow-hidden">
              <div class="h-full bg-white rounded-full transition-all duration-500"
                :style="{ width: Math.min(100, ((course?.soldSlots||0) / (course?.totalSlots||1)) * 100) + '%' }">
              </div>
            </div>
          </div>

          <div v-if="course?.learningOutcomes" class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h2 class="text-xl font-black text-gray-900 mb-5 flex items-center gap-2">
              <span class="w-1 h-6 bg-blue-600 rounded-full inline-block"></span>
              Bạn sẽ học được gì
            </h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div v-for="(item, idx) in parsedLearningOutcomes" :key="idx" class="flex items-start gap-3">
                <svg class="w-5 h-5 text-emerald-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
                <span class="text-sm text-gray-700 leading-relaxed">{{ item }}</span>
              </div>
            </div>
          </div>


          <div class="bg-white rounded-2xl border border-gray-200 p-4 shadow-sm">
            <div class="flex flex-wrap gap-2">
              <button
                type="button"
                class="px-3 py-1.5 rounded-lg border border-orange-200 bg-orange-50 text-orange-700 text-xs font-semibold hover:bg-orange-100 transition"
                @click="scrollToSection('instructor-section')"
              >
                Giảng viên
              </button>
              <button
                type="button"
                class="px-3 py-1.5 rounded-lg border border-amber-200 bg-amber-50 text-amber-700 text-xs font-semibold hover:bg-amber-100 transition"
                @click="scrollToSection('reviews-section')"
              >
                Đánh giá khóa học
              </button>
            </div>
          </div>

          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div class="p-6 border-b border-gray-100">
              <h2 class="text-xl font-black text-gray-900 mb-1 flex items-center gap-2">
                <span class="w-1 h-6 bg-violet-600 rounded-full inline-block"></span>
                Nội dung khoá học
              </h2>
              <p class="text-sm text-gray-500">
                {{ course?.sections?.length || 0 }} chương · {{ course?.totalLessons || 0 }} bài học · {{ formatDuration(course?.totalDuration) }} tổng thời lượng
              </p>
              
              <div v-if="isOwned && totalCompletedLessons > 0" class="mt-4">
                <div class="flex justify-between text-xs font-semibold text-gray-600 mb-1.5">
                  <span>Tiến độ của bạn</span>
                  <span class="text-blue-600 font-black">{{ courseProgressPercent }}%</span>
                </div>
                <div class="h-2.5 bg-gray-200 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-blue-500 to-violet-500 rounded-full transition-all duration-700" :style="{ width: courseProgressPercent + '%' }"></div>
                </div>
                <p class="text-xs text-gray-400 mt-1">{{ totalCompletedLessons }}/{{ course?.totalLessons || 0 }} bài đã hoàn thành</p>
              </div>
            </div>

            <div class="divide-y divide-gray-100">
              <div v-for="(section, sIdx) in course?.sections || []" :key="section.id || sIdx">
                <button @click="toggleChapter(sIdx)" class="w-full flex items-center justify-between px-6 py-4 bg-gray-50 hover:bg-gray-100 transition-colors text-left">
                  <div class="flex items-center gap-3 min-w-0">
                    <svg class="w-4 h-4 text-gray-500 flex-shrink-0 transition-transform duration-200" :class="openChapters.includes(sIdx) ? 'rotate-180' : ''" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
                    <span class="font-bold text-gray-900 text-sm truncate">{{ section.title }}</span>
                    <span v-if="isOwned && sectionProgressCount(section) > 0" class="text-[10px] font-black text-violet-600 bg-violet-50 border border-violet-100 px-2 py-0.5 rounded-full flex-shrink-0">
                      {{ sectionProgressCount(section) }}/{{ section.lessons?.length || 0 }}
                    </span>
                  </div>
                  <span class="text-xs text-gray-500 flex-shrink-0 ml-4 font-medium">{{ section.lessonCount || section.lessons?.length || 0 }} bài · {{ formatDuration(section.totalDuration) }}</span>
                </button>
                <div v-show="openChapters.includes(sIdx)">
                  <div v-for="lesson in section.lessons || []" :key="lesson.id" class="relative flex items-center justify-between px-6 py-3 border-t border-gray-50 hover:bg-blue-50/30 transition-colors group" @mouseenter="hoveredLesson = lesson.id" @mouseleave="hoveredLesson = null">
                    <div class="flex items-center gap-3 min-w-0">
                      <div class="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0 transition-all" :class="lesson.isCompleted ? 'bg-emerald-100 text-emerald-600' : lesson.type === 'VIDEO' ? 'bg-blue-100 text-blue-600' : lesson.type === 'QUIZ' ? 'bg-amber-100 text-amber-600' : 'bg-gray-100 text-gray-500'">
                        <svg v-if="lesson.isCompleted" class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
                        <svg v-else-if="lesson.type === 'VIDEO'" class="w-3.5 h-3.5 fill-current" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                        <svg v-else-if="lesson.type === 'QUIZ'" class="w-3.5 h-3.5 fill-current" viewBox="0 0 24 24"><path d="M19 3H5c-1.1 0-2 .9-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5c0-1.1-.9-2-2-2zm-7 14h-2v-2h2v2zm1.07-7.75l-.9.92C11.45 10.9 11 11.5 11 13h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26a2 2 0 10-3.41-1.41H6a4 4 0 117.07 2.25z"/></svg>
                        <svg v-else class="w-3.5 h-3.5 fill-current" viewBox="0 0 24 24"><path d="M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/></svg>
                      </div>
                      <span class="text-sm truncate transition-colors" :class="lesson.isCompleted ? 'text-gray-400 line-through' : 'text-gray-700 group-hover:text-blue-600'">{{ lesson.title }}</span>
                    </div>
                    <div class="flex items-center gap-3 flex-shrink-0 ml-3">
                      <span v-if="lesson.isPreview" class="text-[10px] font-black text-blue-600 border border-blue-200 bg-blue-50 px-2 py-0.5 rounded-full cursor-pointer hover:bg-blue-100 transition-colors" @click.stop="playPreviewLesson(lesson)">▶ Xem thử</span>
                      <span class="text-xs text-gray-400 font-medium">{{ formatLessonMeta(lesson) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="course?.description" class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h2 class="text-xl font-black text-gray-900 mb-4 flex items-center gap-2"><span class="w-1 h-6 bg-emerald-500 rounded-full inline-block"></span>Mô tả khoá học</h2>
            <div class="prose prose-sm max-w-none text-gray-700 leading-relaxed" v-html="sanitizedCourseDescription"></div>
          </div>

          <div id="instructor-section" class="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h2 class="text-xl font-black text-gray-900 mb-5 flex items-center gap-2"><span class="w-1 h-6 bg-orange-500 rounded-full inline-block"></span>Giảng viên</h2>
            <div class="flex items-start gap-5">
              <img
                :src="course?.authorAvatar || course?.instructorAvatar || avatarFallback"
                :class="[
                  'w-20 h-20 rounded-2xl object-cover border-2 border-gray-100 shadow-sm flex-shrink-0 transition-transform',
                  publicInstructorId ? 'cursor-pointer hover:scale-105' : ''
                ]"
                @click="goToInstructorProfile"
              />
              <div class="flex-1 min-w-0">
                <h3
                  :class="[
                    'text-lg font-black mb-1 transition-colors',
                    publicInstructorId ? 'text-gray-900 cursor-pointer hover:text-blue-600' : 'text-gray-900'
                  ]"
                  @click="goToInstructorProfile"
                >
                  {{ course?.authorName || course?.instructorName || 'MultiLearn Teacher' }}
                </h3>
                <p class="text-sm text-blue-600 font-semibold mb-3">{{ course?.categoryName ? 'Chuyên gia ' + course.categoryName : 'Giảng viên MultiLearn' }}</p>
                <div class="flex flex-wrap gap-4 text-sm text-gray-600 mb-4">
                  <div class="flex items-center gap-1.5"><svg class="w-4 h-4 text-amber-500 fill-current" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg><span class="font-semibold">{{ Number(displayAverageRating).toFixed(1) }}</span><span>Đánh giá trung bình</span></div>
                  <div class="flex items-center gap-1.5"><svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg><span class="font-semibold">{{ Number(course?.studentCount || 0).toLocaleString('vi-VN') }}</span><span>Học viên trong khoá này</span></div>
                </div>
                <button
                  v-if="publicInstructorId"
                  type="button"
                  class="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-blue-200 bg-blue-50 text-blue-700 text-sm font-semibold hover:bg-blue-100 transition"
                  @click="goToInstructorProfile"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h6m0 0v6m0-6L10 16M6 7h3M6 11h3M6 15h3"/>
                  </svg>
                  Xem hồ sơ giảng viên
                </button>
              </div>
            </div>
          </div>

          <div id="reviews-section" class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div class="p-6 border-b border-gray-100"><h2 class="text-xl font-black text-gray-900 flex items-center gap-2"><span class="w-1 h-6 bg-amber-500 rounded-full inline-block"></span>Đánh giá khoá học</h2></div>
            <div class="p-6 border-b border-gray-100 bg-amber-50/30">
              <div class="flex flex-col sm:flex-row items-center gap-6">
                <div class="text-center flex-shrink-0">
                  <div class="text-6xl font-black text-amber-500 leading-none">{{ Number(displayAverageRating).toFixed(1) }}</div>
                  <div class="flex justify-center gap-0.5 my-2"><svg v-for="i in 5" :key="i" class="w-5 h-5 fill-current" :class="i <= Math.round(displayAverageRating) ? 'text-amber-400' : 'text-gray-200'" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg></div>
                  <p class="text-xs text-gray-500 font-medium">{{ ratingCount }} đánh giá</p>
                </div>
                <div class="flex-1 w-full space-y-2">
                  <div v-for="star in [5,4,3,2,1]" :key="star" class="flex items-center gap-3">
                    <div class="flex gap-0.5 flex-shrink-0"><svg v-for="i in 5" :key="i" class="w-3 h-3 fill-current" :class="i <= star ? 'text-amber-400' : 'text-gray-200'" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg></div>
                    <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden"><div class="h-full bg-amber-400 rounded-full transition-all duration-700" :style="{ width: ratingBreakdown[star] + '%' }"></div></div>
                    <span class="text-xs text-gray-500 w-8 text-right font-medium">{{ ratingBreakdown[star] }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!shouldHideReviewForm" class="p-6 border-b border-gray-100">
              <h3 class="font-bold text-gray-900 mb-4">Để lại đánh giá của bạn</h3>
              <div v-if="ratingEligibility?.eligible" class="space-y-4">
                <div><label class="block text-sm font-semibold text-gray-700 mb-2">Số sao</label><div class="flex gap-2"><button v-for="star in [1,2,3,4,5]" :key="star" @click="reviewForm.stars = star" class="transition-transform hover:scale-110 active:scale-95"><svg class="w-8 h-8 fill-current transition-colors" :class="star <= reviewForm.stars ? 'text-amber-400' : 'text-gray-300'" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg></button><span class="ml-2 text-sm text-gray-500 self-center">{{ ['', 'Tệ', 'Chưa tốt', 'Ổn', 'Tốt', 'Rất tốt'][reviewForm.stars] }}</span></div></div>
                <div><label class="block text-sm font-semibold text-gray-700 mb-2">Nhận xét</label><textarea v-model="reviewForm.comment" rows="4" maxlength="1000" placeholder="Chia sẻ cảm nhận của bạn về khoá học này..." class="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 resize-none transition-all"></textarea><p class="text-right text-xs text-gray-400 mt-1">{{ reviewForm.comment.length }}/1000</p></div>
                <button @click="submitReview" :disabled="submittingReview" class="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-xl transition-all disabled:opacity-60 disabled:cursor-not-allowed active:scale-95 shadow-sm">
                  {{ submittingReview ? 'Đang gửi...' : (ratingEligibility?.alreadyRated ? 'Cập nhật đánh giá' : 'Gửi đánh giá') }}
                </button>
              </div>
              <div v-else class="bg-gray-50 border border-gray-200 rounded-xl p-4">
                <p class="text-sm font-semibold text-gray-800 mb-1">{{ ratingEligibility?.message || 'Đăng nhập để đánh giá' }}</p>
                <p v-if="ratingEligibility?.loggedIn && ratingEligibility?.enrolled" class="text-xs text-gray-500">Tiến độ hiện tại: <span class="font-bold">{{ formatProgress(ratingEligibility?.progress) }}</span></p>
              </div>
            </div>

            <div class="divide-y divide-gray-100">
              <div v-if="ratings.length === 0" class="p-10 text-center"><div class="text-4xl mb-3">💬</div><p class="text-gray-500 font-medium">Chưa có đánh giá nào. Hãy là người đầu tiên!</p></div>
              <div v-for="item in ratings.slice(0, visibleRatings)" :key="item.id" :id="`review-${item.id}`" class="p-6 hover:bg-gray-50 transition-colors">
                <div class="flex items-start gap-4">
                  <img :src="item.studentAvatar || getReviewAvatar(item.studentName)" class="w-11 h-11 rounded-full object-cover border border-gray-200 flex-shrink-0"/>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-3 mb-1">
                      <span class="font-bold text-gray-900 text-sm">{{ item.studentName || 'Học viên' }}</span>
                      <div class="flex items-center gap-1">
                        <span class="text-xs text-gray-400">{{ formatDateTime(item.createdAt) }}</span>
                        <template v-if="item.studentId === ratingEligibility?.studentId">
                          <button @click="prefillAndScroll(item)" title="Sửa đánh giá" class="ml-1 p-1 rounded hover:bg-blue-50 text-gray-400 hover:text-blue-500 transition-colors">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15.232 5.232l3.536 3.536M9 13l6.586-6.586a2 2 0 112.828 2.828L11.828 15.828A2 2 0 019.5 16.5H8v-1.5a2 2 0 01.586-1.414z"/></svg>
                          </button>
                          <button @click="deleteReview" title="Xóa đánh giá" class="p-1 rounded hover:bg-red-50 text-gray-400 hover:text-red-500 transition-colors">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6M9 7h6m-7 0a1 1 0 011-1h4a1 1 0 011 1v0H8v0z"/></svg>
                          </button>
                        </template>
                      </div>
                    </div>
                    <div class="flex gap-0.5 mb-3"><svg v-for="i in 5" :key="i" class="w-3.5 h-3.5 fill-current" :class="i <= item.stars ? 'text-amber-400' : 'text-gray-200'" viewBox="0 0 24 24"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg></div>
                    <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ item.comment || 'Không có bình luận.' }}</p>
                  </div>
                </div>
              </div>
              <div v-if="ratings.length > visibleRatings" class="p-5 text-center border-t border-gray-100">
                <button @click="visibleRatings += 6" class="px-5 py-2 text-sm font-semibold text-blue-600 hover:text-blue-700 border border-blue-200 hover:border-blue-400 rounded-xl transition-colors">
                  Xem thêm {{ Math.min(ratings.length - visibleRatings, 6) }} đánh giá
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="lg:w-80 xl:w-96 flex-shrink-0 order-1 lg:order-2">
          <div class="sticky top-6 space-y-4">
            <div class="bg-white rounded-2xl border border-gray-200 shadow-xl overflow-hidden">
              <div class="relative aspect-video bg-gray-900 cursor-pointer group" @click="openPreview">
                <img :src="course?.thumbnail || 'https://placehold.co/400x225?text=MultiLearn'" class="w-full h-full object-cover opacity-90 group-hover:opacity-100 transition-opacity"/>
                <div class="absolute inset-0 flex items-center justify-center"><div class="w-14 h-14 rounded-full bg-white/90 shadow-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300"><svg class="w-5 h-5 text-gray-900 ml-1 fill-current" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg></div></div>
                <div v-if="previewVideoUrl" class="absolute bottom-3 left-0 right-0 text-center text-white text-xs font-bold">👁 Xem thử miễn phí</div>
              </div>

              <div class="p-5">
                <div class="flex items-baseline gap-3 mb-1">
                  <span class="text-3xl font-black text-gray-900">{{ formatPrice(course?.salePrice || course?.price) }}</span>
                  <span v-if="course?.salePrice && Number(course?.salePrice) < Number(course?.price)" class="text-lg text-gray-400 line-through font-medium">{{ formatPrice(course?.price) }}</span>
                </div>
                <div v-if="discountPercentage > 0" class="mb-5"><span class="inline-flex items-center gap-1.5 text-sm font-bold text-red-600 bg-red-50 px-3 py-1 rounded-full border border-red-200">🔥 Giảm {{ discountPercentage }}%</span></div>

                <div v-if="currentUser?.role === 'ADMIN'" class="space-y-3">
                  <div class="bg-blue-50 border border-blue-200 text-blue-700 text-sm font-bold text-center px-4 py-3 rounded-xl">👀 Chế độ xem Admin</div>
                  <button @click="router.push({ path: '/admin/courses', query: { editId: course?.id } })" class="w-full py-3.5 bg-slate-800 hover:bg-slate-900 text-white font-bold rounded-xl transition-all shadow-sm flex items-center justify-center gap-2"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg> Quản lý khoá học</button>
                </div>

                <div v-else-if="isInstructorOwner" class="space-y-3">
                  <div class="bg-indigo-50 border border-indigo-200 text-indigo-700 text-sm font-bold text-center px-4 py-3 rounded-xl">✏️ Khoá học do bạn tạo</div>
                  <button @click="goToManageCourse" class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition-all shadow-md flex items-center justify-center gap-2">Chỉnh sửa khoá học →</button>
                </div>

                <!-- Rental expired banner -->
                <div
                  v-if="currentUser?.role !== 'ADMIN' && !isInstructorOwner && route.query.rentalExpired === '1'"
                  class="mb-3 bg-red-50 border border-red-200 rounded-xl px-4 py-3 text-sm text-red-700 font-semibold flex items-center gap-2"
                >
                  ⏰ Thời hạn thuê đã hết. Gia hạn bên dưới để tiếp tục học.
                </div>

                <div v-else-if="currentUser?.role !== 'ADMIN' && !isInstructorOwner && isOwned" class="space-y-3">
                  <div class="bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm font-bold text-center px-4 py-3 rounded-xl flex items-center justify-center gap-2"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg> Bạn đã sở hữu khoá học này</div>
                  <button @click="goToLearning" class="w-full py-3.5 bg-emerald-600 hover:bg-emerald-700 text-white font-black rounded-xl transition-all shadow-md flex items-center justify-center gap-2 text-base">Vào học ngay <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg></button>
                </div>

                <div v-else-if="currentUser?.role !== 'ADMIN' && !isInstructorOwner" class="space-y-3">
                  <button @click="handleBuyNow" class="w-full py-3.5 text-white font-black rounded-xl transition-all flex items-center justify-center gap-2 text-base active:scale-[0.98]" :class="isFree ? 'bg-emerald-600 hover:bg-emerald-700 shadow-lg shadow-emerald-200' : 'bg-red-600 hover:bg-red-700 shadow-lg shadow-red-200'">{{ isFree ? '🎁 Đăng ký miễn phí' : 'Mua ngay' }}</button>
                  <div v-if="!isFree" class="flex gap-3">
                    <button @click="addToCart" class="flex-1 py-3 border-2 border-gray-900 text-gray-900 font-bold rounded-xl hover:bg-gray-50 transition-colors text-sm">Thêm vào giỏ</button>
                    <button @click="toggleWishlist" class="w-12 border-2 rounded-xl flex items-center justify-center transition-colors hover:bg-gray-50" :class="isWishlisted ? 'border-pink-300 bg-pink-50' : 'border-gray-200'"><svg class="w-5 h-5 transition-colors" :class="isWishlisted ? 'text-pink-500 fill-pink-500' : 'text-gray-400'" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg></button>
                  </div>

                  <!-- Rental section -->
                  <div v-if="course?.rentalEnabled && !isFree" class="mt-4 pt-4 border-t border-gray-100">
                    <p class="text-xs font-black text-gray-700 uppercase tracking-widest mb-2">🕐 Hoặc thuê khóa học</p>
                    <div class="grid grid-cols-3 gap-2">
                      <button v-for="opt in rentalOptions" :key="opt.days"
                        @click="openRentalModal(opt)"
                        class="flex flex-col items-center py-2.5 px-1 border-2 border-amber-300 rounded-xl hover:bg-amber-50 hover:border-amber-400 transition text-center">
                        <span class="text-xs font-black text-amber-700">{{ opt.days }} ngày</span>
                        <span class="text-[11px] font-bold text-gray-600 mt-0.5">{{ formatPrice(opt.price) }}</span>
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Coupon section — chỉ hiện khi có coupon và khóa không miễn phí -->
                <div v-if="courseCoupons.length > 0 && !isFree && !isOwned && !isInstructorOwner" class="mt-4 pt-4 border-t border-gray-100">
                  <p class="text-xs font-black text-gray-900 uppercase tracking-widest mb-2">Mã giảm giá</p>
                  <div class="space-y-2">
                    <div v-for="cp in courseCoupons" :key="cp.id"
                      class="flex items-center justify-between bg-orange-50 border border-orange-200 border-dashed rounded-lg px-3 py-2">
                      <div>
                        <div class="flex items-center gap-2">
                          <span class="font-black text-orange-600 tracking-widest text-sm">{{ cp.code }}</span>
                          <span class="text-xs font-bold text-white px-1.5 py-0.5 rounded"
                            :class="cp.discountType === 'PERCENT' ? 'bg-orange-500' : 'bg-green-500'">
                            {{ cp.discountType === 'PERCENT' ? `-${cp.discountValue}%` : `-${formatPrice(cp.discountValue)}` }}
                          </span>
                        </div>
                        <p v-if="cp.endDate" class="text-xs text-gray-400 mt-0.5">
                          HSD: {{ new Date(cp.endDate).toLocaleDateString('vi-VN') }}
                        </p>
                      </div>
                      <button @click="copyCoupon(cp.code)"
                        class="text-xs font-bold text-orange-600 hover:text-orange-800 border border-orange-300 hover:border-orange-500 px-2 py-1 rounded transition flex items-center gap-1">
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/>
                        </svg>
                        Copy
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Rental Modal -->
                <Teleport to="body">
                  <div v-if="rentalModal.open" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
                    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 overflow-hidden">
                      <div class="bg-gradient-to-r from-amber-500 to-orange-500 px-6 py-5">
                        <h2 class="text-lg font-bold text-white">🕐 Xác nhận thuê khóa học</h2>
                        <p class="text-amber-100 text-sm mt-1">{{ course?.title }}</p>
                      </div>
                      <div class="p-6 space-y-3">
                        <div class="flex justify-between text-sm"><span class="text-gray-500">Thời hạn</span><span class="font-bold text-gray-800">{{ rentalModal.days }} ngày</span></div>
                        <div class="flex justify-between text-sm"><span class="text-gray-500">Giá thuê</span><span class="font-black text-amber-600 text-base">{{ formatPrice(rentalModal.price) }}</span></div>
                        <div class="flex justify-between text-sm"><span class="text-gray-500">Hết hạn</span><span class="font-bold text-gray-800">{{ rentalModal.expiryLabel }}</span></div>
                        <p class="text-xs text-gray-400 pt-1">Sau khi hết hạn, bạn có thể gia hạn để tiếp tục học.</p>
                      </div>
                      <div class="px-6 pb-6 flex gap-3">
                        <button @click="rentalModal.open = false" class="flex-1 py-2.5 border border-gray-200 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50 transition">Hủy</button>
                        <button @click="confirmRent" :disabled="rentalSending" class="flex-1 py-2.5 bg-amber-500 text-white rounded-xl text-sm font-bold hover:bg-amber-600 transition disabled:opacity-50">
                          {{ rentalSending ? 'Đang xử lý...' : '✅ Xác nhận thuê' }}
                        </button>
                      </div>
                    </div>
                  </div>
                </Teleport>

                <div class="mt-5 pt-5 border-t border-gray-100">
                  <p class="text-sm font-black text-gray-900 mb-3 uppercase tracking-widest text-xs">Khoá học bao gồm</p>
                  <ul class="space-y-2.5">
                    <li class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.069A1 1 0 0121 8.82v6.361a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg><span>{{ formatDuration(course?.totalDuration) }} video bài giảng</span></li>
                    <li class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg><span>{{ course?.totalLessons || 0 }} bài học</span></li>
                    <li class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z"/></svg><span>Truy cập trọn đời</span></li>
                    <li class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"/></svg><span>Học trên mọi thiết bị</span></li>
                    <li class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"/></svg><span>Cấp chứng chỉ hoàn thành</span></li>
                    <li v-if="course?.level" class="flex items-center gap-2.5 text-sm text-gray-600"><svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg><span>Trình độ: {{ course?.level }}</span></li>
                  </ul>
                </div>
              </div>
            </div>

            <div class="bg-white rounded-2xl border border-gray-200 p-4 shadow-sm">
              <div class="grid grid-cols-3 gap-3 text-center">
                <div class="p-2"><p class="text-xl font-black text-gray-900">{{ course?.sections?.length || 0 }}</p><p class="text-xs text-gray-500 font-medium mt-0.5">Chương</p></div>
                <div class="p-2 border-x border-gray-100"><p class="text-xl font-black text-gray-900">{{ course?.totalLessons || 0 }}</p><p class="text-xs text-gray-500 font-medium mt-0.5">Bài học</p></div>
                <div class="p-2"><p class="text-xl font-black text-gray-900">{{ Number(course?.studentCount || 0) >= 1000 ? (Number(course?.studentCount)/1000).toFixed(1)+'K' : course?.studentCount || 0 }}</p><p class="text-xs text-gray-500 font-medium mt-0.5">Học viên</p></div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <div v-if="isPreviewOpen" class="fixed inset-0 z-[1000] bg-black/75 flex items-center justify-center p-4 backdrop-blur-sm" @click.self="closePreview">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl overflow-hidden">
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div><h3 class="text-lg font-black text-gray-900">{{ selectedPreviewTitle || previewLesson?.title || 'Xem thử khoá học' }}</h3><p class="text-xs text-gray-400 mt-0.5">Video xem thử miễn phí</p></div>
          <button @click="closePreview" class="w-9 h-9 rounded-full hover:bg-gray-100 text-gray-500 hover:text-gray-900 transition-colors flex items-center justify-center font-bold">✕</button>
        </div>
        <div class="bg-black">
          <video v-if="selectedPreviewUrl || previewVideoUrl" :src="selectedPreviewUrl || previewVideoUrl" controls autoplay class="w-full max-h-[70vh]">Trình duyệt không hỗ trợ video.</video>
          <div v-else class="p-12 text-center text-gray-400 bg-white"><div class="text-4xl mb-3">📹</div><p>Không có video xem thử cho khoá học này.</p></div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <div class="text-6xl mb-4">🔍</div>
      <h2 class="text-2xl font-black text-gray-800 mb-2">Không tìm thấy khoá học</h2>
      <p class="text-gray-500 mb-6">Khoá học này không tồn tại hoặc đã bị xoá.</p>
      <button @click="router.push('/course')" class="px-6 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-colors">Quay lại danh sách</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useConfirm } from '@/composables/useConfirm'
import axiosClient from '@/api/axiosClient'
import WishlistService from '@/services/wishlist.service'
import RatingService from '@/services/rating.service'
import { normalizeMojibakeText } from '@/utils/textEncoding'

const route  = useRoute()
const router = useRouter()
const { t }  = useI18n()
const { confirm } = useConfirm()

// ── STATE ────────────────────────────────────────────────────
const course          = ref(null)
const isLoading       = ref(true)
const openChapters    = ref([0])
const isOwned         = ref(false)
const isWishlisted    = ref(false)
const currentUser     = ref(null)
const isPreviewOpen   = ref(false)
const instructorOwnerFallback = ref(false)

const ratings          = ref([])
const averageRating    = ref(0)
const ratingCount      = ref(0)
const visibleRatings   = ref(6)
const ratingEligibility = ref(null)
const submittingReview  = ref(false)
const reviewForm = ref({ stars: 5, comment: '' })

const hoveredLesson        = ref(null)
const selectedPreviewUrl   = ref('')
const selectedPreviewTitle = ref('')

const courseCoupons = ref([])

// ── RENTAL ───────────────────────────────────────────────────
const rentalSending = ref(false)
const rentalModal = reactive({ open: false, days: 30, price: 0, expiryLabel: '' })

const rentalOptions = computed(() => {
  const p = Number(course.value?.price || 0)
  return [
    { days: 7,  price: Math.round(p * 0.15) },
    { days: 30, price: Math.round(p * 0.30) },
    { days: 90, price: Math.round(p * 0.50) },
  ]
})

const openRentalModal = (opt) => {
  const expiry = new Date()
  expiry.setDate(expiry.getDate() + opt.days)
  rentalModal.open = true
  rentalModal.days = opt.days
  rentalModal.price = opt.price
  rentalModal.expiryLabel = expiry.toLocaleDateString('vi-VN')
}

const confirmRent = async () => {
  if (!currentUser.value) {
    rentalModal.open = false
    const ok = await confirm({ title: 'Yêu cầu đăng nhập', message: 'Vui lòng đăng nhập để thuê khoá học.', confirmText: 'Đăng nhập', cancelText: 'Hủy', variant: 'primary' })
    if (ok) router.push('/login')
    return
  }
  rentalSending.value = true
  try {
    const res = await axiosClient.post(`/student/courses/${course.value.id}/rent`, { duration: rentalModal.days })
    rentalModal.open = false

    if (res?.activated) {
      // Miễn phí — kích hoạt luôn
      isOwned.value = true
      window.__notify?.auto?.('🎉 Thuê khóa học thành công!')
      return
    }

    // Có phí — redirect VNPay
    const payRes = await axiosClient.get('/payment/create-payment', {
      params: { orderId: res.orderId, amount: res.finalAmount }
    })
    if (payRes?.paymentUrl) {
      window.location.href = payRes.paymentUrl
    } else {
      window.__notify?.auto?.('❌ Không thể khởi tạo cổng thanh toán.')
    }
  } catch (e) {
    window.__notify?.auto?.('❌ ' + (e?.response?.data?.message || 'Thuê thất bại, vui lòng thử lại.'))
  } finally {
    rentalSending.value = false
  }
}

// ── COMPUTED ─────────────────────────────────────────────────
const avatarFallback = computed(() => {
  const name = course.value?.authorName || course.value?.instructorName || 'GV'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=e0e7ff&color=4f46e5&bold=true&size=80`
})

const displayAverageRating = computed(() => {
  const api = Number(averageRating.value || 0)
  return api > 0 ? api : Number(course.value?.averageRating || 0)
})

const discountPercentage = computed(() => {
  const p = Number(course.value?.price || 0)
  const s = Number(course.value?.salePrice || 0)
  if (!p || !s || s >= p) return 0
  return Math.round(((p - s) / p) * 100)
})

const cleanText = (value) => {
  if (value == null) return ''
  return normalizeMojibakeText(String(value)).replace(/\uFFFD/g, '').trim()
}

const DESCRIPTION_ALLOWED_TAGS = new Set([
  'p', 'br', 'ul', 'ol', 'li', 'strong', 'b', 'em', 'i', 'u', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'blockquote', 'pre', 'code', 'a', 'img'
])

const sanitizeCourseDescription = (html) => {
  if (!html) return ''
  const parser = new DOMParser()
  const doc = parser.parseFromString(`<div>${html}</div>`, 'text/html')
  const root = doc.body.firstElementChild
  if (!root) return ''

  root.querySelectorAll('script,style,link,meta,iframe,object,embed').forEach((n) => n.remove())

  const allNodes = root.querySelectorAll('*')
  allNodes.forEach((node) => {
    const tag = node.tagName.toLowerCase()
    if (!DESCRIPTION_ALLOWED_TAGS.has(tag)) {
      const parent = node.parentNode
      while (node.firstChild) parent?.insertBefore(node.firstChild, node)
      parent?.removeChild(node)
      return
    }

    Array.from(node.attributes).forEach((attr) => {
      const name = attr.name.toLowerCase()
      const allow =
        (tag === 'a' && ['href', 'target', 'rel', 'title'].includes(name)) ||
        (tag === 'img' && ['src', 'alt', 'title'].includes(name))
      if (!allow) node.removeAttribute(attr.name)
    })

    if (tag === 'a') {
      const href = node.getAttribute('href') || ''
      if (!/^https?:\/\//i.test(href) && !href.startsWith('/')) {
        node.removeAttribute('href')
      }
      node.setAttribute('rel', 'noopener noreferrer')
    }
  })

  return root.innerHTML
}

// Khóa học miễn phí: giá gốc = 0 và không có flash sale giá > 0
const isFree = computed(() => {
  const price = Number(course.value?.price || 0)
  const salePrice = Number(course.value?.salePrice || 0)
  const flashPrice = Number(course.value?.flashSalePrice || 0)
  const effectivePrice = flashPrice > 0 ? flashPrice : (salePrice > 0 ? salePrice : price)
  return effectivePrice <= 0
})

const ratingBreakdown = computed(() => {
  const breakdown = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  if (!ratings.value.length) return breakdown
  const counts = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
  ratings.value.forEach(r => { if (counts[r.stars] !== undefined) counts[r.stars]++ })
  const total = ratings.value.length
  Object.keys(counts).forEach(star => {
    breakdown[star] = Math.round((counts[star] / total) * 100)
  })
  return breakdown
})

const parsedLearningOutcomes = computed(() => {
  const raw = cleanText(course.value?.learningOutcomes)
  if (!raw) return []
  if (raw.includes('<li>')) {
    return raw
      .match(/<li[^>]*>(.*?)<\/li>/gs)
      ?.map(li => cleanText(li.replace(/<[^>]+>/g, '').trim()))
      .filter(Boolean) || []
  }
  return raw.split('\n').map(s => cleanText(s)).filter(Boolean)
})

const sanitizedCourseDescription = computed(() => sanitizeCourseDescription(course.value?.description))

const totalCompletedLessons = computed(() => {
  if (!course.value?.sections?.length) return 0
  return course.value.sections.reduce((total, section) => {
    return total + (section.lessons || []).filter(l => l?.isCompleted).length
  }, 0)
})

const courseProgressPercent = computed(() => {
  const total = course.value?.totalLessons || 0
  if (!total) return 0
  return Math.round((totalCompletedLessons.value / total) * 100)
})

const sectionProgressCount = (section) => {
  return (section?.lessons || []).filter(l => l?.isCompleted).length
}

const previewLesson = computed(() => {
  if (!course.value?.sections?.length) return null
  for (const section of course.value.sections) {
    for (const lesson of (section.lessons || [])) {
      if (lesson?.isPreview && String(lesson.type).toUpperCase() === 'VIDEO' && lesson.videoUrl)
        return lesson
    }
  }
  return null
})

const previewVideoUrl = computed(() => previewLesson.value?.videoUrl || '')

const toNum = (v) => { const n = Number(v); return Number.isFinite(n) ? n : null }

const resolveInstructorUserId = (cd) => {
  if (!cd) return null
  const raw = cd.instructorUserId ?? cd.instructorId ?? cd.authorId ?? cd.instructor?.id ?? cd.author?.id
  return toNum(raw)
}

const publicInstructorId = computed(() => resolveInstructorUserId(course.value))

const isInstructorOwner = computed(() => {
  if (!currentUser.value || !course.value) return false
  if (String(currentUser.value.role || '').toUpperCase() !== 'INSTRUCTOR') return false
  const curId = toNum(currentUser.value.id ?? currentUser.value.userId)
  const insId = resolveInstructorUserId(course.value)
  if (curId !== null && insId !== null) return curId === insId
  return !!instructorOwnerFallback.value
})

const shouldHideReviewForm = computed(() => {
  if (!currentUser.value) return false
  const role = String(currentUser.value.role || '').toUpperCase()
  return role === 'ADMIN' || isInstructorOwner.value
})

// ── FETCH ────────────────────────────────────────────────────
const fetchCourseDetail = async () => {
  // Fix lỗi watcher chết yểu khi không có route params
  if (!route.params?.slug) {
     return;
  }
  
  const slug = route.params.slug;
  if (!slug || ['home','login','register','undefined'].includes(String(slug))) {
    isLoading.value = false; course.value = null; return
  }
  
  isLoading.value = true
  try {
    let res
    try { res = await axiosClient.get(`/courses/slug/${slug}`) }
    catch (e) {
      const s = e?.response?.status
      if (s === 404 || s === 405) res = await axiosClient.get(`/courses/${slug}`)
      else throw e
    }

    if (res) {
      course.value = res
      document.title = `${res.title} - MultiLearn`
      isOwned.value = !!res.isOwned
      instructorOwnerFallback.value = !!res.isInstructorOwner

      await resolveInstructorOwnershipFallback(res.id)
      await resolveStudentOwnershipFallback(res.id, res.slug)

      if (currentUser.value && String(currentUser.value.role||'').toUpperCase() !== 'ADMIN' && !isInstructorOwner.value) {
        await checkWishlistStatus(res.id)
      }

      openChapters.value = res.sections?.length ? [0] : []
      await Promise.all([loadRatings(res.id), loadRatingEligibility(res.id)])
      // Load coupon công khai của instructor (không cần token)
      try {
        courseCoupons.value = await axiosClient.get(`/public/coupons/course/${res.id}`)
      } catch { courseCoupons.value = [] }
    }
  } catch (e) {
    console.error('Error loading course:', e)
    course.value = null
  } finally {
    isLoading.value = false
    await nextTick()
    if (route.hash === '#reviews-section') {
      setTimeout(() => {
        const el = document.getElementById('reviews-section')
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'start' })
          el.classList.add('ring-2', 'ring-blue-500', 'ring-offset-4', 'transition-all', 'duration-1000')
          setTimeout(() => el.classList.remove('ring-2', 'ring-blue-500', 'ring-offset-4'), 2000)
        }
      }, 300)
    }
  }
}

const resolveInstructorOwnershipFallback = async (courseId) => {
  instructorOwnerFallback.value = false
  if (!currentUser.value || String(currentUser.value.role||'').toUpperCase() !== 'INSTRUCTOR') return
  if (resolveInstructorUserId(course.value) !== null) return
  try {
    const res = await axiosClient.get('/courses/my-courses')
    if (Array.isArray(res))
      instructorOwnerFallback.value = res.some(item => Number(item.id) === Number(courseId))
  } catch { instructorOwnerFallback.value = false }
}

const resolveStudentOwnershipFallback = async (courseId, slug) => {
  if (!currentUser.value || instructorOwnerFallback.value) return
  try {
    const res = await axiosClient.get('/enrollments/my-courses')
    if (!Array.isArray(res)) return
    const owned = res.some(item => Number(item?.id) === Number(courseId)
      || (slug && item?.slug && String(item.slug) === String(slug)))
    if (owned) isOwned.value = true
  } catch {}
}

const loadRatings = async (courseId) => {
  if (!courseId) return
  try {
    const [listRes, avgRes, countRes] = await Promise.all([
      RatingService.getByCourse(courseId),
      RatingService.getAverage(courseId),
      RatingService.getCount(courseId)
    ])
    ratings.value       = Array.isArray(listRes) ? listRes : []
    averageRating.value = Number(avgRes || 0)
    ratingCount.value   = Number(countRes || 0)
    if (course.value) course.value.averageRating = averageRating.value
    await scrollToReviewFromHash()
  } catch {
    ratings.value       = []
    averageRating.value = Number(course.value?.averageRating || 0)
    ratingCount.value   = 0
  }
}

const loadRatingEligibility = async (courseId) => {
  if (!courseId || shouldHideReviewForm.value) { ratingEligibility.value = null; return }
  try {
    ratingEligibility.value = await RatingService.getEligibility(courseId)
    // Prefill form nếu đã có đánh giá
    if (ratingEligibility.value?.alreadyRated && ratings.value.length > 0) {
      const myReview = ratings.value.find(r => r.studentId === ratingEligibility.value.studentId)
      if (myReview) reviewForm.value = { stars: myReview.stars, comment: myReview.comment || '' }
    }
  }
  catch { ratingEligibility.value = null }
}

const checkWishlistStatus = async (courseId) => {
  if (!currentUser.value) return
  try {
    const res = await WishlistService.getWishlist(currentUser.value.id)
    if (Array.isArray(res)) {
      const ids = res.map(item => item.course?.id ?? item.courseId).filter(id => id != null)
      isWishlisted.value = ids.some(id => Number(id) === Number(courseId))
    }
  } catch { isWishlisted.value = false }
}

// ── ACTIONS ───────────────────────────────────────────────────
const submitReview = async () => {
  if (!course.value?.id || shouldHideReviewForm.value) return
  if (!currentUser.value) {
    const ok = await confirm({ title:'Yêu cầu đăng nhập', message:'Bạn cần đăng nhập để đánh giá.', confirmText:'Đăng nhập', cancelText:'Hủy', variant:'primary' })
    if (ok) router.push('/login')
    return
  }
  if (!ratingEligibility.value?.eligible) {
    window.__notify?.auto?.(ratingEligibility.value?.message || 'Bạn chưa đủ điều kiện đánh giá.')
    return
  }
  submittingReview.value = true
  try {
    const payload = { stars: Number(reviewForm.value.stars), comment: reviewForm.value.comment }
    if (ratingEligibility.value?.alreadyRated) {
      await RatingService.update(course.value.id, payload)
      window.__notify?.auto?.('✅ Cập nhật đánh giá thành công!')
    } else {
      await RatingService.create(course.value.id, payload)
      window.__notify?.auto?.('✅ Gửi đánh giá thành công!')
    }
    await Promise.all([loadRatings(course.value.id), loadRatingEligibility(course.value.id)])
  } catch (e) {
    const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Không thể gửi đánh giá'
    window.__notify?.auto?.(`❌ ${msg}`)
  } finally { submittingReview.value = false }
}

const prefillAndScroll = (item) => {
  reviewForm.value = { stars: item.stars, comment: item.comment || '' }
  document.querySelector('.p-6.border-b')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const deleteReview = async () => {
  const ok = await confirm({ title: 'Xóa đánh giá', message: 'Bạn có chắc muốn xóa đánh giá này không?', confirmText: 'Xóa', cancelText: 'Hủy', variant: 'danger' })
  if (!ok) return
  submittingReview.value = true
  try {
    await RatingService.delete(course.value.id)
    reviewForm.value = { stars: 5, comment: '' }
    await Promise.all([loadRatings(course.value.id), loadRatingEligibility(course.value.id)])
    window.__notify?.auto?.('✅ Đã xóa đánh giá.')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || 'Không thể xóa đánh giá'
    window.__notify?.auto?.(`❌ ${msg}`)
  } finally { submittingReview.value = false }
}

const handleBuyNow = async () => {
  if (!currentUser.value) {
    sessionStorage.setItem('redirect_url', route.fullPath)
    const ok = await confirm({ title:'Yêu cầu đăng nhập', message:'Vui lòng đăng nhập để mua khóa học.', confirmText:'Đăng nhập', cancelText:'Hủy', variant:'primary' })
    if (ok) router.push('/login')
    return
  }
  if (isInstructorOwner.value) { window.__notify?.auto?.('Bạn không thể mua khóa học của chính mình.'); return }
  if (isOwned.value) { goToLearning(); return }

  const ok = await confirm({
    title: isFree.value ? 'Đăng ký miễn phí' : 'Xác nhận mua khóa học',
    message: isFree.value
      ? `Đăng ký miễn phí khóa học "${course.value.title}"?`
      : `Xác nhận mua khóa học "${course.value.title}"?`,
    confirmText: isFree.value ? 'Đăng ký ngay' : 'Mua ngay',
    cancelText: 'Hủy',
    variant: isFree.value ? 'primary' : 'warning'
  })
  if (!ok) return

  try {
    if (isFree.value) {
      const orderRes = await axiosClient.post('/orders/checkout', null, {
        params: { courseId: course.value.id }
      })
      if (orderRes?.id && (!orderRes.finalAmount || Number(orderRes.finalAmount) <= 0)) {
        isOwned.value = true
        window.__notify?.auto?.('🎉 Đăng ký thành công! Chúc bạn học vui.')
        router.push('/my-courses')
        return
      }
      window.__notify?.auto?.('❌ Không thể đăng ký khóa học miễn phí lúc này.')
      return
    }

    try {
      await axiosClient.post('/cart/add', null, {
        params: { studentId: currentUser.value.id, courseId: course.value.id }
      })
      window.dispatchEvent(new Event('cart-updated'))
    } catch (addErr) {
      const addMsg = addErr?.response?.data || addErr?.message || ''
      if (!String(addMsg).includes('đã có trong giỏ')) {
        throw addErr
      }
    }

    router.push({ path: '/checkout', query: { courseId: String(course.value.id), buyNow: '1' } })
  } catch (e) {
    const msg = e?.response?.data || e?.message || 'Có lỗi xảy ra'
    if (String(msg).includes('đã sở hữu')) { isOwned.value = true; window.__notify?.auto?.('Bạn đã sở hữu khóa học này rồi.'); return }
    window.__notify?.auto?.(`❌ ${msg}`)
  }
}

const addToCart = async () => {
  if (!currentUser.value) {
    const ok = await confirm({ title:'Yêu cầu đăng nhập', message:'Vui lòng đăng nhập để thêm vào giỏ.', confirmText:'Đăng nhập', cancelText:'Hủy', variant:'primary' })
    if (ok) router.push('/login')
    return
  }
  if (isInstructorOwner.value) { window.__notify?.auto?.('Bạn không thể thêm khoá học của mình vào giỏ.'); return }
  if (isOwned.value) { window.__notify?.auto?.('Bạn đã sở hữu khoá học này rồi.'); return }
  try {
    await axiosClient.post('/cart/add', null, { params: { studentId: currentUser.value.id, courseId: course.value.id } })
    window.__notify?.auto?.('✅ Đã thêm vào giỏ hàng!')
    window.dispatchEvent(new Event('cart-updated'))
  } catch (e) {
    const msg = e?.response?.data || e?.message || ''
    if (String(msg).includes('đã có trong giỏ')) { window.__notify?.auto?.('⚠️ Khoá học đã có trong giỏ rồi!'); window.dispatchEvent(new Event('cart-updated')) }
    else if (String(msg).includes('đã sở hữu')) { isOwned.value = true; window.__notify?.auto?.('Bạn đã sở hữu rồi.') }
    else window.__notify?.auto?.(`❌ ${msg}`)
  }
}

const toggleWishlist = async () => {
  if (!currentUser.value) {
    const ok = await confirm({ title:'Yêu cầu đăng nhập', message:'Vui lòng đăng nhập.', confirmText:'Đăng nhập', cancelText:'Hủy', variant:'primary' })
    if (ok) router.push('/login')
    return
  }
  if (isInstructorOwner.value) { window.__notify?.auto?.('Không thể thêm khoá học của mình vào yêu thích.'); return }
  try {
    if (isWishlisted.value) {
      await WishlistService.removeFromWishlist(currentUser.value.id, course.value.id)
      isWishlisted.value = false
      window.__notify?.auto?.('Đã xoá khỏi yêu thích.')
    } else {
      await WishlistService.addToWishlist(currentUser.value.id, course.value.id)
      isWishlisted.value = true
      window.__notify?.auto?.('❤️ Đã thêm vào yêu thích!')
    }
  } catch { window.__notify?.auto?.('❌ Có lỗi xảy ra') }
}

const goToLearning      = () => { if (course.value?.id) router.push(`/learning/course/${course.value.id}`) }
const goToManageCourse = () => { if (course.value?.id) router.push(`/instructor/course/${course.value.id}/manage`) }
const goToInstructorProfile = () => {
  if (!publicInstructorId.value) return
  router.push({ name: 'instructorPublicProfile', params: { id: publicInstructorId.value } })
}

const copyCoupon = (code) => {
  navigator.clipboard.writeText(code).then(() => {
    window.__notify?.auto?.(`Đã copy mã: ${code}`)
  })
}

const toggleChapter = (idx) => {
  if (openChapters.value.includes(idx)) openChapters.value = openChapters.value.filter(i => i !== idx)
  else openChapters.value.push(idx)
}

const openPreview  = async () => {
  if (!previewLesson.value) { window.__notify?.auto?.('Khoá học này chưa có video xem thử.'); return }
  selectedPreviewTitle.value = previewLesson.value.title
  isPreviewOpen.value = true
  try {
    const res = await axiosClient.get(`/learning/lessons/${previewLesson.value.id}/preview-url`)
    selectedPreviewUrl.value = res?.url || res
  } catch {
    selectedPreviewUrl.value = previewLesson.value.videoUrl
  }
}
const closePreview = () => {
  isPreviewOpen.value      = false
  selectedPreviewUrl.value  = ''
  selectedPreviewTitle.value = ''
}

const playPreviewLesson = async (lesson) => {
  if (!lesson?.videoUrl) { window.__notify?.auto?.('Không có video cho bài này.'); return }
  selectedPreviewTitle.value = lesson.title
  isPreviewOpen.value = true
  try {
    const res = await axiosClient.get(`/learning/lessons/${lesson.id}/preview-url`)
    selectedPreviewUrl.value = res?.url || res
  } catch {
    selectedPreviewUrl.value = lesson.videoUrl
  }
}

const scrollToSection = (id) => {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const scrollToReviewFromHash = async () => {
  if (!route.hash?.startsWith('#review-')) return
  await nextTick()
  document.querySelector(route.hash)?.scrollIntoView({ behavior:'smooth', block:'center' })
}

// ── HELPERS ───────────────────────────────────────────────────
const formatPrice = (val) => {
  const n = Number(val || 0)
  if (!n) return t('common.free') || 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(n)
}

const formatDuration = (val) => {
  const total = Number(val || 0)
  if (!total || total <= 0) return '0p'
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  if (h > 0) return m > 0 ? `${h}g ${m}p` : `${h} giờ`
  return `${m || Math.ceil(total / 60)} phút`
}

const formatLessonMeta = (lesson) => {
  const type = String(lesson?.type || '').toUpperCase()
  if (type === 'VIDEO') return formatDuration(lesson?.duration)
  if (type === 'QUIZ')  return lesson?.quizPassingScore ? `Qua môn ${lesson.quizPassingScore}%` : 'Quiz'
  if (type === 'DOCUMENT') return 'Tài liệu'
  if (type === 'TEXT')     return 'Bài viết'
  return ''
}

const formatDate = (val) => {
  if (!val) return new Date().toLocaleDateString('vi-VN')
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('vi-VN')
}

const formatDateTime = (val) => {
  if (!val) return ''
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleString('vi-VN')
}

const formatProgress = (val) => {
  const n = Number(val || 0)
  return Number.isFinite(n) ? `${n.toFixed(0)}%` : '0%'
}

const getReviewAvatar = (name) =>
  `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'User')}&background=dbeafe&color=2563eb&bold=true&size=40`

// ── LIFECYCLE ─────────────────────────────────────────────────
// Fix lỗi Watcher kích hoạt lúc chuyển route
watch(() => route.params.slug, (newSlug) => {
  if (newSlug && route.name === 'CourseDetail' && !['home','login','register','undefined'].includes(String(newSlug))) {
    fetchCourseDetail()
  }
})

watch(() => route.hash, async () => { await scrollToReviewFromHash() })

onMounted(() => {
  const userStr = localStorage.getItem('user_info')
  if (userStr) { try { currentUser.value = JSON.parse(userStr) } catch {} }
  fetchCourseDetail()
})
</script>
