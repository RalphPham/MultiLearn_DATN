import './assets/main.css';
import 'vue-toastification/dist/index.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import Toast from 'vue-toastification';
import i18n from './plugins/i18n';
import { toastOptions } from './plugins/toast';
import notify from './utils/notify';
import App from './App.vue';
import router from './router';

const app = createApp(App);

app.use(createPinia());
app.use(Toast, toastOptions);

if (typeof window !== 'undefined') {
  window.__notify = notify;
}

app.use(router);
app.use(i18n);

app.mount('#app');
