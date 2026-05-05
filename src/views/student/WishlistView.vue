<template>
  <div class="min-h-screen bg-gray-50 py-12">
    <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
      <div class="flex min-h-[500px] flex-col rounded-2xl border border-gray-100 bg-white p-10 shadow-sm">
        <div class="mb-8 flex items-center justify-between border-b border-gray-100 pb-6">
          <div>
            <h2 class="text-3xl font-bold text-gray-900">Danh sách yêu thích</h2>
            <p class="mt-2 text-gray-500">Lưu lại những khóa học bạn quan tâm để xem lại sau</p>
          </div>
          <div class="hidden sm:block">
            <span class="rounded-full bg-pink-50 px-4 py-2 text-sm font-bold text-pink-600">
              {{ wishlistItems.length }} khóa học
            </span>
          </div>
        </div>

        <div v-if="isLoading" class="flex flex-1 items-center justify-center py-20">
          <div class="h-12 w-12 animate-spin rounded-full border-4 border-pink-500 border-t-transparent"></div>
        </div>

        <div v-else-if="wishlistItems.length === 0" class="flex flex-1 flex-col items-center justify-center text-center">
          <div class="mb-6 flex h-32 w-32 items-center justify-center rounded-full bg-pink-50">
            <svg class="h-16 w-16 text-pink-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </div>
          <h3 class="mb-2 text-xl font-bold text-gray-900">Danh sách đang trống</h3>
          <p class="mb-8 max-w-md text-gray-500">
            Bạn chưa yêu thích khóa học nào. Hãy dạo một vòng và thả tim cho những khóa học bổ ích nhé.
          </p>
          <router-link
            to="/course"
            class="rounded-xl bg-gradient-to-r from-pink-500 to-rose-500 px-8 py-3 font-bold text-white transition-all duration-300 hover:scale-105 hover:shadow-lg"
          >
            Khám phá khóa học ngay
          </router-link>
        </div>

        <div v-else class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          <div v-for="item in wishlistItems" :key="item.id" class="group relative flex flex-col overflow-hidden rounded-xl border border-gray-200 transition-all hover:shadow-lg">
            <button
              @click="removeFromWishlist(item)"
              class="absolute right-2 top-2 z-10 rounded-full bg-white/90 p-2 text-gray-400 shadow-sm transition-colors hover:text-red-500"
              title="Xóa khỏi danh sách"
            >
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
            </button>

            <div class="relative h-44 overflow-hidden">
              <router-link :to="'/course/' + (item.slug || item.courseId)">
                <img :src="item.thumbnail || 'https://via.placeholder.com/300x200?text=No+Image'" class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105">
                <div class="absolute inset-0 bg-black/20 opacity-0 transition-opacity group-hover:opacity-100"></div>
              </router-link>
            </div>

            <div class="flex flex-1 flex-col p-5">
              <h3 class="mb-2 line-clamp-2 h-12 font-bold text-gray-900 transition-colors group-hover:text-pink-600">
                <router-link :to="'/course/' + (item.slug || item.courseId)">{{ item.title }}</router-link>
              </h3>

              <div class="mt-auto flex items-end justify-between border-t border-gray-50 pt-4">
                <div class="flex flex-col">
                  <div v-if="item.salePrice && item.salePrice < item.price">
                    <span class="block text-xs text-gray-400 line-through">{{ formatPrice(item.price) }}</span>
                    <span class="text-lg font-bold text-red-600">{{ formatPrice(item.salePrice) }}</span>
                  </div>
                  <div v-else>
                    <span class="text-lg font-bold text-blue-600">{{ formatPrice(item.price) }}</span>
                  </div>
                </div>

                <router-link
                  :to="'/course/' + (item.slug || item.courseId)"
                  class="mb-1 flex items-center gap-1 text-sm font-medium text-gray-500 transition-colors hover:text-pink-600"
                >
                  Xem chi tiết
                  <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import WishlistService from '@/services/wishlist.service'
import { useConfirm } from '@/composables/useConfirm'

const { confirm } = useConfirm()
const wishlistItems = ref([])
const isLoading = ref(true)

const loadWishlist = async () => {
  const userStr = localStorage.getItem('user_info')
  if (!userStr) {
    isLoading.value = false
    return
  }
  const user = JSON.parse(userStr)

  try {
    const res = await WishlistService.getWishlist(user.id)
    if (Array.isArray(res)) {
      wishlistItems.value = res
    }
  } catch (error) {
    console.error('Lỗi tải trang yêu thích:', error)
  } finally {
    isLoading.value = false
  }
}

const removeFromWishlist = async (item) => {
  const confirmed = await confirm({
    title: 'Xác nhận xóa khỏi danh sách yêu thích',
    message: 'Bạn có chắc muốn xóa khóa học này khỏi danh sách yêu thích?',
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return

  const user = JSON.parse(localStorage.getItem('user_info'))
  try {
    await WishlistService.removeFromWishlist(user.id, item.courseId)
    wishlistItems.value = wishlistItems.value.filter((course) => course.id !== item.id)
    window.__notify.auto('Đã xóa khóa học khỏi danh sách yêu thích.')
  } catch (error) {
    window.__notify.auto('Xóa thất bại: ' + (error?.message || 'Có lỗi xảy ra.'))
  }
}

const formatPrice = (val) => {
  if (val === 0 || val === '0' || val == null) return 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

onMounted(loadWishlist)
</script>
