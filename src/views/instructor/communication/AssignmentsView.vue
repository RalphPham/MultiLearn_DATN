<template>
  <div class="flex flex-col h-full bg-white">
    <div class="px-8 py-6 border-b border-gray-100">
      <div class="flex items-center justify-between mb-2">
        <h1 class="text-2xl font-bold text-gray-800">Danh sách bài tập</h1>
        <div class="relative">
             <select v-model="filterCourse" class="text-sm border-none bg-gray-50 rounded-lg px-4 py-2 font-bold text-purple-600 cursor-pointer hover:bg-purple-50 transition focus:ring-0">
                 <option value="all">Tất cả khóa học</option>
                 <option value="react">ReactJS Master</option>
                 <option value="spring">Spring Boot Pro</option>
             </select>
        </div>
      </div>
      <p class="text-sm text-gray-500">Quản lý và chấm điểm các bài tập thực hành do học viên gửi lên.</p>
    </div>

    <div class="px-8 py-4 flex gap-6 border-b border-gray-200 bg-gray-50/50">
        <label class="flex items-center gap-2 text-sm font-medium text-gray-600 cursor-pointer">
            <input type="checkbox" checked class="rounded text-purple-600 focus:ring-purple-500 border-gray-300"> Chưa chấm điểm (2)
        </label>
        <label class="flex items-center gap-2 text-sm font-medium text-gray-600 cursor-pointer">
            <input type="checkbox" class="rounded text-purple-600 focus:ring-purple-500 border-gray-300"> Đã chấm điểm
        </label>
    </div>

    <div class="flex-1 overflow-y-auto p-8 bg-gray-50">
      
      <div class="bg-white rounded-xl border border-gray-200 p-6 mb-4 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start">
              <div class="flex gap-4">
                  <div class="w-12 h-12 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold text-lg">H</div>
                  <div>
                      <h3 class="font-bold text-gray-800 text-lg">Hoàng Văn Nam</h3>
                      <p class="text-sm text-gray-500 mb-1">Khóa học: <span class="font-semibold text-gray-700">ReactJS Master</span> • Bài tập: <span class="font-semibold text-gray-700">Xây dựng ToDo List</span></p>
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                        Chưa chấm điểm
                      </span>
                  </div>
              </div>
              <span class="text-xs text-gray-400 font-medium">Nộp 2 giờ trước</span>
          </div>
          <div class="mt-4 pl-16">
              <p class="text-gray-600 text-sm bg-gray-50 p-3 rounded-lg border border-gray-100 italic">"Em chào thầy, em đã hoàn thành bài tập. Phần CSS em làm hơi khác mẫu một chút ạ..."</p>
              <div class="mt-3 flex gap-3">
                  <button class="text-sm bg-purple-600 text-white px-4 py-2 rounded-lg font-bold hover:bg-purple-700 transition">Chấm điểm ngay</button>
                  <button class="text-sm border border-gray-300 text-gray-600 px-4 py-2 rounded-lg font-medium hover:bg-gray-50 transition">Xem file đính kèm</button>
              </div>
          </div>
      </div>

      <div class="bg-white rounded-xl border border-gray-200 p-6 mb-4 shadow-sm hover:shadow-md transition-shadow">
          <div class="flex justify-between items-start">
              <div class="flex gap-4">
                  <div class="w-12 h-12 rounded-full bg-green-100 text-green-600 flex items-center justify-center font-bold text-lg">T</div>
                  <div>
                      <h3 class="font-bold text-gray-800 text-lg">Trần Thu Hà</h3>
                      <p class="text-sm text-gray-500 mb-1">Khóa học: <span class="font-semibold text-gray-700">Spring Boot Pro</span> • Bài tập: <span class="font-semibold text-gray-700">Thiết kế Database</span></p>
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                        Chưa chấm điểm
                      </span>
                  </div>
              </div>
              <span class="text-xs text-gray-400 font-medium">Nộp hôm qua</span>
          </div>
          <div class="mt-4 pl-16">
              <div class="mt-3 flex gap-3">
                  <button class="text-sm bg-purple-600 text-white px-4 py-2 rounded-lg font-bold hover:bg-purple-700 transition">Chấm điểm ngay</button>
                  <button class="text-sm border border-gray-300 text-gray-600 px-4 py-2 rounded-lg font-medium hover:bg-gray-50 transition">Xem file đính kèm</button>
              </div>
          </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
const filterCourse = ref('all');
// File này chỉ hiển thị giao diện mẫu (UI) để lấp đầy chức năng
// Không gọi API để tránh lỗi 500 khi chưa có Backend
</script>