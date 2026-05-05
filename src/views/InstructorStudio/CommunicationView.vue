<template>
  <div class="flex h-full min-h-screen bg-white font-sans text-gray-900">
    
    <div class="w-64 flex-shrink-0 border-r border-gray-200 bg-white py-8 sticky top-0 h-screen">
      <h2 class="px-6 text-lg font-bold text-gray-800 mb-6">Giao tiếp</h2>
      
      <nav class="space-y-1">
        <router-link 
            to="/instructor/communication/qa" 
            active-class="bg-gray-100 text-black font-bold border-l-4 border-black" 
            class="block pl-6 pr-4 py-3 text-sm text-gray-600 hover:bg-gray-50 hover:text-black transition border-l-4 border-transparent"
        >
            Hỏi đáp (Q&A)
        </router-link>
        
        <router-link 
            to="/instructor/communication/messages" 
            active-class="bg-gray-100 text-black font-bold border-l-4 border-black" 
            class="block pl-6 pr-4 py-3 text-sm text-gray-600 hover:bg-gray-50 hover:text-black transition border-l-4 border-transparent"
        >
            Tin nhắn
        </router-link>

        <router-link 
            to="/instructor/communication/assignments" 
            active-class="bg-gray-100 text-black font-bold border-l-4 border-black" 
            class="block pl-6 pr-4 py-3 text-sm text-gray-600 hover:bg-gray-50 hover:text-black transition border-l-4 border-transparent"
        >
            Bài tập
        </router-link>

        <router-link 
            to="/instructor/communication/announcements" 
            active-class="bg-gray-100 text-black font-bold border-l-4 border-black" 
            class="block pl-6 pr-4 py-3 text-sm text-gray-600 hover:bg-gray-50 hover:text-black transition border-l-4 border-transparent"
        >
            Thông báo
        </router-link>
      </nav>
    </div>

    <div class="flex-1 flex flex-col h-full bg-white overflow-hidden">
        <router-view></router-view>
    </div>

  </div>
</template>

<script setup>
// File này hiện tại là wrapper điều hướng, logic nằm ở các file view con
</script>

<style scoped>
/* Trang trí thêm cho sidebar nếu cần */
nav::-webkit-scrollbar { width: 4px; }
nav::-webkit-scrollbar-thumb { background: #f3f4f6; border-radius: 10px; }
</style>