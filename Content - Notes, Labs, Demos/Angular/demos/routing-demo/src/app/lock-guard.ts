import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LockService } from './lock-service';

export const lockGuard: CanActivateFn = () => {
  const lock = inject(LockService);
  const router = inject(Router);

  // Returning a UrlTree redirects somewhere useful.
  // Returning plain `false` would block with no explanation.
  return lock.locked() ? router.createUrlTree(['/']) : true;
};
