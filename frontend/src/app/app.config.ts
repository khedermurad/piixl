import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import {
  HttpHandlerFn,
  HttpRequest,
  provideHttpClient,
  withFetch,
  withInterceptors,
} from '@angular/common/http';
import { AuthService } from './core/services/auth-service/auth.service';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment.development';

function credentialsInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  let clonedUserReq = req;
  const backendUrlObj = new URL(environment.backendUrl);
  const requestUrlObj = new URL(req.url, window.location.origin);

  if (backendUrlObj.origin === requestUrlObj.origin) {
    clonedUserReq = req.clone({ withCredentials: true });
  }

  return next(clonedUserReq);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([credentialsInterceptor])),
    provideAppInitializer(() => {
      const authService = inject(AuthService);

      return firstValueFrom(authService.checkInitialAuth());
    }),
  ],
};
