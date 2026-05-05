<template>
  <div class="min-h-screen bg-[#f5f5f0] font-sans text-gray-900">

    <!-- TICKER BAR -->
    <div class="bg-slate-900 text-white py-2.5 overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center gap-4 text-[11px] sm:text-xs font-bold tracking-wide">
        <span class="flex-shrink-0 bg-gradient-to-r from-blue-600 to-purple-600 text-white px-2.5 py-0.5 rounded text-[10px] uppercase tracking-widest font-black shadow-sm">TRỰC TIẾP</span>
        <div class="flex items-center gap-5 overflow-hidden whitespace-nowrap">
          <template v-if="!isStatsLoading">
            <span class="flex items-center gap-1.5 flex-shrink-0">
              <span class="w-1.5 h-1.5 rounded-full bg-blue-400"></span>
              {{ formatNumber(stats.totalStudents) }} học viên
            </span>
            <span class="text-gray-600 flex-shrink-0">|</span>
            <span class="flex items-center gap-1.5 flex-shrink-0">
              <span class="w-1.5 h-1.5 rounded-full bg-purple-400"></span>
              {{ formatNumber(stats.totalCourses) }} khoá học
            </span>
          </template>
          <template v-if="!isLiveStatsLoading && liveStats.newEnrollmentsToday > 0">
            <span class="text-gray-600 flex-shrink-0">|</span>
            <span class="flex items-center gap-1.5 flex-shrink-0 text-emerald-400">
              ▲ {{ liveStats.newEnrollmentsToday }} học viên mới hôm nay
            </span>
          </template>
          <span class="text-gray-600 flex-shrink-0">|</span>
          <span class="text-gray-400 flex-shrink-0">{{ today }}</span>
        </div>
      </div>
    </div>

    <!-- MASTHEAD -->
    <div class="bg-white border-b-4 border-gray-900 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
          <div class="flex items-end gap-4">
            <div>
              <h1 class="text-3xl sm:text-4xl font-black leading-none tracking-tight uppercase">
                MultiLearn <span class="text-blue-600">Insider</span>
              </h1>
              <div class="flex items-center gap-2 mt-1">
                <span class="w-2 h-2 rounded-full bg-red-500 animate-pulse"></span>
                <p class="text-[11px] text-gray-500 font-bold tracking-widest uppercase">Tin tức · Khuyến mãi · Học tập</p>
              </div>
            </div>
          </div>
          <nav class="flex flex-wrap items-center gap-1 border-t md:border-t-0 pt-3 md:pt-0">
            <button v-for="tab in tabs" :key="tab.id"
              @click="scrollToSection(tab.id)"
              class="px-4 py-1.5 text-xs font-bold uppercase tracking-widest transition-all border-b-2"
              :class="activeSection === tab.id
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-600 hover:text-gray-900 hover:border-gray-400'">
              {{ tab.label }}
            </button>
          </nav>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-14">

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- SECTION: FLASH SALE (GIỮ NGUYÊN)                       -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section id="section-sale">
        <!-- Section label kiểu báo -->
        <div class="flex items-center gap-3 mb-5">
          <div class="bg-red-600 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">KHUYẾN MÃI</div>
          <div class="flex-1 h-px bg-gray-300"></div>
          <router-link v-if="isAdmin" to="/admin/campaigns" class="text-xs font-bold text-gray-500 hover:text-red-600 transition">⚡ Quản lý Sale</router-link>
        </div>

        <div class="relative rounded-2xl overflow-hidden shadow-2xl bg-gradient-to-br from-rose-500 via-red-500 to-orange-500 pt-6 sm:pt-10 pb-8">
          <div class="absolute inset-0 opacity-[0.15]" style="background-image:radial-gradient(circle at 2px 2px,#fff 1px,transparent 0);background-size:24px 24px;"></div>

          <div class="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6 mb-10 px-6 sm:px-10">
            <div class="text-white">
              <div class="inline-flex items-center gap-2 bg-white/20 backdrop-blur-md px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest border border-white/30 mb-4 shadow-sm">
                🔥 Ưu đãi có hạn
              </div>
              <h2 class="text-3xl sm:text-5xl font-black tracking-tight mb-2 drop-shadow-md">
                {{ activeCampaign ? activeCampaign.name : 'Mã Giảm Giá Đặc Biệt' }}
              </h2>
              <p class="text-red-100 font-medium">
                {{ activeCampaign ? 'Giá tự động giảm trực tiếp trên khóa học.' : 'Nhập mã tại trang thanh toán để nhận ưu đãi đang còn hiệu lực.' }}
              </p>
            </div>
            <div class="flex flex-col sm:flex-row items-center gap-4">
              <div v-if="campaignCountdown || countdown" class="flex items-center gap-3 bg-white px-6 py-3 rounded-2xl shadow-xl">
                <svg class="w-6 h-6 text-orange-500 animate-spin-slow" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                <div>
                  <p class="text-[10px] font-black text-gray-500 uppercase tracking-widest leading-none">Kết thúc sau</p>
                  <p class="text-xl font-black text-red-600 tabular-nums leading-none mt-1">{{ activeCampaign ? campaignCountdown : countdown }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Campaign slider -->
          <div v-if="activeCampaign" class="relative z-10 w-full pl-6 sm:pl-10">
            <div class="flex items-center justify-between mb-4 pr-6 sm:pr-10">
              <span class="text-white font-bold text-sm tracking-wide uppercase">⚡ Đang Sale Rực Lửa:</span>
              <div class="flex gap-2">
                <button @click="scrollSlider('left')" class="w-8 h-8 rounded-full bg-white/20 hover:bg-white/40 flex items-center justify-center text-white backdrop-blur-sm transition-all"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg></button>
                <button @click="scrollSlider('right')" class="w-8 h-8 rounded-full bg-white/20 hover:bg-white/40 flex items-center justify-center text-white backdrop-blur-sm transition-all"><svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg></button>
              </div>
            </div>
            <div class="flex gap-4 overflow-x-auto snap-x snap-mandatory hide-scrollbar pb-6 pr-6 sm:pr-10" ref="sliderRef" @mouseenter="pauseSlider" @mouseleave="startSlider">
              <div v-for="course in campaignItems" :key="course.id"
                class="snap-start shrink-0 w-[280px] bg-white rounded-2xl p-4 shadow-xl transform transition-all hover:-translate-y-2 cursor-pointer flex flex-col group"
                @click="$router.push('/course/slug/' + course.slug)">
                <div class="relative w-full h-36 rounded-xl overflow-hidden mb-4 bg-gray-100">
                  <img :src="course.thumbnail || 'https://placehold.co/280x150'" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"/>
                  <div v-if="course.price > 0 && course.salePrice" class="absolute top-2 right-2 bg-red-600 text-white font-black text-xs px-2 py-1 rounded shadow-lg">
                    -{{ Math.round((1 - course.salePrice / course.price) * 100) }}%
                  </div>
                  <div class="absolute bottom-2 left-2 bg-orange-500 text-white font-black text-[9px] uppercase tracking-widest px-2 py-0.5 rounded shadow">Flash Sale</div>
                </div>
                <h4 class="font-bold text-gray-900 leading-snug line-clamp-2 mb-2 group-hover:text-red-600 transition-colors h-10">{{ course.title }}</h4>
                <div class="mt-auto">
                  <div class="flex items-center gap-2 mb-3">
                    <span class="text-xl font-black text-red-600">{{ formatPrice(course.salePrice) }}</span>
                    <span class="text-xs text-gray-400 line-through">{{ formatPrice(course.price) }}</span>
                  </div>
                  <div v-if="course.totalSlots">
                    <div class="flex justify-between text-[10px] font-bold text-gray-500 mb-1.5 uppercase">
                      <span>Đã bán {{ course.soldSlots || 0 }}</span>
                      <span class="text-red-500">Còn {{ Math.max(0, course.totalSlots - (course.soldSlots || 0)) }}</span>
                    </div>
                    <div class="h-2 w-full bg-gray-100 rounded-full overflow-hidden shadow-inner relative">
                      <div class="absolute inset-y-0 left-0 bg-gradient-to-r from-orange-400 to-red-600 rounded-full" :style="`width:${Math.min(100,((course.soldSlots||0)/course.totalSlots)*100)}%`"></div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="isCampaignItemsLoading" v-for="i in 4" :key="'sk'+i" class="snap-start shrink-0 w-[280px] bg-white/50 rounded-2xl p-4 animate-pulse">
                <div class="w-full h-36 bg-gray-200/50 rounded-xl mb-4"></div><div class="h-4 bg-gray-200/50 rounded w-full mb-2"></div><div class="h-4 bg-gray-200/50 rounded w-2/3"></div>
              </div>
            </div>
          </div>

          <!-- Coupon cards -->
          <div v-else class="relative z-10 px-6 sm:px-10">
            <div v-if="isCouponsLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div v-for="i in 4" :key="i" class="bg-white/20 backdrop-blur-md rounded-2xl p-6 animate-pulse"><div class="h-4 bg-white/30 rounded w-1/3 mb-4"></div><div class="h-10 bg-white/30 rounded w-2/3 mb-4"></div></div>
            </div>
            <div v-else-if="featuredCoupons.length > 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <div v-for="coupon in featuredCoupons" :key="coupon.id" class="bg-white rounded-2xl p-6 shadow-xl transform transition-all hover:-translate-y-1 relative overflow-hidden group">
                <div v-if="coupon.remainingCount !== null && coupon.remainingCount <= 5" class="absolute -right-10 top-4 bg-red-600 text-white text-[10px] font-black px-10 py-1 rotate-45 shadow-md animate-pulse">SẮP HẾT</div>
                <p class="text-xs font-black uppercase tracking-widest mb-3" :class="coupon.discountType==='PERCENT' ? 'text-orange-500' : 'text-blue-500'">{{ coupon.discountType==='PERCENT' ? 'GIẢM PHẦN TRĂM' : 'GIẢM TRỰC TIẾP' }}</p>
                <div class="text-4xl sm:text-5xl font-black text-gray-900 leading-none mb-2 tracking-tight">{{ coupon.discountType==='PERCENT' ? coupon.discountValue+'%' : formatPriceShort(coupon.discountValue) }}</div>
                <p class="text-sm text-gray-500 mb-6 font-medium">Đơn từ {{ coupon.minOrderValue ? formatPrice(coupon.minOrderValue) : 'bất kỳ' }}</p>
                <div class="flex items-center gap-2 mb-5">
                  <div class="flex-1 px-3 py-2.5 bg-gray-50 border border-dashed border-gray-300 rounded-xl font-mono text-sm font-black text-gray-900 tracking-widest text-center select-all">{{ coupon.code }}</div>
                  <button @click="copyCode(coupon.code)" class="flex-shrink-0 w-11 h-11 rounded-xl flex items-center justify-center transition-all shadow-sm active:scale-95" :class="copiedCode===coupon.code ? 'bg-emerald-500 text-white':'bg-gray-900 text-white hover:bg-gray-800'">
                    <svg v-if="copiedCode !== coupon.code" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
                  </button>
                </div>
                <div v-if="coupon.usageLimit">
                  <div class="flex justify-between text-xs mb-1.5 font-bold"><span class="text-gray-500">{{ coupon.usedCount||0 }}/{{ coupon.usageLimit }}</span><span :class="(coupon.remainingCount||0)<=5?'text-red-600':'text-gray-900'">Còn {{ coupon.remainingCount }}</span></div>
                  <div class="h-2 bg-gray-100 rounded-full overflow-hidden"><div class="h-full rounded-full transition-all" :class="usagePercent(coupon)>80?'bg-red-500':usagePercent(coupon)>50?'bg-orange-500':'bg-emerald-500'" :style="`width:${usagePercent(coupon)}%`"></div></div>
                </div>
                <p v-else class="text-xs text-emerald-600 font-bold bg-emerald-50 px-3 py-1.5 rounded-lg text-center">✓ Không giới hạn số lượng</p>
                <p class="text-[11px] text-gray-400 mt-4 font-medium text-center border-t border-gray-100 pt-3">HSD: {{ formatDate(coupon.endDate) }}</p>
              </div>
            </div>
            <div v-else class="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-8 text-center max-w-2xl mx-auto">
              <p class="text-2xl text-white font-bold mb-4">Các ưu đãi đang được cập nhật.</p>
              <router-link to="/course" class="inline-flex items-center gap-2 px-8 py-3 bg-white text-red-600 font-black rounded-xl hover:shadow-lg transition-all">Duyệt khoá học ngay →</router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- SECTION: TIN NỔI BẬT (EDITORIAL LAYOUT)               -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section id="section-bestseller">
        <div class="flex items-center gap-3 mb-6">
          <div class="bg-gray-900 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">NỔI BẬT</div>
          <div class="flex-1 h-px bg-gray-300"></div>
          <router-link to="/course?sort=studentCount,desc" class="text-xs font-bold text-gray-500 hover:text-gray-900 transition">Xem tất cả →</router-link>
        </div>

        <div v-if="isBestsellerLoading" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 bg-gray-200 rounded-2xl aspect-video animate-pulse"></div>
          <div class="space-y-4"><div v-for="i in 3" :key="i" class="flex gap-3 animate-pulse"><div class="w-20 h-14 bg-gray-200 rounded-lg flex-shrink-0"></div><div class="flex-1 space-y-2"><div class="h-3 bg-gray-200 rounded w-full"></div><div class="h-3 bg-gray-200 rounded w-2/3"></div></div></div></div>
        </div>

        <div v-else-if="bestsellerCourses.length > 0" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Featured article -->
          <div v-if="bestsellerCourses[0]" class="lg:col-span-2 group cursor-pointer"
            @click="$router.push('/course/slug/' + bestsellerCourses[0].slug)">
            <div class="relative rounded-2xl overflow-hidden aspect-video bg-gray-900">
              <img :src="bestsellerCourses[0].thumbnail || 'https://placehold.co/800x450/e2e8f0/94a3b8?text=MultiLearn'"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 opacity-80 group-hover:opacity-90"/>
              <div class="absolute inset-0 bg-gradient-to-t from-gray-900/95 via-gray-900/40 to-transparent"></div>
              <div class="absolute top-4 left-4 flex items-center gap-2">
                <span class="bg-amber-400 text-amber-900 text-[10px] font-black px-3 py-1 uppercase tracking-widest">BESTSELLER</span>
                <span class="bg-white/20 backdrop-blur-md text-white text-[10px] font-bold px-3 py-1 uppercase tracking-widest border border-white/20">{{ bestsellerCourses[0].categoryName || 'KHOÁ HỌC' }}</span>
              </div>
              <button @click.stop="toggleWishlist(bestsellerCourses[0].id)" class="absolute top-4 right-4 w-9 h-9 rounded-full flex items-center justify-center transition-all z-10 backdrop-blur-md" :class="wishlistCourseIds.has(bestsellerCourses[0].id) ? 'bg-pink-500 text-white' : 'bg-white/20 text-white hover:bg-white/40'">
                <svg class="w-4 h-4" :fill="wishlistCourseIds.has(bestsellerCourses[0].id) ? 'currentColor':'none'" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/></svg>
              </button>
              <div class="absolute bottom-0 left-0 right-0 p-6">
                <h3 class="text-2xl md:text-3xl font-black text-white leading-tight mb-3 drop-shadow-md group-hover:text-amber-300 transition-colors">{{ bestsellerCourses[0].title }}</h3>
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <img :src="bestsellerCourses[0].instructorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(bestsellerCourses[0].instructorName||'GV')}&background=e0e7ff&color=4f46e5&bold=true&size=40`" class="w-7 h-7 rounded-full border border-white/50 object-cover"/>
                    <div>
                      <p class="text-white/90 text-xs font-bold">{{ bestsellerCourses[0].instructorName }}</p>
                      <p class="text-amber-400 text-[10px] font-black">{{ Number(bestsellerCourses[0].averageRating||0).toFixed(1) }} ★ · {{ Number(bestsellerCourses[0].studentCount||0).toLocaleString('vi-VN') }} học viên</p>
                    </div>
                  </div>
                  <span class="text-white font-black text-lg">{{ formatPrice(bestsellerCourses[0].salePrice || bestsellerCourses[0].price) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Secondary articles -->
          <div class="flex flex-col gap-4">
            <div v-for="(course, idx) in bestsellerCourses.slice(1, 4)" :key="course.id"
              class="group flex gap-4 bg-white rounded-xl border border-gray-200 p-3 hover:border-gray-400 hover:shadow-md transition-all cursor-pointer"
              @click="$router.push('/course/slug/' + course.slug)">
              <div class="relative w-24 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
                <span class="absolute top-1 left-1 bg-black/60 text-white text-[9px] font-black px-1.5 py-0.5 rounded z-10">#{{ idx+2 }}</span>
                <img :src="course.thumbnail || 'https://placehold.co/200x130/e2e8f0/94a3b8?text=EDU'" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"/>
              </div>
              <div class="flex-1 min-w-0">
                <span class="text-[9px] font-black uppercase tracking-widest text-blue-600">{{ course.categoryName || 'KHOÁ HỌC' }}</span>
                <h4 class="text-sm font-bold text-gray-900 line-clamp-2 leading-snug mt-0.5 group-hover:text-blue-600 transition-colors">{{ course.title }}</h4>
                <div class="flex items-center justify-between mt-2">
                  <span class="text-xs font-black text-gray-700">{{ formatPrice(course.salePrice || course.price) }}</span>
                  <span class="text-[10px] text-gray-400">{{ Number(course.studentCount||0).toLocaleString('vi-VN') }} HV</span>
                </div>
              </div>
            </div>

            <div class="mt-auto bg-gray-900 rounded-xl p-4 text-center cursor-pointer hover:bg-gray-800 transition-colors" @click="$router.push('/course')">
              <p class="text-white font-bold text-sm">Khám phá thêm khoá học</p>
              <p class="text-gray-400 text-xs mt-1">→ Xem toàn bộ danh mục</p>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- SECTION: GÓC HỌC TẬP (BLOG POSTS — dữ liệu thật)      -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section>
        <div class="flex items-center gap-3 mb-6">
          <div class="bg-blue-600 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">GÓC HỌC TẬP</div>
          <div class="flex-1 h-px bg-gray-300"></div>
          <router-link to="/blog" class="text-[10px] text-gray-400 font-bold uppercase tracking-widest hover:text-gray-700 transition">Xem tất cả →</router-link>
        </div>

        <!-- Loading -->
        <div v-if="isBlogLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <div v-for="i in 4" :key="i" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-pulse">
            <div class="h-40 bg-gray-200"></div>
            <div class="p-4 space-y-2">
              <div class="h-3 bg-gray-200 rounded w-1/3"></div>
              <div class="h-4 bg-gray-200 rounded w-full"></div>
              <div class="h-4 bg-gray-200 rounded w-4/5"></div>
              <div class="h-3 bg-gray-200 rounded w-1/2"></div>
            </div>
          </div>
        </div>

        <!-- No posts yet -->
        <div v-else-if="blogPosts.length === 0" class="bg-white rounded-xl border border-dashed border-gray-300 p-10 text-center text-gray-400">
          <p class="font-medium">Chưa có bài viết nào.</p>
          <p class="text-sm mt-1">Admin có thể tạo bài viết tự động bằng AI trong trang quản trị.</p>
        </div>

        <!-- Posts grid -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <article v-for="post in blogPosts" :key="post.id"
            class="group bg-white rounded-xl border border-gray-200 overflow-hidden hover:shadow-lg hover:border-gray-400 transition-all cursor-pointer flex flex-col"
            @click="$router.push('/blog/post/' + post.id)">
            <!-- Thumbnail or gradient fallback -->
            <div class="relative h-40 overflow-hidden flex-shrink-0">
              <img v-if="post.thumbnail" :src="post.thumbnail" :alt="post.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"/>
              <div v-else :class="['w-full h-full bg-gradient-to-br flex items-center justify-center', categoryGradient(post.category)]">
                <span class="text-4xl">{{ categoryEmoji(post.category) }}</span>
              </div>
              <div class="absolute top-3 left-3">
                <span class="text-[9px] font-black uppercase tracking-widest px-2 py-1 rounded bg-white/20 text-white border border-white/30 backdrop-blur-sm">
                  {{ post.category }}
                </span>
              </div>
            </div>
            <div class="p-4 flex flex-col flex-1">
              <h3 class="font-black text-gray-900 text-sm leading-snug mb-2 group-hover:text-blue-600 transition-colors line-clamp-2">{{ post.title }}</h3>
              <p class="text-xs text-gray-500 leading-relaxed line-clamp-3 flex-1">{{ post.excerpt }}</p>
              <div class="flex items-center justify-between mt-4 pt-3 border-t border-gray-100">
                <span class="text-[10px] text-gray-400 font-medium">{{ formatDate(post.publishedAt) }}</span>
                <span class="text-[10px] font-bold text-gray-500">{{ post.readTime }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- SECTION: KHOÁ HỌC MỚI + ĐÁNH GIÁ CAO (2 cột)          -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section id="section-new" class="grid grid-cols-1 lg:grid-cols-2 gap-10">

        <!-- Vừa ra mắt -->
        <div>
          <div class="flex items-center gap-3 mb-5">
            <div class="bg-emerald-600 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">VỪA RA MẮT</div>
            <div class="flex-1 h-px bg-gray-300"></div>
          </div>
          <div v-if="isNewestLoading" class="space-y-4">
            <div v-for="i in 4" :key="i" class="flex gap-3 animate-pulse"><div class="w-20 h-14 bg-gray-200 rounded-lg flex-shrink-0"></div><div class="flex-1 space-y-2 py-1"><div class="h-3 bg-gray-200 rounded w-full"></div><div class="h-3 bg-gray-200 rounded w-1/2"></div></div></div>
          </div>
          <div v-else class="space-y-0 divide-y divide-gray-200 bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div v-for="course in newestCourses" :key="course.id"
              class="group flex gap-4 p-4 hover:bg-gray-50 transition-colors cursor-pointer"
              @click="$router.push('/course/slug/' + course.slug)">
              <div class="relative w-20 h-14 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
                <span class="absolute top-1 left-1 z-10 bg-emerald-600 text-white text-[8px] font-black px-1.5 py-0.5 rounded uppercase">Mới</span>
                <img :src="course.thumbnail || 'https://placehold.co/200x130'" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" loading="lazy"/>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="text-[9px] font-black uppercase tracking-widest text-emerald-600">{{ course.categoryName || 'KHOÁ HỌC' }}</span>
                </div>
                <h4 class="font-bold text-gray-900 text-sm line-clamp-2 leading-snug group-hover:text-emerald-600 transition-colors">{{ course.title }}</h4>
                <div class="flex items-center justify-between mt-2">
                  <span class="text-xs text-gray-500">{{ course.instructorName }}</span>
                  <span class="text-xs font-black text-gray-700">{{ formatPrice(course.salePrice || course.price) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Đánh giá cao nhất -->
        <div>
          <div class="flex items-center gap-3 mb-5">
            <div class="bg-amber-500 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">ĐÁNH GIÁ CAO</div>
            <div class="flex-1 h-px bg-gray-300"></div>
            <router-link to="/course?sort=averageRating,desc" class="text-xs font-bold text-gray-500 hover:text-gray-900 transition">Xem thêm →</router-link>
          </div>
          <div v-if="isTopRatedLoading" class="space-y-4">
            <div v-for="i in 4" :key="i" class="flex gap-3 animate-pulse"><div class="w-20 h-14 bg-gray-200 rounded-lg flex-shrink-0"></div><div class="flex-1 space-y-2 py-1"><div class="h-3 bg-gray-200 rounded w-full"></div><div class="h-3 bg-gray-200 rounded w-1/2"></div></div></div>
          </div>
          <div v-else class="space-y-0 divide-y divide-gray-200 bg-white rounded-xl border border-gray-200 overflow-hidden">
            <div v-for="course in topRatedCourses.slice(0,4)" :key="course.id"
              class="group flex gap-4 p-4 hover:bg-gray-50 transition-colors cursor-pointer"
              @click="$router.push('/course/slug/' + course.slug)">
              <div class="relative w-20 h-14 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
                <img :src="course.thumbnail || 'https://placehold.co/200x130'" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"/>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-1.5 mb-1">
                  <span class="text-[9px] font-black uppercase tracking-widest text-amber-500">{{ course.categoryName || 'KHOÁ HỌC' }}</span>
                  <span class="text-[10px] font-black text-amber-500">{{ Number(course.averageRating||0).toFixed(1) }} ★</span>
                </div>
                <h4 class="font-bold text-gray-900 text-sm line-clamp-2 leading-snug group-hover:text-amber-600 transition-colors">{{ course.title }}</h4>
                <div class="flex items-center justify-between mt-2">
                  <span class="text-xs text-gray-500">{{ course.instructorName }}</span>
                  <span class="text-xs font-black text-gray-700">{{ formatPrice(course.salePrice || course.price) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- SECTION: GIẢNG VIÊN (editorial style)                  -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section id="section-instructors">
        <div class="flex items-center gap-3 mb-6">
          <div class="bg-purple-700 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">GIẢNG VIÊN</div>
          <div class="flex-1 h-px bg-gray-300"></div>
          <span class="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Xếp hạng theo học viên</span>
        </div>

        <div v-if="isInstructorsLoading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-5">
          <div v-for="i in 6" :key="i" class="bg-white rounded-xl border border-gray-200 p-5 animate-pulse text-center">
            <div class="w-16 h-16 bg-gray-200 rounded-full mx-auto mb-3"></div>
            <div class="h-3 bg-gray-200 rounded w-3/4 mx-auto mb-2"></div>
            <div class="h-3 bg-gray-200 rounded w-1/2 mx-auto"></div>
          </div>
        </div>

        <div v-else-if="topInstructors.length === 0" class="text-center py-12 bg-white rounded-xl border border-dashed border-gray-300">
          <p class="text-gray-400 font-medium">Chưa có dữ liệu giảng viên.</p>
        </div>

        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <div v-for="(ins, idx) in topInstructors" :key="ins.id"
            class="group bg-white rounded-xl border border-gray-200 p-5 text-center hover:shadow-lg hover:border-purple-300 hover:-translate-y-1 transition-all duration-200 cursor-pointer relative overflow-hidden"
            @click="$router.push({ path: '/course', query: { instructorId: ins.id } })">
            <!-- Rank badge -->
            <div v-if="idx < 3" class="absolute top-3 right-3 w-6 h-6 rounded-full text-[10px] font-black flex items-center justify-center shadow border-2 border-white"
              :class="idx===0?'bg-yellow-400 text-yellow-900':idx===1?'bg-gray-300 text-gray-700':'bg-orange-400 text-orange-900'">{{ idx+1 }}</div>

            <div class="relative inline-block mb-3">
              <img :src="ins.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(ins.fullName||'GV')}&background=ede9fe&color=7c3aed&bold=true&size=100`"
                class="w-16 h-16 rounded-full object-cover ring-2 ring-purple-200 group-hover:ring-purple-400 transition-all mx-auto shadow-sm"/>
            </div>
            <h3 class="text-xs font-black text-gray-900 line-clamp-1 group-hover:text-purple-700 transition-colors mb-3">{{ ins.fullName || 'Giảng viên' }}</h3>
            <div class="grid grid-cols-2 gap-1 border-t border-gray-100 pt-3">
              <div>
                <p class="text-sm font-black text-gray-900">{{ ins.totalCourses }}</p>
                <p class="text-[9px] text-gray-400 font-bold uppercase">Khoá</p>
              </div>
              <div class="border-l border-gray-100">
                <p class="text-sm font-black text-gray-900">{{ ins.totalStudents >= 1000 ? (ins.totalStudents/1000).toFixed(1)+'K' : ins.totalStudents }}</p>
                <p class="text-[9px] text-gray-400 font-bold uppercase">Học viên</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════ -->
      <!-- CTA BANNER                                              -->
      <!-- ═══════════════════════════════════════════════════════ -->
      <section class="bg-gray-900 rounded-2xl p-8 md:p-12 text-white relative overflow-hidden shadow-2xl">
        <div class="absolute inset-0 opacity-10" style="background-image:radial-gradient(circle at 2px 2px,#fff 1px,transparent 0);background-size:24px 24px;"></div>
        <div class="absolute -right-20 -bottom-40 w-96 h-96 bg-blue-600 rounded-full blur-[100px] opacity-30"></div>
        <div class="absolute -left-20 -top-40 w-96 h-96 bg-purple-600 rounded-full blur-[100px] opacity-20"></div>
        <div class="relative z-10 text-center max-w-2xl mx-auto">
          <p class="text-blue-400 text-xs font-black uppercase tracking-widest mb-4">🎓 Sẵn sàng vươn xa?</p>
          <h2 class="text-3xl md:text-4xl font-black leading-tight mb-5 tracking-tight">
            Đầu tư vào bản thân là khoản đầu tư sinh lời nhất.
          </h2>
          <p class="text-gray-300 leading-relaxed mb-8 text-base">
            Hàng nghìn khoá học chất lượng cao đang chờ bạn. Bắt đầu hành trình học tập ngay hôm nay.
          </p>
          <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
            <router-link to="/course" class="w-full sm:w-auto flex items-center justify-center gap-2 px-8 py-3.5 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-xl transition-all shadow-lg active:scale-95">
              🔍 Khám phá khoá học
            </router-link>
            <router-link to="/become-instructor" class="w-full sm:w-auto flex items-center justify-center gap-2 px-8 py-3.5 bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white font-bold rounded-xl transition-all border border-white/20">
              🎙️ Trở thành giảng viên
            </router-link>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import blogService from '@/services/blog.service';

const router = useRouter();

// ── BLOG POSTS ────────────────────────────────────────────────
const blogPosts    = ref([]);
const isBlogLoading = ref(true);

const categoryGradient = (cat) => {
  const map = {
    'XU HƯỚNG':    'from-blue-600 to-cyan-500',
    'MẸO HỌC TẬP': 'from-emerald-500 to-teal-600',
    'SỰ NGHIỆP':   'from-purple-600 to-violet-700',
    'KIẾN THỨC':   'from-amber-500 to-orange-600',
    'NỔI BẬT':     'from-rose-500 to-pink-600',
  };
  return map[cat] || 'from-gray-600 to-gray-800';
};

const categoryEmoji = (cat) => {
  const map = {
    'XU HƯỚNG':    '📈',
    'MẸO HỌC TẬP': '🎯',
    'SỰ NGHIỆP':   '🚀',
    'KIẾN THỨC':   '💡',
    'NỔI BẬT':     '⭐',
  };
  return map[cat] || '📰';
};

const fetchBlogPosts = async () => {
  isBlogLoading.value = true;
  try {
    const res = await blogService.getPosts({ page: 0, size: 4 });
    blogPosts.value = res?.content || [];
  } catch {
    blogPosts.value = [];
  } finally {
    isBlogLoading.value = false;
  }
};

// ── AUTH ─────────────────────────────────────────────────────
const currentUser = ref(null);
const isAdmin     = computed(() => currentUser.value?.role === 'ADMIN');

// ── LOADING ──────────────────────────────────────────────────
const isStatsLoading       = ref(true);
const isLiveStatsLoading   = ref(true);
const isCouponsLoading     = ref(true);
const isBestsellerLoading  = ref(true);
const isNewestLoading      = ref(true);
const isTopRatedLoading    = ref(true);
const isInstructorsLoading = ref(true);
const copiedCode           = ref(null);

// ── DATA ─────────────────────────────────────────────────────
const stats             = ref({ totalStudents:0, totalCourses:0, totalInstructors:0, averageRating:0 });
const liveStats         = ref({ newEnrollmentsToday:0, completedOrdersToday:0, totalReviews:0, avgRatingToday:0 });
const coupons           = ref([]);

const activeCampaign         = ref(null);
const campaignItems          = ref([]);
const isCampaignItemsLoading = ref(false);
const sliderRef              = ref(null);
let sliderTimer              = null;

const bestsellerCourses = ref([]);
const newestCourses     = ref([]);
const topRatedCourses   = ref([]);
const topInstructors    = ref([]);

const cartCourseIds      = ref(new Set());
const wishlistCourseIds  = ref(new Set());
const purchasedCourseIds = ref(new Set());

const countdown         = ref('');
const campaignCountdown = ref('');
let countdownTimer      = null;

const activeCoupons = computed(() => {
  const now = new Date();
  return coupons.value.filter(c => {
    if (c.active === false) return false;
    if (c.isActive === false) return false;
    if (c.endDate && new Date(c.endDate) < now) return false;
    if (c.remainingCount !== null && c.remainingCount !== undefined && c.remainingCount <= 0) return false;
    return true;
  });
});

const featuredCoupons = computed(() =>
  [...activeCoupons.value]
    .sort((a, b) => {
      const aHasEnd = a.endDate ? 0 : 1;
      const bHasEnd = b.endDate ? 0 : 1;
      if (aHasEnd !== bHasEnd) return aHasEnd - bHasEnd;
      if (a.endDate && b.endDate) return new Date(a.endDate) - new Date(b.endDate);
      return Number(b.discountValue || 0) - Number(a.discountValue || 0);
    })
    .slice(0, 4)
);

const updateCountdown = () => {
  const now = new Date().getTime();
  if (activeCampaign.value && activeCampaign.value.endDate) {
    const diff = new Date(activeCampaign.value.endDate).getTime() - now;
    if (diff <= 0) { campaignCountdown.value = '00:00:00'; }
    else {
      const d = Math.floor(diff/86400000), h = Math.floor((diff%86400000)/3600000);
      const m = Math.floor((diff%3600000)/60000), s = Math.floor((diff%60000)/1000);
      campaignCountdown.value = d > 0 ? `${d}n ${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}` : `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
    }
  }
  if (activeCoupons.value.length > 0) {
    const expiring = activeCoupons.value.filter(c => c.endDate);
    if (expiring.length > 0) {
      const earliest = Math.min(...expiring.map(c => new Date(c.endDate).getTime()));
      const diff = earliest - now;
      if (diff <= 0) { countdown.value = '00:00:00'; }
      else {
        const d = Math.floor(diff/86400000), h = Math.floor((diff%86400000)/3600000);
        const m = Math.floor((diff%3600000)/60000), s = Math.floor((diff%60000)/1000);
        countdown.value = d > 0 ? `${d}n ${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}` : `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
      }
    } else { countdown.value = ''; }
  } else { countdown.value = ''; }
};

const today = new Date().toLocaleDateString('vi-VN', { weekday:'long', day:'2-digit', month:'2-digit', year:'numeric' });
const tabs = [
  { id: 'section-sale',        label: 'Khuyến Mãi' },
  { id: 'section-bestseller',  label: 'Nổi Bật' },
  { id: 'section-new',         label: 'Mới Nhất' },
  { id: 'section-instructors', label: 'Giảng Viên' }
];
const activeSection = ref('section-sale');
const scrollToSection = (id) => {
  document.getElementById(id)?.scrollIntoView({ behavior:'smooth', block:'start' });
  activeSection.value = id;
};

const scrollSlider = (direction) => {
  if (!sliderRef.value) return;
  sliderRef.value.scrollBy({ left: direction === 'left' ? -320 : 320, behavior:'smooth' });
};

const startSlider = () => {
  if (sliderTimer) clearInterval(sliderTimer);
  sliderTimer = setInterval(() => {
    if (sliderRef.value) {
      const { scrollLeft, scrollWidth, clientWidth } = sliderRef.value;
      if (scrollLeft + clientWidth >= scrollWidth - 10) sliderRef.value.scrollTo({ left:0, behavior:'smooth' });
      else sliderRef.value.scrollBy({ left:320, behavior:'smooth' });
    }
  }, 3000);
};
const pauseSlider = () => { if (sliderTimer) clearInterval(sliderTimer); };

const fetchUserData = async () => {
  if (!currentUser.value || currentUser.value.role === 'ADMIN' || currentUser.value.role === 'INSTRUCTOR') return;
  const uid = currentUser.value.id || currentUser.value.userId;
  if (!uid) return;
  try {
    const cartRes = await axiosClient.get(`/cart/${uid}`);
    const cartItems = Array.isArray(cartRes) ? cartRes : (cartRes?.cartItems || []);
    cartCourseIds.value = new Set(cartItems.map(item => item.courseId || item.course?.id));
    const wlRes = await axiosClient.get(`/wishlist/user/${uid}`);
    const wlItems = Array.isArray(wlRes) ? wlRes : (wlRes?.content || []);
    wishlistCourseIds.value = new Set(wlItems.map(item => item.courseId || item.course?.id));
    try {
      const enRes = await axiosClient.get(`/enrollments/my-courses`);
      const enItems = Array.isArray(enRes) ? enRes : (enRes?.content || []);
      purchasedCourseIds.value = new Set(enItems.map(item => item.id || item.courseId || item.course?.id));
    } catch(e) { console.warn('Lỗi lấy khoá học đã mua', e); }
  } catch (e) { console.warn('Lỗi tải dữ liệu User:', e); }
};

const fetchStats = async () => {
  isStatsLoading.value = true;
  try {
    const res = await axiosClient.get('/public/stats');
    if (res) { stats.value.totalStudents = res.totalStudents ?? 0; stats.value.totalCourses = res.totalCourses ?? 0; stats.value.totalInstructors = res.totalInstructors ?? 0; stats.value.averageRating = res.averageRating ?? 0; }
  } catch {} finally { isStatsLoading.value = false; }
};

const fetchLiveStats = async () => {
  isLiveStatsLoading.value = true;
  try {
    const res = await axiosClient.get('/public/live-stats');
    if (res) { liveStats.value.newEnrollmentsToday = res.newEnrollmentsToday ?? 0; liveStats.value.completedOrdersToday = res.completedOrdersToday ?? 0; liveStats.value.totalReviews = res.totalReviews ?? 0; liveStats.value.avgRatingToday = res.avgRatingToday ?? 0; }
  } catch {} finally { isLiveStatsLoading.value = false; }
};

const fetchActiveCampaignAndItems = async () => {
  isCampaignItemsLoading.value = true;
  try {
    const res = await axiosClient.get('/campaigns/active');
    if (res && res.id) {
      activeCampaign.value = res;
      const itemsRes = await axiosClient.get('/campaigns/active/items');
      campaignItems.value = itemsRes || [];
      const maxDiscount = (campaignItems.value || []).reduce((max, course) => {
        if (course.salePrice && course.price && Number(course.price) > 0) {
          const pct = Math.round((1 - Number(course.salePrice) / Number(course.price)) * 100);
          return pct > max ? pct : max;
        }
        return max;
      }, 0);
      if (maxDiscount > 0) window.dispatchEvent(new CustomEvent('flash-sale-active', { detail: { percent: maxDiscount, name: res.name || 'Flash Sale' } }));
      else window.dispatchEvent(new Event('flash-sale-ended'));
      setTimeout(startSlider, 500);
    } else {
      activeCampaign.value = null;
      window.dispatchEvent(new Event('flash-sale-ended'));
    }
  } catch { activeCampaign.value = null; window.dispatchEvent(new Event('flash-sale-ended')); }
  finally { isCampaignItemsLoading.value = false; }
};

const fetchCoupons = async () => {
  isCouponsLoading.value = true;
  try {
    const res = await axiosClient.get('/public/coupons/active');
    if (Array.isArray(res)) coupons.value = res;
    else if (res && Array.isArray(res.data)) coupons.value = res.data;
    else coupons.value = [];
  } catch { coupons.value = []; } finally { isCouponsLoading.value = false; }
};

const fetchBestsellers = async () => {
  isBestsellerLoading.value = true;
  try {
    const res = await axiosClient.get('/public/courses', { params: { sort:'studentCount,desc', size:10, page:0 } });
    let list = res?.content ?? (Array.isArray(res) ? res : []);
    list.sort((a, b) => (b.studentCount || 0) - (a.studentCount || 0));
    bestsellerCourses.value = list.slice(0, 4);
  } catch { bestsellerCourses.value = []; } finally { isBestsellerLoading.value = false; }
};

const fetchNewest = async () => {
  isNewestLoading.value = true;
  try {
    const res = await axiosClient.get('/public/courses', { params: { sort:'id,desc', size:10, page:0 } });
    let list = res?.content ?? (Array.isArray(res) ? res : []);
    list.sort((a, b) => (b.id || 0) - (a.id || 0));
    newestCourses.value = list.slice(0, 4);
  } catch { newestCourses.value = []; } finally { isNewestLoading.value = false; }
};

const fetchTopRated = async () => {
  isTopRatedLoading.value = true;
  try {
    const res = await axiosClient.get('/public/courses', { params: { sort:'averageRating,desc', size:10, page:0 } });
    let list = res?.content ?? (Array.isArray(res) ? res : []);
    list.sort((a, b) => (b.averageRating || 0) - (a.averageRating || 0));
    topRatedCourses.value = list.slice(0, 4);
  } catch { topRatedCourses.value = []; } finally { isTopRatedLoading.value = false; }
};

const fetchTopInstructors = async () => {
  isInstructorsLoading.value = true;
  try {
    const res = await axiosClient.get('/public/instructors/top', { params: { size:6 } });
    if (Array.isArray(res)) topInstructors.value = res;
    else if (res && Array.isArray(res.data)) topInstructors.value = res.data;
    else topInstructors.value = [];
  } catch { topInstructors.value = []; } finally { isInstructorsLoading.value = false; }
};


const toggleWishlist = async (courseId) => {
  if (!currentUser.value) { router.push('/login'); return; }
  const uid = currentUser.value.id || currentUser.value.userId;
  try {
    if (wishlistCourseIds.value.has(courseId)) {
      await axiosClient.delete(`/wishlist/remove`, { params: { studentId: uid, courseId } });
      wishlistCourseIds.value.delete(courseId);
      window.__notify?.auto('Đã bỏ yêu thích.');
    } else {
      await axiosClient.post(`/wishlist/add`, null, { params: { studentId: uid, courseId } });
      wishlistCourseIds.value.add(courseId);
      window.__notify?.auto('❤️ Đã thêm vào yêu thích!');
    }
  } catch { window.__notify?.auto('❌ Lỗi cập nhật yêu thích'); }
};

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code);
    copiedCode.value = code;
    setTimeout(() => { copiedCode.value = null; }, 2000);
    window.__notify?.auto('✅ Đã sao chép mã giảm giá!');
  } catch { window.__notify?.auto('❌ Không thể sao chép'); }
};

const formatNumber  = (n) => n ? new Intl.NumberFormat('vi-VN').format(n) : '0';
const formatPrice   = (val) => { const n = Number(val); if (!n) return 'Miễn phí'; return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(n); };
const formatPriceShort = (val) => { const n = Number(val); if (!n) return 'Miễn phí'; if (n >= 1000000) return (n/1000000).toFixed(n%1000000===0?0:1)+'M'; if (n >= 1000) return (n/1000).toFixed(0)+'K'; return new Intl.NumberFormat('vi-VN', { style:'currency', currency:'VND' }).format(n); };
const formatDate    = (d) => d ? new Date(d).toLocaleDateString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric' }) : 'Không giới hạn';
const usagePercent  = (c) => !c.usageLimit ? 0 : Math.min(100, Math.round(((c.usedCount||0)/c.usageLimit)*100));

onMounted(async () => {
  const userStr = localStorage.getItem('user_info');
  const role    = localStorage.getItem('user_role');
  if (userStr) { try { currentUser.value = JSON.parse(userStr); if (role) currentUser.value.role = role; } catch {} }
  await fetchUserData();
  fetchStats();
  fetchLiveStats();
  await fetchActiveCampaignAndItems();
  if (!activeCampaign.value) await fetchCoupons();
  updateCountdown();
  countdownTimer = setInterval(updateCountdown, 1000);
  fetchBestsellers();
  fetchNewest();
  fetchTopRated();
  fetchTopInstructors();
  fetchBlogPosts();
});

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (sliderTimer) clearInterval(sliderTimer);
});
</script>

<style scoped>
.hide-scrollbar::-webkit-scrollbar { display: none; }
.hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
