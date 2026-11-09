import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { QuestionRequest, QuizQuestion } from '../models/quiz.model';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/quiz`;

  findAll(): Observable<QuizQuestion[]> {
    return this.http.get<QuizQuestion[]>(this.url);
  }

  findById(id: number): Observable<QuizQuestion> {
    return this.http.get<QuizQuestion>(`${this.url}/${id}`);
  }

  create(question: QuestionRequest): Observable<QuizQuestion> {
    return this.http.post<QuizQuestion>(this.url, question);
  }

  update(id: number, question: QuestionRequest): Observable<QuizQuestion> {
    return this.http.put<QuizQuestion>(`${this.url}/${id}`, question);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
