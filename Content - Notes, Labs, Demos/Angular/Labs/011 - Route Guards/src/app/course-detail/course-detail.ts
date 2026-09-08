import { AsyncPipe, CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { Course } from '../course';

@Component({
  selector: 'app-course-detail',
  imports: [AsyncPipe, CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './course-detail.html',
  styleUrl: './course-detail.css',
})
export class CourseDetail {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  courseId = Number(this.route.snapshot.paramMap.get('id'));

  course$ = this.http
    .get<Course[]>('/courses.json')
    .pipe(map((courses) => courses.find((course) => course.id === this.courseId)));
}
