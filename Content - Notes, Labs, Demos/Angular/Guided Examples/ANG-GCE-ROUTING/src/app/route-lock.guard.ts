import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { RouteLockService } from './route-lock.service';

/*
This is the route guard function for route #1. Note that routeLockGuard is of type CanActivateFn. 
The purpose of the inject() statement here is to bring in the state of the locks object
from RouteLockService. 
*/
export const routeLockGuard: CanActivateFn = (route, state) => {
  console.log("canActivate function for route one");
  return inject(RouteLockService).locks.lockOne;
};
