<script setup>
const props = defineProps({
  text: String,
  color: { type: String, default: 'blue' } // blue, green, red, yellow, gray
});

const colorClasses = {
  blue: 'bg-blue-100 text-blue-800',
  green: 'bg-green-100 text-green-800',
  red: 'bg-red-100 text-red-800',
  yellow: 'bg-yellow-100 text-yellow-800',
  gray: 'bg-gray-100 text-gray-800'
};
</script>

<template>
  <span :class="['inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium', colorClasses[color]]">
    {{ text }}
  </span>
</template>