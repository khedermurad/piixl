import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  //const authService = inject(AuthService);
  const router = inject(Router);

  if (/*authService.isLoggedIn()*/ false) {
    return false;
  } else {
    return router.parseUrl('');
  }
};
