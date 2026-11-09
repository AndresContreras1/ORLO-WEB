import { Injectable, inject } from '@angular/core';
import {
  Firestore,
  collection,
  collectionData,
  doc,
  docData,
  getDoc,
  getDocs,
  orderBy,
  query,
  setDoc,
  writeBatch,
} from '@angular/fire/firestore';
import { Storage, deleteObject, getDownloadURL, ref, uploadBytes } from '@angular/fire/storage';
import { Observable, map } from 'rxjs';

import { EstiloDoc, PerfilCampos, ProgresoLeccionDoc, ResultadoDoc, UsuarioDoc, EstadoLeccion } from '../../models/firestore.model';
import { StyleDto } from '../../models/app-api.model';
import { FirebaseBase } from './firebase-base';

const USERS = 'users';

/** Perfil, resultados del cuestionario y avance de Aprende en Firestore (users/{uid}). */
@Injectable({ providedIn: 'root' })
export class PerfilService extends FirebaseBase {
  private db = inject(Firestore);
  private storage = inject(Storage);

  /** Al registrarse: solo nombre y fechas. Nunca el correo ni la contraseña. */
  crearUsuario(uid: string, nombre: string): Promise<void> {
    const ahora = Date.now();
    return this.ctx(() => setDoc(doc(this.db, USERS, uid), { name: nombre.trim(), acceptedTermsAt: ahora, createdAt: ahora }));
  }

  /** El perfil en tiempo real: lo que se cambie en la app aparece aquí al instante. */
  perfil$(uid: string): Observable<UsuarioDoc | undefined> {
    return this.ctx(() => docData(doc(this.db, USERS, uid), { idField: 'id' }) as Observable<UsuarioDoc | undefined>);
  }

  async existePerfil(uid: string): Promise<boolean> {
    const snap = await this.ctx(() => getDoc(doc(this.db, USERS, uid)));
    return snap.exists();
  }

  /** Sin consentimiento, los datos de apariencia se guardan vacíos (igual que la app). */
  actualizarPerfil(uid: string, campos: PerfilCampos): Promise<void> {
    const consentimiento = !!campos.sensitiveDataConsent;
    const datos: PerfilCampos = {
      ...campos,
      name: campos.name.trim(),
      bodyType: consentimiento ? campos.bodyType ?? '' : '',
      skinTone: consentimiento ? campos.skinTone ?? '' : '',
      hairColor: consentimiento ? campos.hairColor ?? '' : '',
      eyeColor: consentimiento ? campos.eyeColor ?? '' : '',
    };
    return this.ctx(() => setDoc(doc(this.db, USERS, uid), datos, { merge: true }));
  }

  /** Guarda el resultado en el historial y como resultado actual, en un solo batch. */
  async guardarResultado(uid: string, principal: StyleDto, secundario: StyleDto): Promise<ResultadoDoc> {
    return this.ctx(async () => {
      const usuarioRef = doc(this.db, USERS, uid);
      const resultadoRef = doc(collection(usuarioRef, 'results'));
      const resultado: ResultadoDoc = {
        id: resultadoRef.id,
        primary: aEstiloDoc(principal),
        secondary: aEstiloDoc(secundario),
        createdAt: Date.now(),
      };
      const batch = writeBatch(this.db);
      batch.set(resultadoRef, resultado);
      batch.set(usuarioRef, { currentResult: resultado }, { merge: true });
      await batch.commit();
      return resultado;
    });
  }

  resultados$(uid: string): Observable<ResultadoDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'results'), orderBy('createdAt', 'desc')), { idField: 'id' },
    ) as Observable<ResultadoDoc[]>);
  }

  /** Avance de Aprende en tiempo real, como mapa id de lección → estado. */
  progreso$(uid: string): Observable<Record<number, EstadoLeccion>> {
    return this.ctx(() => collectionData(collection(this.db, USERS, uid, 'lessonProgress')) as Observable<ProgresoLeccionDoc[]>)
      .pipe(map((lista) => Object.fromEntries(lista.map((p) => [p.lessonId, p.status]))));
  }

  marcarProgreso(uid: string, lessonId: number, status: EstadoLeccion): Promise<void> {
    const progreso: ProgresoLeccionDoc = { lessonId, status, updatedAt: Date.now() };
    return this.ctx(() => setDoc(doc(this.db, USERS, uid, 'lessonProgress', String(lessonId)), progreso));
  }

  /** Foto de perfil en profileImages/{uid}.jpg y su URL en el documento. */
  async subirFotoPerfil(uid: string, foto: Blob): Promise<string> {
    return this.ctx(async () => {
      const archivo = ref(this.storage, `profileImages/${uid}.jpg`);
      await uploadBytes(archivo, foto, { contentType: 'image/jpeg' });
      const url = await getDownloadURL(archivo);
      await setDoc(doc(this.db, USERS, uid), { photoUrl: url }, { merge: true });
      return url;
    });
  }

  /** Todo lo que ORLO guarda de la persona, para descargarlo ("ver mis datos"). */
  async exportarMisDatos(uid: string): Promise<Record<string, unknown>> {
    return this.ctx(async () => {
      const usuarioRef = doc(this.db, USERS, uid);
      const datos: Record<string, unknown> = { perfil: (await getDoc(usuarioRef)).data() ?? null };
      for (const sub of ['results', 'lessonProgress', 'garments', 'outfits', 'saved', 'dailyOutfits']) {
        datos[sub] = (await getDocs(collection(usuarioRef, sub))).docs.map((d) => d.data());
      }
      const conversaciones = await getDocs(collection(usuarioRef, 'conversations'));
      datos['conversations'] = await Promise.all(conversaciones.docs.map(async (c) => ({
        ...c.data(),
        messages: (await getDocs(collection(c.ref, 'messages'))).docs.map((m) => m.data()),
      })));
      return datos;
    });
  }

  /**
   * Borra los datos de la persona en Firestore y Storage antes de eliminar la cuenta (como la app).
   * Si algo queda, la Cloud Function lo limpia al borrar la cuenta de Authentication.
   */
  async borrarMisDatos(uid: string, idsDePrendas: string[]): Promise<void> {
    return this.ctx(async () => {
      const usuarioRef = doc(this.db, USERS, uid);
      const refs = [];
      const conversaciones = await getDocs(collection(usuarioRef, 'conversations'));
      for (const c of conversaciones.docs) {
        refs.push(...(await getDocs(collection(c.ref, 'messages'))).docs.map((m) => m.ref), c.ref);
      }
      for (const sub of ['results', 'lessonProgress', 'garments', 'outfits', 'saved', 'dailyOutfits']) {
        refs.push(...(await getDocs(collection(usuarioRef, sub))).docs.map((d) => d.ref));
      }
      // Un batch admite hasta 500 operaciones
      for (let i = 0; i < refs.length; i += 450) {
        const batch = writeBatch(this.db);
        refs.slice(i, i + 450).forEach((r) => batch.delete(r));
        await batch.commit();
      }
      await Promise.all([
        ...idsDePrendas.map((id) => deleteObject(ref(this.storage, `garmentImages/${uid}/${id}.jpg`)).catch(() => undefined)),
        deleteObject(ref(this.storage, `profileImages/${uid}.jpg`)).catch(() => undefined),
      ]);
      const batch = writeBatch(this.db);
      batch.delete(usuarioRef);
      await batch.commit();
    });
  }
}

export function aEstiloDoc(estilo: StyleDto): EstiloDoc {
  return {
    id: estilo.id,
    code: estilo.code,
    name: estilo.name,
    description: estilo.description,
    palette: estilo.palette,
    gallery: estilo.gallery,
  };
}
