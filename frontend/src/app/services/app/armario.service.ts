import { Injectable, inject } from '@angular/core';
import {
  Firestore,
  collection,
  collectionData,
  deleteDoc,
  doc,
  docData,
  orderBy,
  query,
  setDoc,
  updateDoc,
} from '@angular/fire/firestore';
import { Storage, deleteObject, getDownloadURL, ref, uploadBytes } from '@angular/fire/storage';
import { Observable } from 'rxjs';

import { GuardadoDoc, OutfitDelDiaDoc, OutfitDoc, PrendaDoc } from '../../models/firestore.model';
import { ExploreItemDto } from '../../models/app-api.model';
import { ignorarSiNoExiste } from '../../utils/storage';
import { FirebaseBase } from './firebase-base';

const USERS = 'users';

/** Mi armario: prendas, outfits, guardados de Explora y outfit del día, en tiempo real. */
@Injectable({ providedIn: 'root' })
export class ArmarioService extends FirebaseBase {
  private db = inject(Firestore);
  private storage = inject(Storage);

  prendas$(uid: string): Observable<PrendaDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'garments'), orderBy('createdAt', 'desc')), { idField: 'id' },
    ) as Observable<PrendaDoc[]>);
  }

  prenda$(uid: string, id: string): Observable<PrendaDoc | undefined> {
    return this.ctx(() => docData(doc(this.db, USERS, uid, 'garments', id), { idField: 'id' }) as Observable<PrendaDoc | undefined>);
  }

  /** Id nuevo para una prenda (sirve también para el nombre de su foto). */
  nuevoIdDePrenda(uid: string): string {
    return this.ctx(() => doc(collection(this.db, USERS, uid, 'garments')).id);
  }

  /**
   * Crea o actualiza una prenda. Si hay foto nueva, se sube primero a garmentImages/{uid}/{id}.jpg
   * (misma ruta que la app) y se guarda su URL.
   */
  async guardarPrenda(uid: string, prenda: PrendaDoc, foto?: Blob | null): Promise<void> {
    return this.ctx(async () => {
      let photoUrl = prenda.photoUrl ?? null;
      if (foto) {
        const archivo = ref(this.storage, `garmentImages/${uid}/${prenda.id}.jpg`);
        await uploadBytes(archivo, foto, { contentType: 'image/jpeg' });
        // &v= evita que el navegador muestre la foto anterior desde caché (igual que la app)
        photoUrl = `${await getDownloadURL(archivo)}&v=${Date.now()}`;
      }
      // El id es el del documento: no se guarda como campo (igual que la app)
      const { id, ...resto } = prenda;
      await setDoc(doc(this.db, USERS, uid, 'garments', id), { ...resto, name: prenda.name.trim(), photoUrl, favorite: prenda.favorite ?? false });
    });
  }

  marcarPrendaFavorita(uid: string, id: string, favorita: boolean): Promise<void> {
    return this.ctx(() => updateDoc(doc(this.db, USERS, uid, 'garments', id), { favorite: favorita }));
  }

  /** La foto y los outfits que usaban la prenda los limpia la Cloud Function. */
  async borrarPrenda(uid: string, prenda: PrendaDoc): Promise<void> {
    return this.ctx(async () => {
      await deleteDoc(doc(this.db, USERS, uid, 'garments', prenda.id));
      if (prenda.photoUrl?.includes('garmentImages')) {
        await deleteObject(ref(this.storage, `garmentImages/${uid}/${prenda.id}.jpg`)).catch(ignorarSiNoExiste);
      }
    });
  }

  outfits$(uid: string): Observable<OutfitDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'outfits'), orderBy('createdAt', 'desc')), { idField: 'id' },
    ) as Observable<OutfitDoc[]>);
  }

  outfit$(uid: string, id: string): Observable<OutfitDoc | undefined> {
    return this.ctx(() => docData(doc(this.db, USERS, uid, 'outfits', id), { idField: 'id' }) as Observable<OutfitDoc | undefined>);
  }

  async guardarOutfit(uid: string, outfit: Omit<OutfitDoc, 'id'> & { id?: string }): Promise<string> {
    return this.ctx(async () => {
      const outfitsRef = collection(this.db, USERS, uid, 'outfits');
      const ref = outfit.id ? doc(outfitsRef, outfit.id) : doc(outfitsRef);
      const { id: _id, ...datos } = outfit;
      await setDoc(ref, { ...datos, name: outfit.name.trim() });
      return ref.id;
    });
  }

  marcarOutfitFavorito(uid: string, id: string, favorito: boolean): Promise<void> {
    return this.ctx(() => updateDoc(doc(this.db, USERS, uid, 'outfits', id), { favorite: favorito }));
  }

  borrarOutfit(uid: string, id: string): Promise<void> {
    return this.ctx(() => deleteDoc(doc(this.db, USERS, uid, 'outfits', id)));
  }

  guardados$(uid: string): Observable<GuardadoDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'saved'), orderBy('savedAt', 'desc')),
    ) as Observable<GuardadoDoc[]>);
  }

  guardar(uid: string, item: ExploreItemDto): Promise<void> {
    const guardado: GuardadoDoc = {
      itemId: item.id, name: item.name, imageUrl: item.imageUrl, category: item.category, savedAt: Date.now(),
    };
    return this.ctx(() => setDoc(doc(this.db, USERS, uid, 'saved', String(item.id)), guardado));
  }

  quitarGuardado(uid: string, itemId: number): Promise<void> {
    return this.ctx(() => deleteDoc(doc(this.db, USERS, uid, 'saved', String(itemId))));
  }

  outfitDelDia$(uid: string, fecha: string): Observable<OutfitDelDiaDoc | undefined> {
    return this.ctx(() => docData(doc(this.db, USERS, uid, 'dailyOutfits', fecha)) as Observable<OutfitDelDiaDoc | undefined>);
  }

  guardarOutfitDelDia(uid: string, fecha: string, outfit: OutfitDelDiaDoc): Promise<void> {
    return this.ctx(() => setDoc(doc(this.db, USERS, uid, 'dailyOutfits', fecha), outfit));
  }
}

/** Fecha local AAAA-MM-DD (id del outfit del día). */
export function fechaDeHoy(): string {
  const d = new Date();
  const dos = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
}
