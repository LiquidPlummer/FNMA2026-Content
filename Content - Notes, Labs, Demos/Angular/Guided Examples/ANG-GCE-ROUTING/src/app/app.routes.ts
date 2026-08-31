import { Routes } from '@angular/router';
import { routeLockGuard } from './route-lock.guard';
import { canOpenRoute, RouteLockService } from './route-lock.service';
import { RouteOneComponent } from './route-one/route-one.component';
import { RouteTwoComponent } from './route-two/route-two.component';
import { RouteThreeComponent } from './route-three/route-three.component';

/*
This is the routes array, containing all the routes. Each one is an object with a path,
and component. There are also optional properties we can add, such as canActivate, which 
guards the route.
*/
export const routes: Routes = [
    /*
    Route one uses the route guard routeLockGuard in door-lock.guard.ts
    */
    {path: 'one', component: RouteOneComponent, canActivate: [routeLockGuard]},

    /*
    Route two uses the route guard canActivate, which is a member function of 
    RouteLockService in route-lock.service.ts
    */
    {path: 'two', component: RouteTwoComponent, canActivate: [RouteLockService]},

    /*
    Route three uses the route guard canOpenRoute, which is in route-lock.service.ts,
    but note that this funciton is not a member of the RouteLockService class.
    */
    {path: 'three', component: RouteThreeComponent, canActivate: [canOpenRoute]},
];
