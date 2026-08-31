import { inject, Injectable } from '@angular/core';
import { CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

/*
This is a service class, it's injectable and made available globally with the 
root provider. This class holds the locks array, which is the state that controls
our route guards.
*/
@Injectable({
  providedIn: 'root'
})
export class RouteLockService {

  constructor() { }

  /*
  locks is simply an object which holds the boolean state of each of three locks. If
  the lock is true, then the route is active and can be used. Otherwise it cannot.
  */
  locks = {
    lockOne: false,
    lockTwo: false,
    lockThree: false
  };

  

  /*
  images is an object to hold paths to images which will show a locked or unlocked route.
  Don't worry too much about this, it's just for the images which are shown. To learn more
  about how this implementation works, research "Angular Property Binding", which is
  something we will learn about in another demo.
  */
  images = {
    lockOne: "assets/lock_closed.png",
    lockTwo: "assets/lock_closed.png",
    lockThree: "assets/lock_closed.png"
  }

  /*
  This is the route guard function used for route #2. Logically it just checks if locks.lockTwo
  is true or false. Note that this variable is of type CanActivateFn.
  */
  canActivate: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    console.log("canActivate member funciton for route 2");
    return this.locks.lockTwo;
  }
  
  /*
  The following three toggle functions switch the state of the locks between true and false,
  then changes the image shown to indicate if the route is currently locked or unlocked.
  */
  toggleLockOne(): void {
    if(this.locks.lockOne) {
      console.log("unlocking route one...")
      this.locks.lockOne = false;
      this.images.lockOne = "assets/lock_closed.png"
    } else {
      console.log("locking route one...")
      this.locks.lockOne = true;
      this.images.lockOne = "assets/lock_open.png"
    }
  }

  toggleLockTwo(): void {
    if(this.locks.lockTwo) {
      console.log("unlocking route two...")
      this.locks.lockTwo = false;
      this.images.lockTwo = "assets/lock_closed.png"
    } else {
      console.log("locking route two...")
      this.locks.lockTwo = true;
      this.images.lockTwo = "assets/lock_open.png"
    }
  }

  toggleLockThree(): void {
    if(this.locks.lockThree) {
      console.log("unlocking route three...")
      this.locks.lockThree = false;
      this.images.lockThree = "assets/lock_closed.png"
    } else {
      console.log("locking route three...")
      this.locks.lockThree = true;
      this.images.lockThree = "assets/lock_open.png"
    }
  }
}

/*
This is the route guard function used for route #3. Logically it just checks if locks.lockThree
is true or false. Note that this variable is of type CanActivateFn.
*/
export const canOpenRoute: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot,) => {
  console.log("canActivate funciton for route 3");
  return inject(RouteLockService).locks.lockThree;
}