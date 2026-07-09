import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service/auth.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';

export const guestGuard: CanActivateFn = (route, state) => {
  const platformId = inject(PLATFORM_ID);
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!isPlatformBrowser(platformId)) {
    return true;
  }

  if (authService.isLoading()) {
    return toObservable(authService.isLoading).pipe(
      filter((loading) => !loading),
      take(1),
      map(() => checkGuestAccess(authService, router)),
    );
  }

  return checkGuestAccess(authService, router);
};

function checkGuestAccess(
  authService: AuthService,
  router: Router,
): boolean | ReturnType<Router['createUrlTree']> {
  if (authService.currentUser()) {
    return router.createUrlTree(['/dashboard']);
  }
  return true;
}
