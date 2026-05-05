<template>
  <div class="animate-fade-in">
      <div class="flex gap-6 border-b border-gray-200 mb-4">
          <button class="pb-2 border-b-2 border-gray-900 font-bold text-gray-900 text-sm">Đánh giá</button>
          <button class="pb-2 border-b-2 border-transparent font-medium text-gray-500 hover:text-gray-700 text-sm">Thông tin chi tiết <span class="bg-green-100 text-green-700 text-[10px] px-1 rounded ml-1">Mới</span></button>
      </div>
      
      <div class="flex flex-wrap items-center justify-between gap-4 mb-6">
          <div class="flex items-center gap-4">
              <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer"><input type="checkbox" class="rounded border-gray-300 text-purple-600 focus:ring-purple-500"> Chưa trả lời</label>
              <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer"><input type="checkbox" class="rounded border-gray-300 text-purple-600 focus:ring-purple-500"> Có một bình luận</label>
          </div>
          <div class="flex items-center gap-4">
              <button class="border border-purple-600 text-purple-600 text-sm font-bold px-4 py-2 hover:bg-purple-50 rounded-sm">Xuất sang CSV...</button>
          </div>
      </div>

      <div class="border border-gray-300 rounded-lg p-4 flex gap-4 items-start mb-8 shadow-sm">
          <svg class="w-6 h-6 text-purple-600 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <p class="text-sm text-gray-800 font-medium pt-0.5">Có thể mất đến 48 giờ để các xếp hạng đã phê duyệt của học viên hiển thị trên trang tổng quan khóa học của bạn.</p>
      </div>

      <div class="text-center text-gray-500 py-12">Không tìm thấy đánh giá nào</div>
  </div>
</template>