import { Component } from '@angular/core';
import { CourseList } from './course-list/course-list';
import { Home } from './home/home';

@Component({
  selector: 'app-root',
  imports: [CourseList, Home],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
