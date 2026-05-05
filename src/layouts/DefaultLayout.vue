<template>
  <div class="min-h-screen flex flex-col bg-white text-gray-900"
    style="font-family:'DM Sans','Segoe UI',system-ui,sans-serif;" @click="handleGlobalClick">

    <div v-if="showAnnouncement && maxFlashSalePercent > 0"
      class="relative z-[200] overflow-hidden text-center py-2.5 px-12 text-xs sm:text-sm font-semibold text-white"
      style="background:linear-gradient(90deg,#1d2a6e,#2563eb 40%,#7c3aed 70%,#1d2a6e);">
      <span class="inline-flex items-center gap-2 flex-wrap justify-center">
        <span class="animate-pulse text-yellow-300">🔥</span>
        {{ flashSaleName }} — Giảm tới <strong class="text-yellow-300">{{ maxFlashSalePercent }}%</strong> tất cả khoá học!
        <router-link to="/course"
          class="inline-flex items-center gap-1 underline underline-offset-2 hover:text-yellow-300 font-black transition-colors">
          Xem ngay
          <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M9 5l7 7-7 7" />
          </svg>
        </router-link>
      </span>
      <button @click.stop="showAnnouncement = false"
        class="absolute right-4 top-1/2 -translate-y-1/2 text-white/50 hover:text-white transition-colors p-1 rounded hover:bg-white/10">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <header class="sticky top-0 z-[100] border-b transition-all duration-300"
      :class="isScrolled ? 'bg-white/97 backdrop-blur-xl shadow-md shadow-black/6 border-gray-200' : 'bg-white border-gray-200'"
      @click.stop>
      <div class="max-w-[1340px] mx-auto px-4 sm:px-6">
        <div class="flex items-center h-[68px] gap-2 lg:gap-3">

          <router-link to="/" class="flex items-center gap-2 group select-none flex-shrink-0 mr-1">
            <img src="/logo.png" alt="MultiLearn"
              class="w-9 h-9 rounded-[10px] shadow-lg transition-all group-hover:scale-105 object-cover" />
            <span class="text-[22px] font-black tracking-tight hidden sm:block leading-none">
              <span class="text-gray-900">Multi</span><span style="color:#2563eb;">Learn</span>
            </span>
          </router-link>

          <div class="hidden lg:flex relative h-[68px] items-center flex-shrink-0" @mouseenter="isMegaMenuOpen = true"
            @mouseleave="isMegaMenuOpen = false; activeCategoryId = null">
            <button class="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-semibold transition-all border"
              :class="isMegaMenuOpen ? 'text-blue-600 bg-blue-50 border-blue-200' : 'text-gray-700 border-transparent hover:bg-gray-100 hover:border-gray-200'">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h10M4 18h7" />
              </svg>
              Danh mục
              <svg class="w-3 h-3 transition-transform duration-200"
                :class="isMegaMenuOpen ? 'rotate-180 text-blue-500' : 'text-gray-400'" fill="none" stroke="currentColor"
                viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <Transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 translate-y-2"
              enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-100 ease-in"
              leave-from-class="opacity-100" leave-to-class="opacity-0">
              <div v-show="isMegaMenuOpen"
                class="absolute top-[67px] left-0 flex shadow-2xl shadow-black/12 border border-gray-200 rounded-b-2xl overflow-hidden bg-white z-[101]"
                style="min-height:400px;min-width:560px;">

                <div class="w-[240px] bg-white border-r border-gray-100 py-2 flex-shrink-0 overflow-y-auto"
                  style="max-height:460px;">
                  <p class="px-4 pb-2 pt-1 text-[10px] font-black text-gray-400 uppercase tracking-widest">Tất cả danh
                    mục</p>
                  <div v-if="categoriesTree.length === 0">
                    <div v-for="i in 6" :key="i"
                      class="mx-2 mb-1 px-3 py-2.5 rounded-lg animate-pulse flex gap-2 items-center">
                      <div class="w-6 h-6 bg-gray-200 rounded-lg flex-shrink-0"></div>
                      <div class="h-3 bg-gray-200 rounded flex-1"></div>
                    </div>
                  </div>
                  <div v-for="cat in categoriesTree" :key="cat.id" @mouseenter="activeCategoryId = cat.id"
                    @click="goToCategory(cat.id)"
                    class="mx-2 px-3 py-2.5 flex items-center justify-between cursor-pointer rounded-xl transition-all"
                    :class="activeCategoryId === cat.id ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100 hover:text-blue-600'">
                    <div class="flex items-center gap-2.5">
                      <img v-if="cat.iconUrl" :src="cat.iconUrl" class="w-5 h-5 object-contain rounded flex-shrink-0"
                        :alt="cat.name" />
                      <span v-else class="text-base w-5 text-center flex-shrink-0">📁</span>
                      <span class="text-sm font-semibold leading-tight">{{ cat.name }}</span>
                    </div>
                    <svg v-if="cat.children?.length" class="w-3.5 h-3.5 flex-shrink-0 opacity-50" fill="none"
                      stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                  </div>
                </div>

                <div class="flex-1 bg-gray-50/60 py-3 px-1 min-w-[300px]">
                  <template v-if="activeCategory">
                    <p
                      class="px-4 pb-2 text-[10px] font-black text-gray-400 uppercase tracking-widest flex items-center gap-1.5">
                      <img v-if="activeCategory.iconUrl" :src="activeCategory.iconUrl"
                        class="w-4 h-4 object-contain rounded" />
                      <span v-else>📁</span>
                      {{ activeCategory.name }}
                    </p>
                    <div class="overflow-y-auto" style="max-height:400px;">
                      <div v-for="child in activeCategory.children" :key="child.id" @click.stop="goToCategory(child.id)"
                        class="flex items-center justify-between mx-2 px-3 py-2.5 rounded-xl cursor-pointer text-sm text-gray-600 hover:bg-white hover:text-blue-600 hover:shadow-sm transition-all font-medium group">
                        <span>{{ child.name }}</span>
                        <span v-if="child.courseCount"
                          class="text-[10px] text-gray-400 bg-gray-100 group-hover:bg-blue-100 group-hover:text-blue-600 px-2 py-0.5 rounded-full font-bold transition-colors">
                          {{ child.courseCount }}
                        </span>
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <div class="flex flex-col items-center justify-center h-full gap-3 text-gray-400 py-16">
                      <div class="w-12 h-12 rounded-2xl bg-gray-100 flex items-center justify-center text-2xl">🔍</div>
                      <p class="text-sm font-medium text-center px-4">Di chuột vào danh mục<br />để xem khoá học</p>
                    </div>
                  </template>
                </div>
              </div>
            </Transition>
          </div>

          <div class="hidden md:flex flex-1 max-w-2xl mx-2 lg:mx-3">
            <div class="relative w-full">
              <button @click="handleSearch"
                class="absolute left-0 top-0 h-full w-11 flex items-center justify-center text-gray-400 hover:text-blue-600 transition-colors z-10">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </button>
              <input v-model="searchQuery" @keyup.enter="handleSearch" @focus="isSearchFocused = true"
                @blur="isSearchFocused = false" type="text"
                :placeholder="$t?.('search_placeholder') || 'Tìm kiếm khoá học, giảng viên, kỹ năng...'"
                class="w-full pl-11 pr-10 py-[9px] rounded-xl border-2 outline-none text-sm font-medium placeholder-gray-400 transition-all duration-200 bg-gray-50"
                :class="isSearchFocused ? 'border-blue-500 bg-white ring-4 ring-blue-500/10 shadow-sm' : 'border-gray-300 hover:border-gray-400'" />
              <button v-if="searchQuery" @click="searchQuery = ''"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clip-rule="evenodd" />
                </svg>
              </button>
            </div>
          </div>

          <nav class="hidden xl:flex items-center gap-0.5 flex-shrink-0 text-sm font-semibold">

            <div class="relative h-[68px] flex items-center" @mouseenter="isCoursesMenuOpen = true"
              @mouseleave="isCoursesMenuOpen = false">
              <router-link to="/course" class="flex items-center gap-1 px-3 py-2 rounded-lg transition-all"
                :class="isCoursesMenuOpen ? 'text-blue-600 bg-blue-50' : 'text-gray-700 hover:text-blue-600 hover:bg-gray-100'">
                Khoá học
                <svg class="w-3 h-3 transition-transform duration-200"
                  :class="isCoursesMenuOpen ? 'rotate-180 text-blue-500' : 'text-gray-400'" fill="none"
                  stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
                </svg>
              </router-link>
              <Transition enter-active-class="transition duration-150 ease-out"
                enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100"
                leave-to-class="opacity-0">
                <div v-show="isCoursesMenuOpen"
                  class="absolute top-[67px] left-0 bg-white rounded-b-2xl shadow-2xl shadow-black/10 border border-gray-200 p-5 z-[101]"
                  style="min-width:520px;">
                  <div class="grid grid-cols-2 gap-4">

                    <div>
                      <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-3">Nổi bật</p>
                      <div class="space-y-0.5">
                        <router-link v-for="item in quickCourseLinks" :key="item.sortKey"
                          :to="{ path: '/course', query: item.query }"
                          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-gray-700 hover:bg-blue-50 hover:text-blue-700 transition-all"
                          @click="isCoursesMenuOpen = false">
                          <span class="text-xl w-6 text-center flex-shrink-0">{{ item.icon }}</span>
                          <div>
                            <p class="font-semibold leading-tight">{{ item.label }}</p>
                            <p class="text-xs text-gray-400 mt-0.5">{{ item.sub }}</p>
                          </div>
                        </router-link>
                      </div>
                    </div>

                    <div>
                      <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-3">Theo lĩnh vực</p>

                      <div v-if="isCatsLoading" class="space-y-1">
                        <div v-for="i in 5" :key="i"
                          class="flex items-center gap-2 px-3 py-2.5 rounded-xl animate-pulse">
                          <div class="w-1.5 h-1.5 bg-gray-200 rounded-full flex-shrink-0"></div>
                          <div class="h-3.5 bg-gray-200 rounded w-32"></div>
                        </div>
                      </div>

                      <div v-else class="space-y-0.5">
                        <router-link v-for="cat in categoriesTree.slice(0, 6)" :key="cat.id"
                          :to="{ path: '/course', query: { categoryId: cat.id } }"
                          class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm text-gray-600 hover:bg-gray-100 hover:text-blue-700 transition-all font-medium group"
                          @click="isCoursesMenuOpen = false">
                          <img v-if="cat.iconUrl" :src="cat.iconUrl" :alt="cat.name"
                            class="w-4 h-4 object-contain rounded-sm flex-shrink-0" />
                          <span v-else class="w-1.5 h-1.5 rounded-full bg-blue-400 flex-shrink-0"></span>
                          <span class="truncate">{{ cat.name }}</span>
                          <span v-if="cat.courseCount"
                            class="ml-auto text-[10px] text-gray-400 bg-gray-100 group-hover:bg-blue-100 group-hover:text-blue-600 px-1.5 py-0.5 rounded-full font-bold transition-colors flex-shrink-0">
                            {{ cat.courseCount }}
                          </span>
                        </router-link>

                        <router-link v-if="categoriesTree.length > 6" to="/course"
                          class="flex items-center gap-2 px-3 py-2 rounded-xl text-xs font-bold text-blue-600 hover:bg-blue-50 transition-colors"
                          @click="isCoursesMenuOpen = false">
                          Xem tất cả {{ categoriesTree.length }} danh mục →
                        </router-link>
                      </div>

                      <div class="mt-3 p-3 bg-blue-50 rounded-xl border border-blue-100">
                        <p class="text-xs font-bold text-blue-800">🎓 Học miễn phí hôm nay</p>
                        <p class="text-xs text-blue-600 mt-0.5">Khoá học 0đ đang chờ bạn</p>
                        <router-link :to="{ path: '/course', query: { free: 'true' } }"
                          class="inline-block mt-2 text-xs font-bold text-white bg-blue-600 px-3 py-1.5 rounded-lg hover:bg-blue-700 transition-colors"
                          @click="isCoursesMenuOpen = false">
                          Khám phá →
                        </router-link>
                      </div>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <div class="relative h-[68px] flex items-center" @mouseenter="isBlogMenuOpen = true"
              @mouseleave="isBlogMenuOpen = false">
              <router-link to="/blog" class="flex items-center gap-1 px-3 py-2 rounded-lg transition-all"
                :class="isBlogMenuOpen ? 'text-blue-600 bg-blue-50' : 'text-gray-700 hover:text-blue-600 hover:bg-gray-100'">
                Tin tức
                <svg class="w-3 h-3 transition-transform duration-200"
                  :class="isBlogMenuOpen ? 'rotate-180 text-blue-500' : 'text-gray-400'" fill="none"
                  stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
                </svg>
              </router-link>
              <Transition enter-active-class="transition duration-150 ease-out"
                enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100"
                leave-to-class="opacity-0">
                <div v-show="isBlogMenuOpen"
                  class="absolute top-[67px] left-0 w-72 bg-white rounded-b-2xl shadow-2xl shadow-black/10 border border-gray-200 p-4 z-[101]">
                  <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-3">Chủ đề</p>
                  <div class="space-y-0.5 mb-4">
                    <router-link v-for="item in blogTopics" :key="item.to" :to="item.to"
                      class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-gray-700 hover:bg-gray-100 transition-all"
                      @click="isBlogMenuOpen = false">
                      <span class="text-lg w-6 text-center">{{ item.icon }}</span>
                      <div>
                        <p class="font-semibold leading-tight">{{ item.label }}</p>
                        <p class="text-xs text-gray-400">{{ item.sub }}</p>
                      </div>
                    </router-link>
                  </div>
                  <div v-if="latestPosts.length" class="border-t border-gray-100 pt-3">
                    <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2">Bài mới nhất</p>
                    <router-link
                      v-for="post in latestPosts"
                      :key="post.id"
                      :to="`/blog/post/${post.id}`"
                      class="flex gap-3 p-2 rounded-xl hover:bg-gray-50 transition-colors group"
                      @click="isBlogMenuOpen = false">
                      <img :src="post.thumbnail" :alt="post.title"
                        class="w-14 h-10 rounded-lg object-cover flex-shrink-0" />
                      <div class="min-w-0">
                        <p class="text-xs font-semibold text-gray-800 line-clamp-2 group-hover:text-blue-600 transition-colors">
                          {{ post.title }}</p>
                        <p class="text-[10px] text-gray-400 mt-0.5">{{ timeAgo(post.publishedAt) }}</p>
                      </div>
                    </router-link>
                  </div>
                </div>
              </Transition>
            </div>

            <router-link v-if="isLoggedIn && currentRole !== 'ADMIN'" to="/my-courses"
              active-class="text-blue-600 bg-blue-50"
              class="flex items-center gap-1.5 px-3 py-2 rounded-lg text-gray-700 hover:text-blue-600 hover:bg-blue-50 transition-all whitespace-nowrap">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 14l9-5-9-5-9 5 9 5zm0 0v6" />
              </svg>
              Học tập
            </router-link>

            <router-link v-if="isLoggedIn && currentUser.role === 'INSTRUCTOR'" to="/instructor"
              class="flex items-center gap-1.5 px-3 py-2 rounded-lg font-bold text-violet-700 hover:bg-violet-50 transition-all border border-violet-200 hover:border-violet-400 whitespace-nowrap">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17H3a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v10a2 2 0 01-2 2h-2" />
              </svg>
              Studio
            </router-link>
            <router-link v-else-if="!isLoggedIn || currentUser.role === 'STUDENT'" to="/become-instructor"
              class="flex items-center gap-1.5 px-3 py-2 rounded-lg text-gray-700 hover:text-violet-700 hover:bg-violet-50 transition-all whitespace-nowrap">
              Dạy trên MultiLearn
            </router-link>
          </nav>

          <div class="flex items-center gap-0.5 sm:gap-1 flex-shrink-0 ml-auto xl:ml-0">

            <button @click="switchLanguage"
              class="hidden sm:flex items-center gap-1.5 px-2.5 py-2 rounded-lg text-xs font-bold text-gray-600 hover:bg-gray-100 transition-all border border-transparent hover:border-gray-200">
              <img :src="$i18n?.locale === 'vi' ? 'https://flagcdn.com/w40/vn.png' : 'https://flagcdn.com/w40/us.png'"
                class="w-5 h-3.5 rounded-sm object-cover shadow-sm" alt="flag" />
              <span class="uppercase tracking-wider hidden lg:block">{{ $i18n?.locale || 'vi' }}</span>
            </button>

            <div class="hidden lg:block w-px h-5 bg-gray-200 mx-0.5 flex-shrink-0"></div>

            <!-- Notification bell: STUDENT và INSTRUCTOR khi ở ngoài trang /instructor -->
            <StudentNotificationBell v-if="isLoggedIn && currentRole === 'STUDENT'" />

            <!-- Tin nhắn: STUDENT và INSTRUCTOR (khi ở ngoài trang /instructor) -->
            <router-link v-if="isLoggedIn && currentRole === 'STUDENT'" to="/messages"
              class="relative p-2 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-all group"
              title="Tin nhắn">
              <svg class="w-[22px] h-[22px] group-hover:scale-110 transition-transform" fill="none"
                stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
              </svg>
              <span v-if="studentUnreadMsgCount > 0"
                class="absolute -top-0.5 -right-0.5 min-w-[16px] h-4 px-0.5 bg-blue-500 text-white text-[9px] font-black rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                {{ studentUnreadMsgCount > 9 ? '9+' : studentUnreadMsgCount }}
              </span>
            </router-link>

            <template v-if="isLoggedIn && currentRole === 'STUDENT'">
              <router-link to="/wishlist"
                class="relative p-2 rounded-lg text-gray-500 hover:text-pink-500 hover:bg-pink-50 transition-all group"
                title="Yêu thích">
                <svg class="w-[22px] h-[22px] group-hover:scale-110 transition-transform" fill="none"
                  stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </router-link>

              <router-link to="/cart"
                class="relative p-2 rounded-lg text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition-all group"
                title="Giỏ hàng">
                <svg class="w-[22px] h-[22px] group-hover:scale-110 transition-transform" fill="none"
                  stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span v-if="cartCount > 0"
                  class="absolute -top-0.5 -right-0.5 min-w-[16px] h-4 px-0.5 bg-red-500 text-white text-[9px] font-black rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                  {{ cartCount > 9 ? '9+' : cartCount }}
                </span>
              </router-link>
            </template>

            <button v-if="isLoggedIn && currentRole === 'ADMIN'" @click="router.push('/admin/dashboard')"
              class="hidden md:flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-bold rounded-xl hover:bg-slate-800 transition-all shadow-md flex-shrink-0">
              <span class="text-yellow-400">⚡</span> Quản trị
            </button>

            <div class="hidden sm:block w-px h-5 bg-gray-200 mx-0.5 flex-shrink-0"></div>

            <template v-if="!isLoggedIn">
              <router-link to="/login"
                class="hidden sm:flex px-4 py-2 text-sm font-semibold text-gray-700 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-all border border-gray-300 hover:border-blue-300 whitespace-nowrap">
                Đăng nhập
              </router-link>
              <router-link to="/register"
                class="px-4 py-2 text-sm font-bold text-white rounded-xl shadow-md transition-all active:scale-95 whitespace-nowrap"
                style="background:linear-gradient(135deg,#2563eb,#4f46e5);box-shadow:0 4px 14px rgba(37,99,235,.35);">
                Đăng ký
              </router-link>
            </template>

            <template v-else>
              <div class="relative" @click.stop>
                <button @click="toggleUserDropdown"
                  class="flex items-center gap-2 pl-1 pr-2 py-1 rounded-xl hover:bg-gray-100 transition-all border border-transparent hover:border-gray-200 ml-0.5">
                  <div class="relative flex-shrink-0">
                    <img
                      :src="currentUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser.fullName || 'U')}&background=2563eb&color=fff&bold=true&size=80`"
                      class="w-8 h-8 rounded-full object-cover ring-2 ring-white shadow-sm" />
                    <span
                      class="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white"></span>
                  </div>
                  <div class="hidden sm:block text-left min-w-0 max-w-[90px]">
                    <p class="text-xs font-bold text-gray-900 truncate leading-tight">{{ currentUser.fullName ||
                      currentUser.email }}</p>

                    <p class="text-[10px] font-semibold leading-tight"
                      :class="currentRole === 'ADMIN' ? 'text-red-500' : currentRole === 'INSTRUCTOR' ? 'text-violet-600' : 'text-emerald-600'">
                      {{ roleDisplay.label }}
                    </p>
                  </div>
                  <svg class="w-3.5 h-3.5 text-gray-400 transition-transform duration-200 hidden sm:block flex-shrink-0"
                    :class="isUserDropdownOpen ? 'rotate-180' : ''" fill="none" stroke="currentColor"
                    viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                <Transition enter-active-class="transition duration-150 ease-out"
                  enter-from-class="opacity-0 scale-95 -translate-y-2"
                  enter-to-class="opacity-100 scale-100 translate-y-0"
                  leave-active-class="transition duration-100 ease-in" leave-from-class="opacity-100"
                  leave-to-class="opacity-0 scale-95">
                  <div v-if="isUserDropdownOpen"
                    class="absolute right-0 top-[calc(100%+8px)] bg-white rounded-2xl shadow-2xl shadow-black/12 border border-gray-200 overflow-hidden z-[102] origin-top-right"
                    style="width:272px">
                    <div class="relative px-5 py-4 overflow-hidden"
                      style="background:linear-gradient(135deg,#1e40af,#4f46e5);">
                      <div class="absolute inset-0 opacity-10"
                        style="background-image:radial-gradient(circle at 2px 2px,#fff 1px,transparent 0);background-size:20px 20px;">
                      </div>
                      <div class="relative flex items-center gap-3">
                        <img
                          :src="currentUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser.fullName || 'U')}&background=ffffff&color=2563eb&bold=true&size=80`"
                          class="w-12 h-12 rounded-xl object-cover ring-2 ring-white/30 shadow-lg flex-shrink-0" />
                        <div class="min-w-0 flex-1">
                          <p class="text-sm font-black text-white truncate">{{ currentUser.fullName || 'Học viên' }}</p>
                          <p class="text-xs text-blue-200 truncate">{{ currentUser.email }}</p>

                          <span
                            class="inline-flex mt-1 items-center px-2 py-0.5 rounded-full text-[10px] font-black border"
                            :class="currentRole === 'ADMIN' ? 'bg-red-500 text-white border-red-400' : currentRole === 'INSTRUCTOR' ? 'bg-violet-500 text-white border-violet-400' : 'bg-emerald-500 text-white border-emerald-400'">
                            {{ roleDisplay.label }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <div class="py-2">
                      <template v-if="currentUser.role !== 'ADMIN'">
                        <router-link v-for="item in userMenuItems" :key="item.to" :to="item.to"
                          @click="isUserDropdownOpen = false"
                          class="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                          <div
                            class="w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0 text-base transition-all"
                            :style="{ backgroundColor: item.bgColor }">{{ item.icon }}</div>
                          <div class="min-w-0">
                            <p class="font-semibold leading-tight">{{ item.label }}</p>
                            <p class="text-xs text-gray-400 leading-tight mt-0.5">{{ item.desc }}</p>
                          </div>
                        </router-link>

                        <div v-if="currentUser.role === 'INSTRUCTOR'" class="px-4 py-3 border-t border-gray-100 mt-1">
                          <router-link to="/instructor" @click="isUserDropdownOpen = false"
                            class="flex items-center justify-center gap-2 w-full px-4 py-2.5 rounded-xl text-sm font-black text-white active:scale-95 transition-all"
                            style="background:linear-gradient(135deg,#7c3aed,#4f46e5);">
                            🚀 Vào Instructor Studio
                          </router-link>
                        </div>
                      </template>
                    </div>

                    <div class="border-t border-gray-100">
                      <button @click="logout"
                        class="flex w-full items-center gap-3 px-4 py-3 text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors group">
                        <div
                          class="w-8 h-8 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0 group-hover:bg-red-600 group-hover:text-white transition-all">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                          </svg>
                        </div>
                        {{ $t?.('auth.logout') || 'Đăng xuất' }}
                      </button>
                    </div>
                  </div>
                </Transition>
              </div>
            </template>

            <button @click.stop="isMobileMenuOpen = !isMobileMenuOpen"
              class="xl:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors flex-shrink-0 ml-0.5">
              <svg v-if="!isMobileMenuOpen" class="w-[22px] h-[22px]" fill="none" stroke="currentColor"
                viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else class="w-[22px] h-[22px]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <Transition enter-active-class="transition duration-200 ease-out" enter-from-class="opacity-0 -translate-y-3"
        enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-150 ease-in"
        leave-from-class="opacity-100" leave-to-class="opacity-0 -translate-y-3">
        <div v-if="isMobileMenuOpen" class="xl:hidden border-t border-gray-200 bg-white shadow-xl" @click.stop>
          <div class="px-4 pt-3 pb-2">
            <div class="relative">
              <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none"
                fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input v-model="searchQuery" @keyup.enter="handleSearch(); isMobileMenuOpen = false" type="text"
                placeholder="Tìm kiếm khoá học..."
                class="w-full pl-10 pr-4 py-2.5 bg-gray-100 rounded-xl text-sm outline-none focus:ring-2 focus:ring-blue-500/30 focus:bg-white transition-all" />
            </div>
          </div>
          <nav class="px-3 pb-4 space-y-0.5">
            <div class="px-2 pt-2 pb-1 text-[10px] font-black text-gray-400 uppercase tracking-widest">Điều hướng</div>
            <router-link to="/course" @click="isMobileMenuOpen = false" class="mobile-nav-link">📚 Khoá
              học</router-link>
            <router-link to="/blog" @click="isMobileMenuOpen = false" class="mobile-nav-link">📰 Tin tức &
              Blog</router-link>
            <router-link v-if="isLoggedIn && currentRole !== 'ADMIN'" to="/my-courses"
              @click="isMobileMenuOpen = false" class="mobile-nav-link">🎓 Góc học tập</router-link>
            <router-link v-if="isLoggedIn && currentRole !== 'ADMIN'" to="/wishlist"
              @click="isMobileMenuOpen = false" class="mobile-nav-link">❤️ Yêu thích</router-link>
            <router-link v-if="isLoggedIn && currentRole !== 'ADMIN'" to="/cart" @click="isMobileMenuOpen = false"
              class="mobile-nav-link">
              🛒 Giỏ hàng
              <span v-if="cartCount"
                class="ml-auto bg-red-500 text-white text-[10px] font-black px-1.5 py-0.5 rounded-full">{{ cartCount
                }}</span>
            </router-link>
            <router-link v-if="!isLoggedIn || currentUser.role === 'STUDENT'" to="/become-instructor"
              @click="isMobileMenuOpen = false" class="mobile-nav-link">🎙️ Dạy trên MultiLearn</router-link>
            <router-link v-if="isLoggedIn && currentUser.role === 'INSTRUCTOR'" to="/instructor"
              @click="isMobileMenuOpen = false" class="mobile-nav-link" style="color:#7c3aed">🚀 Instructor
              Studio</router-link>

            <template v-if="!isLoggedIn">
              <div class="pt-2 px-2 border-t border-gray-100 mt-2">
                <div class="grid grid-cols-2 gap-2">
                  <router-link to="/login" @click="isMobileMenuOpen = false"
                    class="flex items-center justify-center px-4 py-2.5 rounded-xl text-sm font-bold text-gray-700 border-2 border-gray-300 hover:border-blue-400 hover:text-blue-600 transition-all">
                    Đăng nhập
                  </router-link>
                  <router-link to="/register" @click="isMobileMenuOpen = false"
                    class="flex items-center justify-center px-4 py-2.5 rounded-xl text-sm font-bold text-white transition-all"
                    style="background:linear-gradient(135deg,#2563eb,#4f46e5)">
                    Đăng ký
                  </router-link>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="pt-2 border-t border-gray-100 mt-2">
                <button @click="logout"
                  class="w-full flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors">
                  🚪 Đăng xuất
                </button>
              </div>
            </template>
          </nav>
        </div>
      </Transition>
    </header>

    <main class="flex-1 w-full relative z-10">
      <router-view v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>

    <footer class="bg-gray-950 text-gray-400 relative overflow-hidden">
      </footer>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useConfirm } from '@/composables/useConfirm';
import axiosClient from '@/api/axiosClient';
import StudentNotificationBell from '@/components/common/StudentNotificationBell.vue';

const router = useRouter();
const route = useRoute();
const { t, locale } = useI18n();
const { confirm } = useConfirm();

// UI STATE
const showAnnouncement = ref(true);
const maxFlashSalePercent = ref(0);
const flashSaleName = ref('');

const handleFlashSaleActive = (e) => {
  maxFlashSalePercent.value = e.detail.percent;
  flashSaleName.value = e.detail.name;
};
const handleFlashSaleEnded = () => { maxFlashSalePercent.value = 0; flashSaleName.value = ''; };
const isScrolled = ref(false);
const isSearchFocused = ref(false);
const isMobileMenuOpen = ref(false);
const isMegaMenuOpen = ref(false);
const isCoursesMenuOpen = ref(false);
const isBlogMenuOpen = ref(false);
const isUserDropdownOpen = ref(false);
const searchQuery = ref('');
const newsletterEmail = ref('');

// AUTH
const isLoggedIn = ref(false);
const currentUser = ref({}); // <-- Đây là Quyền thực tế trong Database
const cartCount = ref(0);
const studentUnreadMsgCount = ref(0);

// BLOG
const latestPosts = ref([]);
const fetchLatestPosts = async () => {
  try {
    const res = await axiosClient.get('/public/blog/posts?page=0&size=3');
    latestPosts.value = res?.content ?? [];
  } catch { latestPosts.value = []; }
};
function timeAgo(dateStr) {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins} phút trước`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs} giờ trước`;
  return `${Math.floor(hrs / 24)} ngày trước`;
}

// CATEGORIES
const categoriesTree = ref([]);
const isCatsLoading = ref(true);
const activeCategoryId = ref(null);
const activeCategory = computed(() =>
  activeCategoryId.value
    ? (categoriesTree.value.find(c => c.id === activeCategoryId.value) ?? null)
    : null
);

// 🔥 ĐÂY CHÍNH LÀ TRÁI TIM CỦA VIỆC "BIẾN HÌNH" 🔥
// Xác định xem NGAY LÚC NÀY user đang đóng vai trò gì dựa vào trang họ đứng
const currentRole = computed(() => {
  const r = currentUser.value.role;
  if (r === 'ADMIN') return 'ADMIN';
  if (r === 'INSTRUCTOR' && route.path.startsWith('/instructor')) return 'INSTRUCTOR';
  return 'STUDENT'; // Mặc định ở ngoài trang chủ, kể cả INSTRUCTOR cũng bị xem là STUDENT
});

// Chữ hiển thị
const roleDisplay = computed(() => {
  const r = currentRole.value;
  if (r === 'ADMIN') return { label: 'Quản trị viên' };
  if (r === 'INSTRUCTOR') return { label: 'Giảng viên' };
  return { label: 'Học viên' };
});


// STATIC DATA
const quickCourseLinks = [
  { sortKey: 'bestseller', icon: '🔥', label: 'Bestseller', sub: 'Được mua nhiều nhất', query: { sort: 'studentCount,desc' } },
  { sortKey: 'newest', icon: '✨', label: 'Khoá học mới', sub: 'Vừa được thêm gần đây', query: { sort: 'id,desc' } },
  { sortKey: 'free', icon: '🆓', label: 'Miễn phí 100%', sub: 'Không mất một đồng', query: { free: 'true' } },
  { sortKey: 'rating', icon: '⭐', label: 'Đánh giá cao', sub: 'Từ 4.7★ trở lên', query: { sort: 'averageRating,desc' } },
];
const blogTopics = [
  { to: '/blog?topic=tech', icon: '💻', label: 'Công nghệ', sub: 'Xu hướng & tips lập trình' },
  { to: '/blog?topic=career', icon: '🚀', label: 'Nghề nghiệp', sub: 'Kỹ năng & lộ trình' },
  { to: '/blog?topic=study', icon: '📖', label: 'Học tập', sub: 'Phương pháp hiệu quả' },
  { to: '/blog?topic=news', icon: '📣', label: 'Tin MultiLearn', sub: 'Cập nhật từ nền tảng' },
];
const userMenuItems = [
  { to: '/profile', icon: '👤', label: 'Hồ sơ của tôi', desc: 'Cập nhật thông tin cá nhân', bgColor: '#dbeafe' },
  { to: '/my-courses', icon: '📚', label: 'Khoá học của tôi', desc: 'Tiếp tục hành trình học', bgColor: '#ede9fe' },
  { to: '/wishlist', icon: '❤️', label: 'Danh sách yêu thích', desc: 'Khoá học đã lưu', bgColor: '#fce7f3' },
  { to: '/cart', icon: '🛒', label: 'Giỏ hàng', desc: 'Xem và thanh toán', bgColor: '#dcfce7' },
  { to: '/transactions', icon: '🧾', label: 'Lịch sử giao dịch', desc: 'Xem lịch sử thanh toán', bgColor: '#fef9c3' },
  { to: '/achievements', icon: '🏅', label: 'Thành tích', desc: 'Huy hiệu & streak học tập', bgColor: '#fef3c7' },
];
const footerSocials = [
  { name: 'Facebook', icon: '📘', url: '#' },
  { name: 'YouTube', icon: '▶️', url: '#' },
  { name: 'TikTok', icon: '🎵', url: '#' },
  { name: 'LinkedIn', icon: '💼', url: '#' },
  { name: 'Zalo', icon: '💬', url: '#' },
];

// METHODS
const toggleUserDropdown = () => { isUserDropdownOpen.value = !isUserDropdownOpen.value; };
const handleGlobalClick = () => { isUserDropdownOpen.value = false; isMobileMenuOpen.value = false; };
const switchLanguage = () => { locale.value = locale.value === 'vi' ? 'en' : 'vi'; localStorage.setItem('lang', locale.value); };
const handleSearch = () => { if (searchQuery.value.trim()) { router.push({ path: '/course', query: { q: searchQuery.value } }); isMobileMenuOpen.value = false; } };
const subscribeNewsletter = () => { if (!newsletterEmail.value.trim()) return; window.__notify?.auto?.('✅ Đăng ký thành công!'); newsletterEmail.value = ''; };

const goToCategory = (id) => {
  isMegaMenuOpen.value = false;
  activeCategoryId.value = null;
  router.push({ path: '/course', query: { categoryId: id } });
};

const fetchCategoryTree = async () => {
  isCatsLoading.value = true;
  try {
    const res = await axiosClient.get('/categories');
    const list = Array.isArray(res) ? res : (res?.content ?? []);
    categoriesTree.value = list.filter(c => !c.parentId);
  } catch { /* silent */ } finally {
    isCatsLoading.value = false;
  }
};

const updateStudentMsgCount = async () => {
  if (currentUser.value.role !== 'STUDENT') return; 
  try {
    const res = await axiosClient.get('/messages/student/unread-count');
    studentUnreadMsgCount.value = typeof res === 'number' ? res : 0;
  } catch { /* silent */ }
};

const updateCartCount = async () => {
  if (!localStorage.getItem('access_token')) return;
  if (!['STUDENT', 'INSTRUCTOR'].includes(currentUser.value.role)) return;
  const raw = localStorage.getItem('user_info');
  if (!raw) return;
  try {
    const user = JSON.parse(raw);
    const uid = user.id || user.userId;
    if (!uid) return;
    const res = await axiosClient.get(`/cart/${uid}`);
    cartCount.value = Array.isArray(res) ? res.length : (res?.cartItems?.length ?? 0);
  } catch { /* silent */ }
};

const checkAuth = () => {
  const token = localStorage.getItem('access_token');
  const raw = localStorage.getItem('user_info');
  // Chú ý: Ở đây ta chỉ lấy role thật từ Database lưu trong user_info
  
  if (token && raw) {
    try {
      isLoggedIn.value = true;
      currentUser.value = JSON.parse(raw); // Lưu role thật là INSTRUCTOR vào đây
      
      updateCartCount();
      updateStudentMsgCount();
    } catch {
      isLoggedIn.value = false;
      currentUser.value = {};
    }
  } else {
    isLoggedIn.value = false;
    currentUser.value = {};
    cartCount.value = 0;
  }
};

const logout = async () => {
  isUserDropdownOpen.value = false;
  const ok = await confirm({
    title: 'Xác nhận đăng xuất', message: 'Bạn có chắc chắn muốn đăng xuất không?',
    confirmText: 'Đăng xuất', cancelText: 'Hủy', variant: 'warning',
  });
  if (!ok) return;
  try {
    // Báo backend xóa refresh token
    await axiosClient.post('/auth/logout-all');
  } catch (error) {
    console.error("Lỗi khi logout:", error);
  } finally {
    ['access_token', 'refresh_token', 'user_info', 'user_role', 'admin_role', 'admin_permissions'].forEach(k => localStorage.removeItem(k));
    isLoggedIn.value = false; currentUser.value = {}; cartCount.value = 0;
    router.push('/login');
  }
};

const onScroll = () => { isScrolled.value = window.scrollY > 8; };

onMounted(() => {
  checkAuth();
  fetchCategoryTree();
  fetchLatestPosts();
  window.addEventListener('scroll', onScroll, { passive: true });
  window.addEventListener('user-info-updated', checkAuth);
  window.addEventListener('cart-updated', updateCartCount);
  window.addEventListener('messages-updated', updateStudentMsgCount);
  window.addEventListener('flash-sale-active', handleFlashSaleActive);
  window.addEventListener('flash-sale-ended', handleFlashSaleEnded);
  const lang = localStorage.getItem('lang');
  if (lang) locale.value = lang;
});

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
  window.removeEventListener('user-info-updated', checkAuth);
  window.removeEventListener('cart-updated', updateCartCount);
  window.removeEventListener('messages-updated', updateStudentMsgCount);
  window.removeEventListener('flash-sale-active', handleFlashSaleActive);
  window.removeEventListener('flash-sale-ended', handleFlashSaleEnded);
});

watch(() => route.path, () => {
  checkAuth(); isUserDropdownOpen.value = false; isMobileMenuOpen.value = false;
});
</script>

<style scoped>
.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 1rem;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
  transition: all 0.15s;
  width: 100%;
}

.mobile-nav-link:hover {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.page-enter-active,
.page-leave-active {
  transition: opacity .18s ease, transform .18s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
