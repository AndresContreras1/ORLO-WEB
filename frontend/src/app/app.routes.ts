import { Routes } from '@angular/router';

import { AdminLayoutComponent } from './components/admin-layout/admin-layout.component';
import { authGuard } from './guards/auth.guard';
import { AiConfigComponent } from './pages/ai-config/ai-config.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { LandingComponent } from './pages/landing/landing.component';
import { LessonFormComponent } from './pages/lesson-form/lesson-form.component';
import { LessonsComponent } from './pages/lessons/lessons.component';
import { LoginComponent } from './pages/login/login.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { QuizFormComponent } from './pages/quiz-form/quiz-form.component';
import { QuizComponent } from './pages/quiz/quiz.component';
import { StoreFormComponent } from './pages/store-form/store-form.component';
import { StoresComponent } from './pages/stores/stores.component';
import { StyleFormComponent } from './pages/style-form/style-form.component';
import { StylesComponent } from './pages/styles/styles.component';
import { UsersComponent } from './pages/users/users.component';

/** Las rutas específicas (new) van antes de las que llevan :id. */
export const routes: Routes = [
  { path: '', component: LandingComponent, title: 'ORLO · Descubre cómo te gusta vestirte' },
  { path: 'login', component: LoginComponent, title: 'Entrar · ORLO' },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent, title: 'Resumen · ORLO' },
      { path: 'styles', component: StylesComponent, title: 'Estilos · ORLO' },
      { path: 'styles/new', component: StyleFormComponent, title: 'Nuevo estilo · ORLO' },
      { path: 'styles/:id', component: StyleFormComponent, title: 'Editar estilo · ORLO' },
      { path: 'quiz', component: QuizComponent, title: 'Cuestionario · ORLO' },
      { path: 'quiz/new', component: QuizFormComponent, title: 'Nueva pregunta · ORLO' },
      { path: 'quiz/:id', component: QuizFormComponent, title: 'Editar pregunta · ORLO' },
      { path: 'lessons', component: LessonsComponent, title: 'Lecciones · ORLO' },
      { path: 'lessons/new', component: LessonFormComponent, title: 'Nueva lección · ORLO' },
      { path: 'lessons/:id', component: LessonFormComponent, title: 'Editar lección · ORLO' },
      { path: 'stores', component: StoresComponent, title: 'Tiendas · ORLO' },
      { path: 'stores/new', component: StoreFormComponent, title: 'Nueva tienda · ORLO' },
      { path: 'stores/:id', component: StoreFormComponent, title: 'Editar tienda · ORLO' },
      { path: 'ai', component: AiConfigComponent, title: 'Asesor IA · ORLO' },
      { path: 'users', component: UsersComponent, title: 'Usuarios · ORLO' },
    ],
  },
  { path: '**', component: NotFoundComponent, title: 'No encontrada · ORLO' },
];
