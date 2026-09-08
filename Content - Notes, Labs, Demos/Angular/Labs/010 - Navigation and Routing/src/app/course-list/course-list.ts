import { AsyncPipe, CurrencyPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { catchError, of } from 'rxjs';
import { Course } from '../course';

@Component({
  selector: 'app-course-list',
  imports: [AsyncPipe, CurrencyPipe],
  templateUrl: './course-list.html',
  styleUrl: './course-list.css',
})
export class CourseList {
  private http = inject(HttpClient);

  courses$ = this.http.get<Course[]>('/courses.json').pipe(
    catchError((error) => {
      console.error('Could not load the catalog:', error.message);
      return of([]);
    }),
  );
}
