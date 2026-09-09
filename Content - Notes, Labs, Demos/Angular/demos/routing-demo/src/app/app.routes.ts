import { Routes } from '@angular/router';
import { Home } from './home/home';
import { About } from './about/about';
import { Secret } from './secret/secret';
import { lockGuard } from './lock-guard';
import { Parent } from './parent/parent';
import { ChildOne } from './child-one/child-one';
import { ChildTwo } from './child-two/child-two';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'about', component: About },
  { path: 'secret', component: Secret, canActivate: [lockGuard], outlet: "secretOutlet"},
  { path: 'parent', component: Parent, children: [
      {path: 'one', component: ChildOne},
      {path: 'two', component: ChildTwo}
  ] }
];
