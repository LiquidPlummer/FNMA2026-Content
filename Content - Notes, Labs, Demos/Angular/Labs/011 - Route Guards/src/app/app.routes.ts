import { Routes } from '@angular/router';
import { CourseDetail } from './course-detail/course-detail';
import { CourseList } from './course-list/course-list';
import { Home } from './home/home';
import { NotFound } from './not-found/not-found';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'courses', component: CourseList },
  { path: 'courses/:id', component: CourseDetail },
  { path: '**', component: NotFound },
];
