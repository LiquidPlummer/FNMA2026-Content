import { Component, signal } from '@angular/core';
import { Course } from '../course';

const CATALOG: Course[] = [
  {
    id: 101,
    title: 'intro to angular',
    instructor: 'elena marsh',
    startDate: '2026-01-20T09:00:00',
    tuition: 895,
    seatsLeft: 12,
    format: 'in-person',
  },
  {
    id: 102,
    title: 'typescript fundamentals',
    instructor: 'priya raman',
    startDate: '2026-02-03T18:30:00',
    tuition: 640.5,
    seatsLeft: 3,
    format: 'online',
  },
  {
    id: 103,
    title: 'web accessibility',
    instructor: 'devon blake',
    startDate: '2026-02-17T13:00:00',
    tuition: 1250,
    seatsLeft: 0,
    format: 'hybrid',
  },
  {
    id: 104,
    title: 'database design',
    instructor: 'sam ortiz',
    startDate: '2026-03-02T09:00:00',
    tuition: 1100,
    seatsLeft: 24,
    format: 'in-person',
  },
  {
    id: 105,
    title: 'version control with git',
    instructor: 'maya chen',
    startDate: '2026-03-16T16:15:00',
    tuition: 425,
    seatsLeft: 1,
    format: 'online',
  },
];

@Component({
  selector: 'app-course-list',
  imports: [],
  templateUrl: './course-list.html',
  styleUrl: './course-list.css',
})
export class CourseList {
  courses = signal<Course[]>(CATALOG);

  clearCatalog() {
    this.courses.set([]);
  }

  restoreCatalog() {
    this.courses.set(CATALOG);
  }
}
