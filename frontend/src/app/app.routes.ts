import { Routes } from '@angular/router';

import { AdminLayoutComponent } from './components/admin-layout/admin-layout.component';
import { OnboardingLayoutComponent } from './components/onboarding-layout/onboarding-layout.component';
import { UserLayoutComponent } from './components/user-layout/user-layout.component';
import { authGuard } from './guards/auth.guard';
import { invitadoGuard, recorridoGuard, usuarioGuard } from './guards/usuario.guard';
import { AiConfigComponent } from './pages/ai-config/ai-config.component';
import { AprendeComponent } from './pages/app/aprende/aprende.component';
import { ArmarioLayoutComponent } from './pages/app/armario/armario-layout.component';
import { HistorialComponent } from './pages/app/armario/historial.component';
import { MiEstiloComponent } from './pages/app/armario/mi-estilo.component';
import { OutfitFormComponent } from './pages/app/armario/outfit-form.component';
import { OutfitsComponent } from './pages/app/armario/outfits.component';
import { PrendaFormComponent } from './pages/app/armario/prenda-form.component';
import { PrendasComponent } from './pages/app/armario/prendas.component';
import { AsesorComponent } from './pages/app/asesor/asesor.component';
import { CuentaComponent } from './pages/app/cuenta/cuenta.component';
import { CuestionarioComponent } from './pages/app/cuestionario/cuestionario.component';
import { EntrarComponent } from './pages/app/entrar/entrar.component';
import { ExploraComponent } from './pages/app/explora/explora.component';
import { InicioComponent } from './pages/app/inicio/inicio.component';
import { LeccionComponent } from './pages/app/leccion/leccion.component';
import { LegalComponent } from './pages/app/legal/legal.component';
import { PerfilComponent } from './pages/app/perfil/perfil.component';
import { PremiumComponent } from './pages/app/premium/premium.component';
import { PrendaExploraComponent } from './pages/app/prenda-explora/prenda-explora.component';
import { RegistroComponent } from './pages/app/registro/registro.component';
import { ResultadoComponent } from './pages/app/resultado/resultado.component';
import { TiendasComponent } from './pages/app/tiendas/tiendas.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ExploreItemFormComponent } from './pages/explore-item-form/explore-item-form.component';
import { ExploreItemsComponent } from './pages/explore-items/explore-items.component';
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

/**
 * Web de usuarios en la raíz (misma cuenta de Firebase que la app) y panel de administración en /admin.
 * Las rutas específicas (nueva, new) van antes de las que llevan :id.
 */
export const routes: Routes = [
  { path: '', pathMatch: 'full', component: LandingComponent, canActivate: [invitadoGuard], title: 'ORLO · Descubre cómo te gusta vestirte' },
  { path: 'entrar', component: EntrarComponent, canActivate: [invitadoGuard], title: 'Entrar · ORLO' },
  { path: 'registro', component: RegistroComponent, canActivate: [invitadoGuard], title: 'Crear cuenta · ORLO' },
  { path: 'terminos', component: LegalComponent, data: { tipo: 'terminos' }, title: 'Términos · ORLO' },
  { path: 'privacidad', component: LegalComponent, data: { tipo: 'privacidad' }, title: 'Privacidad · ORLO' },

  // Primeros pasos: perfil, cuestionario, resultado y oferta premium
  {
    path: '',
    component: OnboardingLayoutComponent,
    canActivate: [usuarioGuard],
    children: [
      { path: 'perfil-inicial', component: PerfilComponent, data: { modo: 'inicial' }, title: 'Tu perfil · ORLO' },
      { path: 'cuestionario', component: CuestionarioComponent, title: 'Cuestionario · ORLO' },
      { path: 'resultado', component: ResultadoComponent, title: 'Tu estilo · ORLO' },
      { path: 'premium', component: PremiumComponent, title: 'Premium · ORLO' },
    ],
  },

  // Las cuatro secciones y la cuenta
  {
    path: '',
    component: UserLayoutComponent,
    canActivate: [usuarioGuard, recorridoGuard],
    children: [
      { path: 'inicio', component: InicioComponent, title: 'Inicio · ORLO' },
      { path: 'aprende', component: AprendeComponent, title: 'Aprende · ORLO' },
      { path: 'aprende/:id', component: LeccionComponent, title: 'Lección · ORLO' },
      { path: 'asesor', component: AsesorComponent, title: 'Asesor · ORLO' },
      { path: 'asesor/:id', component: AsesorComponent, title: 'Asesor · ORLO' },
      { path: 'explora', component: ExploraComponent, title: 'Explora · ORLO' },
      { path: 'explora/tiendas', component: TiendasComponent, title: 'Tiendas · ORLO' },
      { path: 'explora/prenda/:id', component: PrendaExploraComponent, title: 'Prenda · ORLO' },
      {
        path: 'armario',
        component: ArmarioLayoutComponent,
        children: [
          { path: '', redirectTo: 'prendas', pathMatch: 'full' },
          { path: 'prendas', component: PrendasComponent, title: 'Mis prendas · ORLO' },
          { path: 'prendas/nueva', component: PrendaFormComponent, title: 'Agregar prenda · ORLO' },
          { path: 'prendas/:id', component: PrendaFormComponent, title: 'Editar prenda · ORLO' },
          { path: 'outfits', component: OutfitsComponent, title: 'Mis outfits · ORLO' },
          { path: 'outfits/nuevo', component: OutfitFormComponent, title: 'Crear outfit · ORLO' },
          { path: 'outfits/:id', component: OutfitFormComponent, title: 'Editar outfit · ORLO' },
          { path: 'estilo', component: MiEstiloComponent, title: 'Mi estilo · ORLO' },
          { path: 'historial', component: HistorialComponent, title: 'Historial · ORLO' },
        ],
      },
      { path: 'cuenta', component: CuentaComponent, title: 'Mi cuenta · ORLO' },
      { path: 'cuenta/perfil', component: PerfilComponent, data: { modo: 'editar' }, title: 'Mis preferencias · ORLO' },
    ],
  },

  // Panel de administración (JWT propio del backend)
  { path: 'login', component: LoginComponent, title: 'Entrar al panel · ORLO' },
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
      { path: 'explore', component: ExploreItemsComponent, title: 'Explora · ORLO' },
      { path: 'explore/new', component: ExploreItemFormComponent, title: 'Nueva prenda · ORLO' },
      { path: 'explore/:id', component: ExploreItemFormComponent, title: 'Editar prenda · ORLO' },
      { path: 'stores', component: StoresComponent, title: 'Tiendas · ORLO' },
      { path: 'stores/new', component: StoreFormComponent, title: 'Nueva tienda · ORLO' },
      { path: 'stores/:id', component: StoreFormComponent, title: 'Editar tienda · ORLO' },
      { path: 'ai', component: AiConfigComponent, title: 'Asesor IA · ORLO' },
      { path: 'users', component: UsersComponent, title: 'Usuarios · ORLO' },
    ],
  },
  { path: '**', component: NotFoundComponent, title: 'No encontrada · ORLO' },
];
