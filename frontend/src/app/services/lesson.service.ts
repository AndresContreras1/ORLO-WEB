import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Lesson, LessonRequest } from '../models/lesson.model';

@Injectable({ providedIn: 'root' })
export class LessonService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/lessons`;

  findAll(): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(this.url);
  }

  findById(id: number): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.url}/${id}`);
  }

  create(lesson: LessonRequest): Observable<Lesson> {
    return this.http.post<Lesson>(this.url, lesson);
  }

  update(id: number, lesson: LessonRequest): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.url}/${id}`, lesson);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
