import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  isLoggedIn = signal(false);

  signIn() {
    this.isLoggedIn.set(true);
  }

  signOut() {
    this.isLoggedIn.set(false);
  }
}
