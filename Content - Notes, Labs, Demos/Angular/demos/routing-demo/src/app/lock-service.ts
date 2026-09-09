import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LockService {
  readonly locked = signal(true);

  toggle() {
    this.locked.update(v => !v);
  }
}
