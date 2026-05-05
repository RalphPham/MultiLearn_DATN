import { EventBus } from 'vue-toastification';

export const toastEventBus = new EventBus();

export const toastOptions = {
  position: 'top-right',
  timeout: 3500,
  closeOnClick: true,
  pauseOnFocusLoss: true,
  pauseOnHover: true,
  draggable: true,
  hideProgressBar: false,
  newestOnTop: true,
  eventBus: toastEventBus
};
