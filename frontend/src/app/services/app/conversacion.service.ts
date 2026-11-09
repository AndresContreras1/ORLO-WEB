import { Injectable, inject } from '@angular/core';
import {
  Firestore,
  collection,
  collectionData,
  doc,
  getDocs,
  orderBy,
  query,
  setDoc,
  writeBatch,
} from '@angular/fire/firestore';
import { Observable } from 'rxjs';

import { ConversacionDoc, MensajeDoc } from '../../models/firestore.model';
import { FirebaseBase } from './firebase-base';

const USERS = 'users';

/** Conversaciones con el asesor (users/{uid}/conversations/{id}/messages). */
@Injectable({ providedIn: 'root' })
export class ConversacionService extends FirebaseBase {
  private db = inject(Firestore);

  conversaciones$(uid: string): Observable<ConversacionDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'conversations'), orderBy('updatedAt', 'desc')), { idField: 'id' },
    ) as Observable<ConversacionDoc[]>);
  }

  /** Mensajes en tiempo real: si se escribe desde el celular, aparecen aquí. */
  mensajes$(uid: string, conversacionId: string): Observable<MensajeDoc[]> {
    return this.ctx(() => collectionData(
      query(collection(this.db, USERS, uid, 'conversations', conversacionId, 'messages'), orderBy('createdAt', 'asc')),
      { idField: 'id' },
    ) as Observable<MensajeDoc[]>);
  }

  async crear(uid: string, titulo: string): Promise<string> {
    return this.ctx(async () => {
      const ref = doc(collection(this.db, USERS, uid, 'conversations'));
      const conversacion: Omit<ConversacionDoc, 'id'> = { title: titulo.trim().slice(0, 60), updatedAt: Date.now() };
      await setDoc(ref, conversacion);
      return ref.id;
    });
  }

  /** Guarda el mensaje y actualiza la fecha de la conversación en un batch. */
  async agregarMensaje(uid: string, conversacionId: string, mensaje: Omit<MensajeDoc, 'id'>): Promise<void> {
    return this.ctx(async () => {
      const conversacionRef = doc(this.db, USERS, uid, 'conversations', conversacionId);
      const mensajeRef = doc(collection(conversacionRef, 'messages'));
      const batch = writeBatch(this.db);
      batch.set(mensajeRef, mensaje);
      batch.update(conversacionRef, { updatedAt: mensaje.createdAt });
      await batch.commit();
    });
  }

  async borrar(uid: string, conversacionId: string): Promise<void> {
    return this.ctx(async () => {
      const conversacionRef = doc(this.db, USERS, uid, 'conversations', conversacionId);
      const mensajes = await getDocs(collection(conversacionRef, 'messages'));
      const refs = [...mensajes.docs.map((m) => m.ref), conversacionRef];
      for (let i = 0; i < refs.length; i += 450) {
        const batch = writeBatch(this.db);
        refs.slice(i, i + 450).forEach((r) => batch.delete(r));
        await batch.commit();
      }
    });
  }
}
