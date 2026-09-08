import { Component } from '@angular/core';
import { CourseList } from './course-list/course-list';

@Component({
  selector: 'app-root',
  imports: [CourseList],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
