<template>
  <div class="animate-fade-in">
      <div class="border border-gray-300 rounded-lg p-4 mb-8 shadow-sm flex flex-col sm:flex-row gap-4 justify-between items-start bg-white">
          <div class="flex gap-4">
              <svg class="w-6 h-6 text-purple-600 flex-shrink-0 mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <p class="text-sm text-gray-800 font-medium">Chúng tôi đã cập nhật cách tính số lượng người duy nhất truy cập vào khóa học...</p>
          </div>
          <button class="border border-gray-400 text-gray-700 hover:bg-gray-50 px-3 py-1.5 text-sm font-bold rounded-sm whitespace-nowrap">Hủy bỏ</button>
      </div>

      <div class="flex justify-between items-center mb-8 border-b border-gray-100 pb-4">
          <span class="text-sm text-gray-600">Để tìm hiểu thêm, hãy chọn một khóa học</span>
          <select class="border border-gray-300 rounded px-3 py-2 text-sm font-bold text-gray-700 hover:bg-gray-50 cursor-pointer"><option>12 tháng trước</option></select>
      </div>

      <div class="text-center py-12">
          <h3 class="text-lg font-medium text-gray-900 mb-3">Chưa có khóa học nào...</h3>
          <p class="text-gray-600 max-w-lg mx-auto mb-8 text-sm">Sau khi bạn xuất bản khóa học, hãy truy cập vào đây để tìm hiểu về lưu lượng truy cập.</p>
          <router-link to="/instructor/courses" class="border border-purple-600 text-purple-600 font-bold px-6 py-3 hover:bg-purple-50 transition rounded-sm text-sm">
              Chuyển đến Bảng điều khiển của giảng viên
          </router-link>
      </div>
  </div>
</template>