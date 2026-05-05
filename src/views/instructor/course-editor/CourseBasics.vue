<template>
  <div class="animate-fade-in grid grid-cols-1 lg:grid-cols-12 gap-8">
    <div class="lg:col-span-8 space-y-8">
      <div class="space-y-4">
        <div><label class="font-bold text-gray-800 block mb-2 text-sm">Tiêu đề khóa học</label><input v-model="courseData.title" class="w-full border border-gray-300 p-3 rounded focus:border-black focus:ring-0 outline-none transition text-sm font-medium" placeholder="Ví dụ: Java Spring Boot từ Zero đến Hero" /></div>
        <div><label class="font-bold text-gray-800 block mb-2 text-sm">Phụ đề (Mô tả ngắn)</label><textarea v-model="courseData.subtitle" class="w-full border border-gray-300 p-3 rounded focus:border-black focus:ring-0 outline-none transition text-sm h-24 resize-none" placeholder="Tóm tắt nội dung chính..."></textarea></div>
      </div>
      <div>
        <label class="font-bold text-gray-800 block mb-2 text-sm">Hình ảnh khóa học</label>
        <div class="flex gap-4 items-center p-4 border border-gray-200 rounded-md bg-gray-50">
          <div class="w-40 h-24 bg-gray-200 border border-gray-300 flex items-center justify-center overflow-hidden rounded relative"><img v-if="courseData.imagePreview" :src="courseData.imagePreview" class="w-full h-full object-cover" /><span v-else class="text-gray-400 text-xs">No Image</span></div>
          <div><p class="text-xs text-gray-600 mb-2">Kích thước chuẩn: <strong>750x422 pixel</strong>.</p><label class="cursor-pointer text-purple-600 font-bold text-sm hover:underline">Tải hình ảnh<input type="file" class="hidden" accept="image/*" @change="handleImageUpload"></label></div>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-4">
        <div><label class="font-bold text-gray-800 block mb-2 text-sm">Danh mục</label><select v-model="courseData.categoryId" class="w-full border border-gray-300 p-2.5 rounded focus:border-black outline-none bg-white text-sm"><option :value="null">-- Chọn --</option><option value="1">Lập trình</option><option value="2">Ngoại ngữ</option><option value="3">Kinh doanh</option></select></div>
        <div><label class="font-bold text-gray-800 block mb-2 text-sm">Trình độ</label><select v-model="courseData.level" class="w-full border border-gray-300 p-2.5 rounded focus:border-black outline-none bg-white text-sm"><option value="BEGINNER">Sơ cấp</option><option value="INTERMEDIATE">Trung cấp</option><option value="EXPERT">Chuyên gia</option><option value="ALL_LEVELS">Tất cả</option></select></div>
        <div><label class="font-bold text-gray-800 block mb-2 text-sm">Ngôn ngữ</label><select class="w-full border border-gray-300 p-2.5 rounded focus:border-black outline-none bg-white text-sm"><option>Tiếng Việt</option><option>English</option></select></div>
      </div>
      <div><label class="font-bold text-gray-800 block mb-2 text-sm">Mô tả khóa học</label><div class="border border-gray-300 rounded overflow-hidden"><div class="bg-gray-50 border-b border-gray-300 p-2 flex gap-2"><button class="font-bold text-gray-600 hover:text-black px-2">B</button><button class="italic text-gray-600 hover:text-black px-2">I</button></div><textarea v-model="courseData.description" class="w-full p-4 outline-none text-sm min-h-[150px]" placeholder="Chi tiết nội dung khóa học..."></textarea></div></div>
      <div><label class="font-bold text-gray-800 block mb-2 text-sm">Học viên sẽ học được gì?</label><div class="space-y-2"><div v-for="(item, index) in courseData.learningOutcomes" :key="index" class="flex gap-2"><input v-model="item.text" class="flex-1 border border-gray-300 p-2 rounded focus:border-black outline-none text-sm" placeholder="Ví dụ: Hiểu rõ về Java Core..." /><button @click="removeOutcome(index)" class="text-gray-400 hover:text-red-500">×</button></div><button @click="addOutcome" class="text-purple-600 font-bold text-sm hover:underline">+ Thêm mục tiêu</button></div></div>
    </div>
    <div class="lg:col-span-4">
      <div class="sticky top-8 bg-white border border-gray-200 rounded-lg shadow-lg overflow-hidden group">
        <div class="h-40 bg-gray-200 relative"><img v-if="courseData.imagePreview" :src="courseData.imagePreview" class="w-full h-full object-cover" /><div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-xs">Preview</div></div>
        <div class="p-4"><h3 class="font-bold text-gray-900 line-clamp-2 mb-1">{{ courseData.title || 'Tiêu đề...' }}</h3><p class="text-xs text-gray-500 line-clamp-2 mb-2">{{ courseData.subtitle || 'Mô tả ngắn...' }}</p><div class="text-xs text-gray-500 mb-2">Giảng viên Demo</div><div class="font-bold text-lg text-gray-900">{{ Number(courseData.salePrice || courseData.price || 0).toLocaleString('vi-VN') }} ₫</div></div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps(['courseData']);
const courseData = props.courseData;

const handleImageUpload = (e) => {
  const file = e.target.files[0];
  if (file) {
    // Dùng FileReader để chuyển ảnh thành chuỗi Base64
    // Chuỗi này có thể lưu vào LocalStorage và hiển thị lại được mãi mãi
    const reader = new FileReader();
    
    reader.onload = (e) => {
      courseData.imagePreview = e.target.result; // Lưu chuỗi Base64 vào biến preview
    };
    
    reader.readAsDataURL(file);
    courseData.imageFile = file; // Vẫn giữ file gốc nếu cần upload API sau này
  }
};

const addOutcome = () => courseData.learningOutcomes.push({ text: '' });
const removeOutcome = (i) => courseData.learningOutcomes.splice(i, 1);
</script>