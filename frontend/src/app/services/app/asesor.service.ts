import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AdvisorProfile, AdvisorReply, AdvisorRequest, OutfitSuggestion, WardrobeItem } from '../../models/app-api.model';
import { PrendaDoc, UsuarioDoc } from '../../models/firestore.model';
import {
  CATEGORIAS_PRENDA,
  CLIMAS,
  COLORES_DE_CABELLO,
  COLORES_DE_OJOS,
  COLORES_PRENDA,
  OCASIONES,
  PRESUPUESTOS,
  RANGOS_DE_EDAD,
  TONOS_DE_PIEL,
  TRATO_PRENDAS,
  paraAsesor,
} from '../../models/profile-options';

/** Asesor IA de la API ORLO. Arma el perfil que se envía con el mismo criterio que la app. */
@Injectable({ providedIn: 'root' })
export class AsesorService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/advisor`;

  enviar(request: AdvisorRequest): Observable<AdvisorReply> {
    return this.http.post<AdvisorReply>(`${this.url}/messages`, request);
  }

  outfitDelDia(profile: AdvisorProfile, wardrobe: WardrobeItem[]): Observable<OutfitSuggestion> {
    return this.http.post<OutfitSuggestion>(`${this.url}/daily-outfit`, { profile, wardrobe });
  }
}

/** Perfil para el asesor: solo el primer nombre y los datos en palabras, nada de correo. */
export function perfilParaAsesor(perfil: UsuarioDoc | undefined | null): AdvisorProfile {
  if (!perfil) {
    return { occasions: [] };
  }
  const consentimiento = !!perfil.sensitiveDataConsent;
  return {
    name: perfil.name?.trim().split(/\s+/)[0],
    ageRange: paraAsesor(RANGOS_DE_EDAD, perfil.ageRange),
    garmentPronoun: paraAsesor(TRATO_PRENDAS, perfil.garmentPronoun),
    primaryStyle: perfil.currentResult?.primary?.name,
    secondaryStyle: perfil.currentResult?.secondary?.name,
    skinTone: consentimiento ? paraAsesor(TONOS_DE_PIEL, perfil.skinTone) : undefined,
    hairColor: consentimiento ? paraAsesor(COLORES_DE_CABELLO, perfil.hairColor) : undefined,
    eyeColor: consentimiento ? paraAsesor(COLORES_DE_OJOS, perfil.eyeColor) : undefined,
    climate: paraAsesor(CLIMAS, perfil.climate),
    budget: paraAsesor(PRESUPUESTOS, perfil.budget),
    occasions: (perfil.occasions ?? []).map((o) => paraAsesor(OCASIONES, o)).filter((o): o is string => !!o),
  };
}

/** Prendas del armario en palabras, para que el asesor proponga con lo que ya hay. */
export function armarioParaAsesor(prendas: PrendaDoc[]): WardrobeItem[] {
  return prendas.slice(0, 80).map((p) => ({
    name: p.name,
    category: paraAsesor(CATEGORIAS_PRENDA, p.category) ?? p.category,
    color: paraAsesor(COLORES_PRENDA, p.color) ?? p.color,
  }));
}
