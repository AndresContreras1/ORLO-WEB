export interface LessonExample {
  imageUrl: string;
  text: string;
}

/** Igual a AdminLessonDto del backend. */
export interface Lesson {
  id: number;
  title: string;
  summary: string;
  mainIdea: string;
  tip: string;
  order: number;
  minutes: number;
  coverUrl: string | null;
  freeSample: boolean;
  usesColorProfile: boolean;
  published: boolean;
  reviewQuestion: string | null;
  reviewOptions: string[];
  reviewCorrectIndex: number | null;
  examples: LessonExample[];
}

export type LessonRequest = Omit<Lesson, 'id'>;
