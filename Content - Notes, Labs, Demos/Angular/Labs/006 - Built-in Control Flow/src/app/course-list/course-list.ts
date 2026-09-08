import { Component, signal } from '@angular/core';
import { Course } from '../course';

const CATALOG: Course[] = [
  { id: 101, title: 'Intro to Angular', instructor: 'Elena Marsh', seatsLeft: 12, format: 'in-person' },
  { id: 102, title: 'TypeScript Fundamentals', instructor: 'Priya Raman', seatsLeft: 3, format: 'online' },
  { id: 103, title: 'Web Accessibility', instructor: 'Devon Blake', seatsLeft: 0, format: 'hybrid' },
  { id: 104, title: 'Database Design', instructor: 'Sam Ortiz', seatsLeft: 24, format: 'in-person' },
  { id: 105, title: 'Version Control with Git', instructor: 'Maya Chen', seatsLeft: 1, format: 'online' },
];

@Component({
  selector: 'app-course-list',
  imports: [],
  templateUrl: './course-list.html',
  styleUrl: './course-list.css',
})
export class CourseList {
  courses = signal<Course[]>(CATALOG);
}
