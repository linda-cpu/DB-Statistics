import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth.interceptor';
import { definePreset } from '@primeuix/themes';

export const RedPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#fff0f0',
      100: '#ffe1e1',
      200: '#ffc7c7',
      300: '#ffa3a3',
      400: '#ff6b6b',
      500: '#f01414',
      600: '#c81010',
      700: '#a00d0d',
      800: '#780a0a',
      900: '#500707',
      950: '#320404',
    },
    colorScheme: {
      light: {
        primary: {
          color: '{primary.500}', // Setzt --p-primary-color auf #f01414
          hoverColor: '{primary.600}', // Setzt --p-primary-hover-color auf #c81010
          activeColor: '{primary.700}', // Setzt --p-primary-active-color auf #a00d0d
          // Textfarbe auf dem primären Button (Weiß)
          contrastColor: '#ffffff', // Setzt --p-primary-contrast-color
        },
      },
    },
  },
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    providePrimeNG({
      theme: {
        preset: RedPreset,
        options: {
          darkModeSelector: false,
        },
      },
    }),
  ],
};
